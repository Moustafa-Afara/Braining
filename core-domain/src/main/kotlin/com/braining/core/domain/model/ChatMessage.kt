package com.braining.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val role: MessageRole,
    val content: String,
)

@Serializable
enum class MessageRole {
    SYSTEM, USER, ASSISTANT
}
