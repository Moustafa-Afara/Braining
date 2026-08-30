package com.braining.core.data.history

import android.content.Context
import com.braining.core.domain.clarify.ClarifyTurn
import com.braining.core.domain.history.SessionRecord
import com.braining.core.domain.history.SessionRepository
import com.braining.core.domain.text.ArabicNormalizer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Room behind the domain interface.
 *
 * **Nothing here throws.** The same rule `AppPreferencesImpl` and `EncryptedKeyStoreImpl` follow:
 * a history that cannot be read should cost the user their history, not their app. A corrupt
 * `turnsJson` costs that row its turns and keeps its idea, its prompt and its answer — all three
 * of which are what the list and the re-run actually need.
 */
@Singleton
class SessionRepositoryImpl @Inject constructor(
    private val dao: SessionDao,
    /**
     * Held for one operation: `VACUUM` after a full delete. See [deleteAll].
     *
     * Room offers no typed way to run it — it cannot go in a transaction and returns no rows —
     * so it goes through the support helper, which is the documented route rather than a
     * workaround.
     */
    private val database: BrainingDatabase,
    @ApplicationContext private val context: Context,
) : SessionRepository {

    /**
     * A private [Json] rather than the injected one.
     *
     * `CoreDataModule`'s instance is tuned for **provider responses** — `isLenient`,
     * `ignoreUnknownKeys` — which are the right settings for parsing someone else's JSON and the
     * wrong ones for a format this app both writes and reads. Here `ignoreUnknownKeys` is kept
     * for one specific reason: it is what lets a **newer** build's record be read by an older one
     * without an exception, which matters the moment a sixth `ClarifyTurn` kind is added.
     */
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override fun all(): Flow<List<SessionRecord>> = dao.all().map { list -> list.map(::toDomain) }

    override fun search(query: String): Flow<List<SessionRecord>> {
        val normalized = ArabicNormalizer.normalize(query)
        if (normalized.isEmpty()) return all()
        return dao.search(escapeLike(normalized)).map { list -> list.map(::toDomain) }
    }

    override suspend fun byId(id: Long): SessionRecord? =
        runCatching { dao.byId(id)?.let(::toDomain) }.getOrNull()

    override suspend fun save(record: SessionRecord): Long =
        runCatching { dao.upsert(toEntity(record)) }.getOrDefault(SessionRecord.NEW)

    override suspend fun delete(id: Long) {
        runCatching { dao.delete(id) }
    }

    /**
     * Delete every row **and give the disk back**.
     *
     * **`VACUUM` is not housekeeping here, it is correctness.** SQLite does not shrink its file
     * when rows are deleted; without this, «احذف الكل» would leave the size readout in Settings
     * unchanged, and a user acting on that number would conclude the delete had failed. The
     * readout is what `ANSWERS.md` Part 1 §10 put in place of a size cap, so a figure that lies
     * defeats the ruling.
     */
    override suspend fun deleteAll() {
        runCatching { dao.deleteAll() }
        withContext(Dispatchers.IO) {
            runCatching { database.openHelper.writableDatabase.execSQL("VACUUM") }
        }
    }

    override suspend fun recent(limit: Int): List<SessionRecord> =
        runCatching { dao.recent(limit).map(::toDomain) }.getOrDefault(emptyList())

    /**
     * The database file plus its sidecars.
     *
     * **`-wal` and `-shm` are counted deliberately.** Room runs in write-ahead-logging mode, and
     * the write-ahead log routinely holds more bytes than the main file between checkpoints.
     * Reporting only `.db` would show a user with a full history a figure of a few kilobytes, and
     * a number that disagrees with Android's own app-info screen is worse than no number
     * (`PROJECT_STATE.md` §10 entry 6).
     */
    override suspend fun storageBytes(): Long = withContext(Dispatchers.IO) {
        runCatching {
            val base = context.getDatabasePath(BrainingDatabase.NAME)
            listOf(base, File(base.path + "-wal"), File(base.path + "-shm"))
                .filter { it.exists() }
                .sumOf { it.length() }
        }.getOrDefault(0L)
    }

    // ── mapping ──────────────────────────────────────────────────────────────────────────

    private fun toEntity(r: SessionRecord): SessionEntity = SessionEntity(
        id = r.id,
        createdAt = r.createdAt,
        idea = r.idea,
        // The explicit-serializer overload, which is this project's established form
        // (`BaseHttpProvider`, every provider). It needs no reified extension import and cannot
        // silently resolve to a different overload after a library bump.
        turnsJson = runCatching {
            json.encodeToString(TURNS, r.turns)
        }.getOrDefault("[]"),
        frameworkId = r.frameworkId,
        forgedPrompt = r.forgedPrompt,
        answer = r.answer,
        providerName = r.providerName,
        model = r.model,
        summary = r.summary,
        title = r.title,
        // Written by the same function the query uses. The title joins the haystack because it is
        // the line the user actually reads in the list — searching for what you can see must
        // work. The **prompt** is deliberately still out: it is English boilerplate on every row
        // (`# ROLE`, `# CONTEXT`, `# OBJECTIVE`) and would make a search for a common English
        // word return the entire history.
        searchText = ArabicNormalizer.normalize(
            listOf(r.title, r.idea, r.summary, r.answer).joinToString(" "),
        ),
    )

    private fun toDomain(e: SessionEntity): SessionRecord = SessionRecord(
        id = e.id,
        createdAt = e.createdAt,
        idea = e.idea,
        turns = runCatching {
            json.decodeFromString(TURNS, e.turnsJson)
        }.getOrDefault(emptyList()),
        frameworkId = e.frameworkId,
        forgedPrompt = e.forgedPrompt,
        answer = e.answer,
        providerName = e.providerName,
        model = e.model,
        summary = e.summary,
        title = e.title,
    )

    /**
     * Neutralize `LIKE`'s own wildcards.
     *
     * `%` and `_` mean "anything" and "any one character" inside `LIKE`, so a user searching for
     * `_` would match every row and a user searching for a path or a variable name would get
     * nonsense. The backslash is escaped first — otherwise escaping `%` would introduce a
     * backslash that the next pass would escape again.
     */
    private fun escapeLike(s: String): String =
        s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")

    private companion object {
        /** One serializer, built once. `ClarifyTurn` is sealed, so this covers every kind. */
        private val TURNS = ListSerializer(ClarifyTurn.serializer())
    }
}
