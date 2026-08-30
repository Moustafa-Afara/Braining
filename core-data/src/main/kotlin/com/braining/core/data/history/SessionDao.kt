package com.braining.core.data.history

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Query("SELECT * FROM sessions ORDER BY createdAt DESC")
    fun all(): Flow<List<SessionEntity>>

    /**
     * Substring match over the normalized column.
     *
     * **`[query]` must already be normalized by the caller** — the same
     * `ArabicNormalizer.normalize` that wrote `searchText`. That is the one invariant this file
     * cannot enforce and the whole reason the normalizer is a single shared object rather than a
     * private helper: two nearly-identical folds would produce a search that works for whoever
     * wrote it and silently fails for the user (`docs/M5_DESIGN_NOTE.md` §5).
     *
     * The `ESCAPE` clause is not decoration. `%` and `_` are wildcards in `LIKE`, so a user
     * searching for a literal underscore — a file name, a variable — would otherwise match
     * anything. The caller escapes them with `\` and this names `\` as the escape character.
     */
    @Query(
        "SELECT * FROM sessions WHERE searchText LIKE '%' || :query || '%' ESCAPE '\\' " +
            "ORDER BY createdAt DESC",
    )
    fun search(query: String): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun byId(id: Long): SessionEntity?

    @Query("SELECT * FROM sessions ORDER BY createdAt DESC LIMIT :limit")
    suspend fun recent(limit: Int): List<SessionEntity>

    /**
     * `REPLACE`, so saving a record that already has an id updates that row.
     *
     * The alternative — insert-or-update decided by the caller — puts the same branch in every
     * call site. A run whose answer is refined three times by feedback must end as **one** row;
     * three near-duplicates would be indistinguishable in the list and would each carry a slice
     * of the same conversation.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SessionEntity): Long

    @Query("DELETE FROM sessions WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM sessions")
    suspend fun deleteAll()
}
