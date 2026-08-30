package com.braining.core.domain.history

import com.braining.core.domain.clarify.ClarifyTurn

/**
 * One finished run, as history holds it: the idea, the interrogation, the prompt that was
 * forged from it, and the answer that came back.
 *
 * **A record is written when an answer completes, not when an interrogation starts.**
 * `docs/M5_DESIGN_NOTE.md` §2: a run that produced no answer has nothing to re-run and nothing
 * worth searching, and a list full of abandoned attempts is a list the user stops opening. That
 * is a statement about *history*, not about the value of an abandoned run — `PROJECT_STATE.md`
 * §10 entry 29 stands, and gate run 2 remains the most instructive of the three.
 *
 * **What is deliberately absent, and must stay absent.** No audio, no reference to audio, no
 * field one could be put in. `ANSWERS.md` Part 6 §M2-10 deletes the recording the moment the
 * transcript returns, and the microphone rationale promises so in both locales. Room arriving in
 * this project is exactly the moment "we could keep the audio for playback" becomes a plausible
 * idea; it is not one, and the promise on the permission dialog is why.
 *
 * This is a domain type, so it holds no Room annotations. `:core-data` owns the table and maps
 * to and from this — the same split `AppPreferences` and `AppPreferencesImpl` have kept since M1,
 * and the reason the storage library can be replaced without a feature module noticing.
 */
data class SessionRecord(
    /** Room's key. [NEW] until the row exists. */
    val id: Long = NEW,
    /** Epoch millis. Sorting, and the relative-time label. */
    val createdAt: Long,
    /**
     * The transcript **as the user left it**, transcription errors included.
     *
     * The same rule `ClarifySession.originalIdea` has kept since M3: the interrogation recovers
     * meaning through conversation rather than by correcting the input, and a record that
     * silently stored a cleaned-up version would misrepresent what was actually asked.
     */
    val idea: String,
    /** The interrogation. Empty when the user declared the idea mature immediately. */
    val turns: List<ClarifyTurn> = emptyList(),
    val frameworkId: String = "",
    /** The English prompt. **This is what re-run re-runs.** */
    val forgedPrompt: String = "",
    /** The answer as the user last saw it. */
    val answer: String = "",
    /** [com.braining.core.domain.model.ProviderId.name] of whoever actually answered. */
    val providerName: String = "",
    val model: String = "",
    /**
     * The two-line Arabic summary CLARIFY reads back on later sessions.
     *
     * Stored rather than recomputed because the thing it is derived from — the engine's own
     * `[[كافٍ]]` turn — is itself stored, and a value derived at read time is a value that can
     * silently change when the derivation is edited. See [SessionSummary].
     */
    val summary: String = "",
    /**
     * The session's name in the list — **two to five Arabic words, written by the model.**
     *
     * The owner, 2026-08-28: the list was titled with the first sixty characters of the
     * transcript, which is a dictation and not a name. It is produced by the forge call that
     * happens anyway (`ForgePrompt.TITLE_MARKER`), so it costs no extra request.
     *
     * **Empty is normal, not broken.** A model that ignored the marker, or a row written before
     * this field existed, leaves it blank and the list falls back to `SessionSummary.titleOf` —
     * exactly what it showed before. A fallback that is also the old behaviour cannot regress.
     */
    val title: String = "",
) {

    /** The name to show, with the fallback applied once and in one place. */
    val displayTitle: String get() = title.ifBlank { SessionSummary.titleOf(idea) }
    companion object {
        /** Not yet written. Room assigns the real id on insert. */
        const val NEW: Long = 0L
    }
}
