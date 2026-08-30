package com.braining.ai.providers.openai

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
class OpenAiProvider @Inject constructor(
    keyStore: EncryptedKeyStore,
    httpClient: HttpClient,
    json: Json,
) : BaseHttpProvider(keyStore, httpClient, json) {

    override val id = ProviderId.OPENAI
    override val capabilities = ProviderCapabilities(
        maxContextTokens = 128_000,
        costTier = CostTier.MEDIUM,
        supportsVision = true,
        supportsTools = true,
    )

    override val baseUrl = "https://api.openai.com"

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
        }
    }

    override fun parseSSELine(line: String): AiChunk? {
        return try {
            val event = json.parseToJsonElement(line).jsonObject
            val choices = event["choices"]?.jsonArray ?: return null
            if (choices.isEmpty()) return null
            val delta = choices[0].jsonObject["delta"]?.jsonObject
            // contentOrNull, not content — see the note in DeepSeekProvider.parseSSELine.
            // JsonNull.content returns the string "null", and OpenAI sends
            // "content": null on tool-call and role-only chunks, and "finish_reason": null
            // on every non-final chunk.
            val content = delta?.get("content")?.jsonPrimitive?.contentOrNull
            if (content != null) {
                AiChunk.Token(content)
            } else {
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
                // Deliberately the chat model, not the cheaper gpt-4o-mini it used to be.
                // The few tokens saved were not worth the failure mode: if the chat model is
                // retired and the verify model is not, Settings shows a green tick and every
                // message fails. Verify what you actually use.
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
