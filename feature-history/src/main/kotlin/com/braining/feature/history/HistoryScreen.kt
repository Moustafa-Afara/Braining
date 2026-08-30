package com.braining.feature.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.braining.core.domain.history.RelativeTime
import com.braining.core.domain.history.SessionRecord
import com.braining.core.domain.model.ProviderId
import com.braining.core.domain.text.StorageSize
import com.braining.core.ui.components.PrimaryButton
import com.braining.core.ui.components.QuietButton
import com.braining.core.ui.state.BrainingEmptyState
import com.braining.core.ui.state.BrainingLoadingState
import com.braining.core.ui.text.BidiText

/**
 * The session list — `BRAINING.md` §12, `ANSWERS.md` Part 1 §10 and Part 11 §K1.
 *
 * **Three states, and they are genuinely three.** Loading, empty-because-new, and
 * empty-because-the-search-found-nothing say different things and must not share a sentence: a
 * new user told «لا نتائج للبحث» learns nothing, and a searching user told «لم تبدأ أي جلسة بعد»
 * is told something false.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit = {},
    /** Re-run: hands the id to the NavGraph, which opens CLARIFY on the saved prompt. */
    onRerun: (Long) -> Unit = {},
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    // One clock read per recomposition of the list, not one per row: fifteen rows each calling
    // `currentTimeMillis` would label the same instant fifteen slightly different ways.
    val now = remember(state.sessions) { System.currentTimeMillis() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.history_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        // AutoMirrored — hard constraint 6: directional icons mirror in RTL.
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.history_back),
                        )
                    }
                },
                actions = {
                    if (state.sessions.isNotEmpty() || state.query.isNotEmpty()) {
                        IconButton(onClick = viewModel::askDeleteAll) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = stringResource(R.string.history_delete_all),
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::updateQuery,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
                label = { Text(stringResource(R.string.history_search_label)) },
                placeholder = { Text(stringResource(R.string.history_search_hint)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.query.isNotEmpty()) {
                        IconButton(onClick = viewModel::clearQuery) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = stringResource(R.string.history_search_clear),
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            )

            // The undo bar. Placed above the list rather than floating over it so it can never
            // cover the row the user is about to tap — a snackbar that hides content is the
            // modality problem of `2026-08-17-A` in miniature.
            state.undoable?.let { record ->
                UndoBar(
                    title = record.displayTitle,
                    onUndo = viewModel::undoDelete,
                    onDismiss = viewModel::dismissUndo,
                )
            }

            when {
                state.loading -> BrainingLoadingState(
                    label = stringResource(R.string.history_loading),
                )

                state.sessions.isEmpty() && state.query.isNotEmpty() -> BrainingEmptyState(
                    title = stringResource(R.string.history_no_results_title),
                    hint = stringResource(R.string.history_no_results_hint),
                )

                state.sessions.isEmpty() -> BrainingEmptyState(
                    title = stringResource(R.string.history_empty_title),
                    hint = stringResource(R.string.history_empty_hint),
                )

                else -> LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.sessions, key = { it.id }) { record ->
                        SessionCard(
                            record = record,
                            now = now,
                            onRerun = { onRerun(record.id) },
                            onDelete = { viewModel.delete(record) },
                        )
                    }
                }
            }

            if (state.sessions.isNotEmpty()) {
                HorizontalDivider()
                Text(
                    text = storageLine(state.storage),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }

    if (state.confirmingDeleteAll) {
        AlertDialog(
            onDismissRequest = viewModel::cancelDeleteAll,
            title = { Text(stringResource(R.string.history_delete_all_title)) },
            // The count is in the question. «هل تحذف الكل؟» does not tell someone whether "all"
            // is two sessions or two hundred, which is the only fact that makes the answer
            // obvious either way.
            // **The unfiltered total, not the visible list.** «احذف الكل» deletes everything;
            // a dialog counting the three rows a search left on screen would understate what is
            // about to happen by two orders of magnitude.
            text = {
                Text(
                    pluralStringResource(
                        R.plurals.history_delete_all_body,
                        state.totalSessions,
                        state.totalSessions,
                    ),
                )
            },
            confirmButton = {
                PrimaryButton(onClick = viewModel::confirmDeleteAll) {
                    Text(stringResource(R.string.history_delete_all_confirm))
                }
            },
            dismissButton = {
                QuietButton(onClick = viewModel::cancelDeleteAll) {
                    Text(stringResource(R.string.history_delete_all_cancel))
                }
            },
        )
    }
}

@Composable
private fun UndoBar(title: String, onUndo: () -> Unit, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.history_deleted, title),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.weight(1f),
            )
            QuietButton(onClick = onUndo) { Text(stringResource(R.string.history_undo)) }
            IconButton(onClick = onDismiss) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = stringResource(R.string.history_undo_dismiss),
                )
            }
        }
    }
}

@Composable
private fun SessionCard(
    record: SessionRecord,
    now: Long,
    onRerun: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = ageLabel(now, record.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                if (record.providerName.isNotBlank()) {
                    Text(
                        // The record stores `ProviderId.name` — a stable key that survives a
                        // rename of the display string — so the display name is resolved here.
                        // An unknown name (a provider removed since the row was written) falls
                        // back to the raw value rather than showing nothing.
                        text = ProviderId.entries
                            .firstOrNull { it.name == record.providerName }
                            ?.displayName
                            ?: record.providerName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Column(modifier = Modifier.padding(top = 6.dp)) {
                BidiText(
                    // The model's own short name, falling back to the opening of the idea when
                    // there is none. The fallback lives on the record so the list and the undo
                    // bar cannot disagree about what a session is called.
                    text = record.displayTitle,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (record.summary.isNotBlank()) {
                    Column(modifier = Modifier.padding(top = 4.dp)) {
                        BidiText(
                            text = record.summary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Column(modifier = Modifier.height(8.dp)) {}
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // «أعِد التنفيذ» is offered only where there is something to run. A button that
                // is present and inert teaches the user the list cannot be trusted — the same
                // argument that removed the GitHub Models stub (`ANSWERS.md` Part 8 §D1).
                if (record.forgedPrompt.isNotBlank()) {
                    QuietButton(onClick = onRerun) {
                        Text(stringResource(R.string.history_rerun))
                    }
                }
                Column(modifier = Modifier.weight(1f)) {}
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.history_delete_one),
                    )
                }
            }
        }
    }
}

/**
 * The relative-time label.
 *
 * The bucket comes from the domain and the **wording** from `<plurals>`, because Arabic has a
 * dual: «منذ يومين» is not «منذ ٢ أيام», and no amount of string formatting in Kotlin gets that
 * right in both locales. See `RelativeTime`'s KDoc.
 */
@Composable
private fun ageLabel(now: Long, then: Long): String {
    val age = RelativeTime.of(now, then)
    return when (age.bucket) {
        RelativeTime.Bucket.TODAY -> stringResource(R.string.history_age_today)
        RelativeTime.Bucket.YESTERDAY -> stringResource(R.string.history_age_yesterday)
        RelativeTime.Bucket.DAYS ->
            pluralStringResource(R.plurals.history_age_days, age.count, age.count)
        RelativeTime.Bucket.WEEKS ->
            pluralStringResource(R.plurals.history_age_weeks, age.count, age.count)
        RelativeTime.Bucket.MONTHS ->
            pluralStringResource(R.plurals.history_age_months, age.count, age.count)
    }
}

@Composable
private fun storageLine(size: StorageSize.Formatted): String {
    val unit = when (size.unit) {
        StorageSize.Unit.BYTES -> stringResource(R.string.unit_bytes)
        StorageSize.Unit.KILOBYTES -> stringResource(R.string.unit_kilobytes)
        StorageSize.Unit.MEGABYTES -> stringResource(R.string.unit_megabytes)
        StorageSize.Unit.GIGABYTES -> stringResource(R.string.unit_gigabytes)
    }
    return stringResource(R.string.history_storage, size.value, unit)
}
