package com.braining.core.domain.speech

import com.braining.core.domain.model.SttError
import kotlinx.coroutines.flow.Flow

/**
 * On-device speech recognition, behind an interface so the engine can be replaced.
 *
 * `ANSWERS.md` Part 1 §1 chooses Android's `SpeechRecognizer` as the default — zero APK cost,
 * no model download — **and pre-approves Vosk as the replacement** if the mandatory 60–90
 * second Arabic gate fails. That swap must not touch this file. Everything the UI consumes is
 * a [TranscriptionEvent]; nothing here names an engine, a threading model, or a permission.
 */
interface SpeechToText {

    /**
     * Is there any recognition engine on this device at all?
     *
     * Checked **before showing the microphone button** (`docs/M2_DESIGN_NOTE.md` §6): a button
     * that is present and always fails is worse than a button that is absent. `suspend`
     * because an implementation may have to bind to a service to find out.
     */
    suspend fun isAvailable(): Boolean

    /**
     * Start listening. One flow carries everything the UI needs; [stop] ends it.
     *
     * **Why amplitude rides in the same stream as text.** It arrives from the same platform
     * listener, and splitting it out would mean a second interface and a second piece of state
     * to keep in sync by hand. The project already made this call once: Developer Mode passed
     * `AiChunk.Meta` down the token stream rather than adding a method to `AiProvider`, and the
     * reasoning is written into `AiChunk.kt`. Same shape, same decision.
     *
     * @param languageTag BCP-47. Arabic is the default because this app is Arabic-first; the
     *   parameter exists so the English UI toggle can dictate recognition too.
     */
    fun transcribe(languageTag: String = "ar"): Flow<TranscriptionEvent>

    /**
     * Stop listening and let the engine deliver whatever it has.
     *
     * Distinct from cancelling the flow's coroutine: this is the user pressing "done" and
     * expecting their last sentence to survive, not the user abandoning the recording.
     */
    fun stop()
}

/**
 * Everything a transcription run can report.
 *
 * The stream ends with exactly one of [Completed] or [Failed]. It may carry any number of
 * [Partial], [Segment] and [Amplitude] events before that, in any order.
 */
sealed interface TranscriptionEvent {

    /**
     * An unstable guess that will change while the user keeps talking. Show it, but never
     * append it to accumulated text — the next [Partial] replaces it.
     */
    data class Partial(val text: String) : TranscriptionEvent

    /**
     * A stabilised span of speech. Segments are **appended** to build the final transcript.
     *
     * **More than one segment means the engine restarted.** `SpeechRecognizer` stops at the
     * first meaningful silence, and a natural Arabic paragraph is full of real pauses, so the
     * implementation restarts it and stitches the pieces. The owner ruled that acceptable as a
     * mitigation (`ANSWERS.md` Part 5 §M2-3) **on condition that the segment count of each
     * gate run is published** in `PROJECT_STATE.md` §7 — because the gap between stopping and
     * starting swallows words, and a stitched transcript is not the same achievement as an
     * uninterrupted one. Count these; do not quietly average them away.
     */
    data class Segment(val text: String) : TranscriptionEvent

    /**
     * Microphone loudness, normalised to `0f..1f`. Feeds the waveform and nothing else.
     *
     * `docs/BRAND.md` §6: the logo's five bars *are* the live visualiser, and that is "the
     * signature interaction of the app". It has to move with real sound — a decorative
     * animation that ignores the microphone is the one outcome this event exists to prevent.
     */
    data class Amplitude(val level: Float) : TranscriptionEvent

    /**
     * Which configuration the engine actually accepted — emitted once it is ready to listen.
     *
     * **Why this exists.** The implementation walks an ordered ladder of language tags and
     * offline/online modes, and until 2026-08-06 nothing recorded which rung won. That
     * silence hid a real defect for two days: the offline rung led the ladder, so on a device
     * with the offline pack installed the app *always* used the weak on-device model and
     * never reached the far more accurate network one. The transcript was poor and there was
     * no way to tell whether the cause was the model, the dialect tag, or the audio.
     *
     * Two facts, surfaced in Developer Mode, make the next bad transcript diagnosable instead
     * of merely disappointing. Do not remove this because the ladder "obviously" works.
     */
    data class EngineConfig(val languageTag: String, val offline: Boolean) : TranscriptionEvent

    /** Terminal. The run failed; [error] is classified, not phrased. */
    data class Failed(val error: SttError) : TranscriptionEvent

    /** Terminal. The engine finished normally and delivered everything it had. */
    data object Completed : TranscriptionEvent
}
