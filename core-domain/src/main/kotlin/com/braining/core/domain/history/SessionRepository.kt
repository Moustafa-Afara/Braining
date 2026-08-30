package com.braining.core.domain.history

import kotlinx.coroutines.flow.Flow

/**
 * Reads and writes the session history.
 *
 * **On-device only, and that is the ruling rather than the current implementation.**
 * `ANSWERS.md` Part 11 §K5: no sync, no server, closed question. There is deliberately no
 * `sync()`, no `upload()`, and no remote id on [SessionRecord] — an interface with a hole shaped
 * like a server invites one to be built. If the question is ever reopened it should be reopened
 * by a ruling and a redesign, not by an agent filling in an obvious blank.
 */
interface SessionRepository {

    /** Everything, newest first. Live: a delete or a save updates any screen collecting it. */
    fun all(): Flow<List<SessionRecord>>

    /**
     * Sessions whose text matches [query], newest first. A blank query is [all].
     *
     * Matching runs over a **normalized** copy of the text, written at save time by
     * [com.braining.core.domain.text.ArabicNormalizer]. `docs/M5_DESIGN_NOTE.md` §5: a user who
     * types `احمد` must find `أحمد`, and the failure without it is invisible — an empty result
     * that looks like an empty history.
     */
    fun search(query: String): Flow<List<SessionRecord>>

    suspend fun byId(id: Long): SessionRecord?

    /**
     * Insert or update. Returns the row id — the caller keeps it so the next update of the same
     * run overwrites rather than appending a near-duplicate every time an answer is refined.
     */
    suspend fun save(record: SessionRecord): Long

    suspend fun delete(id: Long)

    suspend fun deleteAll()

    /**
     * The most recent [limit] summaries, newest first — **the context CLARIFY reads back**.
     *
     * Only the fields that feed [HistoryContext] are needed, but the whole record is returned
     * rather than a narrower type: a second projection type is a second thing to keep in step
     * with the table for no gain at this size.
     */
    suspend fun recent(limit: Int): List<SessionRecord>

    /**
     * Bytes the history occupies on disk, for the readout in Settings.
     *
     * **The database file's own size, not a sum of string lengths.** `ANSWERS.md` Part 1 §10 asks
     * for storage *used*, and a user comparing this figure with Android's app-info screen must
     * not find two different numbers — page overhead, indices and the write-ahead log are part of
     * what history costs them.
     */
    suspend fun storageBytes(): Long
}
