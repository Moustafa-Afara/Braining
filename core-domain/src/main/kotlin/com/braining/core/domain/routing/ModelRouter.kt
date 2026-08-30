package com.braining.core.domain.routing

import com.braining.core.domain.model.AiError
import com.braining.core.domain.model.ProviderId

/**
 * Chooses the path, and chooses again when the first choice fails.
 *
 * **There is no classification call, and that is a ruling, not an omission** (owner, 2026-08-17).
 * `BRAINING.md` §3 says the router "MUST first classify each request as Path A or Path B", and
 * `ANSWERS.md` Part 2 §11 adds that such classification must be an API call rather than a local
 * keyword rule. Both stand — for M6. Today the second branch does not exist, so a classifier
 * would spend a network round trip and the user's money on every single request to answer a
 * question with one possible answer, and would leave a branch nobody can test in the middle of
 * the product's hot path.
 *
 * What ships instead is the part that is real now: **the decision is visible, and it is
 * overridable.** [route] always returns Path A; M6 replaces this one method.
 */
interface ModelRouter {

    /** The decision for a request about to be sent. Always [RoutingDecision.Direct] today. */
    fun route(selected: ProviderId, model: String): RoutingDecision

    /**
     * Whether [error] is worth trying a different provider for.
     *
     * **The split is "whose fault is it".** A timeout, an exhausted quota, a regional refusal, an
     * empty balance and a 5xx are all facts about *one provider*, and another provider may well
     * answer. A missing or rejected key, a forbidden action and a dead network are facts about the
     * *user's setup* — silently routing around them would spend a second key to hide a problem the
     * user has to fix anyway, and they would never learn the first key was wrong.
     */
    fun isRecoverable(error: AiError): Boolean

    /**
     * The next provider to try, or null when there is none.
     *
     * @param keyed providers the user actually has a key for. A fallback to a provider with no key
     *   trades one failure for a worse one — the second error would name a missing key and read as
     *   if the user had done something wrong.
     * @param alreadyTried every provider this request has burned, including [failed]. Without it a
     *   two-provider setup can ping-pong forever.
     */
    fun fallback(
        failed: ProviderId,
        error: AiError,
        keyed: Set<ProviderId>,
        alreadyTried: Set<ProviderId>,
    ): ProviderId?

    /**
     * **Every** provider that could take over, in order — not just the first.
     *
     * Added 2026-08-28 when the owner reversed his own ruling of 17 August: instead of hopping
     * automatically, the app now stops and offers him the choice. That needs the whole list, and
     * [fallback] needs its head, so **one function produces the order and the other takes the
     * front of it.** Two independent orderings would eventually disagree, and the disagreement
     * would be invisible — the automatic hop and the offered list would name different providers
     * for the same failure.
     *
     * Empty means no fallback is appropriate at all, which is a different statement from "you
     * have no other keys": [isRecoverable] refuses to route around the user's own setup.
     */
    fun fallbackCandidates(
        failed: ProviderId,
        error: AiError,
        keyed: Set<ProviderId>,
        alreadyTried: Set<ProviderId>,
    ): List<ProviderId>
}

/**
 * The router as it exists before the PC bridge.
 *
 * Pure Kotlin, no Android, no Hilt annotations, no I/O — it lives in `:core-domain` **so that it
 * can be unit-tested with the JUnit dependency this module already declares.** `ANSWERS.md`
 * Part 1 §9 asked for the router to be unit-tested before it was written; putting it anywhere
 * else would have meant adding a test dependency to a build the project has twice broken by
 * touching dependencies.
 *
 * It is bound in `:ai-providers`' Hilt module, next to the provider map it chooses between.
 */
class DefaultModelRouter : ModelRouter {

    override fun route(selected: ProviderId, model: String): RoutingDecision =
        RoutingDecision.Direct(
            provider = selected,
            model = model,
            reason = RouteReason.SELECTED_BY_USER,
        )

    override fun isRecoverable(error: AiError): Boolean = when (error) {
        is AiError.Timeout,
        is AiError.RateLimited,
        is AiError.ProviderDown,
        is AiError.RegionBlocked,
        is AiError.InsufficientCredit,
        -> true

        // The user's own setup. Routing around these hides them.
        is AiError.MissingKey,
        is AiError.InvalidKey,
        is AiError.Forbidden,
        -> false

        // No connectivity at all — a second provider is reached over the same dead network.
        is AiError.NoNetwork -> false

        // Unclassified. Retrying an unknown failure on someone else's key is a guess with a bill
        // attached, and `AiError.Unknown` is precisely the case nobody has diagnosed yet.
        is AiError.Unknown -> false
    }

    /**
     * **Deterministic order, on purpose: declaration order in [ProviderId].**
     *
     * A "best available" heuristic would make the same failure fall back differently on two runs
     * and turn every comparison in this project into a measurement of the router. Deterministic is
     * also the only version that can be pinned by a test.
     */
    override fun fallback(
        failed: ProviderId,
        error: AiError,
        keyed: Set<ProviderId>,
        alreadyTried: Set<ProviderId>,
    ): ProviderId? = fallbackCandidates(failed, error, keyed, alreadyTried).firstOrNull()

    override fun fallbackCandidates(
        failed: ProviderId,
        error: AiError,
        keyed: Set<ProviderId>,
        alreadyTried: Set<ProviderId>,
    ): List<ProviderId> {
        if (!isRecoverable(error)) return emptyList()
        return ProviderId.entries.filter { candidate ->
            candidate != failed && candidate in keyed && candidate !in alreadyTried
        }
    }
}
