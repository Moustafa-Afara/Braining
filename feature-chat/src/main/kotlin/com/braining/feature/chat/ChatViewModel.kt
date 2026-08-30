package com.braining.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.braining.ai.providers.toAiError
import com.braining.core.domain.model.AiChunk
import com.braining.core.domain.model.AiError
import com.braining.core.domain.model.AiRequest
import com.braining.core.domain.model.ChatMessage
import com.braining.core.domain.model.MessageRole
import com.braining.core.domain.model.ProviderId
import com.braining.core.domain.model.RequestDiagnostics
import com.braining.core.domain.model.SttError
import com.braining.core.domain.model.TokenUsage
import com.braining.core.domain.provider.AiProvider
import com.braining.core.domain.speech.SpeechToText
import com.braining.core.domain.speech.TranscriptionEvent
import com.braining.core.domain.store.AppPreferences
import com.braining.core.domain.store.EncryptedKeyStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatMessageUi(
    val role: MessageRole,
    val content: String,
    val isStreaming: Boolean = false,
    /** Populated only in Developer Mode, only once the reply has finished. */
    val diagnostics: RequestDiagnostics? = null,
)

/**
 * Voice capture state (M2).
 *
 * The transcript itself is **not** here — segments land in [ChatUiState.inputText], the same
 * field the keyboard writes to. That is the design note's central requirement: the text must be
 * editable before sending, because Arabic transcription will make mistakes and M3 will build on
 * whatever this leaves behind. One field means editing, sending and clearing already work.
 */
data class VoiceUiState(
    val isRecording: Boolean = false,
    /** 0f..1f, drives the waveform and nothing else. */
    val amplitude: Float = 0f,
    /** The engine's unstable guess. Displayed live, never appended — the next one replaces it. */
    val partial: String = "",
    /**
     * How many stabilised segments this run produced. **More than one means the engine
     * restarted mid-paragraph**, and the owner's ruling (`ANSWERS.md` Part 5 §M2-3) requires
     * this number to be reported for each of the three gate runs. Surfaced in Developer Mode
     * so the gate can be measured rather than guessed at.
     */
    val segments: Int = 0,
    /**
     * The language tag the engine actually accepted, and whether that rung was the on-device
     * one. Both come from [TranscriptionEvent.EngineConfig] and exist so a poor transcript can
     * be attributed: the wrong dialect and the weaker offline model produce the same complaint
     * and have different fixes. Shown in Developer Mode only — a user does not need them, and
     * on 2026-08-06 their absence cost two days of not knowing which model was even running.
     */
    val engineTag: String? = null,
    val engineOffline: Boolean = false,
    /**
     * How long the last run listened, in seconds. Shown in Developer Mode beside the segment
     * count.
     *
     * **Why the app measures this instead of asking.** A word count is uninterpretable without
     * it — 84 words is unremarkable over 45 seconds and catastrophic over 90, and on 2026-08-06
     * two consecutive dictation tests were reported without it and could be read but not
     * measured. Asking a human for a number the device already knows is a measurement design
     * error, not an oversight by the human.
     */
    val durationSeconds: Int = 0,
    val error: SttError? = null,
    /**
     * Whether the device has any recognition engine. False hides the microphone button
     * outright (`docs/M2_DESIGN_NOTE.md` §6) — a button that is always going to fail is worse
     * than no button. Expected to be false on some Xiaomi HyperOS builds.
     */
    val engineAvailable: Boolean = false,
)

data class ChatUiState(
    val messages: List<ChatMessageUi> = emptyList(),
    val inputText: String = "",
    val isGenerating: Boolean = false,
    val selectedProvider: ProviderId = ProviderId.GEMINI,
    /** Resolved from the user's Settings override, falling back to [ProviderId.defaultModel]. */
    val selectedModel: String = ProviderId.GEMINI.defaultModel,
    /**
     * The classified failure of the last request, resolved to a sentence by the screen
     * from string resources. Typed rather than pre-phrased: the provider and domain layers
     * must not build user-facing text.
     */
    val error: AiError? = null,
    val tokenUsage: String? = null,
    val developerMode: Boolean = false,
    /**
     * Diagnostics for a request that failed. A failed request removes its assistant bubble,
     * so without this the endpoint and body — exactly what you need when diagnosing a
     * failure — would be thrown away at the moment they became useful.
     */
    val lastDiagnostics: RequestDiagnostics? = null,
    val voice: VoiceUiState = VoiceUiState(),
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val keyStore: EncryptedKeyStore,
    private val appPreferences: AppPreferences,
    private val providers: Map<String, @JvmSuppressWildcards AiProvider>,
    private val speechToText: SpeechToText,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var currentJob: Job? = null
    private var voiceJob: Job? = null

    /**
     * The input field as it was **before** the microphone was tapped.
     *
     * Transcribed segments land in the input field as they arrive — that is what makes the words
     * appear while you speak, and it is deliberate. The consequence, reported by the owner on
     * 2026-08-18: by the time «إلغاء» is pressed, the words are already in the box, so cancelling
     * cancelled nothing. **The button said one thing and the code did another**, which is the
     * shape of §10 entry 14.
     *
     * Restoring this on cancel is what makes the label true. It also means «إلغاء» after dictating
     * on top of typed text puts the typed text back rather than leaving a mixture.
     */
    private var inputBeforeVoice: String = ""

    /**
     * Monotonic start of the current dictation. `nanoTime`, not `currentTimeMillis`, for the
     * same reason the provider timings use it: a clock adjustment mid-recording must not be
     * able to produce a negative duration.
     */
    private var voiceStartedAt: Long = 0L

    private fun elapsedVoiceSeconds(): Int =
        if (voiceStartedAt == 0L) 0
        else ((System.nanoTime() - voiceStartedAt) / 1_000_000_000L).toInt()

    /**
     * The user's per-provider model overrides from Settings. Held here so that switching
     * provider and sending a message both resolve against the same, current map.
     */
    private var modelOverrides: Map<String, String> = emptyMap()

    init {
        // Asked once, up front, so the microphone button is never offered on a device that
        // cannot honour it. Re-checked on each tap would be more correct but would also put a
        // service bind in the tap path; an engine disappearing mid-session is rare and the
        // NoEngine branch of transcribe() still catches it.
        viewModelScope.launch {
            val available = speechToText.isAvailable()
            _uiState.update { it.copy(voice = it.voice.copy(engineAvailable = available)) }
        }

        viewModelScope.launch {
            appPreferences.developerMode.collect { enabled ->
                _uiState.update { it.copy(developerMode = enabled) }
            }
        }

        // **Restore the provider the user last chose.** Until 2026-08-17 the selection lived
        // only in this object, so every cold start silently reset the app to Gemini — which is
        // regionally blocked at the owner's location, so the first message of every session
        // failed for a reason that had nothing to do with the message. `firstOrNull` by name
        // rather than `valueOf`: an enum entry can be removed (GitHub Models was, the same day)
        // and a saved name that no longer exists must be ignored, not throw.
        viewModelScope.launch {
            appPreferences.selectedProvider.collect { saved ->
                val restored = saved
                    ?.let { name -> ProviderId.entries.firstOrNull { it.name == name } }
                    ?: return@collect
                _uiState.update {
                    if (it.selectedProvider == restored) {
                        it
                    } else {
                        it.copy(
                            selectedProvider = restored,
                            selectedModel = resolveModel(restored),
                        )
                    }
                }
            }
        }

        viewModelScope.launch {
            appPreferences.selectedModels.collect { overrides ->
                modelOverrides = overrides
                // Re-resolve the current provider's model so an edit made in Settings takes
                // effect on the open chat rather than at the next app start.
                _uiState.update { it.copy(selectedModel = resolveModel(it.selectedProvider)) }
            }
        }
    }

    private fun resolveModel(providerId: ProviderId): String =
        modelOverrides[providerId.name]?.takeIf { it.isNotBlank() } ?: providerId.defaultModel

    fun updateInput(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    /**
     * Put message [index] back in the input box and **drop it and everything after it**.
     *
     * The owner's ruling, 2026-08-07 (option ب): edit behaves like ChatGPT's — the conversation
     * rewinds to that point. The alternative, copying the text into the box and leaving the old
     * exchange in place, keeps the history but leaves the model reading a question it has already
     * answered, which is the pollution `2026-08-07-F` documented.
     *
     * **This destroys content and there is no history yet** (M5 owns persistence). It is guarded
     * only by being unavailable mid-stream: a generation in flight is cancelled first, because
     * removing the message a running request was built from would leave a reply arriving for a
     * question that no longer exists.
     */
    fun editMessage(index: Int) {
        val messages = _uiState.value.messages
        if (index !in messages.indices) return
        val text = messages[index].content

        currentJob?.cancel()
        _uiState.update {
            it.copy(
                messages = it.messages.take(index),
                inputText = text,
                isGenerating = false,
                error = null,
            )
        }
    }

    fun selectProvider(providerId: ProviderId) {
        // The `when` this replaces hard-coded a second copy of every model name, including
        // "deepseek-chat", which DeepSeek shut down on 2026-07-24. Names now come from
        // ProviderId, overridden by whatever the user typed in Settings.
        _uiState.update {
            it.copy(
                selectedProvider = providerId,
                selectedModel = resolveModel(providerId),
            )
        }

        // And it is remembered. The state update above stays: the menu must answer the tap now,
        // not after a disk write — the same split as `setSelectedModel`.
        viewModelScope.launch { appPreferences.setSelectedProvider(providerId) }
    }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isEmpty() || _uiState.value.isGenerating) return

        val userMessage = ChatMessageUi(role = MessageRole.USER, content = text)
        _uiState.update {
            it.copy(
                messages = it.messages + userMessage,
                inputText = "",
                isGenerating = true,
                error = null,
                tokenUsage = null,
                lastDiagnostics = null,
            )
        }

        val provider = providers.values.find { it.id == _uiState.value.selectedProvider }
        if (provider == null) {
            _uiState.update {
                it.copy(
                    isGenerating = false,
                    error = AiError.Unknown(
                        provider = it.selectedProvider,
                        status = null,
                        detail = "Provider not found",
                    ),
                )
            }
            return
        }

        val aiMessages = _uiState.value.messages.map {
            ChatMessage(role = it.role, content = it.content)
        }

        val developerMode = _uiState.value.developerMode
        val request = AiRequest(
            model = _uiState.value.selectedModel,
            messages = aiMessages,
            stream = true,
            diagnostics = developerMode,
        )

        val assistantIndex = _uiState.value.messages.size
        _uiState.update {
            it.copy(messages = it.messages + ChatMessageUi(
                role = MessageRole.ASSISTANT,
                content = "",
                isStreaming = true,
            ))
        }

        currentJob = viewModelScope.launch {
            val responseBuilder = StringBuilder()

            // Timing is measured here rather than in the provider on purpose: this is the
            // point where a token becomes visible to the user, so these numbers describe
            // what the user actually experienced, not what the socket did.
            val startedAt = System.nanoTime()
            var firstChunkAt: Long? = null
            var chunkCount = 0
            var meta: AiChunk.Meta? = null

            fun diagnosticsSnapshot(usage: TokenUsage?): RequestDiagnostics? {
                val captured = meta ?: return null
                return RequestDiagnostics(
                    endpoint = captured.endpoint,
                    requestBody = captured.requestBody,
                    firstChunkMillis = firstChunkAt?.let { (it - startedAt) / NANOS_PER_MILLI },
                    totalMillis = (System.nanoTime() - startedAt) / NANOS_PER_MILLI,
                    chunkCount = chunkCount,
                    usage = usage,
                )
            }

            provider.complete(request)
                .catch { throwable ->
                    // Last line of defence: nothing reaching this point may crash the app.
                    // Socket and timeout exceptions arrive HERE — BaseHttpProvider lets them
                    // propagate rather than phrasing them itself — so this is where they are
                    // classified into typed AiErrors (NoNetwork, Timeout, Unknown).
                    _uiState.update { state ->
                        val msgs = state.messages.toMutableList()
                        if (assistantIndex < msgs.size && msgs[assistantIndex].isStreaming) {
                            msgs.removeAt(assistantIndex)
                        }
                        state.copy(
                            messages = msgs,
                            isGenerating = false,
                            error = throwable.toAiError(state.selectedProvider),
                            lastDiagnostics = diagnosticsSnapshot(null),
                        )
                    }
                }
                .collect { chunk ->
                    when (chunk) {
                        is AiChunk.Meta -> meta = chunk

                        is AiChunk.Token -> {
                            chunkCount++
                            if (firstChunkAt == null) firstChunkAt = System.nanoTime()
                            responseBuilder.append(chunk.text)
                            _uiState.update { state ->
                                val msgs = state.messages.toMutableList()
                                if (assistantIndex < msgs.size) {
                                    msgs[assistantIndex] = msgs[assistantIndex].copy(
                                        content = responseBuilder.toString(),
                                    )
                                }
                                state.copy(messages = msgs)
                            }
                        }

                        is AiChunk.Done -> {
                            val diagnostics = diagnosticsSnapshot(chunk.usage)
                            _uiState.update { state ->
                                val msgs = state.messages.toMutableList()
                                if (assistantIndex < msgs.size) {
                                    msgs[assistantIndex] = msgs[assistantIndex].copy(
                                        isStreaming = false,
                                        diagnostics = diagnostics,
                                    )
                                }
                                state.copy(
                                    messages = msgs,
                                    isGenerating = false,
                                    tokenUsage = chunk.usage?.let { u ->
                                        "Tokens: ${u.promptTokens} in / ${u.completionTokens} out / ${u.totalTokens} total"
                                    },
                                )
                            }
                        }

                        is AiChunk.Error -> {
                            val diagnostics = diagnosticsSnapshot(null)
                            _uiState.update { state ->
                                val msgs = state.messages.toMutableList()
                                if (assistantIndex < msgs.size) {
                                    msgs.removeAt(assistantIndex)
                                }
                                state.copy(
                                    messages = msgs,
                                    isGenerating = false,
                                    error = chunk.error,
                                    lastDiagnostics = diagnostics,
                                )
                            }
                        }
                    }
                }
        }
    }

    fun cancelGeneration() {
        currentJob?.cancel()
        _uiState.update {
            it.copy(
                isGenerating = false,
                messages = it.messages.map { msg ->
                    if (msg.isStreaming) msg.copy(isStreaming = false) else msg
                },
            )
        }
    }

    // ------------------------------------------------------------------------------------
    // M2 — voice capture
    // ------------------------------------------------------------------------------------

    /**
     * Begin transcribing. The caller is responsible for holding `RECORD_AUDIO` first — the
     * permission dance needs an Activity, so it belongs in the screen, not here.
     *
     * @param languageTag follows the app's own language toggle, so a user reading an English
     *   UI dictates in English without a second setting to find.
     */
    fun startVoice(languageTag: String) {
        if (_uiState.value.voice.isRecording) return
        inputBeforeVoice = _uiState.value.inputText

        _uiState.update {
            // engineTag is cleared with the rest: a tag left over from the previous run would
            // be read as describing this one, and a run that fails before the engine is ready
            // never overwrites it.
            it.copy(
                voice = it.voice.copy(
                    isRecording = true,
                    partial = "",
                    segments = 0,
                    engineTag = null,
                    durationSeconds = 0,
                    error = null,
                ),
            )
        }
        voiceStartedAt = System.nanoTime()

        voiceJob = viewModelScope.launch {
            speechToText.transcribe(languageTag)
                .catch { throwable ->
                    // Nothing from an engine may crash the app. An unexpected throwable is not
                    // a classified case, so it becomes EngineFailure with no platform code.
                    if (throwable is CancellationException) throw throwable
                    _uiState.update {
                        it.copy(
                            voice = it.voice.copy(
                                isRecording = false,
                                amplitude = 0f,
                                partial = "",
                                error = SttError.EngineFailure(UNCLASSIFIED_ENGINE_ERROR),
                            ),
                        )
                    }
                }
                .collect { event ->
                    when (event) {
                        is TranscriptionEvent.Amplitude ->
                            _uiState.update { it.copy(voice = it.voice.copy(amplitude = event.level)) }

                        is TranscriptionEvent.Partial ->
                            _uiState.update { it.copy(voice = it.voice.copy(partial = event.text)) }

                        is TranscriptionEvent.Segment -> _uiState.update { state ->
                            // Straight into the input field, which is what makes the transcript
                            // editable before sending. Appending rather than replacing is what
                            // stitches a paragraph back together after the engine restarts.
                            state.copy(
                                inputText = appendSegment(state.inputText, event.text),
                                voice = state.voice.copy(
                                    partial = "",
                                    segments = state.voice.segments + 1,
                                ),
                            )
                        }

                        is TranscriptionEvent.EngineConfig -> _uiState.update { state ->
                            state.copy(
                                voice = state.voice.copy(
                                    engineTag = event.languageTag,
                                    engineOffline = event.offline,
                                ),
                            )
                        }

                        is TranscriptionEvent.Failed -> _uiState.update {
                            it.copy(
                                voice = it.voice.copy(
                                    isRecording = false,
                                    amplitude = 0f,
                                    partial = "",
                                    durationSeconds = elapsedVoiceSeconds(),
                                    error = event.error,
                                ),
                            )
                        }

                        TranscriptionEvent.Completed -> _uiState.update {
                            it.copy(
                                voice = it.voice.copy(
                                    isRecording = false,
                                    amplitude = 0f,
                                    partial = "",
                                    durationSeconds = elapsedVoiceSeconds(),
                                ),
                            )
                        }
                    }
                }
        }
    }

    /** The user pressed "done". Lets the engine deliver its last sentence — see [SpeechToText.stop]. */
    fun stopVoice() {
        speechToText.stop()
    }

    /**
     * The user dismissed the sheet without finishing. Unlike [stopVoice] this abandons the run:
     * cancelling the collector tears the recogniser down through the flow's `awaitClose`.
     */
    /** «إلغاء» — abandon the run and give the field back the way it was found. See the note on
     * [inputBeforeVoice]: the segments are already in the box by the time this is pressed. */
    fun cancelVoice() {
        voiceJob?.cancel()
        voiceJob = null
        speechToText.stop()
        _uiState.update {
            it.copy(
                inputText = inputBeforeVoice,
                voice = it.voice.copy(isRecording = false, amplitude = 0f, partial = ""),
            )
        }
    }

    fun dismissVoiceError() {
        _uiState.update { it.copy(voice = it.voice.copy(error = null)) }
    }

    /** The system permission dialog came back denied. Same typed error as the engine's own. */
    fun onMicrophonePermissionDenied() {
        _uiState.update {
            it.copy(voice = it.voice.copy(isRecording = false, error = SttError.PermissionDenied))
        }
    }

    fun clearChat() {
        currentJob?.cancel()
        voiceJob?.cancel()
        _uiState.value = ChatUiState(
            selectedProvider = _uiState.value.selectedProvider,
            selectedModel = _uiState.value.selectedModel,
            developerMode = _uiState.value.developerMode,
            // Engine availability is a property of the device, not of the conversation.
            // Rediscovering it would blank the microphone button for a moment on every clear.
            voice = VoiceUiState(engineAvailable = _uiState.value.voice.engineAvailable),
        )
    }

    private companion object {
        const val NANOS_PER_MILLI = 1_000_000L

        /** No platform code exists for a throwable the engine did not classify. */
        const val UNCLASSIFIED_ENGINE_ERROR = -1

        /**
         * Joins a stabilised segment onto whatever is already in the input field. The space is
         * the visible cost of a restart: the engine gives back two sentences with no idea how
         * long the gap between them was, so this is a stitch, not a recovery.
         */
        fun appendSegment(existing: String, segment: String): String =
            if (existing.isBlank()) segment else "${existing.trimEnd()} $segment"
    }
}
