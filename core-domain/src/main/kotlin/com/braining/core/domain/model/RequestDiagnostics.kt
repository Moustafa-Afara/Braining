package com.braining.core.domain.model

/**
 * What one request actually did, for Developer Mode.
 *
 * The point of this type is to make streaming non-negotiable to argue about: [chunkCount]
 * of 1 means the response arrived in a single block, many means it streamed. That one
 * number settles in a glance a question that previously cost a day of diagnosis.
 *
 * [endpoint] and [requestBody] are captured in the provider layer and are **already
 * redacted** — see `BaseHttpProvider.redactSecrets`. Nothing carrying an API key may reach
 * this type; hard constraint 3 makes that a release blocker, not a preference.
 */
data class RequestDiagnostics(
    val endpoint: String,
    val requestBody: String,
    /** Time until the first token reached the UI. Null if no token ever arrived. */
    val firstChunkMillis: Long? = null,
    /** Time from send until the stream closed, successfully or not. */
    val totalMillis: Long? = null,
    val chunkCount: Int = 0,
    val usage: TokenUsage? = null,
)
