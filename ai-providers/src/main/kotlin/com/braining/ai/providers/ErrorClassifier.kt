package com.braining.ai.providers

import com.braining.core.domain.model.AiError
import com.braining.core.domain.model.ProviderId
import io.ktor.client.plugins.HttpRequestTimeoutException
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Classifies a network-level failure into a typed [AiError].
 *
 * Lives here, in the provider layer, rather than in `core-domain`, because one of the
 * branches is Ktor's own [HttpRequestTimeoutException] and the domain layer must not
 * depend on a client library. The call site that matters is `ChatViewModel`'s `.catch`:
 * socket and timeout exceptions arrive there from `BaseHttpProvider.complete()` — which
 * deliberately lets them propagate instead of swallowing them into a String — so this is
 * where a stalled socket becomes a user-actionable sentence.
 *
 * Order matters: [SocketTimeoutException] and [ConnectException] are subtypes of
 * [SocketException] / `IOException`, so the specific branches come first.
 */
fun Throwable.toAiError(providerId: ProviderId): AiError = when (this) {
    is HttpRequestTimeoutException -> AiError.Timeout(providerId)
    is SocketTimeoutException -> AiError.Timeout(providerId)

    // ConnectException covers Ktor's ConnectTimeoutException too (a typealias for it), and
    // both "connection refused" and "network is unreachable" arrive as SocketException.
    is ConnectException -> AiError.NoNetwork(providerId)
    is UnknownHostException -> AiError.NoNetwork(providerId)
    is SocketException -> AiError.NoNetwork(providerId)

    // Anything else is genuinely unexpected; the raw message is kept for Developer Mode.
    else -> AiError.Unknown(providerId, status = null, detail = message)
}
