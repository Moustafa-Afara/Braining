package com.braining.core.ui.voice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.braining.core.ui.R
import com.braining.core.ui.components.PrimaryButton
import com.braining.core.ui.components.QuietButton
import com.braining.core.ui.text.BidiText

/**
 * The recording panel: the live mark, what has been heard so far, and two ways out.
 *
 * **It is docked at the foot of the screen and is deliberately NOT a modal sheet.**
 *
 * It was a `ModalBottomSheet` until 2026-08-17. A modal draws a scrim over the page, swallows
 * every touch behind it, and reads a touch outside itself as "dismiss". The owner's report was
 * exact: while answering a question by voice he could not scroll the conversation back to re-read
 * what he was answering, and the first touch that tried to ended the recording. Neither is a bug
 * inside the sheet — both are what a modal *is*. So the modality went, not the layout.
 *
 * The screen now places this panel as an ordinary child at its foot, in place of the input row.
 * The conversation above keeps its own scrolling and its own touches; nothing here reaches it.
 *
 * **The cancel button exists because the swipe-away did.** Removing the modal removed the gesture
 * that abandoned a run without keeping its words, so the gesture is replaced by a labelled
 * control rather than left to be discovered — `PROJECT_STATE.md` §10 entry 26.
 *
 * @param transcript what is already committed — the same string the input field holds, so the
 *   user watches their words accumulate in the place they will later edit them.
 * @param partial the engine's unstable guess, shown dimmed so it reads as "still deciding"
 *   rather than as text that has been accepted.
 * @param onDone stop recording and keep the words.
 * @param onCancel stop recording and drop them.
 */
@Composable
fun VoiceCapturePanel(
    amplitude: Float,
    transcript: String,
    partial: String,
    onDone: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BrainingWaveform(amplitude = amplitude)

            Text(
                text = stringResource(R.string.voice_listening),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )

            // Bounded and scrollable. The ceiling is lower than the old sheet's 220dp on
            // purpose: this panel now shares the screen with the conversation instead of
            // covering it, and every dp it takes is a dp the user cannot read back.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 44.dp, max = 120.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (transcript.isBlank() && partial.isBlank()) {
                    Text(
                        text = stringResource(R.string.voice_speak_now),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    // Through BidiText, because dictated Arabic will contain English terms and
                    // this file must not be the one place that decides direction for itself —
                    // core-ui/text/BidiText.kt already cost a day of getting that wrong.
                    if (transcript.isNotBlank()) {
                        BidiText(
                            text = transcript,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    if (partial.isNotBlank()) {
                        BidiText(
                            text = partial,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                QuietButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.voice_cancel))
                }
                PrimaryButton(
                    onClick = onDone,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.voice_done))
                }
            }
        }
    }
}
