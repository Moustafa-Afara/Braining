package com.braining.ai.providers.gemini

import com.braining.ai.providers.BaseHttpProvider
import com.braining.ai.providers.toAiError
import com.braining.core.domain.model.AiChunk
import com.braining.core.domain.model.AiError
import com.braining.core.domain.model.AiRequest
import com.braining.core.domain.model.CostTier
import com.braining.core.domain.model.ProviderCapabilities
import com.braining.core.domain.model.ProviderId
import com.braining.core.domain.store.EncryptedKeyStore
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiProvider @Inject constructor(
    keyStore: EncryptedKeyStore,
    httpClient: HttpClient,
    json: Json,
) : BaseHttpProvider(keyStore, httpClient, json) {

    override val id = ProviderId.GEMINI
    override val capabilities = ProviderCapabilities(
        maxContextTokens = 1_000_000,
        costTier = CostTier.FREE,
        supportsVision = true,
        supportsTools = true,
    )

    override val baseUrl = "https://generativelanguage.googleapis.com"

    // `alt=sse` is mandatory: without it streamGenerateContent returns one big JSON
    // array instead of `data:` events, so the SSE reader in BaseHttpProvider finds
    // nothing to parse and the answer never appears.
    // Uses the model chosen in Settings so different models can be tried without a
    // rebuild — Gemini names the model in the path, unlike the OpenAI-style providers.
    override fun getEndpoint(request: AiRequest): String {
        val model = request.model.ifBlank { DEFAULT_MODEL }
        return "/v1beta/models/$model:streamGenerateContent?alt=sse"
    }

    // Gemini accepts the key either as a ?key= query parameter or as this header.
    // The header is used because the base class only knows how to add headers — the
    // previous empty map meant every chat request went out unauthenticated.
    override fun buildAuthHeaders(apiKey: String) = mapOf("x-goog-api-key" to apiKey)

    override fun buildRequestBody(request: AiRequest, apiKey: String): JsonObject {
        val contents = buildJsonArray {
            request.messages.forEach { msg ->
                if (msg.role != com.braining.core.domain.model.MessageRole.SYSTEM) {
                    add(buildJsonObject {
                        put("role", JsonPrimitive(if (msg.role == com.braining.core.domain.model.MessageRole.ASSISTANT) "model" else "user"))
                        put("parts", buildJsonArray {
                            add(buildJsonObject {
                                put("text", JsonPrimitive(msg.content))
                            })
                        })
                    })
                }
            }
        }
        return buildJsonObject {
            put("contents", contents)
            request.systemPrompt?.let { sys ->
                put("systemInstruction", buildJsonObject {
                    put("parts", buildJsonArray {
                        add(buildJsonObject {
                            put("text", JsonPrimitive(sys))
                        })
                    })
                })
            }
            put("generationConfig", buildJsonObject {
                put("maxOutputTokens", JsonPrimitive(request.maxTokens))
                put("temperature", JsonPrimitive(request.temperature))
            })
        }
    }

    override fun parseSSELine(line: String): AiChunk? {
        return try {
            val event = json.parseToJsonElement(line).jsonObject
            val candidates = event["candidates"]?.jsonArray
            if (candidates.isNullOrEmpty()) return null
            val content = candidates[0].jsonObject["content"]?.jsonObject
            val parts = content?.get("parts")?.jsonArray
            if (parts.isNullOrEmpty()) return null

            // Iterate every part, do not index [0]. gemini-3.5-flash is a thinking model, so a
            // chunk can carry a thought part ahead of the text part; reading only parts[0]
            // would find no "text" and silently drop the chunk, losing answer content.
            //
            // Parts flagged `thought` are the model's private reasoning and are skipped — they
            // do not belong in the answer bubble.
            //
            // contentOrNull, not content: JsonNull.content returns the literal string "null".
            // The identical mistake in DeepSeekProvider printed hundreds of "null" into a real
            // reply on 2026-08-03. See the note there.
            val text = parts.joinToString(separator = "") { part ->
                val obj = part.jsonObject
                if (obj["thought"]?.jsonPrimitive?.booleanOrNull == true) {
                    ""
                } else {
                    obj["text"]?.jsonPrimitive?.contentOrNull.orEmpty()
                }
            }
            if (text.isEmpty()) null else AiChunk.Token(text)
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun verify(apiKey: String): AiError? {
        return try {
            val body = buildJsonObject {
                put("contents", buildJsonArray {
                    add(buildJsonObject {
                        put("role", JsonPrimitive("user"))
                        put("parts", buildJsonArray {
                            add(buildJsonObject { put("text", JsonPrimitive("Hi")) })
                        })
                    })
                })
                put("generationConfig", buildJsonObject {
                    put("maxOutputTokens", JsonPrimitive(10))
                })
            }
            val response = httpClient.post("$baseUrl/v1beta/models/$DEFAULT_MODEL:generateContent") {
                header("x-goog-api-key", apiKey)
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(JsonObject.serializer(), body))
            }
            verifyResult(response, apiKey)
        } catch (e: Exception) {
            // No network / timeout while verifying are real failures, classified like any
            // other transport error instead of being phrased into a message here.
            e.toAiError(id)
        }
    }

    companion object {
        /**
         * Delegates to [ProviderId.GEMINI] rather than repeating the name. This was a third
         * copy of the model string — Settings and Chat held the other two — and a model name
         * duplicated across modules is a name that gets half-updated when the vendor retires
         * it. Gemini is the one provider that needs the constant at all, because it names the
         * model in the URL path instead of the request body.
         *
         * History: gemini-2.0-flash reached shutdown on 1 June 2026 and answered every
         * request with HTTP 429 RESOURCE_EXHAUSTED. gemini-3.5-flash is stable and carries no
         * announced shutdown date as of 2026-07-30.
         * See https://ai.google.dev/gemini-api/docs/deprecations
         */
        val DEFAULT_MODEL: String = ProviderId.GEMINI.defaultModel
    }
}
