package com.braining.ai.providers.ollama

import com.braining.ai.providers.BaseHttpProvider
import com.braining.ai.providers.toAiError
import com.braining.core.domain.model.AiChunk
import com.braining.core.domain.model.AiError
import com.braining.core.domain.model.AiRequest
import com.braining.core.domain.model.CostTier
import com.braining.core.domain.model.ProviderCapabilities
import com.braining.core.domain.model.ProviderId
import com.braining.core.domain.model.TokenUsage
import com.braining.core.domain.net.LocalEndpoint
import com.braining.core.domain.store.AppPreferences
import com.braining.core.domain.store.EncryptedKeyStore
import io.ktor.client.HttpClient
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Models running on the user's own computer, over the local network.
 *
 * ## Why this one is worth the exceptions it required
 *
 * It is the only provider that **cannot run out and cannot send a bill.** The owner met Gemini's
 * daily quota on 2026-08-30 and the answer stopped mid-thought; a model on his own machine has no
 * quota to meet. It is also the only one that works with the internet down, and the only one
 * where the conversation never leaves the room — which for an app whose whole purpose is turning
 * half-formed ideas into prompts is not a small thing.
 *
 * ## It speaks OpenAI, deliberately
 *
 * Ollama exposes both a native `/api/chat` (newline-delimited JSON) and an OpenAI-compatible
 * `/v1/chat/completions` (server-sent events). **This uses the compatible one**, so
 * `BaseHttpProvider`'s SSE loop, `parseSSELine`, the error classifier and the diagnostics panel
 * all work unchanged. A second streaming format in this codebase would be a second thing to get
 * wrong, for no behaviour the user could see.
 * Ref: https://docs.ollama.com/api/openai-compatibility
 *
 * ## Three exceptions, and what each one is defending
 *
 * **No key** ([requiresKey] = false). Ollama authenticates nobody. Its docs say to send the
 * literal string `ollama` as a placeholder; this sends no header at all, which is the same thing
 * to the server and avoids putting a fake credential anywhere near the key store.
 *
 * **An address that changes** ([resolveBaseUrl]). Read from preferences per request and validated
 * every time by [LocalEndpoint], which refuses anything that is not on a private network. That
 * validation is the app's cleartext guarantee and it must not be hoisted, cached or skipped —
 * see the note on [LocalEndpoint] itself.
 *
 * **A third state.** Every other provider answers or fails. This one can be *asleep*, because the
 * PC it lives on can be switched off. [probe] exists so the fallback chooser can ask before it
 * offers, and so Settings can tell the user which of three different things is wrong.
 */
@Singleton
class OllamaProvider @Inject constructor(
    keyStore: EncryptedKeyStore,
    httpClient: HttpClient,
    json: Json,
    private val appPreferences: AppPreferences,
) : BaseHttpProvider(keyStore, httpClient, json) {

    override val id = ProviderId.OLLAMA

    override val capabilities = ProviderCapabilities(
        // Whatever the user pulled decides the real number, and it varies by an order of
        // magnitude between models. 8k is the floor a small local model reliably has; claiming
        // more here would make the app promise a context the machine cannot hold.
        maxContextTokens = 8_192,
        costTier = CostTier.LOW,
        supportsVision = false,
        supportsTools = false,
    )

    /** Never used: [resolveBaseUrl] answers instead. Present because the base class demands it. */
    override val baseUrl = ""

    override val requiresKey = false

    override suspend fun resolveBaseUrl(): String? = LocalEndpoint.baseUrlOrNull(
        appPreferences.ollamaUrl.first(),
        allowTunnel = appPreferences.ollamaTunnel.first(),
    )

    /** No auth, by design. See the class note. */
    override fun buildAuthHeaders(apiKey: String): Map<String, String> = emptyMap()

    override fun buildRequestBody(request: AiRequest, apiKey: String): JsonObject {
        val messages = buildJsonArray {
            request.systemPrompt?.let { sys ->
                add(buildJsonObject {
                    put("role", JsonPrimitive("system"))
                    put("content", JsonPrimitive(sys))
                })
            }
            request.messages.forEach { msg ->
                add(buildJsonObject {
                    put("role", JsonPrimitive(msg.role.name.lowercase()))
                    put("content", JsonPrimitive(msg.content))
                })
            }
        }
        return buildJsonObject {
            put("model", JsonPrimitive(request.model))
            put("max_tokens", JsonPrimitive(request.maxTokens))
            put("temperature", JsonPrimitive(request.temperature))
            put("stream", JsonPrimitive(request.stream))
            put("messages", messages)
        }
    }

    /** Byte-for-byte the OpenAI delta shape — that is the point of using the compatible API. */
    override fun parseSSELine(line: String): AiChunk? {
        return try {
            val event = json.parseToJsonElement(line).jsonObject
            val choices = event["choices"]?.jsonArray ?: return null
            if (choices.isEmpty()) return null
            val delta = choices[0].jsonObject["delta"]?.jsonObject
            // contentOrNull, never content: JsonNull.content is the *string* "null", and a
            // role-only opening chunk would print it into the answer.
            val content = delta?.get("content")?.jsonPrimitive?.contentOrNull
            if (content != null) {
                AiChunk.Token(content)
            } else {
                val finishReason = choices[0].jsonObject["finish_reason"]?.jsonPrimitive?.contentOrNull
                if (finishReason != null) {
                    val usage = event["usage"]?.jsonObject?.let {
                        TokenUsage(
                            promptTokens = it["prompt_tokens"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0,
                            completionTokens = it["completion_tokens"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0,
                            totalTokens = it["total_tokens"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0,
                        )
                    }
                    AiChunk.Done(usage)
                } else null
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * What a check of the connection found.
     *
     * **Four outcomes, not two**, because "it did not work" is the answer that sent the owner to
     * three correct settings screens in M2 (`PROJECT_STATE.md` §10 entry 1). Each of these has a
     * different fix and the user cannot guess which one they are in.
     */
    sealed interface Probe {
        /** Reachable, with the models the user has actually pulled. May legitimately be empty. */
        data class Reachable(val models: List<String>) : Probe

        /** The field is empty. Not a failure — nothing has been set up yet. */
        data object NotConfigured : Probe

        /** Typed, but not an address, or not a private one. [reason] distinguishes them. */
        data class BadAddress(val reason: LocalEndpoint.Result) : Probe

        /**
         * Nothing answered.
         *
         * Overwhelmingly the same three causes, in order: the PC is asleep, Ollama is bound to
         * `127.0.0.1` and cannot be reached from another device, or Windows Firewall is refusing
         * the port. The UI names all three rather than making the user guess.
         */
        data class Unreachable(val detail: String) : Probe
    }

    /**
     * Ask the machine whether it is there, and what it has.
     *
     * `/v1/models` is one cheap GET that answers reachability and the model list at once — asking
     * twice would be two chances to disagree with itself.
     *
     * **Never throws.** It is called from the fallback path, where an exception would cost the
     * user their error card instead of their provider list.
     */
    suspend fun probe(timeoutMillis: Long = TEST_BUTTON_TIMEOUT_MS): Probe {
        val raw = appPreferences.ollamaUrl.first()
        val parsed = LocalEndpoint.parse(raw, allowTunnel = appPreferences.ollamaTunnel.first())
        if (parsed is LocalEndpoint.Result.Empty) return Probe.NotConfigured
        val base = (parsed as? LocalEndpoint.Result.Ok)?.url ?: return Probe.BadAddress(parsed)

        return try {
            val response = httpClient.get("$base/v1/models") {
                // **Its own budget, far below the client's 15-second connect timeout.** A
                // sleeping PC does not refuse a connection, it says nothing at all, so the only
                // thing that ends the wait is the clock. Fifteen seconds is fine for a user who
                // pressed a button; it is unusable on the fallback path, where the probe runs
                // while someone stares at an error card waiting to be told who else could
                // answer. The caller picks.
                timeout {
                    requestTimeoutMillis = timeoutMillis
                    connectTimeoutMillis = timeoutMillis
                    socketTimeoutMillis = timeoutMillis
                }
            }
            if (!response.status.isSuccess()) {
                return Probe.Unreachable("HTTP ${response.status.value}")
            }
            val body = response.bodyAsText()
            val models = json.parseToJsonElement(body)
                .jsonObject["data"]
                ?.jsonArray
                ?.mapNotNull { it.jsonObject["id"]?.jsonPrimitive?.contentOrNull }
                ?.filter { it.isNotBlank() }
                .orEmpty()
                .sorted()
            Probe.Reachable(models)
        } catch (e: CancellationException) {
            // Rethrown, never reported. Swallowing it here would tell the user their PC is
            // unreachable because they navigated away, and would leave the coroutine's parent
            // believing the child is still alive.
            throw e
        } catch (e: Exception) {
            // The message is the transport's own — "connect timed out", "Connection refused" —
            // and it is the single most useful line for telling a sleeping PC from a firewall.
            // No key exists here, so there is nothing to redact.
            Probe.Unreachable(e.message ?: e::class.simpleName.orEmpty())
        }
    }

    /**
     * Reachability **is** verification for this provider.
     *
     * There is no key to be right or wrong, so the only question a check can answer is whether
     * the machine is there — and unlike the other four, asking costs nothing: no tokens, no
     * money, no quota. The `apiKey` parameter is ignored and exists only to satisfy the
     * interface.
     */
    override suspend fun verify(apiKey: String): AiError? = toError(probe())

    /**
     * Map a [Probe] onto the app's error vocabulary — **and this mapping decides whether the
     * fallback chooser appears**, which is the part worth reading twice.
     *
     * `DefaultModelRouter.isRecoverable` offers other providers for `ProviderDown` and refuses to
     * for `MissingKey` and `Unknown`. That is exactly the split wanted here:
     *
     * - A **sleeping PC** is a provider that is down. Another provider can absolutely answer, and
     *   this is the single commonest way this provider fails. `NoNetwork` would have been the
     *   tempting choice and it is wrong — it means *the phone* has no connectivity, and the
     *   router refuses a fallback for it precisely because a second provider would be reached
     *   over the same dead network. The phone's network here is fine; one machine on it is
     *   asleep.
     * - A **missing or malformed address** is the user's own setup. Routing around it would hide
     *   the thing they have to fix, which is the 2026-08-28 ruling in `ANSWERS.md` Part 11.
     *
     * **The 503 is synthetic and that is stated rather than hidden.** Nothing returned it — the
     * connection never got far enough for anyone to return anything. `ProviderDown` requires a
     * status, 503 is the one code that means precisely "this service is unavailable right now",
     * and the alternative was inventing a less accurate number or losing the fallback offer.
     */
    private fun toError(probe: Probe): AiError? = when (probe) {
        is Probe.Reachable -> null
        is Probe.NotConfigured -> AiError.MissingKey(id)
        is Probe.BadAddress -> AiError.Unknown(id, status = null, detail = describe(probe.reason))
        is Probe.Unreachable -> AiError.ProviderDown(id, status = SERVICE_UNAVAILABLE)
    }

    private fun describe(reason: LocalEndpoint.Result): String = when (reason) {
        is LocalEndpoint.Result.NotPrivate ->
            "Address ${reason.host} is not on a private network; Braining will not send prompts " +
                "in cleartext to a routable address."
        else -> "The address could not be read as an IP address and port."
    }

    companion object {
        private const val SERVICE_UNAVAILABLE = 503

        /** A user pressed a button and is watching. Long enough to be believed. */
        const val TEST_BUTTON_TIMEOUT_MS = 6_000L

        /**
         * A provider just failed and the user is waiting to be told who else could answer.
         *
         * On a home network a machine that is awake answers this in single-digit milliseconds,
         * so anything longer than this is almost certainly a machine that is not. Spending five
         * seconds to confirm that would make every failure of every OTHER provider slower, which
         * is the wrong trade for a provider that is not even the one that failed.
         */
        const val FALLBACK_PROBE_TIMEOUT_MS = 1_200L
    }
}
