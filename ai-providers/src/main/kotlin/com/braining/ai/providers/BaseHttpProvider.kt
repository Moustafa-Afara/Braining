package com.braining.ai.providers

import com.braining.core.domain.model.AiChunk
import com.braining.core.domain.model.AiError
import com.braining.core.domain.model.AiRequest
import com.braining.core.domain.model.TokenUsage
import com.braining.core.domain.provider.AiProvider
import com.braining.core.domain.store.EncryptedKeyStore
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

abstract class BaseHttpProvider(
    protected val keyStore: EncryptedKeyStore,
    protected val httpClient: HttpClient,
    protected val json: Json,
) : AiProvider {

    protected abstract val baseUrl: String
    protected abstract fun buildAuthHeaders(apiKey: String): Map<String, String>
    protected abstract fun buildRequestBody(request: AiRequest, apiKey: String): JsonObject
    protected abstract fun parseSSELine(line: String): AiChunk?

    override fun complete(request: AiRequest): Flow<AiChunk> = flow {
        try {
            // The key read stays inside the try: it touches the Android Keystore. Whatever
            // it throws propagates to the collector, whose `.catch` is the last line of
            // defence — the chat must show an error, never crash.
            val apiKey = keyStore.getKey(id.name)
            if (apiKey.isNullOrBlank()) {
                emit(AiChunk.Error(AiError.MissingKey(id)))
                return@flow
            }

            val requestBody = buildRequestBody(request, apiKey)
            val url = "$baseUrl${getEndpoint(request)}"

            // Developer Mode: hand the UI the two facts only this layer knows — where the
            // request actually went and what was actually sent. Emitted before the first
            // token so it is available even if the request then fails.
            if (request.diagnostics) {
                val serialised = json.encodeToString(JsonObject.serializer(), requestBody)
                emit(
                    AiChunk.Meta(
                        endpoint = redactSecrets(url, apiKey),
                        requestBody = redactSecrets(serialised, apiKey),
                    ),
                )
            }

            // preparePost + execute is what makes streaming actually stream.
            //
            // A plain httpClient.post() is a NON-STREAMING request: Ktor loads and caches
            // the entire response body in memory before it returns the HttpResponse, so the
            // ByteReadChannel obtained afterwards is an already-complete in-memory copy, not
            // the live socket. The SSE loop below was always correct — it simply ran after
            // the last token had arrived and then emitted every chunk within milliseconds,
            // which the user saw as one block after ~7 seconds. Only HttpStatement.execute
            // hands us the network channel while the response is still arriving.
            // Ref: https://ktor.io/docs/client-responses.html#streaming
            // DO NOT revert this to post().
            //
            // Emitting into the flow from inside the execute block is safe: on JVM, Ktor
            // 3.5 runs the block in the CALLER's coroutine context unless
            // -Dio.ktor.client.statement.useEngineDispatcher=true is set, so the flow
            // context invariant is not violated.
            httpClient.preparePost(url) {
                contentType(ContentType.Application.Json)
                buildAuthHeaders(apiKey).forEach { (k, v) -> header(k, v) }
                setBody(json.encodeToString(JsonObject.serializer(), requestBody))
            }.execute { response ->
                // Ktor does not throw on 4xx/5xx by default. Without this check an auth
                // failure was read as an empty SSE stream and the UI hung on "generating".
                if (!response.status.isSuccess()) {
                    emit(AiChunk.Error(classifyHttpError(response, apiKey)))
                    return@execute
                }

                var sawDone = false
                val channel = response.bodyAsChannel()
                while (true) {
                    val line = channel.readUTF8Line() ?: break
                    if (!line.startsWith("data:")) continue
                    val data = line.removePrefix("data:").trim()
                    if (data.isEmpty()) continue
                    if (data == "[DONE]") {
                        sawDone = true
                        emit(AiChunk.Done(TokenUsage()))
                        break
                    }
                    parseSSELine(data)?.let { chunk ->
                        if (chunk is AiChunk.Done) sawDone = true
                        emit(chunk)
                    }
                }

                // Gemini simply closes the stream instead of sending [DONE]. Without this
                // the chat would stay stuck in the "generating" state after a valid answer.
                if (!sawDone) {
                    emit(AiChunk.Done(TokenUsage()))
                }
            }
        } catch (e: CancellationException) {
            // The user pressed stop — cancellation is not an error, let it propagate.
            throw e
        }
        // No `catch (e: Exception)` here, deliberately. Network-level failures — no
        // connectivity, DNS failure, socket or request timeouts — must not be phrased by
        // this layer into a String; that is exactly how errors used to surface as English
        // text built at the wrong layer. They propagate to the collector, where
        // ChatViewModel's `.catch` classifies them into typed AiErrors via
        // `Throwable.toAiError` (NoNetwork / Timeout / Unknown).
    }.flowOn(Dispatchers.IO)

    /**
     * Turns a verification call into a truthful result. Ktor's default configuration
     * returns 401/403 as an ordinary response, so a provider that only wrapped the call
     * in try/catch reported every invalid key as valid.
     *
     * Returns `null` when the response is a success, or the classified [AiError] otherwise.
     * [apiKey] is passed through so an [AiError.Unknown] detail is redacted at the source.
     */
    protected suspend fun verifyResult(response: HttpResponse, apiKey: String): AiError? =
        if (response.status.isSuccess()) {
            null
        } else {
            classifyHttpError(response, apiKey)
        }

    /**
     * Classifies a non-success HTTP response into a typed [AiError] by status code.
     *
     * [AiError.RegionBlocked] is the one case a code alone cannot decide: Google answers
     * regional refusal with **400**, a code that would otherwise read as a broken request.
     * The response body's message is therefore matched too — on the marker "location is not
     * supported" (case-insensitive), never the full sentence, since wording shifts between
     * providers and versions. A 400 that does not match stays [AiError.Unknown]; not every
     * 400 is a regional block.
     */
    private suspend fun classifyHttpError(response: HttpResponse, apiKey: String): AiError {
        val raw = runCatching { response.bodyAsText() }.getOrNull().orEmpty().trim()
        // Anthropic, OpenAI, DeepSeek and Gemini all wrap failures as
        // {"error": {"message": "..."}}. Showing that one field is far more readable
        // than dumping the raw JSON and truncating it mid-sentence.
        val detail = runCatching {
            json.parseToJsonElement(raw)
                .jsonObject["error"]
                ?.jsonObject
                ?.get("message")
                ?.jsonPrimitive
                ?.content
        }.getOrNull() ?: raw
        val status = response.status.value
        return when {
            // **Billing is checked before the status codes, deliberately.** An empty balance
            // arrives as 400 from Anthropic and as 429 from OpenAI, so a status-first order
            // would classify the same condition as a broken request at one provider and a rate
            // limit at the other. The body is the only place either of them says what is
            // actually wrong — the identical reasoning as RegionBlocked, one line below.
            CREDIT_MARKERS.any { detail.contains(it, ignoreCase = true) } ->
                AiError.InsufficientCredit(id, status)

            status == 401 -> AiError.InvalidKey(id, status)
            status == 403 -> AiError.Forbidden(id, status)
            status == 429 -> AiError.RateLimited(id, status)
            status >= 500 -> AiError.ProviderDown(id, status)
            detail.contains(REGION_BLOCKED_MARKER, ignoreCase = true) -> AiError.RegionBlocked(id, status)
            // The detail is the provider's own text, so it goes through redactSecrets
            // before it can ever be displayed (hard constraint 3) — even though the normal
            // UI shows only the provider name and the status.
            else -> AiError.Unknown(id, status, redactSecrets(detail, apiKey).take(MAX_DETAIL_LENGTH))
        }
    }

    /**
     * OpenAI-style providers carry the model in the request body, so the path is fixed.
     * Gemini puts the model in the URL and therefore needs the request.
     */
    protected open fun getEndpoint(request: AiRequest): String = "/v1/chat/completions"

    /**
     * Removes anything key-shaped before it can be displayed.
     *
     * Two independent passes on purpose. The first removes the key we were handed, which
     * covers every provider written so far. The second catches key-bearing query
     * parameters regardless of which key produced them — Gemini accepts `?key=`, and the
     * next provider someone adds may put a token in the URL without anyone remembering
     * this function exists. Hard constraint 3 makes a leaked key a release blocker, so the
     * defence must not depend on future authors being careful.
     */
    private fun redactSecrets(text: String, apiKey: String): String {
        val withoutKey = if (apiKey.isNotBlank()) text.replace(apiKey, REDACTED) else text
        return SECRET_QUERY_PARAM.replace(withoutKey) { match ->
            match.groupValues[1] + REDACTED
        }
    }

    private companion object {
        const val REDACTED = "••••REDACTED••••"
        const val MAX_DETAIL_LENGTH = 500

        /**
         * Matched against the provider's error message, case-insensitively, to classify a
         * regional refusal. Kept deliberately short — "location is not supported" — because
         * providers rephrase the sentence around it; confirmed on device 2026-08-03 with
         * Google's "User location is not supported for the API use." (HTTP 400).
         */
        const val REGION_BLOCKED_MARKER = "location is not supported"

        /**
         * Markers for an exhausted balance, matched case-insensitively against the provider's
         * own message. Fragments, never whole sentences — the wording moves between providers
         * and versions, and `insufficient_quota` is OpenAI's machine-readable code rather than
         * prose, which makes it the most stable of the three.
         */
        val CREDIT_MARKERS = listOf(
            "credit balance is too low",
            "insufficient_quota",
            "insufficient credit",
        )

        val SECRET_QUERY_PARAM =
            Regex("([?&](?:key|api_key|apikey|access_token|token)=)[^&\\s\"]+", RegexOption.IGNORE_CASE)
    }
}
