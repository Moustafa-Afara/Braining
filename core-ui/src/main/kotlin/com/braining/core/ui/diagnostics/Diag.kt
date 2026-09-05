package com.braining.core.ui.diagnostics

import android.util.Log

/**
 * One tag for everything Braining wants to see in a device log.
 *
 * Added 2026-09-05, after four hours and three captures were spent establishing that a black
 * rectangle was not a crash — because this app wrote **nothing** to logcat and every line about it
 * in 160,000 was the system talking about it from outside (`PROJECT_STATE.md` §10 entry 64). A
 * single `adb logcat -s BRAINING` now shows all of ours and none of the sensor spam.
 *
 * This is `android.util.Log`, not a file, on purpose. It is **phase 1 of the field-diagnostics
 * mechanism** the owner asked for on 2026-09-05 (`docs/M7_FIELD_DIAGNOSTICS.md`): the first job is
 * to make the app *say* something. The captured file, the device / network / locale context and
 * the share flow are that later milestone's work; this is the seam they hang on. It logs no
 * message text and no keys — a tag and a state word, never content.
 */
object Diag {
    const val TAG = "BRAINING"

    fun log(message: String) {
        Log.d(TAG, message)
    }

    /**
     * Route every uncaught exception, on any thread, through our tag before the platform's own
     * handler runs.
     *
     * It does **not** swallow anything: the previous handler is still called, so the system dialog
     * and the `am_crash` record are exactly as before. It only guarantees that if Braining ever
     * dies of an exception, the stack trace carries Braining's tag and is one grep away instead of
     * lost among framework noise.
     */
    fun installCrashHandler() {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            Log.e(TAG, "UNCAUGHT on thread '${thread.name}'", error)
            previous?.uncaughtException(thread, error)
        }
    }
}
