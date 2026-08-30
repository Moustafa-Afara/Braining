package com.braining.core.data.history

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * The one database in the app. History only.
 *
 * **Settings stay in `SharedPreferences` and that is a ruling, not an oversight** —
 * `PROJECT_STATE.md` §8: a handful of values does not justify a second storage library, and
 * `AppPreferencesImpl` carries the full reasoning. Room is here because history is a *list* that
 * must be queried, sorted and searched, which is the thing preferences cannot do.
 *
 * **No `fallbackToDestructiveMigration`, anywhere, ever.** It is the one line that would make a
 * future schema change silently delete every session the user has. `ANSWERS.md` Part 1 §10 keeps
 * text "indefinitely until the user deletes it" — an app that quietly empties its own history on
 * an upgrade has broken that promise in the way the user is least able to detect. A missing
 * migration must fail loudly in the developer's hands instead, which is what the absence of that
 * call guarantees.
 *
 * **Schemas are exported** to `core-data/schemas/` so that each migration can be written against
 * the real previous version rather than against someone's memory of it.
 */
@Database(
    entities = [SessionEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class BrainingDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao

    companion object {
        const val NAME = "braining_history.db"

        /**
         * Version 1 → 2: the model-written session name.
         *
         * **The first migration this project has ever had, and it is written rather than
         * avoided.** The tempting alternative — `fallbackToDestructiveMigration()` — makes the
         * schema change compile in one line and deletes every session the user has, silently, on
         * upgrade. `ANSWERS.md` Part 1 §10 keeps their text until *they* delete it, and this file
         * says so at the top.
         *
         * `NOT NULL DEFAULT ''` rather than a nullable column: every existing row gets a value
         * the code already knows how to handle, because an empty title falls back to the opening
         * of the idea — which is exactly what version 1 displayed. **The upgrade is invisible to
         * the user, which is the point.**
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE sessions ADD COLUMN title TEXT NOT NULL DEFAULT ''")
            }
        }
    }
}
