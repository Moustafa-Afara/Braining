package com.braining.core.ui.voice

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.braining.core.ui.R

/**
 * Why the microphone is wanted — shown **before** Android's own prompt.
 *
 * `docs/M2_DESIGN_NOTE.md` §6: the system dialog explains nothing, and a user who declines it
 * without context may never be asked again, so the explanation cannot come after a refusal.
 *
 * **In `core-ui` because two screens now ask for the microphone** (Chat and Clarify) and §0's
 * standing rule sends anything two features need here rather than to a peer. There is a second,
 * sharper reason it is a composable rather than a pair of string ids: the rationale text has
 * already been corrected **twice** for promising something the code did not do — first claiming
 * the audio never leaves the device, then that on-device was preferred. One copy, one sentence,
 * one place to correct it the next time the engine changes.
 */
@Composable
fun MicPermissionDialog(onAllow: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.voice_permission_title)) },
        text = { Text(stringResource(R.string.voice_permission_rationale)) },
        confirmButton = {
            TextButton(onClick = onAllow) {
                Text(stringResource(R.string.voice_permission_allow))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.voice_permission_cancel))
            }
        },
    )
}
