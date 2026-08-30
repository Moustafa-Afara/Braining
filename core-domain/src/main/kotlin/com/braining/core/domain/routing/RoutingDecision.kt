package com.braining.core.domain.routing

import com.braining.core.domain.model.ProviderId

/**
 * Where a request goes, and **why** — the "why" is half the point.
 *
 * `BRAINING.md` §5: "Every routing decision is TRANSPARENT: show which model handled the request
 * + a one-line rationale." A decision the user cannot see is a decision they cannot override, and
 * this project has already paid once for a provider being swapped underneath a measurement.
 *
 * **Sealed, as `ANSWERS.md` Part 2 §8 approved.** The two paths carry genuinely different data:
 * a direct call knows its provider and model, and a PC-bridge call knows neither.
 *
 * **[RouteReason] is an enum and not a sentence.** Hard constraint 6 keeps user-facing text in
 * resources — the same rule that makes `AiError` carry facts and `core-ui` do the phrasing. A
 * rationale string built here would be English in an Arabic-first app, in the one layer that must
 * not know what language the screen is in.
 */
sealed interface RoutingDecision {

    val reason: RouteReason

    /**
     * Path A — straight to a provider from the phone. The self-sufficient core (`BRAINING.md` §3).
     *
     * @param replacing the provider this one is standing in for, when [reason] is
     *   [RouteReason.FALLBACK_AFTER_FAILURE]. Carried so the notice can name **both** — "X failed,
     *   so Y answered" tells the user something; "Y answered" hides the event entirely.
     */
    data class Direct(
        val provider: ProviderId,
        val model: String,
        override val reason: RouteReason,
        val replacing: ProviderId? = null,
    ) : RoutingDecision

    /**
     * Path B — the request needs the user's PC. **Nothing produces this yet.**
     *
     * It exists so that M6 adds a branch to a type that already has one, rather than widening a
     * type every caller has already assumed is single-valued. `BRAINING.md` §3 requires the app to
     * say "PC not connected — this task needs it" and offer to queue; that sentence has a type to
     * hang on before it has a bridge to describe.
     */
    data class NeedsPc(
        override val reason: RouteReason = RouteReason.NEEDS_PC,
    ) : RoutingDecision
}

/** Why the router decided what it decided. Phrased by `core-ui`, never here. */
enum class RouteReason {
    /** The provider showing on screen. The only reason produced today. */
    SELECTED_BY_USER,

    /** The selected provider failed in a way another provider could survive. */
    FALLBACK_AFTER_FAILURE,

    /** M6. The request acts on the PC's files. */
    NEEDS_PC,
}
