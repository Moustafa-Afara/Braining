package com.braining.core.ui.input

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type

/**
 * **Ctrl+Enter sends. Enter alone inserts a newline.**
 *
 * Requested by the owner on 2026-08-07, after typing a multi-line answer to a Clarify question on
 * a hardware keyboard and finding no way to send it.
 *
 * **The choice is not arbitrary and is worth defending against the obvious alternative.** Making
 * Enter send is what most chat apps do — and it is wrong for *this* app. Braining's input is not
 * a chat line; it is an idea being described, and `docs/M2_DESIGN_NOTE.md` §1 made the transcript
 * editable precisely because these fields hold paragraphs. A field where Enter sends is a field
 * you cannot write a paragraph in, and the user finds that out by sending half a thought.
 *
 * **`onPreviewKeyEvent`, not `onKeyEvent`:** the text field consumes Enter to insert its newline,
 * so a handler that runs after it never sees the key at all. Preview runs first and returning
 * `true` stops the field from also inserting a line break on the same press.
 *
 * **Key-down only.** A key press delivers both a down and an up event; acting on both would send
 * the same text twice from one keystroke.
 *
 * Costs nothing on a touch-only device: no hardware keyboard, no key events, no behaviour change.
 * It is `core-ui` rather than a feature module because Chat and Clarify both need it, and §9's
 * standing rule is that anything two features need lives here (`2026-08-04-B`).
 */
@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.submitOnCtrlEnter(onSubmit: () -> Unit): Modifier = onPreviewKeyEvent { event ->
    val isEnter = event.key == Key.Enter || event.key == Key.NumPadEnter
    if (event.type == KeyEventType.KeyDown && event.isCtrlPressed && isEnter) {
        onSubmit()
        true
    } else {
        false
    }
}
