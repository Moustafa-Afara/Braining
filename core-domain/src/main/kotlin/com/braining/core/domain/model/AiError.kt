package com.braining.core.domain.model

/**
 * A provider failure classified at the transport or HTTP layer, ready for the UI to
 * resolve into a sentence from string resources.
 *
 * Domain and data layers must never phrase user-facing text — that is how every failure
 * previously reached the user as a single English string built at the wrong layer
 * (`BaseHttpProvider` emitting "No API key configured…" or "HTTP nnn — message"). [AiError]
 * carries only the facts the UI needs to speak for itself: the provider, the HTTP status
 * where one exists, and the provider's raw message kept exclusively for Developer Mode.
 *
 * The provider is the only datum every branch shares, so the UI can always name the
 * provider even when nothing else is known.
 */
sealed interface AiError {
    val provider: ProviderId

    /** No key is stored for this provider. No HTTP status exists — nothing was sent. */
    data class MissingKey(override val provider: ProviderId) : AiError

    /** 401 — the stored key was rejected. */
    data class InvalidKey(override val provider: ProviderId, val status: Int) : AiError

    /** 403 — the key is accepted but not entitled to this action. */
    data class Forbidden(override val provider: ProviderId, val status: Int) : AiError

    /** 429 — the provider's quota is exhausted. */
    data class RateLimited(override val provider: ProviderId, val status: Int) : AiError

    /** 5xx — the provider's service is failing. */
    data class ProviderDown(override val provider: ProviderId, val status: Int) : AiError

    /** No connectivity — DNS or connection-level failure. No HTTP status exists. */
    data class NoNetwork(override val provider: ProviderId) : AiError

    /** The request exceeded the configured timeout. No HTTP status exists. */
    data class Timeout(override val provider: ProviderId) : AiError

    /**
     * The provider does not serve the user's region. Confirmed on device 2026-08-03 with
     * Gemini: Google answers regional refusal with HTTP **400** — a code that would
     * otherwise read as a broken request — carrying the body "User location is not
     * supported for the API use." The classification therefore matches the *text*, not the
     * code alone. See `BaseHttpProvider.classifyHttpError`.
     */
    data class RegionBlocked(override val provider: ProviderId, val status: Int) : AiError

    /**
     * The account has no money left.
     *
     * **A billing state wearing someone else's status code**, which is why it is matched on the
     * body like [RegionBlocked] and not on the number. Anthropic reports an empty balance as
     * HTTP **400** with "Your credit balance is too low…"; OpenAI reports it as **429** with
     * `insufficient_quota`, which is indistinguishable from a rate limit by code alone. Left
     * unclassified it read as «حدث خطأ غير متوقّع (400)» — a sentence that sends the user to
     * check their key, their network and their model name, none of which is wrong.
     *
     * **This one fires on a date rather than at random:** the owner's Anthropic promo credit
     * expires 19 Sep 2026.
     */
    data class InsufficientCredit(override val provider: ProviderId, val status: Int) : AiError

    /**
     * Anything that fits none of the above. [detail] is the provider's raw text, for
     * Developer Mode display only — never shown in the normal UI, and redacted before it
     * can be rendered anywhere (`BaseHttpProvider.redactSecrets`).
     */
    data class Unknown(
        override val provider: ProviderId,
        val status: Int?,
        val detail: String?,
    ) : AiError
}
