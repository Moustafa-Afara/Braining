package com.braining.core.ui.diagnostics

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
import com.braining.core.domain.diagnostics.PromptPreview
import com.braining.core.ui.components.CopyIconButton
import com.braining.core.domain.model.RequestDiagnostics
import com.braining.core.ui.R
import com.braining.core.ui.text.BidiDirection
import com.braining.core.ui.text.BidiText

/**
 * Developer Mode readout for one request (`ANSWERS.md` Part 2 §9).
 *
 * Collapsed it shows only the three numbers that matter at a glance; chunk count is first
 * because 1 versus many is the whole question when streaming is in doubt. Tapping reveals the
 * resolved endpoint, the exact bytes sent, and token usage.
 *
 * The endpoint and body arrive **already redacted** from `BaseHttpProvider.redactSecrets`. This
 * composable must never be given raw request data — hard constraint 3 makes a leaked key a
 * release blocker, not a preference.
 *
 * ## Why this lives in `core-ui`
 *
 * It was private to `feature-chat` until 2026-08-07, when CLARIFY became the second screen that
 * needs it. §9's standing rule, set by `2026-08-04-B` after `AiErrorMessage` caused the same
 * problem: **feature modules are siblings; anything two of them need goes to `core-ui`, never to
 * a peer.** The alternative — `feature-clarify` depending on `feature-chat` — compiles happily
 * right up until `feature-chat` needs something back, at which point Gradle rejects the cycle
 * outright and the fix is no longer fifteen minutes.
 *
 * The strings moved with it and were **renamed** `chat_dev_*` → `dev_*`, exactly as the A3 error
 * strings were. The rename is the point rather than tidiness: because it is a rename and not a
 * copy, any call site left behind fails as an unresolved symbol at compile time instead of
 * silently resolving to a stale duplicate.
 */
@Composable
fun DiagnosticsPanel(diagnostics: RequestDiagnostics) {
    var expanded by remember { mutableStateOf(false) }

    /**
     * Whether the raw JSON is showing under the readable view.
     *
     * Defaults to false: the readable view answers the question nine times out of ten, and the
     * tenth is why the raw is still one tap away rather than deleted.
     */
    var showRaw by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(top = 8.dp),
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(modifier = Modifier.height(6.dp))

        BidiText(
            text = stringResource(
                R.string.dev_summary,
                diagnostics.chunkCount,
                diagnostics.firstChunkMillis ?: 0L,
                diagnostics.totalMillis ?: 0L,
            ),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // The single fact this whole feature exists to surface.
        if (diagnostics.chunkCount <= 1) {
            BidiText(
                text = stringResource(R.string.dev_single_chunk),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        if (expanded) {
            // A capture that never arrived must not look like a capture that is merely empty.
            //
            // Without this the expanded view shows two labels with nothing under them, which
            // reads exactly like "I tapped and nothing happened" — and the reader has no way to
            // tell a broken tap from a request that was never captured. `2026-08-06-D` is the
            // entry about a diagnostic that was confidently wrong being worse than none; a
            // diagnostic that is confidently *blank* fails the same way, more quietly.
            if (diagnostics.endpoint.isEmpty() && diagnostics.requestBody.isEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                BidiText(
                    text = stringResource(R.string.dev_no_capture),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            DiagnosticsField(
                label = stringResource(R.string.dev_endpoint),
                value = diagnostics.endpoint,
            )

            // ── what was actually asked, readable ────────────────────────────────────────
            //
            // The owner, 2026-08-28: the body "متداخل ببعضه وفيه رموز \n ورموز إنجليزية". He is
            // right — it is one JSON line in which every newline is the two characters `\n`,
            // English keys wrap Arabic text, and the whole thing reads left-to-right.
            //
            // **The raw body did not go away, and that is deliberate.** It is the thing that
            // found three real faults in this project, precisely because it is the bytes and not
            // a description of them. `PROJECT_STATE.md` §10 entry 6: a diagnostic that is
            // confidently wrong is worse than none, and a prettified view is a summary — a
            // missing character survives it and dies in the raw. So: readable first, raw one tap
            // away, and the reader is told which one they are looking at.
            val sections = remember(diagnostics.requestBody) {
                PromptPreview.of(diagnostics.requestBody)
            }

            if (sections.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                BidiText(
                    text = stringResource(R.string.dev_request_readable),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                sections.forEach { section -> PromptSection(section) }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(
                        if (showRaw) R.string.dev_request_hide_raw else R.string.dev_request_show_raw,
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        // Its own click target, and it stops the tap from also collapsing the
                        // whole panel — the parent Column is clickable, and a control nested
                        // inside a clickable parent that does not consume its own tap is a
                        // control that appears to do two things at once.
                        .clickable { showRaw = !showRaw }
                        .padding(vertical = 4.dp),
                )
            }

            if (showRaw || sections.isEmpty()) {
                DiagnosticsField(
                    label = stringResource(R.string.dev_request),
                    value = diagnostics.requestBody,
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            val usage = diagnostics.usage
            BidiText(
                text = if (usage == null || usage.totalTokens == 0) {
                    stringResource(R.string.dev_tokens_missing)
                } else {
                    stringResource(
                        R.string.dev_tokens,
                        usage.promptTokens,
                        usage.completionTokens,
                        usage.totalTokens,
                    )
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * One labelled part of the request, as a person would read it.
 *
 * **RTL and content-directed**, unlike [DiagnosticsField]: this text *is* the Arabic prompt, not
 * JSON wrapped around it, so `BidiText`'s default content detection is right here and forcing LTR
 * would be the mistake. The two functions differ in exactly that, which is why they are two
 * functions rather than one with a flag.
 */
@Composable
private fun PromptSection(section: PromptPreview.Section) {
    val label = when (section.kind) {
        PromptPreview.Kind.MODEL -> R.string.dev_part_model
        PromptPreview.Kind.SYSTEM -> R.string.dev_part_system
        PromptPreview.Kind.USER -> R.string.dev_part_user
        PromptPreview.Kind.ASSISTANT -> R.string.dev_part_assistant
        PromptPreview.Kind.RAW -> R.string.dev_part_raw
    }
    Spacer(modifier = Modifier.height(6.dp))
    // Label and copy on one line. The owner asked for this on 2026-08-30: the part he wants out
    // of the app is almost always **one** section — the system prompt — and selecting several
    // thousand characters of it by hand on a phone is not something anyone does twice.
    Row(verticalAlignment = Alignment.CenterVertically) {
        BidiText(
            text = stringResource(label),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        CopyIconButton(
            text = section.text,
            contentDescription = stringResource(R.string.copy_part),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    // An empty part is shown as empty rather than skipped — see `PromptPreview.of`. A turn that
    // silently disappeared from this panel would be the panel lying about the request.
    if (section.text.isBlank()) {
        BidiText(
            text = stringResource(R.string.dev_part_empty),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
        )
    } else {
        BidiText(
            text = section.text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
    // **Named, never silent.** A preview that shortens without saying so can hide the very line
    // the reader is looking for and leave them concluding it was never sent.
    if (section.truncated > 0) {
        BidiText(
            text = stringResource(R.string.dev_part_truncated, section.truncated),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
private fun DiagnosticsField(label: String, value: String) {
    Spacer(modifier = Modifier.height(8.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        BidiText(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
        )
        // The raw body too: it is the one a bug report should carry, because it is the bytes.
        CopyIconButton(
            text = value,
            contentDescription = stringResource(R.string.copy_action),
            tint = MaterialTheme.colorScheme.primary,
        )
    }
    // Forced LTR: a URL and a JSON body read left-to-right no matter how much Arabic the payload
    // happens to contain. Letting content detection decide would flip the whole blob the moment
    // the Arabic prompt outweighed the JSON syntax around it — and with CLARIFY that is no
    // longer hypothetical: its system prompt is several hundred Arabic characters inside one
    // JSON string, which is by some margin the most Arabic-heavy body this app will ever send.
    BidiText(
        text = value,
        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        forced = BidiDirection.Ltr,
    )
}
