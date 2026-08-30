package com.braining.ai.providers.deepseek

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
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeepSeekProvider @Inject constructor(
    keyStore: EncryptedKeyStore,
    httpClient: HttpClient,
    json: Json,
) : BaseHttpProvider(keyStore, httpClient, json) {

    override val id = ProviderId.DEEPSEEK
    override val capabilities = ProviderCapabilities(
        maxContextTokens = 64_000,
        costTier = CostTier.LOW,
        supportsVision = false,
        supportsTools = true,
    )

    override val baseUrl = "https://api.deepseek.com"

    override fun buildAuthHeaders(apiKey: String) = mapOf(
        "Authorization" to "Bearer $apiKey",
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

            // Thinking mode is ON by default on V4, at `high` effort. Left alone it spent 38
            // seconds reasoning before the first visible token and then returned an empty
            // answer (owner's device, 2026-08-03): reasoning tokens are billed against
            // max_tokens, so a high-effort chain can consume the whole 4096 budget and leave
            // nothing for the reply.
            //
            // Disabling it also restores like-for-like behaviour. The retired `deepseek-chat`
            // pointed at V4-Flash's NON-thinking mode, which is the configuration whose
            // streaming the owner verified on 2026-07-29. Plain `deepseek-v4-flash` defaults
            // to thinking, so replacing the name alone silently changed the product.
            //
            // Note for whoever enables this later: thinking mode ignores `temperature`,
            // `top_p`, `presence_penalty` and `frequency_penalty` — silently, without error —
            // and returns its chain of thought in `reasoning_content` alongside `content`.
            // Ref: https://api-docs.deepseek.com/guides/thinking_mode
            put("thinking", buildJsonObject { put("type", JsonPrimitive("disabled")) })
        }
    }

    override fun parseSSELine(line: String): AiChunk? {
        return try {
            val event = json.parseToJsonElement(line).jsonObject
            val choices = event["choices"]?.jsonArray ?: return null
            if (choices.isEmpty()) return null
            val delta = choices[0].jsonObject["delta"]?.jsonObject

            // contentOrNull, NOT content. A JSON `null` parses to JsonNull, which is a real
            // non-null JsonElement — so `?.` does not short-circuit — and JsonNull.content
            // returns the literal four-character string "null". Using .content here appended
            // the word "null" to the reply for every chunk that carried no text.
            //
            // deepseek-v4-flash is a thinking model: during its reasoning phase it streams
            // {"delta":{"content":null,"reasoning_content":"..."}}, thousands of chunks with a
            // null content. The result was hundreds of "null" glued in front of the real
            // answer. This never showed on deepseek-chat, which mapped to the NON-thinking
            // mode and therefore never sent a null content — the model swap exposed it.
            val content = delta?.get("content")?.jsonPrimitive?.contentOrNull

            // Reasoning chunks fall through to null and are dropped, which is correct: the
            // model's private reasoning does not belong in the answer bubble.
            if (content != null) {
                AiChunk.Token(content)
            } else {
                // Same trap, and it was hidden behind the one above. OpenAI-shaped APIs send
                // "finish_reason": null on every non-final chunk, so .content would have
                // returned "null" — non-null — and ended the stream on the very first
                // reasoning chunk. Fixing the line above without this one would have turned a
                // visible mess into a silent truncation.
                val finishReason = choices[0].jsonObject["finish_reason"]?.jsonPrimitive?.contentOrNull
                if (finishReason != null) {
                    val usage = event["usage"]?.jsonObject?.let {
                        com.braining.core.domain.model.TokenUsage(
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

    override suspend fun verify(apiKey: String): AiError? {
        return try {
            val body = buildJsonObject {
                // Must be the same model chat uses. This said "deepseek-chat" — retired
                // 2026-07-24 — so verify() would have reported a perfectly valid key as
                // invalid. Verifying against a model you do not actually send to is worse
                // than not verifying: it answers a question nobody asked.
                put("model", JsonPrimitive(id.defaultModel))
                put("max_tokens", JsonPrimitive(10))
                put("stream", JsonPrimitive(false))
                put("messages", buildJsonArray {
                    add(buildJsonObject {
                        put("role", JsonPrimitive("user"))
                        put("content", JsonPrimitive("Hi"))
                    })
                })
            }
            val response = httpClient.post("$baseUrl/v1/chat/completions") {
                header("Authorization", "Bearer $apiKey")
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
}
