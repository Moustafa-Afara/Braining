package com.braining.core.ui.error

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.braining.core.ui.text.BidiDirection
import com.braining.core.ui.text.BidiText
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.braining.core.domain.model.AiError
import com.braining.core.ui.R

/**
 * Resolves a classified [AiError] into a user-facing sentence, from string resources.
 *
 * This is the single place the app phrases provider failures, for every screen that shows
 * them: the chat error card and the Settings verify status today, and anything M2+ adds.
 * The provider is always named; the HTTP status is shown wherever the error carries one
 * (MissingKey, NoNetwork and Timeout have no status because nothing was ever sent).
 *
 * RegionBlocked is deliberately phrased as a steering sentence rather than a status: the
 * owner's ruling (ANSWERS.md Part 3 §B) is that the app must state the regional refusal
 * plainly and point at another provider — "HTTP 400" tells the user nothing actionable.
 *
 * [AiError.Unknown.detail] is never rendered here — it is the provider's raw text and is
 * kept for Developer Mode only.
 *
 * **Why this lives in `core-ui` and not in a feature module.** It was born in `feature-chat`
 * (2026-08-03-E), which forced `feature-settings` to depend on `feature-chat` just to reach
 * its `R` class — a sibling-to-sibling dependency that dragged the whole chat feature into
 * Settings and would have hit a Gradle cycle the day `feature-chat` needed anything back.
 * Both features already depend on `core-ui`, so this is the home that costs nothing. Any new
 * screen that has to phrase a failure imports it from here; do not copy the `when` block.
 */
@Composable
fun AiError.toUserMessage(): String = when (this) {
    is AiError.MissingKey -> stringResource(
        R.string.error_missing_key,
        provider.displayName,
    )

    is AiError.InvalidKey -> stringResource(
        R.string.error_invalid_key,
        provider.displayName,
        status,
    )

    is AiError.Forbidden -> stringResource(
        R.string.error_forbidden,
        provider.displayName,
        status,
    )

    is AiError.RateLimited -> stringResource(
        R.string.error_rate_limited,
        provider.displayName,
        status,
    )

    is AiError.ProviderDown -> stringResource(
        R.string.error_provider_down,
        provider.displayName,
        status,
    )

    is AiError.NoNetwork -> stringResource(
        R.string.error_no_network,
        provider.displayName,
    )

    is AiError.Timeout -> stringResource(
        R.string.error_timeout,
        provider.displayName,
    )

    is AiError.RegionBlocked -> stringResource(
        R.string.error_region_blocked,
        provider.displayName,
    )

    is AiError.InsufficientCredit -> stringResource(
        R.string.error_insufficient_credit,
        provider.displayName,
        status,
    )

    is AiError.Unknown -> stringResource(
        R.string.error_unknown,
        provider.displayName,
        status?.let { " ($it)" } ?: "",
    )
}

/**
 * The provider's own words about a failure — **Developer Mode only.**
 *
 * ### The §9 item this closes, open since 2026-08-17
 *
 * `BaseHttpProvider` captures the provider's raw message, redacts any key out of it, and hands
 * it up as `AiError.Unknown.detail`. Until now **nothing rendered it.** The entry in
 * `PROJECT_STATE.md` §9 reads: "the provider's own sentence is captured and redacted and then
 * shown to nobody, so an unclassified failure is still a status code and a shrug."
 *
 * The owner met that shrug on 2026-08-28: Gemini refused his key and the app said «حدث خطأ غير
 * متوقّع». Three different causes produce that sentence and none of them can be told apart from
 * it — which is `PROJECT_STATE.md` §10 entry 1 exactly: a code names the symptom the platform
 * saw, not the cause.
 *
 * ### Why it is gated on Developer Mode
 *
 * The text is English, unstructured, and written by a vendor for their own engineers. Showing it
 * to an ordinary user replaces one unhelpful sentence with two. `AiError` exists precisely so
 * that the normal path stays in Arabic and stays typed; this is the escape hatch for the case
 * the typing could not cover, and it belongs where every other capture already lives.
 *
 * **Already redacted at the provider.** It must never be given raw request data — hard constraint
 * 3 makes a leaked key a release blocker, not a preference.
 *
 * Renders nothing at all when there is no detail, so a caller can place it unconditionally.
 */
@Composable
fun ProviderErrorDetail(
    error: AiError?,
    developerMode: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!developerMode) return
    val detail = (error as? AiError.Unknown)?.detail?.trim()
    if (detail.isNullOrEmpty()) return

    Column(modifier = modifier.padding(top = 6.dp)) {
        BidiText(
            text = stringResource(R.string.dev_provider_said),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        // Forced LTR and monospace, like every other capture: this is a vendor's English
        // sentence, often carrying a JSON fragment, and letting content detection flip it would
        // scramble the one line the reader came for.
        BidiText(
            text = detail,
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            forced = BidiDirection.Ltr,
        )
    }
}
