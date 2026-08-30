package com.braining.core.ui.state

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.braining.core.ui.components.QuietButton

/**
 * The three states every list and every waiting screen needs, written once.
 *
 * **Why they are here and not drawn by hand in each screen.** `BRAINING.md` §12 asks for
 * "complete loading / empty / error states on every screen", which is not a specification —
 * followed literally by four different agents it produces four different sentences about the same
 * situation, in four layouts, and the app stops looking like one thing. It is the same failure as
 * the two identical copy icons (`PROJECT_STATE.md` §10 entry 26) and the same fix as
 * `BrainingButtons`: one implementation, used everywhere, so a change to how the app says
 * "nothing here yet" happens once.
 *
 * **Every one of them takes an explanation, and [BrainingEmptyState] takes a *hint* as well.**
 * An empty state that only says «لا يوجد شيء» has told the user what they can already see. The
 * hint is what they should do about it, and it is a required parameter rather than an optional
 * one precisely so that it cannot be quietly skipped.
 */
@Composable
fun BrainingEmptyState(
    title: String,
    hint: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = hint,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * A spinner **with a sentence**.
 *
 * A bare spinner is the diagnostic `PROJECT_STATE.md` §10 entry 6 calls worse than none: it is
 * confidently blank. It says something is happening and refuses to say what, so a request that
 * has silently died looks exactly like one that is working.
 */
@Composable
fun BrainingLoadingState(
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CircularProgressIndicator(modifier = Modifier.size(28.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * A failure with a way out.
 *
 * [onRetry] is nullable because some failures genuinely have no retry — but where one exists it
 * must be offered. A dead network that leaves the user on a dead end is the shape of defect this
 * project has now fixed twice, in `ChatViewModel` and in `ClarifyViewModel.retry()`.
 *
 * The message is passed in already phrased, from `AiErrorMessage` or a string resource. **Nothing
 * here writes a sentence about a failure** — that vocabulary belongs to `AiError` and to the
 * resources, which is the whole point of A3.
 */
@Composable
fun BrainingErrorState(
    message: String,
    modifier: Modifier = Modifier,
    retryLabel: String? = null,
    onRetry: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
        )
        if (onRetry != null && retryLabel != null) {
            QuietButton(onClick = onRetry) { Text(retryLabel) }
        }
    }
}
