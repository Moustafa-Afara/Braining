package com.braining.speech

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.braining.core.domain.model.SttError
import com.braining.core.domain.speech.SpeechToText
import com.braining.core.domain.speech.TranscriptionEvent
import com.braining.core.domain.store.EncryptedKeyStore
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.header
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import java.util.Locale
import kotlin.math.log10
import kotlin.math.sqrt

/**
 * Live transcription through Deepgram, over one WebSocket held open for the whole dictation.
 *
 * ## Why this exists
 *
 * [AndroidSpeechToText] passes the M2 gate and still loses words. Google's recogniser ends an
 * utterance every few seconds — measured at 6.0 and 4.8 words per segment across two real
 * dictations — and every ending is a gap where speech goes missing. Reading a written passage
 * hides it, because a reader pauses at full stops and the restarts land in silence; spontaneous
 * speech has no such gaps, so each restart cuts mid-phrase. It is structural, not tuning: the
 * silence hints are set, `EXTRA_SEGMENTED_SESSION` is refused by the engine, and the restart is
 * already as tight as `Handler.post` allows.
 *
 * **One socket, open from the first word to «تمّ». No restart, therefore no seam.**
 *
 * ## Audio never touches storage
 *
 * The owner ruled (`ANSWERS.md` Part 6 §M2-10) that raw audio is deleted the moment the
 * transcript returns. Streaming makes that stronger than the ruling asked: PCM goes from the
 * microphone buffer straight into the socket and is overwritten on the next read. **No file is
 * ever created**, so «لا يحفظ التطبيق الصوت» is a property of this code rather than a policy
 * someone has to keep.
 *
 * ## Amplitude
 *
 * We own the samples now, so the waveform is driven by a real RMS over real PCM instead of
 * Google's `onRmsChanged`, whose scale the platform documents only loosely.
 */
@Singleton
class DeepgramSpeechToText @Inject constructor(
    @ApplicationContext private val context: Context,
    private val keyStore: EncryptedKeyStore,
    // Named: the WebSocket-capable client provided by SpeechNetworkModule, not the shared
    // provider client from :core-data, which has no WebSockets plugin installed.
    @Named("speech") private val httpClient: HttpClient,
    @Named("speech") private val json: Json,
) : SpeechToText {

    @Volatile private var stopRequested = false

    /**
     * Available when a key is stored. Deliberately **not** a network check: the microphone
     * button's presence should not flicker with signal strength, and a request that fails is
     * reported as a typed error rather than by hiding the control.
     */
    override suspend fun isAvailable(): Boolean = !keyStore.getKey(KEY_ID).isNullOrBlank()

    override fun transcribe(languageTag: String): Flow<TranscriptionEvent> = callbackFlow {
        stopRequested = false
        val resolvedTag = resolveTag(languageTag)

        val apiKey = keyStore.getKey(KEY_ID)
        if (apiKey.isNullOrBlank()) {
            // Never a silent downgrade to the device engine. A user who configured a key and is
            // being served something else has no way to discover it.
            trySend(TranscriptionEvent.Failed(SttError.MissingKey))
            close()
            return@callbackFlow
        }

        // Context.checkSelfPermission, not ContextCompat: it exists since API 23 and minSdk is
        // 26, so the AndroidX helper would buy nothing and pull `androidx.core` into a module
        // that currently has no AndroidX dependency at all.
        if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            trySend(TranscriptionEvent.Failed(SttError.PermissionDenied))
            close()
            return@callbackFlow
        }

        val minBuffer = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL, ENCODING)
        if (minBuffer <= 0) {
            trySend(TranscriptionEvent.Failed(SttError.EngineFailure(minBuffer)))
            close()
            return@callbackFlow
        }

        val recorder = try {
            AudioRecord(
                // VOICE_RECOGNITION, not MIC: it asks the platform to skip the aggressive
                // noise suppression and AGC tuned for phone calls, which help a human listener
                // and hurt a recogniser.
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                SAMPLE_RATE,
                CHANNEL,
                ENCODING,
                minBuffer * BUFFER_MULTIPLIER,
            )
        } catch (_: IllegalArgumentException) {
            trySend(TranscriptionEvent.Failed(SttError.EngineFailure(0)))
            close()
            return@callbackFlow
        }

        if (recorder.state != AudioRecord.STATE_INITIALIZED) {
            recorder.release()
            trySend(TranscriptionEvent.Failed(SttError.EngineFailure(recorder.state)))
            close()
            return@callbackFlow
        }

        val session = try {
            httpClient.webSocketSession(urlFor(resolvedTag)) {
                // Header, never `?token=` in the URL. Deepgram accepts both because browser
                // WebSocket clients cannot set headers; hard constraint 3 forbids a key in a
                // URL outright, and `redactSecrets` exists because such a key leaks into every
                // diagnostic that touches it.
                header("Authorization", "Token $apiKey")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            recorder.release()
            // The handshake failed. **Ask the system whether a network exists rather than
            // guessing**: with one there is a connection and Deepgram refused us, which is
            // almost always the key; without one, nothing could have succeeded.
            //
            // Inspecting the exception for a 401 would be the direct route and is not taken —
            // the exact type Ktor raises for a non-101 upgrade is not something to assume, and
            // hard constraint 2 is a standing reminder of what assuming an API costs here.
            // Connectivity is a fact the platform will state plainly.
            val error = if (hasNetwork()) SttError.InvalidKey else SttError.NetworkRequired
            trySend(TranscriptionEvent.Failed(error))
            close()
            return@callbackFlow
        }

        trySend(TranscriptionEvent.EngineConfig(languageTag = resolvedTag, offline = false))

        // Reader: Deepgram's results arrive asynchronously and are not paced by our writes, so
        // they get their own coroutine. Interim results become Partial, finals become Segment —
        // the same two events the UI already renders, which is why no screen changes here.
        val reader = launch(Dispatchers.IO) {
            try {
                for (frame in session.incoming) {
                    if (frame !is Frame.Text) continue
                    parseMessage(frame.readText())?.let { trySend(it) }
                }
                // The loop ends when Deepgram closes the socket, which is its answer to
                // CloseStream — and this is the ONLY place that knows the run finished.
                //
                // Its absence in the first version is what the owner saw on 2026-08-06: «تمّ»
                // stopped the audio, the socket closed, and nothing ever said so. The sheet
                // stayed open on «أستمع…», `isRecording` never cleared, and the duration read
                // «٠ ثانية» because that field is only computed when Completed or Failed
                // arrives. Three symptoms, one missing line.
                trySend(TranscriptionEvent.Completed)
                close()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                trySend(TranscriptionEvent.Failed(SttError.NetworkRequired))
                close()
            }
        }

        // Writer: read PCM and push it. Also the only place amplitude comes from.
        val writer = launch(Dispatchers.IO) {
            val buffer = ByteArray(minBuffer)
            var sawAudio = false
            try {
                recorder.startRecording()
                while (isActive && !stopRequested) {
                    val read = recorder.read(buffer, 0, buffer.size)
                    if (read <= 0) continue
                    sawAudio = true
                    trySend(TranscriptionEvent.Amplitude(rms(buffer, read)))
                    session.send(Frame.Binary(true, buffer.copyOf(read)))
                }

                // «تمّ». CloseStream tells Deepgram to flush what it is still holding and send
                // the last transcript before closing. Tearing the socket down here instead
                // would discard the sentence the user just finished — the same mistake
                // `2026-08-04-H` fixed once already for the Google engine, in a different
                // mechanism.
                session.send(Frame.Text(CLOSE_STREAM))
                if (!sawAudio) trySend(TranscriptionEvent.Failed(SttError.NoSpeechDetected))
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                trySend(TranscriptionEvent.Failed(SttError.NetworkRequired))
            }
        }

        awaitClose {
            writer.cancel()
            reader.cancel()
            runCatching { recorder.stop() }
            recorder.release()
            // cancel(), not close(): awaitClose runs while this flow's scope is being torn
            // down, so a `launch { session.close() }` here would create an already-cancelled
            // job and the socket would leak. A DefaultClientWebSocketSession is a
            // CoroutineScope, so cancelling it closes the connection synchronously. The
            // graceful goodbye — CloseStream — belongs on the «تمّ» path, not this one: this
            // path is the user dismissing the sheet, and they did not ask for their words.
            runCatching { session.cancel() }
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Stops feeding audio. Does **not** tear the socket down — the writer sends `CloseStream`
     * and the reader stays alive for the final transcript that follows it.
     */
    override fun stop() {
        stopRequested = true
    }

    /**
     * Deepgram sends several message types down the same socket; only `Results` carries text.
     *
     * Every field is read defensively. `2026-08-03-C` cost a day to the fact that
     * `JsonNull.content` returns the four-character string `"null"` rather than Kotlin `null` —
     * so `contentOrNull` here is not a style preference, it is the fix for a bug this project
     * has already paid for once.
     */
    private fun parseMessage(raw: String): TranscriptionEvent? = runCatching {
        val root = json.parseToJsonElement(raw).jsonObject
        if (root["type"]?.jsonPrimitive?.contentOrNull != "Results") return null

        val text = root["channel"]?.jsonObject
            ?.get("alternatives")?.jsonArray
            ?.firstOrNull()?.jsonObject
            ?.get("transcript")?.jsonPrimitive?.contentOrNull
            ?.takeIf { it.isNotBlank() }
            ?: return null

        // is_final marks the last, best version of an utterance. Everything before it is a
        // guess that will be replaced — which is exactly the Partial/Segment split the UI was
        // built around for the Google engine.
        val isFinal = root["is_final"]?.jsonPrimitive?.booleanOrNull ?: false
        if (isFinal) TranscriptionEvent.Segment(text) else TranscriptionEvent.Partial(text)
    }.getOrNull()

    /**
     * Does this device currently have a usable network?
     *
     * Only ever used to tell a rejected key apart from a dead connection. `ACCESS_NETWORK_STATE`
     * is a normal permission — no runtime prompt — and the answer is a fact rather than an
     * inference, which is the whole reason for asking.
     */
    private fun hasNetwork(): Boolean = runCatching {
        val manager = context.getSystemService(ConnectivityManager::class.java)
        val capabilities = manager?.getNetworkCapabilities(manager.activeNetwork)
        capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }.getOrDefault(false)

    /**
     * Turns the app's bare language (`"ar"`, `"en"`) into the regional tag Deepgram should use.
     *
     * **This was missing in the first version and the owner caught it on the device:** the
     * Developer Mode line read `ar · عبر الشبكة` when his ruling (`ANSWERS.md` Part 6 §M2-9)
     * is `ar-SY`. `ChatViewModel` passes the app's UI language, which is bare `ar`, and the
     * regional expansion lived only inside [AndroidSpeechToText]'s attempt ladder. The design
     * note said "one tag, no ladder" and I read that as "no resolution either" — so Deepgram was
     * asked for generic Arabic and the dialect ruling silently did not apply.
     *
     * No ladder here, and that part was right: Android's engine lies about which languages it
     * supports, so it must be probed; Deepgram publishes its list, so one tag is chosen and
     * sent. What was wrong was choosing *no* tag at all.
     *
     * The device's own region wins when it names an Arabic country — a friend in Cairo gets
     * `ar-EG`, which is why the app does not ship the owner's dialect as a constant
     * (`ANSWERS.md` Part 3). The owner's device reports `SY`, so for him both paths agree.
     */
    private fun resolveTag(languageTag: String): String {
        if (languageTag.contains('-')) return languageTag
        val region = Locale.getDefault().country
            .takeIf { it.isNotBlank() && it in TRUSTED_REGIONS[languageTag].orEmpty() }
            ?: DEFAULT_REGION[languageTag]
            ?: return languageTag
        return "$languageTag-$region"
    }

    private fun urlFor(languageTag: String): String =
        "$ENDPOINT?model=$MODEL&language=$languageTag&encoding=linear16" +
            "&sample_rate=$SAMPLE_RATE&channels=1&interim_results=true&punctuate=true"

    private companion object {
        const val KEY_ID = "DEEPGRAM"
        const val ENDPOINT = "wss://api.deepgram.com/v1/listen"
        const val MODEL = "nova-3"
        const val CLOSE_STREAM = """{"type":"CloseStream"}"""

        /** Fallback region when the device's own does not name one Deepgram serves. */
        val DEFAULT_REGION = mapOf("ar" to "SY", "en" to "US")

        /**
         * Regions whose device setting is trusted to pick the variant. Deepgram documents 16
         * Arabic variants; these are the ones that make sense to inherit from a phone's locale.
         */
        val TRUSTED_REGIONS = mapOf(
            "ar" to setOf(
                "AE", "DZ", "EG", "IQ", "JO", "KW", "LB", "MA",
                "PS", "QA", "SA", "SD", "SY", "TD", "TN",
            ),
            "en" to setOf("AU", "CA", "GB", "IE", "IN", "NZ", "US", "ZA"),
        )

        /**
         * 16 kHz mono PCM. Deepgram's `linear16` takes raw samples, so there is no encoder and
         * no media library — the bytes `AudioRecord` produces are the bytes that go on the
         * wire. 16 kHz is the standard rate for speech models; higher costs bandwidth and buys
         * nothing a recogniser can use.
         */
        const val SAMPLE_RATE = 16_000
        const val CHANNEL = AudioFormat.CHANNEL_IN_MONO
        const val ENCODING = AudioFormat.ENCODING_PCM_16BIT

        /**
         * The minimum buffer is the point at which the device drops samples if we are late.
         * Recording at exactly that size means any hiccup — GC, a slow socket write — is
         * audible as a hole in the transcript, so take some headroom.
         */
        const val BUFFER_MULTIPLIER = 2

        /**
         * Quietest level the waveform reacts to, in dBFS. Below this the bars sit at rest.
         * −50 dB is roughly a silent room; ordinary speech at arm's length lands around
         * −30 to −12 dB, which spreads across most of the bar's travel.
         */
        const val FLOOR_DB = -50.0

        /**
         * Little-endian 16-bit PCM → RMS → **decibels** → 0f..1f for the waveform.
         *
         * **The decibel step is the fix, not decoration.** The first version returned linear
         * RMS, and on 2026-08-06 the owner reported the bars barely moving while Deepgram was
         * transcribing him perfectly — the audio was fine, the meter was not. Normal speech
         * sits near an RMS of 0.03–0.09 of full scale, so a linear mapping pins the bars to the
         * bottom tenth of their travel and the mark looks dead during a working dictation.
         * Hearing is logarithmic; a level meter that is not will always look broken.
         *
         * `docs/BRAND.md` §6 calls the five-bar mark "the signature interaction of the app; get
         * it right" — a meter that under-reports is the same failure as one that ignores the
         * microphone, just harder to notice.
         */
        fun rms(buffer: ByteArray, length: Int): Float {
            var sum = 0.0
            var i = 0
            while (i + 1 < length) {
                val sample = ((buffer[i + 1].toInt() shl 8) or (buffer[i].toInt() and 0xFF)).toShort()
                sum += sample.toDouble() * sample.toDouble()
                i += 2
            }
            val count = (length / 2).coerceAtLeast(1)
            val level = sqrt(sum / count) / Short.MAX_VALUE
            if (level <= 0.0) return 0f
            val db = 20.0 * log10(level)
            return ((db - FLOOR_DB) / -FLOOR_DB).toFloat().coerceIn(0f, 1f)
        }
    }
}
