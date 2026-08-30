package com.braining.ai.providers.anthropic

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
class AnthropicProvider @Inject constructor(
    keyStore: EncryptedKeyStore,
    httpClient: HttpClient,
    json: Json,
) : BaseHttpProvider(keyStore, httpClient, json) {

    override val id = ProviderId.ANTHROPIC
    override val capabilities = ProviderCapabilities(
        maxContextTokens = 200_000,
        costTier = CostTier.HIGH,
        supportsVision = true,
        supportsTools = true,
    )

    override val baseUrl = "https://api.anthropic.com"

    override fun getEndpoint(request: AiRequest) = "/v1/messages"

    override fun buildAuthHeaders(apiKey: String) = mapOf(
        "x-api-key" to apiKey,
        "anthropic-version" to "2023-06-01",
    )

    override fun buildRequestBody(request: AiRequest, apiKey: String): JsonObject {
        val messages = buildJsonArray {
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
            put("stream", JsonPrimitive(request.stream))
            put("messages", messages)
            request.systemPrompt?.let { put("system", JsonPrimitive(it)) }
        }
    }

    override fun parseSSELine(line: String): AiChunk? {
        return try {
            val event = json.parseToJsonElement(line).jsonObject
            when (event["type"]?.jsonPrimitive?.content) {
                "content_block_delta" -> {
                    val delta = event["delta"]?.jsonObject
                    // contentOrNull — see the note in DeepSeekProvider.parseSSELine.
                    // A thinking delta carries no "text", and .content would yield "null".
                    val text = delta?.get("text")?.jsonPrimitive?.contentOrNull
                    text?.let { AiChunk.Token(it) }
                }
                "message_stop" -> AiChunk.Done(null)
                "error" -> {
                    val error = event["error"]?.jsonObject
                    AiChunk.Error(
                        AiError.Unknown(
                            provider = id,
                            status = null,
                            detail = error?.get("message")?.jsonPrimitive?.content,
                        ),
                    )
                }
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun verify(apiKey: String): AiError? {
        return try {
            val request = AiRequest(
                model = "claude-sonnet-5",
                messages = listOf(
                    com.braining.core.domain.model.ChatMessage(
                        role = com.braining.core.domain.model.MessageRole.USER,
                        content = "Hi"
                    )
                ),
                maxTokens = 10,
                stream = false,
            )
            val messages = buildJsonArray {
                add(buildJsonObject {
                    put("role", JsonPrimitive("user"))
                    put("content", JsonPrimitive("Hi"))
                })
            }
            val body = buildJsonObject {
                // Same model chat uses — see the note in DeepSeekProvider.verify().
                put("model", JsonPrimitive(id.defaultModel))
                put("max_tokens", JsonPrimitive(10))
                put("stream", JsonPrimitive(false))
                put("messages", messages)
            }
            val response = httpClient.post("$baseUrl/v1/messages") {
                header("x-api-key", apiKey)
                header("anthropic-version", "2023-06-01")
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
