package com.braining.core.domain.model

/**
 * A speech-recognition failure, classified in the domain and phrased by the UI.
 *
 * This deliberately mirrors [AiError]. `docs/M2_DESIGN_NOTE.md` §6 makes the point sharply:
 * A3 spent a whole work unit pulling English sentences out of the provider and data layers,
 * and rebuilding them here in M2 would undo that within a day. So no branch below carries a
 * message — only the facts the UI needs to speak for itself, from string resources.
 *
 * Every case is something a user can act on, or at minimum understand. That is the test for
 * adding one: if the UI could say nothing more useful than "an error occurred", it belongs in
 * [EngineFailure] with its raw code rather than in a branch of its own.
 */
sealed interface SttError {

    /**
     * `RECORD_AUDIO` was refused. Recoverable — the UI explains how to grant it, and on a
     * permanent refusal points at system settings. `docs/M2_DESIGN_NOTE.md` §6: refusal is not
     * a malfunction, and must never be presented as one.
     */
    data object PermissionDenied : SttError

    /**
     * No recognition engine exists on this device. Checked up front by
     * `SpeechToText.isAvailable()` so the microphone button can be hidden rather than offered
     * and then failing — but also reachable at runtime, because an engine can be disabled
     * between the check and the tap.
     *
     * Expected on Xiaomi HyperOS, which does not always ship one (`docs/M2_DESIGN_NOTE.md` §9).
     * If this is what the test device reports, the code is not at fault and Vosk becomes the
     * only path to local testing.
     */
    data object NoEngine : SttError

    /** The engine heard nothing it could transcribe. Common, benign, and worth saying plainly. */
    data object NoSpeechDetected : SttError

    /**
     * A cloud transcription engine is selected but its API key is missing or blank.
     *
     * **Its whole purpose is to prevent a silent downgrade.** The alternative — quietly falling
     * back to the device engine — would leave a user believing they are getting the accuracy
     * they configured while receiving something else, with nothing on screen to reveal it.
     * 2026-08-06 was spent almost entirely on faults of exactly that shape: a symptom reported
     * as a cause, an ignored setting, a stale label. This is the same class, caught in advance.
     */
    data object MissingKey : SttError

    /**
     * A cloud engine refused the key that was supplied.
     *
     * Separate from [NetworkRequired] because the first version of the Deepgram engine reported
     * every handshake failure as a network problem, and on 2026-08-06 a deliberately wrong key
     * told the owner to «تحقّق من اتصالك» on a device with full signal. He would have gone to
     * check his router. The remedy for a bad key is in Settings; the remedy for no network is
     * somewhere else entirely, and a message that names the wrong one costs more than saying
     * nothing.
     */
    data object InvalidKey : SttError

    /**
     * The engine needs a network it does not have. Some implementations require one even when
     * asked for offline recognition via `EXTRA_PREFER_OFFLINE`, which is a hint, not a
     * guarantee. Distinct from [EngineFailure] because the user can actually fix it.
     */
    data object NetworkRequired : SttError

    /**
     * The engine has no usable model for the requested language. Critical for an Arabic-first
     * app: the UI must say *which* language is missing and point at the download, not report a
     * generic failure.
     *
     * Not in the design note's original list; added because "Arabic is unavailable" and "the
     * engine broke" call for entirely different sentences.
     *
     * [code] distinguishes two causes that look identical to the user but need different
     * remedies, and telling them apart is the whole reason it is carried:
     *  - `ERROR_LANGUAGE_NOT_SUPPORTED` (12) — the engine does not know this language **tag**.
     *    Often a tag problem (`"ar"` where the engine wants `"ar-SA"`), not a missing model.
     *  - `ERROR_LANGUAGE_UNAVAILABLE` (13) — the language is known but not usable right now.
     *    With `EXTRA_PREFER_OFFLINE` set, this almost always means the **offline recognition
     *    pack** is not downloaded — which is a different thing from the system language or the
     *    keyboard language being installed, and is what confuses people who check and find
     *    Arabic "already there".
     *
     * Surfaced in Developer Mode. Do not show a bare number to an ordinary user.
     */
    data class LanguageUnavailable(val languageTag: String, val code: Int) : SttError

    /**
     * Anything else the platform reported. [code] is the raw `SpeechRecognizer.ERROR_*`
     * constant, kept for Developer Mode and for diagnosis — the UI shows a generic sentence
     * and may append the number, exactly as [AiError.Unknown] handles an unmapped HTTP status.
     */
    data class EngineFailure(val code: Int) : SttError
}
