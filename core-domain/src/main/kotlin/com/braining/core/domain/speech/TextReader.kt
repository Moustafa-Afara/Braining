package com.braining.core.domain.speech

import kotlinx.coroutines.flow.Flow

/**
 * Reads an answer aloud. `ANSWERS.md` Part 1 §7 and Part 11 §K3 — **opt-in, off by default.**
 *
 * The mirror of [SpeechToText] and deliberately a separate interface rather than two more methods
 * on it: nothing that transcribes needs to speak, the two have different permissions (one needs
 * the microphone, this needs nothing), and a single interface would force every implementation to
 * stub half of itself.
 *
 * ### What the first attempt got wrong, recorded here because the shape recurs
 *
 * The 2026-08-28 build shipped `speak`, `stop` and a boolean. The owner's report: the button
 * "ينضغط مرتين" — it turned into «أوقف الاستماع» and back within a moment — and sometimes ran
 * silently. Both symptoms have one cause and **the interface was what hid it**: a boolean can say
 * *speaking* or *not speaking*, and has no way at all to say *your phone has no Arabic voice*.
 * So the failure was reported as an instantaneous success followed by an instantaneous finish.
 *
 * `PROJECT_STATE.md` §10 entry 7 — a test whose pass and fail look identical is not a test — and
 * entry 5: when a subsystem chooses a strategy at runtime, the chosen strategy is diagnostic
 * output. [status] is that output.
 */
interface TextReader {

    /**
     * Everything the reader knows about itself, live.
     *
     * A flow rather than a suspend query because the answer **changes**: the engine is built
     * lazily, initialises asynchronously, and only learns whether it has a voice at the moment it
     * is asked to use one. A one-shot check runs before any of that is true.
     */
    val status: Flow<ReaderStatus>

    /**
     * The character range of the word being spoken **right now**, inside the text passed to
     * [speak]. Null between utterances.
     *
     * Fed by the platform's word-boundary callback, which exists from API 26 — this project's
     * minSdk exactly. **Not every engine reports it**, which is why [ReaderStatus.reportsWords]
     * exists: a highlight that never moves must be explainable as "your engine does not report
     * word boundaries" rather than looking like a frozen screen.
     */
    val spokenRange: Flow<IntRange?>

    /**
     * Speak [text]. Replaces anything already being spoken.
     *
     * **Replaces rather than queues, deliberately.** The user pressing the button on a second
     * answer means "read this one", never "read this after the other one finishes" — a queue here
     * would make the button's effect depend on state the user cannot see.
     */
    fun speak(text: String, languageTag: String)

    fun stop()
}

/**
 * The reader's own account of itself. Read by the UI to decide what to offer and what to explain.
 */
data class ReaderStatus(
    /** Null until the engine's asynchronous init has answered. */
    val ready: Boolean? = null,
    /** True while an utterance is actually being spoken — set by the engine, never optimistically. */
    val speaking: Boolean = false,
    /**
     * Whether a voice for the requested language is genuinely usable.
     *
     * **Null until something has been spoken, and that is not laziness.** `isLanguageAvailable`
     * routinely answers "available" on a device whose voice data is not installed; the truth only
     * arrives when `setLanguage` is called for real. So the app asks the cheap question first and
     * corrects itself with the expensive one — and this field is how the correction reaches the
     * screen instead of vanishing.
     */
    val voiceUsable: Boolean? = null,
    /** True once a word boundary has ever been reported. See [TextReader.spokenRange]. */
    val reportsWords: Boolean = false,
    val failure: ReaderFailure? = null,
    /** The engine package, e.g. `com.google.android.tts`. Diagnostic only. */
    val engine: String = "",
    /** The raw `TextToSpeech.LANG_*` result of the last `setLanguage`. Diagnostic only. */
    val languageCode: Int? = null,
)

/**
 * Why the reader could not speak — **typed, never phrased here.** The same rule `AiError` follows:
 * the domain carries facts, the UI carries sentences.
 */
sealed interface ReaderFailure {
    /** No text-to-speech engine on the device at all. */
    data object NoEngine : ReaderFailure

    /**
     * An engine exists but has no voice for the language.
     *
     * **The commonest one by far, and the one the first build could not report.** It is also the
     * only one the user can fix, which is why the screen that shows it offers the way.
     */
    data object NoVoiceForLanguage : ReaderFailure

    /** The engine accepted the request and then failed. [code] is the platform's own. */
    data class SpeakFailed(val code: Int) : ReaderFailure
}
