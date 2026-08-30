package com.braining.speech

import com.braining.core.domain.speech.SpeechToText
import com.braining.core.domain.speech.TranscriptionEvent
import com.braining.core.domain.store.EncryptedKeyStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Chooses which engine runs: Deepgram when a key is stored, the device engine otherwise.
 *
 * ## Why a router and not a Hilt qualifier
 *
 * The choice is made at the moment of the tap, from state the user can change without leaving
 * the app. A Hilt binding is fixed when the graph is built, so entering a key in Settings would
 * not take effect until the process restarted — the same class of bug as the stale Developer
 * Mode label in `2026-08-06-D`, where the app was right and the thing reporting it was late.
 *
 * ## Why the device engine stays
 *
 * Owner's instruction, 2026-08-06. It is the only path that works with no network and no key,
 * and a friend without a Deepgram account is exactly the user `ANSWERS.md` Part 3 protects.
 *
 * ## The one thing this must never do
 *
 * **Fall back silently.** If Deepgram is selected and its call fails, that failure is reported.
 * Quietly re-running the request on the device engine would hand the user a worse transcript
 * and no way to know why — and a stitched result would poison every measurement taken
 * afterwards, which is the objection that settled D2 in `docs/DEEPGRAM_DESIGN_NOTE.md`.
 * The routing decision happens **once, before any audio**, and is then final for that run.
 */
@Singleton
class RoutingSpeechToText @Inject constructor(
    private val keyStore: EncryptedKeyStore,
    private val deepgram: DeepgramSpeechToText,
    private val android: AndroidSpeechToText,
) : SpeechToText {

    /**
     * The engine the last [transcribe] chose, so [stop] reaches the one that is actually
     * listening. Without it, «تمّ» would be delivered to whichever engine happened to be
     * bound and the other would keep the microphone open.
     */
    @Volatile private var active: SpeechToText? = null

    /**
     * True if *either* engine can run. Deepgram needs a key; the device engine needs an
     * installed recogniser. The microphone button appears if either is possible.
     */
    override suspend fun isAvailable(): Boolean =
        deepgram.isAvailable() || android.isAvailable()

    /**
     * The key is read **inside** the flow, not before returning it.
     *
     * `transcribe` is not a `suspend` function, so reading the store here in the obvious way
     * would mean `runBlocking` on whichever thread tapped the microphone — the main one. The
     * Keystore read is quick, and "quick" on the main thread is how ANRs are written. Wrapping
     * the decision in `flow { }` gives a suspending scope for free, and the choice still lands
     * before a single byte of audio is captured. The value is re-read for every dictation, so a
     * key entered in Settings takes effect on the next tap — no restart.
     */
    override fun transcribe(languageTag: String): Flow<TranscriptionEvent> = flow {
        val engine = if (!keyStore.getKey(KEY_ID).isNullOrBlank()) deepgram else android
        active = engine
        emitAll(engine.transcribe(languageTag))
    }

    override fun stop() {
        active?.stop()
    }

    private companion object {
        const val KEY_ID = "DEEPGRAM"
    }
}
