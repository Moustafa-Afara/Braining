package com.braining.core.ui.error

import com.braining.core.ui.components.CopyIconButton
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Row
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
 * What to do with a failure: **copy it**, and — in Developer Mode — read what the provider
 * actually said.
 *
 * ## The §9 item this closed, and the incident that proved it
 *
 * `BaseHttpProvider` captures the provider's raw message, redacts any key out of it, and hands it
 * up as `AiError.Unknown.detail`. Until 2026-08-28 **nothing rendered it**, and §9 carried the
 * entry: "shown to nobody, so an unclassified failure is still a status code and a shrug."
 *
 * Two days later that shrug had a name. Gemini refused the owner's key, the app said «حدث خطأ غير
 * متوقّع», and three causes were indistinguishable — until this line printed Google's own words:
 * `Unexpected char 0x2014 at 37 in x-goog-api-key value`. **An em dash inside the key.** Not the
 * model, not the quota, not the region. One character, invisible on a phone, and unfindable
 * without the provider's sentence. `ApiKeySanitizer` now prevents it; this is what diagnosed it.
 *
 * ## Why the copy button is not behind Developer Mode
 *
 * The owner asked for it on 2026-08-30, and the reason is the whole distribution plan: a friend
 * who hits an error cannot read a stack trace, cannot enable Developer Mode, and cannot retype an
 * English sentence into a message accurately. **They can press copy and paste it.** The button
 * copies the Arabic sentence *and* the provider's raw text, so one paste carries everything
 * needed to diagnose it.
 *
 * The **raw detail** stays behind Developer Mode: it is English, unstructured, and written by a
 * vendor for their own engineers. `AiError` exists so the normal path stays typed and in Arabic.
 *
 * **Already redacted at the provider.** It must never be given raw request data — hard constraint
 * 3 makes a leaked key a release blocker, not a preference.
 *
 * Renders nothing at all when there is no error, so a caller can place it unconditionally.
 */
@Composable
fun ProviderErrorDetail(
    error: AiError?,
    developerMode: Boolean,
    modifier: Modifier = Modifier,
) {
    if (error == null) return
    val message = error.toUserMessage()
    val detail = (error as? AiError.Unknown)?.detail?.trim()

    Column(modifier = modifier.padding(top = 4.dp)) {
        if (developerMode && !detail.isNullOrEmpty()) {
            BidiText(
                text = stringResource(R.string.dev_provider_said),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            // Forced LTR and monospace, like every other capture: this is a vendor's English
            // sentence, often carrying a JSON fragment, and letting content detection flip it
            // would scramble the one line the reader came for.
            BidiText(
                text = detail,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                forced = BidiDirection.Ltr,
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // One paste carries everything: the Arabic sentence the user can read, and the
            // provider's own words that actually identify the fault. Splitting them into two
            // buttons would guarantee that only one of them ever gets sent.
            CopyIconButton(
                text = if (detail.isNullOrEmpty()) message else "$message\n\n$detail",
                contentDescription = stringResource(R.string.copy_error),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            BidiText(
                text = stringResource(R.string.copy_error),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
