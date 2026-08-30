package com.braining.core.data.history

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The history table.
 *
 * **Separate from `SessionRecord`, which is the domain type.** The same split `AppPreferences`
 * and `AppPreferencesImpl` have kept since M1: the domain must not import Room, so that the
 * storage library can be replaced without a feature module noticing, and so that
 * `:core-domain` stays a pure Kotlin module its tests can run in with no Android at all
 * (`PROJECT_STATE.md` §0 rule 11 — the 23 existing checks depend on that).
 *
 * **`turnsJson` is a string, deliberately.** A `@TypeConverter` would hide the encoding inside
 * Room's generated code, where nothing can test it and a schema change would surface as a runtime
 * cast failure. Here the conversion is one visible function in [SessionRepositoryImpl], it is
 * failure-tolerant by construction, and a corrupt row costs its turns rather than the screen.
 *
 * **`searchText` is denormalized on purpose.** It holds the idea, the summary and the answer,
 * already folded by `ArabicNormalizer`. Doing that fold in SQL is impossible; doing it in Kotlin
 * at query time would mean loading every row to filter it. `docs/M5_DESIGN_NOTE.md` §5.
 */
@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val createdAt: Long,
    val idea: String,
    val turnsJson: String,
    val frameworkId: String,
    val forgedPrompt: String,
    val answer: String,
    val providerName: String,
    val model: String,
    val summary: String,
    /**
     * Added in schema version 2. `NOT NULL DEFAULT ''` in the migration, so every row written by
     * version 1 keeps working and simply has no name yet — which the UI already handles, because
     * an empty title falls back to the opening of the idea.
     */
    val title: String,
    /** Normalized haystack. Written by `ArabicNormalizer`, read by a plain SQL `LIKE`. */
    val searchText: String,
)
