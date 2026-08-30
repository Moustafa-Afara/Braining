package com.braining.core.domain.model

data class AiRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val systemPrompt: String? = null,
    val maxTokens: Int = 4096,
    val temperature: Double = 0.7,
    val stream: Boolean = true,
    /**
     * When true the provider emits an [AiChunk.Meta] before the first token.
     *
     * Off by default and gated on Developer Mode: capturing the outgoing body means holding
     * the prompt in memory for the life of the session, which is pointless for an ordinary
     * user and needlessly widens the surface on which a key could ever be exposed.
     */
    val diagnostics: Boolean = false,
)
