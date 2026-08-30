package com.braining.core.domain.model

sealed class AiChunk {
    data class Token(val text: String) : AiChunk()
    data class Done(val usage: TokenUsage?) : AiChunk()

    /**
     * Carries a classified [AiError] rather than a pre-phrased String. The provider layer
     * knows what went wrong; the UI owns the sentence. The error's own `status` field is
     * the single home for the HTTP code — there is no separate `code` here to drift.
     */
    data class Error(val error: AiError) : AiChunk()

    /**
     * Emitted once, before any token, and only when [AiRequest.diagnostics] is set.
     *
     * The provider is the only layer that knows the resolved URL and the exact bytes it is
     * about to send; the ViewModel is the only layer that can time what the user perceives.
     * Passing these two facts up as a chunk keeps that split honest — no extra interface
     * method, no provider reaching into the UI.
     *
     * Both fields are redacted at the source. See `BaseHttpProvider.redactSecrets`.
     */
    data class Meta(val endpoint: String, val requestBody: String) : AiChunk()
}

data class TokenUsage(
    val promptTokens: Int = 0,
    val completionTokens: Int = 0,
    val totalTokens: Int = 0,
)
