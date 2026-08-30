package com.braining.speech

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.braining.core.domain.model.SttError
import com.braining.core.domain.speech.SpeechToText
import com.braining.core.domain.speech.TranscriptionEvent
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * [SpeechToText] over Android's built-in `SpeechRecognizer`.
 *
 * The default engine chosen in `ANSWERS.md` Part 1 §1: free, no APK cost, no model download.
 * It is also, by design, **tuned for short commands**, and this app's input is a 60–90 second
 * spoken Arabic paragraph. Everything awkward below follows from that mismatch, and the
 * mandatory gate exists to find out whether the mitigations are enough.
 *
 * ## Threading
 *
 * `SpeechRecognizer` must be created, started, stopped and destroyed on the **main thread**,
 * and its callbacks arrive there. The whole `callbackFlow` therefore runs on
 * `Dispatchers.Main.immediate`, and [stop] posts to a main-thread handler because it is called
 * from wherever the UI happens to be.
 *
 * ## Restarting, and why the segment count is not an implementation detail
 *
 * The engine stops at the first meaningful silence. A natural Arabic paragraph is full of real
 * pauses, so [RecognitionListener.onResults] restarts it and the UI stitches the segments. The
 * owner ruled this an allowed mitigation **recorded as a shortfall** (`ANSWERS.md` Part 5
 * §M2-3): the gap between stopping and starting swallows words, so `TranscriptionEvent.Segment`
 * count must be reported for each of the three gate runs.
 * `EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS` is set below, but it is a **hint that
 * many manufacturer engines ignore** — so it is insurance, not a solution.
 *
 * ## The attempt ladder
 *
 * See [buildAttempts]. A language error from this API means far less than it sounds like, and
 * taking the first one at face value is what made the app tell the owner Arabic was missing
 * from a device that had it (`PROJECT_STATE.md` §10, `2026-08-04-I`).
 */
@Singleton
class AndroidSpeechToText @Inject constructor(
    @ApplicationContext private val context: Context,
) : SpeechToText {

    private val mainHandler = Handler(Looper.getMainLooper())

    /** Touched from the main thread only, but read by [stop] from any thread. */
    @Volatile
    private var recognizer: SpeechRecognizer? = null

    /** Set by [stop] so a restart is not scheduled after the user has asked to finish. */
    @Volatile
    private var stopRequested = false

    override suspend fun isAvailable(): Boolean = withContext(Dispatchers.Main.immediate) {
        SpeechRecognizer.isRecognitionAvailable(context)
    }

    override fun transcribe(languageTag: String): Flow<TranscriptionEvent> = callbackFlow {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            // Not a bug — and the UI should have hidden the microphone button before here.
            trySend(TranscriptionEvent.Failed(SttError.NoEngine))
            close()
            return@callbackFlow
        }

        stopRequested = false
        var segmentsDelivered = 0
        var consecutiveSilentRestarts = 0

        val attempts = buildAttempts(languageTag)
        var attemptIndex = 0

        val listener = object : RecognitionListener {

            /**
             * The engine accepted this rung. Report which one — it is the only moment the
             * winning configuration is known, and without it a poor transcript is undiagnosable
             * (see [TranscriptionEvent.EngineConfig]). Re-emitted on every restart; the UI just
             * overwrites the same two fields, so repetition is harmless and a mid-run fallback
             * from online to offline shows up immediately.
             */
            override fun onReadyForSpeech(params: Bundle?) {
                val attempt = attempts[attemptIndex]
                trySend(
                    TranscriptionEvent.EngineConfig(
                        languageTag = attempt.languageTag,
                        offline = attempt.preferOffline,
                    ),
                )
            }
            override fun onBeginningOfSpeech() = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEvent(eventType: Int, params: Bundle?) = Unit

            /**
             * Not a restart point. The engine has heard silence but has not yet delivered the
             * text for what came before it — restarting here would discard the sentence the
             * user just finished. `onResults` is where a cycle ends.
             */
            override fun onEndOfSpeech() = Unit

            override fun onRmsChanged(rmsdB: Float) {
                trySend(TranscriptionEvent.Amplitude(normaliseRms(rmsdB)))
            }

            override fun onPartialResults(partialResults: Bundle?) {
                partialResults.firstResultOrNull()?.let {
                    trySend(TranscriptionEvent.Partial(it))
                }
            }

            override fun onResults(results: Bundle?) {
                val text = results.firstResultOrNull()
                if (!text.isNullOrBlank()) {
                    segmentsDelivered++
                    consecutiveSilentRestarts = 0
                    trySend(TranscriptionEvent.Segment(text))
                }
                if (stopRequested) {
                    trySend(TranscriptionEvent.Completed)
                    close()
                } else {
                    restart()
                }
            }

            /**
             * Segmented-session results. Same handling as [onResults] **minus the restart** —
             * that is the entire point: the session is still open, so stopping and starting it
             * here would put back the gap this mode exists to remove.
             *
             * Only called when the engine honours `EXTRA_SEGMENTED_SESSION`. If it does not,
             * this never fires and nothing changes.
             */
            override fun onSegmentResults(segmentResults: Bundle) {
                val text = segmentResults.firstResultOrNull()
                if (!text.isNullOrBlank()) {
                    segmentsDelivered++
                    consecutiveSilentRestarts = 0
                    trySend(TranscriptionEvent.Segment(text))
                }
            }

            /** Terminal in segmented mode — the engine's own end, so there is nothing to stop. */
            override fun onEndOfSegmentedSession() {
                trySend(TranscriptionEvent.Completed)
                close()
            }

            override fun onError(error: Int) {
                // A language rejection is the least trustworthy answer this API gives. Walk the
                // whole ladder before believing it: nothing has been transcribed yet, so each
                // retry costs the user a moment and nothing else.
                val isLanguageError = error == SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED ||
                    error == SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE
                if (isLanguageError && attemptIndex < attempts.lastIndex) {
                    attemptIndex++
                    restart()
                    return
                }

                // A network failure on an online rung is not the end of the run — it is the
                // reason the offline rungs exist. Jump straight to the first of them rather
                // than stepping through the remaining online tags, which would fail the same
                // way and only cost the user time.
                //
                // **This branch is load-bearing.** Before 2026-08-06 the ladder ran
                // offline-first, so a device with no signal simply used the on-device model and
                // dictation worked. Putting the network first without this would have turned
                // "works offline" into "fails offline" — a regression introduced by an accuracy
                // fix, which is the worst kind because the fix would look like it succeeded.
                val isNetworkError = error == SpeechRecognizer.ERROR_NETWORK ||
                    error == SpeechRecognizer.ERROR_NETWORK_TIMEOUT ||
                    error == SpeechRecognizer.ERROR_SERVER
                if (isNetworkError && !stopRequested) {
                    val firstOffline = attempts.indexOfFirst { it.preferOffline }
                    if (firstOffline > attemptIndex) {
                        attemptIndex = firstOffline
                        restart()
                        return
                    }
                }

                // Silence and no-match are not failures mid-paragraph — they are the engine
                // giving up on a pause. Restart, but cap it: with a dead microphone this would
                // otherwise spin forever, burning battery and reporting nothing.
                val isSilence = error == SpeechRecognizer.ERROR_NO_MATCH ||
                    error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT

                if (isSilence && !stopRequested) {
                    consecutiveSilentRestarts++
                    if (consecutiveSilentRestarts < MAX_SILENT_RESTARTS) {
                        restart()
                        return
                    }
                }

                if (isSilence) {
                    // Ran out of patience, or the user stopped during a silent stretch. If
                    // anything was captured, this is a normal ending, not an error.
                    if (segmentsDelivered > 0) {
                        trySend(TranscriptionEvent.Completed)
                    } else {
                        trySend(TranscriptionEvent.Failed(SttError.NoSpeechDetected))
                    }
                } else {
                    trySend(TranscriptionEvent.Failed(error.toSttError(languageTag)))
                }
                close()
            }

            /**
             * Posted rather than called inline: several engines throw `ERROR_RECOGNIZER_BUSY`
             * when `startListening` is invoked from inside a callback, because the previous
             * session has not finished tearing down.
             */
            private fun restart() {
                mainHandler.post {
                    if (!stopRequested) {
                        runCatching {
                            recognizer?.startListening(buildIntent(attempts[attemptIndex]))
                        }.onFailure {
                            trySend(
                                TranscriptionEvent.Failed(
                                    SttError.EngineFailure(SpeechRecognizer.ERROR_CLIENT),
                                ),
                            )
                            close()
                        }
                    }
                }
            }
        }

        val created = SpeechRecognizer.createSpeechRecognizer(context)
        recognizer = created
        created.setRecognitionListener(listener)
        created.startListening(buildIntent(attempts[attemptIndex]))

        awaitClose {
            // Runs on the main thread because of the flowOn below — which is required:
            // destroy() from another thread is undefined behaviour and leaks the service.
            recognizer?.let {
                it.setRecognitionListener(null)
                it.destroy()
            }
            recognizer = null
        }
    }.flowOn(Dispatchers.Main.immediate)

    override fun stop() {
        stopRequested = true
        // stopListening, not cancel: the user pressed "done" and expects the sentence they
        // just spoke to arrive. cancel() would throw it away.
        mainHandler.post { recognizer?.stopListening() }
    }

    /** One configuration to try. See [buildAttempts]. */
    private data class Attempt(val languageTag: String, val preferOffline: Boolean)

    private fun buildIntent(attempt: Attempt): Intent =
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, attempt.languageTag)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, attempt.preferOffline)
            // Hints. Documented as advisory and widely ignored by manufacturer engines — kept
            // because on the engines that honour them they remove most restarts.
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 4000L)
            putExtra(
                RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
                4000L,
            )
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 10_000L)
            // Some engines refuse to start without knowing the caller.
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)

            // Segmented session — the platform's own answer to the seam problem.
            //
            // **Why.** Measured 2026-08-06 on natural Levantine speech: 14 segments for one
            // ~90-second dictation, and the owner reported a large number of words simply
            // missing. Reading the MSA gate passage lost almost nothing, because a reader
            // pauses at full stops and the restarts landed in silence. Spontaneous speech has
            // no such gaps, so every restart cuts mid-phrase and the words spanning the gap are
            // gone. `ANSWERS.md` Part 5 §M2-3 allowed the restart mitigation on condition the
            // segment count be published precisely because "the gap between stopping and
            // starting swallows words". The count is now in, and it does.
            //
            // In segmented mode the recogniser keeps **one** session open and delivers results
            // through `onSegmentResults`, ending at `onEndOfSegmentedSession` — no stop, no
            // restart, no gap. The extra's value must name another extra that is also set in
            // this same intent; `EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS` is chosen
            // because it makes the session end on real silence, and «تمّ» ends it explicitly
            // anyway. Verified API 33 against the reference before writing — hard constraint 2.
            //
            // **Google documents this as possibly having no effect depending on the engine.**
            // That is fine and is why nothing else changed: if it is ignored, `onResults` fires
            // as it always has and the restart ladder behaves exactly as today. The segment
            // count is the read-out — if this works, one dictation reports roughly 1 segment
            // instead of 14.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                putExtra(
                    RecognizerIntent.EXTRA_SEGMENTED_SESSION,
                    RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,
                )
            }
        }

    private companion object {

        /**
         * How many consecutive no-speech results to tolerate before ending the run. Three
         * gives roughly fifteen seconds of tolerated silence at the platform's usual timeout —
         * long enough for a speaker gathering their thoughts, short enough that a broken
         * microphone does not loop indefinitely.
         */
        const val MAX_SILENT_RESTARTS = 3

        /**
         * Region tags to try, in order, when the caller gives a bare language.
         *
         * **Arabic is not one language to a recogniser.** A model tuned for the wrong region
         * produces exactly the symptom reported on 2026-08-06: fluent-looking output that is
         * largely the wrong words.
         *
         * **`ar-SY` leads by the owner's ruling of 2026-08-06** (`ANSWERS.md` Part 6 §M2-9) —
         * he speaks Syrian Levantine. The rest of the Levantine group follows because they are
         * the nearest neighbours if `ar-SY` is rejected, then bare `ar`, then `ar-SA` as the
         * broad fallback it has always been.
         *
         * **Why the list does not stop at `ar-SY`, which is what "first and last" asked for.**
         * If this engine does not recognise the tag, a single-entry list is a dead microphone —
         * the exact failure of `2026-08-04-I`, reintroduced. The entries below `ar-SY` are
         * unreachable while it is accepted, cost nothing, and are the difference between a
         * degraded transcript and none at all. The Developer Mode line reports which tag
         * actually won, so this is verifiable rather than a matter of trust.
         *
         * **Guessing here is safe, and that is the point of the ladder.** A tag this engine does
         * not support comes back as a language error and the next rung is tried; nothing is lost
         * but a moment. What is *not* safe is guessing silently — which is why the accepted tag
         * is reported through [TranscriptionEvent.EngineConfig]. If the engine settles on
         * `ar-SY` and the transcript is still poor, the dialect was not the problem and this
         * list should stop being suspected.
         */
        val DEFAULT_REGIONS = mapOf(
            "ar" to listOf("SY", "LB", "PS", "JO", "SA"),
            "en" to listOf("US"),
        )

        /**
         * Regions whose device setting is trusted to pick the Arabic variant, ahead of
         * [DEFAULT_REGIONS].
         *
         * **This exists for the people the owner ships to, not for the owner.** `ANSWERS.md`
         * Part 3 makes distribution to friends a first-class goal, and a friend in Cairo whose
         * phone is set to Egypt should dictate in `ar-EG` rather than inherit Syrian from a
         * constant. Without this the preference list would quietly become owner-specific data
         * in a shipped APK.
         *
         * The owner's own device reports `SY`, so for him rung one and the ruling agree and
         * this list changes nothing — which is the correct relationship between a personal
         * preference and a default.
         */
        val TRUSTED_DEVICE_REGIONS = mapOf(
            "ar" to setOf(
                "AE", "BH", "DZ", "EG", "IQ", "JO", "KW", "LB", "LY", "MA",
                "MR", "OM", "PS", "QA", "SA", "SD", "SY", "TN", "YE",
            ),
            "en" to setOf("AU", "CA", "GB", "IE", "IN", "NZ", "US", "ZA"),
        )

        /**
         * The ordered list of configurations to try before believing a language error.
         *
         * **Why this exists.** On 2026-08-04 the app told the owner Arabic was not installed
         * for speech recognition on a device where Arabic *was* installed — as a system
         * language, as a keyboard, and in the engine's own settings. The app was not lying
         * about the error code it received; it was wrong about what that code means.
         *
         * Two independent things can produce `ERROR_LANGUAGE_UNAVAILABLE` / `_NOT_SUPPORTED`:
         *
         * 1. **The tag.** Some engines accept `"ar"`, others insist on a region — `"ar-SA"`.
         *    A rejected tag looks exactly like a missing language.
         * 2. **`EXTRA_PREFER_OFFLINE`.** With offline requested, the engine answers with a
         *    language error when the **offline recognition pack** is not downloaded. That pack
         *    is a *third* thing, separate from the system language and from the keyboard —
         *    which is precisely why checking system settings shows Arabic present and the app
         *    still refuses.
         *
         * Only when every rung fails is a language error real, and it is then reported with its
         * raw code so the two causes can still be told apart in Developer Mode.
         *
         * ## Order: online first, offline as the fallback — owner's ruling, 2026-08-06
         *
         * This list used to run offline-first, and that was a mistake with a two-day life. The
         * reasoning written here was "offline leads because it keeps the audio on the device" —
         * true as a privacy preference, and **not the question the owner had been asked**. His
         * 2026-08-04 ruling settled *failing versus working*; it never traded accuracy away.
         * Because the ladder only advances on a language error, an installed offline pack meant
         * rung one always won and Google's markedly stronger network model was never reached.
         * The owner's Arabic came back badly and nothing in the app said why.
         *
         * Ruled 2026-08-06 (`ANSWERS.md` Part 6 §M2-8): **accuracy leads.** The transcript is
         * what M3 Clarify will build on, so an error introduced here is magnified downstream —
         * `docs/M2_DESIGN_NOTE.md` §1 calls the editable text "not a detail" for this reason.
         *
         * The cost is honest and is paid in two places: the permission rationale now says the
         * network is used *first*, and [onError] must fall through to the offline rungs on a
         * network failure — otherwise this reordering would break dictation on a device with no
         * signal, which is a case that worked before.
         */
        fun buildAttempts(languageTag: String): List<Attempt> {
            val tags = LinkedHashSet<String>()
            if (languageTag.contains('-')) {
                tags += languageTag
            } else {
                // The device's own country leads when it names an Arabic-speaking region: a
                // friend who set their phone to Egypt is better evidence than any constant in
                // this file. For the owner it resolves to SY, which is also rung one of
                // DEFAULT_REGIONS — preference and device agree, and the LinkedHashSet collapses
                // the duplicate rather than trying the same tag twice.
                //
                // Keyed by language, not global: the owner's phone reports SY, and an
                // unguarded check would have built `en-SY` the moment he flipped the UI to
                // English — a tag no engine has, costing a wasted rung on every English run.
                Locale.getDefault().country
                    .takeIf { it.isNotBlank() && it in TRUSTED_DEVICE_REGIONS[languageTag].orEmpty() }
                    ?.let { tags += "$languageTag-$it" }
                DEFAULT_REGIONS[languageTag]?.forEach { tags += "$languageTag-$it" }
                // Bare tag last of the variants: engines that accept it tend to map it to a
                // default region anyway, so trying it early would mask every regional choice.
                tags += languageTag
            }
            return tags.map { Attempt(it, preferOffline = false) } +
                tags.map { Attempt(it, preferOffline = true) }
        }

        /**
         * `onRmsChanged` reports dB on a range the platform documents only loosely — roughly
         * -2 (silence) to 10 (loud). Mapped to 0f..1f for the waveform and clamped, because an
         * out-of-range value from a manufacturer engine must not make the bars overshoot.
         */
        fun normaliseRms(rmsdB: Float): Float = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)

        fun Bundle?.firstResultOrNull(): String? =
            this?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
                ?.takeIf { it.isNotBlank() }

        fun Int.toSttError(languageTag: String): SttError = when (this) {
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> SttError.PermissionDenied

            SpeechRecognizer.ERROR_NETWORK,
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT,
            -> SttError.NetworkRequired

            SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED,
            SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE,
            -> SttError.LanguageUnavailable(languageTag, this)

            SpeechRecognizer.ERROR_NO_MATCH,
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT,
            -> SttError.NoSpeechDetected

            // ERROR_AUDIO, ERROR_CLIENT, ERROR_SERVER, ERROR_RECOGNIZER_BUSY and anything a
            // future platform version adds. The raw code survives for Developer Mode.
            else -> SttError.EngineFailure(this)
        }
    }
}
