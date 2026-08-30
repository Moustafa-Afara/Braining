package com.braining.core.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp

/**
 * Puts [text] on the clipboard. One implementation, so every copy control in the app behaves the
 * same way and looks the same.
 *
 * **Android shows its own confirmation toast**, so this stays silent. A second confirmation of
 * its own would be the app talking over the system.
 *
 * **It is disabled, not hidden, when there is nothing to copy.** A control that vanishes and
 * reappears as content streams in is a control the user cannot aim at; one that is visibly
 * inert says the same thing and stays where their thumb expects it.
 *
 * `LocalClipboardManager` is deprecated in favour of `LocalClipboard`, whose API is suspending.
 * The replacement is recorded in `PROJECT_STATE.md` §9 rather than taken here: this file and the
 * two older copy buttons in chat and Clarify should move together, and mixing two clipboard APIs
 * in one app is worse than using one deprecated one consistently.
 */
@Composable
fun CopyIconButton(
    text: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
) {
    val clipboard = LocalClipboardManager.current
    IconButton(
        onClick = { clipboard.setText(AnnotatedString(text)) },
        enabled = text.isNotBlank(),
        modifier = modifier.size(32.dp),
    ) {
        Icon(
            Icons.Filled.ContentCopy,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(16.dp),
        )
    }
}
