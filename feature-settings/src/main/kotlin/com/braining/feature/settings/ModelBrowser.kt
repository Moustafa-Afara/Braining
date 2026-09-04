package com.braining.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.braining.core.domain.model.RemoteModel
import com.braining.core.ui.text.BidiDirection
import com.braining.core.ui.text.BidiText

/**
 * Pick a model from the several hundred a key can reach.
 *
 * ## Why a browser and not a text field
 *
 * OpenRouter's ids are namespaced and numerous — `anthropic/claude-sonnet-4`, `qwen/qwen3-8b`,
 * `meta-llama/llama-4-scout`. Typed from memory they fail as a 404 that reads like a broken app,
 * and the user has no way to discover what the right spelling was. The same reasoning that gave
 * Ollama a picker, at a scale that makes it not optional.
 *
 * ## Search, because a list of hundreds is not a choice
 *
 * The filter matches the id **and** the human name, so both «claude» and «Anthropic» find the
 * same rows — a user knows one or the other, rarely both, and which one they know is not
 * something this screen can predict.
 *
 * Free models are marked and sorted first (`OpenRouterProvider.listModels`): for someone deciding
 * whether to try this app at all, "costs nothing" is the only property of a model that changes
 * the decision.
 */
@Composable
fun ModelBrowser(
    models: List<RemoteModel>,
    loading: Boolean,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    // `rememberSaveable`, not `remember`. A plain `remember` is exactly what a rotation
    // discards — so a user who had typed «claude» into a list of several hundred lost it by
    // turning the phone. The earlier comment here asserted the reverse; it was wrong, and the
    // code was written to match the wrong sentence.
    var query by rememberSaveable { mutableStateOf("") }

    // Filtered here rather than in the ViewModel: which rows are visible is a property of what
    // is on screen, not of the app, and the whole list is already in memory. `remember(models,
    // query)` keeps the filter off the recomposition path while the user types.
    val visible = remember(models, query) {
        val q = query.trim()
        if (q.isEmpty()) {
            models
        } else {
            models.filter {
                it.id.contains(q, ignoreCase = true) || it.label.contains(q, ignoreCase = true)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.openrouter_close)) }
        },
        title = {
            BidiText(
                text = stringResource(R.string.openrouter_browse),
                style = MaterialTheme.typography.titleMedium,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                when {
                    loading -> Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        BidiText(
                            text = stringResource(R.string.openrouter_loading),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }

                    models.isEmpty() -> BidiText(
                        text = stringResource(R.string.openrouter_none),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )

                    else -> {
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            label = { Text(stringResource(R.string.openrouter_search)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        BidiText(
                            text = stringResource(R.string.openrouter_count, visible.size),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        // Bounded so the dialog cannot grow past the screen on a long list, and
                        // lazy so several hundred rows cost only the ones being looked at.
                        LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                            items(visible, key = { it.id }) { model ->
                                ModelRow(model = model, onPick = { onPick(model.id); onDismiss() })
                            }
                        }
                    }
                }
            }
        },
    )
}

@Composable
private fun ModelRow(model: RemoteModel, onPick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPick)
            .padding(vertical = 6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BidiText(
                text = model.label,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            if (model.free) {
                BidiText(
                    text = stringResource(R.string.openrouter_free_badge),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
        // Forced LTR: the id is an identifier that goes into a request verbatim, and one
        // reordered by content detection is a string the user cannot match against the site.
        BidiText(
            text = model.id,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            forced = BidiDirection.Ltr,
        )
    }
}
