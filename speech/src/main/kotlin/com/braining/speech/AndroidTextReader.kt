package com.braining.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.braining.core.domain.speech.ReaderFailure
import com.braining.core.domain.speech.ReaderStatus
import com.braining.core.domain.speech.TextReader
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The platform [TextToSpeech], behind the domain interface. **No new dependency** — it is in
 * Android itself.
 *
 * ## What the owner found, and what it turned out to be
 *
 * 2026-08-28: pressing «استمع» flipped the label to «أوقف الاستماع» and back "بلحظة سريعة", and
 * repeated attempts sometimes ran with no sound at all.
 *
 * Both are one fault. `setLanguage` returns `LANG_MISSING_DATA` on a device whose Arabic voice
 * has never been downloaded — and the previous version answered that by returning quietly, which
 * set *speaking* true on the press and false a millisecond later. **The app was reporting a
 * missing voice as an utterance that finished instantly.** `PROJECT_STATE.md` §10 entry 7: a
 * failure that looks identical to a success is not a failure anyone can act on.
 *
 * Three things follow, and each is a rule this class now keeps:
 *
 * **1. Nothing sets `speaking` optimistically.** Only `onStart` does. A button that flips because
 * a request was *sent* is describing the code's intention, not the device's behaviour — §10 entry
 * 14, the label that describes what the code used to do.
 *
 * **2. `isLanguageAvailable` is treated as a hint, not an answer.** Engines routinely report a
 * language as available and then refuse it at `setLanguage`. The cheap question decides whether
 * to *offer* the button; the expensive one decides what actually happened, and corrects
 * [ReaderStatus.voiceUsable] when it disagrees.
 *
 * **3. Every failure is named.** `ReaderFailure` reaches the screen, which turns it into an
 * Arabic sentence with somewhere to go. A missing voice is the one problem here the user can
 * actually fix.
 *
 * ## The word-boundary callback
 *
 * `onRangeStart` exists from API 26 — this project's minSdk exactly — and reports the character
 * span of each word as it is spoken. Not every engine implements it, so
 * [ReaderStatus.reportsWords] records whether it ever fired: a highlight that never moves has to
 * be explainable rather than mysterious.
 *
 * Like every other engine and store in this project, **it does not throw.** A device with no TTS
 * costs the user a button, not the screen.
 */
@Singleton
class AndroidTextReader @Inject constructor(
    @ApplicationContext private val context: Context,
) : TextReader {

    private val _status = MutableStateFlow(ReaderStatus())
    override val status: Flow<ReaderStatus> = _status.asStateFlow()

    private val _spokenRange = MutableStateFlow<IntRange?>(null)
    override val spokenRange: Flow<IntRange?> = _spokenRange.asStateFlow()

    private var engine: TextToSpeech? = null

    /** At most one. A list would speak a superseded answer after the current one. */
    private var pending: Pair<String, String>? = null

    private val listener = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {
            // **The only place `speaking` becomes true.** See rule 1 in the class KDoc.
            _status.update { it.copy(speaking = true, failure = null) }
        }

        override fun onDone(utteranceId: String?) {
            _status.update { it.copy(speaking = false) }
            _spokenRange.value = null
        }

        @Deprecated("Required by the abstract class; the typed overload below is the live one.")
        override fun onError(utteranceId: String?) {
            _status.update { it.copy(speaking = false, failure = ReaderFailure.SpeakFailed(-1)) }
            _spokenRange.value = null
        }

        override fun onError(utteranceId: String?, errorCode: Int) {
            _status.update {
                it.copy(speaking = false, failure = ReaderFailure.SpeakFailed(errorCode))
            }
            _spokenRange.value = null
        }

        override fun onStop(utteranceId: String?, interrupted: Boolean) {
            _status.update { it.copy(speaking = false) }
            _spokenRange.value = null
        }

        /**
         * The word being spoken, as a character range into the text handed to [speak].
         *
         * `frame` is ignored: it is an audio-frame index, useful for synchronising to a rendered
         * file and meaningless for a live utterance.
         */
        override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
            if (end > start) _spokenRange.value = start until end
            if (!_status.value.reportsWords) _status.update { it.copy(reportsWords = true) }
        }
    }

    private fun ensureEngine(): TextToSpeech? {
        engine?.let { return it }
        return runCatching {
            TextToSpeech(context) { code ->
                val ok = code == TextToSpeech.SUCCESS
                _status.update {
                    it.copy(
                        ready = ok,
                        engine = if (ok) runCatching { engine?.defaultEngine }.getOrNull().orEmpty() else "",
                        failure = if (ok) it.failure else ReaderFailure.NoEngine,
                    )
                }
                if (ok) {
                    pending?.let { (text, tag) ->
                        pending = null
                        reallySpeak(text, tag)
                    }
                } else {
                    // An engine that failed to initialise must not leave the UI in a state the
                    // user cannot leave.
                    pending = null
                    _status.update { it.copy(speaking = false) }
                }
            }.also { created ->
                engine = created
                created.setOnUtteranceProgressListener(listener)
            }
        }.getOrElse {
            _status.update { s -> s.copy(ready = false, failure = ReaderFailure.NoEngine) }
            null
        }
    }

    /**
     * **The text is spoken exactly as given — not trimmed, not normalised, not touched.**
     *
     * `onRangeStart` reports character offsets into whatever string was handed to the engine, and
     * the screen maps those offsets onto the string it is *displaying*. Trimming here would shift
     * every highlight by however much leading whitespace the answer happened to have, and the
     * mark would sit one word off for the whole recitation — visibly wrong and impossible to
     * attribute, because both strings look identical.
     *
     * The only guard is emptiness, which cannot move an index.
     */
    override fun speak(text: String, languageTag: String) {
        if (text.isBlank()) return
        val body = text

        val tts = ensureEngine()
        if (tts == null) {
            _status.update { it.copy(speaking = false, failure = ReaderFailure.NoEngine) }
            return
        }

        _spokenRange.value = null

        if (_status.value.ready != true) {
            // Queued, not dropped — the engine is still starting. **`speaking` stays false**:
            // nothing is being spoken yet, and saying otherwise is the flicker this class was
            // rewritten to remove.
            pending = body to languageTag
            return
        }
        reallySpeak(body, languageTag)
    }

    private fun reallySpeak(text: String, languageTag: String) {
        val tts = engine ?: return
        runCatching {
            // `setLanguage`, not `language = …`: it returns an `int`, so Kotlin synthesises a
            // read-only property from the getter and an assignment does not compile. The return
            // value is the whole point anyway — it is the only honest answer about whether this
            // device can say this language.
            val code = tts.setLanguage(Locale.forLanguageTag(languageTag))
            val usable = code >= TextToSpeech.LANG_AVAILABLE

            _status.update {
                it.copy(
                    languageCode = code,
                    voiceUsable = usable,
                    speaking = false,
                    failure = if (usable) null else ReaderFailure.NoVoiceForLanguage,
                )
            }

            // **Named, not silent.** The previous version returned here without a word, which is
            // what made a missing voice look like an utterance that finished instantly.
            if (!usable) return

            // QUEUE_FLUSH, not QUEUE_ADD: `TextReader.speak` promises replacement.
            val result = tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
            if (result == TextToSpeech.ERROR) {
                _status.update {
                    it.copy(speaking = false, failure = ReaderFailure.SpeakFailed(result))
                }
            }
        }.onFailure {
            _status.update { s ->
                s.copy(speaking = false, failure = ReaderFailure.SpeakFailed(-1))
            }
        }
    }

    override fun stop() {
        pending = null
        runCatching { engine?.stop() }
        _spokenRange.value = null
        _status.update { it.copy(speaking = false) }
    }

    private companion object {
        const val UTTERANCE_ID = "braining_answer"
    }
}
