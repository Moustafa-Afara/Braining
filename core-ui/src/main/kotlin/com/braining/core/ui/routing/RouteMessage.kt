package com.braining.core.ui.routing

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.braining.core.domain.routing.RouteReason
import com.braining.core.domain.routing.RoutingDecision
import com.braining.core.ui.R

/**
 * Turns a [RoutingDecision] into the one line `BRAINING.md` §5 requires: **which model handled the
 * request, and why.**
 *
 * The same split as `AiError.toUserMessage()`, for the same reason: the domain layer carries facts
 * — a provider, a model, an enum — and this layer, which is the only one that knows what language
 * the screen is in, does the phrasing. A rationale string built in the router would have been
 * English in an Arabic-first app.
 *
 * **The fallback sentence names both providers.** "Claude answered" hides the event; "DeepSeek
 * failed, so Claude answered" is the whole point of showing the line at all.
 */
@Composable
fun RoutingDecision.Direct.toUserMessage(): String = when (reason) {
    RouteReason.SELECTED_BY_USER -> stringResource(
        R.string.route_answered_by,
        provider.displayName,
        model,
    )

    RouteReason.FALLBACK_AFTER_FAILURE -> stringResource(
        R.string.route_fallback,
        replacing?.displayName.orEmpty(),
        provider.displayName,
        model,
    )

    // Not reachable from Direct today. Phrased rather than left to render an empty line, because
    // the day M6 makes it reachable, a blank strip is a worse bug than a wrong word.
    RouteReason.NEEDS_PC -> stringResource(R.string.route_needs_pc)
}
