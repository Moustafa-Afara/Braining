package com.braining.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.braining.core.domain.history.SessionRecord
import com.braining.core.domain.history.SessionRepository
import com.braining.core.domain.text.StorageSize
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val query: String = "",
    val sessions: List<SessionRecord> = emptyList(),
    /**
     * How many sessions exist **in total**, ignoring any search.
     *
     * Separate from `sessions.size` because «احذف الكل» deletes everything while the list may be
     * showing three rows out of two hundred. A confirmation that says "1 session will be deleted"
     * and then wipes two hundred is worse than no confirmation at all — it is a wrong one, and a
     * diagnostic that is confidently wrong is believed (`PROJECT_STATE.md` §10 entry 6).
     */
    val totalSessions: Int = 0,
    /**
     * True until the first list arrives from the database.
     *
     * **Distinct from "empty", and the distinction is the whole reason it exists.** A list that
     * has not loaded and a history that has nothing in it look identical on screen, and telling a
     * new user «لا توجد جلسات» while the query is still running is a diagnostic that is
     * confidently wrong — `PROJECT_STATE.md` §10 entry 6.
     */
    val loading: Boolean = true,
    val storage: StorageSize.Formatted = StorageSize.format(0),

    /**
     * The session «حذف» just removed, held **only** so «تراجع» can put it back.
     *
     * The same trade as the forged prompt's «امسح» / «تراجع» pair, for the same reason: a
     * confirmation dialog taxes every deletion to protect the rare mistake, while an undo costs
     * nothing until the mistake happens. Deleting a second session replaces this one — an undo
     * stack is a promise about ordering that nothing on screen would explain.
     */
    val undoable: SessionRecord? = null,

    /**
     * True while «احذف الكل» is asking.
     *
     * **This one *does* get a dialog, and the split is deliberate rather than inconsistent.**
     * Delete-one is frequent and reversible in one tap; delete-all is rare and destroys
     * everything the undo could have restored. The rule this project follows: undo where the
     * action is cheap and common, confirm where it is rare and total.
     */
    val confirmingDeleteAll: Boolean = false,
)

/**
 * The history list.
 *
 * **It knows nothing about CLARIFY.** Re-running a session is a navigation event carrying an id;
 * this ViewModel neither imports `:feature-clarify` nor could. Feature modules are siblings —
 * hard constraint 8, paid for twice already (`AiErrorMessage`, `DiagnosticsPanel`).
 */
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: SessionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val queryFlow = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private val results = queryFlow
        // Typing Arabic on a phone produces a keystroke every few hundred milliseconds, and each
        // one would otherwise start a new database query and cancel the last. The debounce is not
        // about database load — SQLite would cope — it is about the list flickering between
        // partial results while the user is still writing the word.
        //
        // **Zero delay for an empty query**, because clearing the box — and the very first
        // emission when the screen opens — must not sit behind a quarter-second of nothing. Both
        // `debounce` overloads are `@FlowPreview` in coroutines 1.9, so the opt-in above buys the
        // per-value one at no extra cost.
        .debounce { if (it.isEmpty()) 0L else SEARCH_DEBOUNCE_MS }
        .flatMapLatest { q -> repository.search(q) }

    init {
        viewModelScope.launch {
            results.collect { list ->
                _uiState.update { it.copy(sessions = list, loading = false) }
            }
        }
        // The unfiltered count, for the delete-all confirmation. A second collector rather than
        // a second query shape: `all()` is already a live flow and this costs one subscription.
        viewModelScope.launch {
            repository.all().collect { list ->
                _uiState.update { it.copy(totalSessions = list.size) }
            }
        }

        refreshStorage()
    }

    fun updateQuery(text: String) {
        // Typing is moving on. An undo bar that outlives the moment it belongs to is clutter,
        // and one still offering to restore a session after the list has been filtered to
        // something else is worse than clutter — it is an offer about a row nobody can see.
        _uiState.update { it.copy(query = text, undoable = null) }
        queryFlow.value = text
    }

    fun clearQuery() = updateQuery("")

    /**
     * Remove one session and offer it back.
     *
     * The record is captured **before** the delete, from the list already on screen, rather than
     * re-read afterwards — by then it is gone and there is nothing to hold.
     */
    fun delete(record: SessionRecord) {
        _uiState.update { it.copy(undoable = record) }
        viewModelScope.launch {
            repository.delete(record.id)
            refreshStorage()
        }
    }

    /**
     * Put it back at the same id.
     *
     * The row's identity is preserved deliberately: anything that had referred to that session —
     * a re-run route the user backed out of, say — still refers to the same thing after an undo.
     */
    fun undoDelete() {
        val record = _uiState.value.undoable ?: return
        _uiState.update { it.copy(undoable = null) }
        viewModelScope.launch {
            repository.save(record)
            refreshStorage()
        }
    }

    /** The offer expires when the user moves on. An undo bar that never leaves is clutter. */
    fun dismissUndo() = _uiState.update { it.copy(undoable = null) }

    fun askDeleteAll() = _uiState.update { it.copy(confirmingDeleteAll = true) }

    fun cancelDeleteAll() = _uiState.update { it.copy(confirmingDeleteAll = false) }

    fun confirmDeleteAll() {
        _uiState.update { it.copy(confirmingDeleteAll = false, undoable = null) }
        viewModelScope.launch {
            repository.deleteAll()
            refreshStorage()
        }
    }

    private fun refreshStorage() {
        viewModelScope.launch {
            val bytes = repository.storageBytes()
            _uiState.update { it.copy(storage = StorageSize.format(bytes)) }
        }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 250L
    }
}
