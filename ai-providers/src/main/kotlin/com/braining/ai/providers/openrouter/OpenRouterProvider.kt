package com.braining.ai.providers.openrouter

import com.braining.ai.providers.BaseHttpProvider
import com.braining.ai.providers.toAiError
import com.braining.core.domain.model.AiChunk
import com.braining.core.domain.model.AiError
import com.braining.core.domain.model.AiRequest
import com.braining.core.domain.model.CostTier
import com.braining.core.domain.model.ProviderCapabilities
import com.braining.core.domain.model.ProviderId
import com.braining.core.domain.model.ModelCatalog
import com.braining.core.domain.model.RemoteModel
import com.braining.core.domain.model.TokenUsage
import com.braining.core.domain.store.EncryptedKeyStore
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
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
 * One key, many models — including models from providers the user has no account with.
 *
 * ## Why this closes M5 rather than adding to it
 *
 * The four hosted providers each need their own account, their own payment method and their own
 * key, and the owner's friends have to repeat that four times before the app is useful. OpenRouter
 * is one signup that reaches Claude, GPT, Gemini, Llama, Qwen and several hundred others, and it
 * is where **Ox Alpha** lives — the model the owner asked for on 2026-08-28, which is the reason
 * this provider is in the milestone at all.
 *
 * It also answers the failure that started all of this: when Gemini's free quota ran out
 * mid-answer, every alternative required a *different* key. Here the fallback chooser can hop
 * between models behind one credential.
 *
 * ## OpenAI-compatible, so almost nothing new
 *
 * `/api/v1/chat/completions`, `Authorization: Bearer`, SSE deltas in OpenAI's shape. That means
 * `BaseHttpProvider`'s streaming loop, `parseSSELine`, the error classifier and the diagnostics
 * panel all work unchanged — the same reasoning that made Ollama cheap.
 * Ref: https://openrouter.ai/docs/quickstart
 *
 * ## The model name is the whole risk, and [listModels] is the answer
 *
 * OpenRouter ids are namespaced — `anthropic/claude-sonnet-4`, `qwen/qwen3-8b` — and there are
 * several hundred of them. Typing one from memory is how this provider fails, and it fails as a
 * 404 that reads like a broken app. `PROJECT_STATE.md` §10 has an entry about exactly this: a
 * retired model name was wrong in three places at once. So the app **fetches the list** and the
 * user picks, the same decision made for Ollama and for the same reason.
 */
@Singleton
class OpenRouterProvider @Inject constructor(
    keyStore: EncryptedKeyStore,
    httpClient: HttpClient,
    json: Json,
) : BaseHttpProvider(keyStore, httpClient, json) {

    override val id = ProviderId.OPENROUTER

    override val capabilities = ProviderCapabilities(
        // Whatever the chosen model has, and it ranges from 4k to over a million. This is a
        // conservative floor rather than a promise; the real number rides on the model.
        maxContextTokens = 32_000,
        costTier = CostTier.MEDIUM,
        supportsVision = true,
        supportsTools = true,
    )

    /** `/api` here so the inherited `/v1/chat/completions` path lands correctly. */
    override val baseUrl = "https://openrouter.ai/api"

    override fun buildAuthHeaders(apiKey: String) = mapOf(
        "Authorization" to "Bearer $apiKey",
        // Optional at OpenRouter, and sent because it is honest: it identifies which app made
        // the request on the user's own usage page, which is the one place they will look when
        // a bill surprises them. It carries no personal data — the app's name, nothing else.
        "X-Title" to APP_TITLE,
    )

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

    /** OpenAI's delta shape, byte for byte — the reason this provider cost so little to add. */
    override fun parseSSELine(line: String): AiChunk? {
        return try {
            val event = json.parseToJsonElement(line).jsonObject
            val choices = event["choices"]?.jsonArray ?: return null
            if (choices.isEmpty()) return null
            val delta = choices[0].jsonObject["delta"]?.jsonObject
            // contentOrNull, never content: JsonNull.content is the string "null", and
            // OpenRouter sends a role-only opening chunk like everyone else.
            val content = delta?.get("content")?.jsonPrimitive?.contentOrNull
            if (content != null) {
                AiChunk.Token(content)
            } else {
                val finish = choices[0].jsonObject["finish_reason"]?.jsonPrimitive?.contentOrNull
                if (finish != null) {
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
     * Verify by **listing models**, not by sending a completion.
     *
     * Every other provider's `verify` spends a real request, and `OpenAiProvider` documents why
     * it deliberately uses the chat model: verifying with a *different* model can show a green
     * tick while chat fails. That reasoning does not transfer here, and doing the same thing
     * would be actively wrong:
     *
     * - A completion here costs money on a model the user may not have chosen yet.
     * - The one thing a key can be wrong about — is it valid? — is answered by any authenticated
     *   endpoint, and this one costs nothing.
     * - **The model is not fixed at verify time.** It is picked afterwards, from the list this
     *   same call returns. Verifying a model the user has not chosen would be a green tick about
     *   a decision that has not been made.
     *
     * So the tick means "this key works", which is the only claim it can honestly make here.
     */
    override suspend fun verify(apiKey: String): AiError? = try {
        val response = httpClient.get("$baseUrl/v1/models") {
            header("Authorization", "Bearer $apiKey")
        }
        verifyResult(response, apiKey)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        e.toAiError(id)
    }

    /**
     * The models this key can reach — free ones first, then alphabetically by id.
     *
     * **Free models are surfaced deliberately.** OpenRouter marks some models as costing nothing,
     * and for the friends this APK is meant for that is the difference between trying the app and
     * not. They sort to the top for the same reason Gemini sits first in Settings.
     *
     * Never throws: an empty list costs the user a picker, never the screen.
     */
    suspend fun listModels(apiKey: String): List<RemoteModel> = try {
        val response = httpClient.get("$baseUrl/v1/models") {
            header("Authorization", "Bearer $apiKey")
        }
        if (!response.status.isSuccess()) {
            emptyList()
        } else {
            // **The socket stays here; the parsing lives in `:core-domain`.** That module has
            // JUnit and this one has no test source set at all, and the parsing is the half that
            // can actually be got wrong — a renamed field, a price arriving as "0.0", a model
            // with no name. See `ModelCatalog`.
            ModelCatalog.parse(json.parseToJsonElement(response.bodyAsText()).jsonObject)
        }
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        emptyList()
    }

    private companion object {
        /** Latin, and deliberately not the Arabic name: this is an HTTP header value. */
        const val APP_TITLE = "Braining"
    }
}
