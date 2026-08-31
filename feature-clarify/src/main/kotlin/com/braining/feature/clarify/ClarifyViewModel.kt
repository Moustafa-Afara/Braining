package com.braining.feature.clarify

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.braining.core.domain.clarify.ClarifyEngine
import com.braining.core.domain.clarify.ClarifyEvent
import com.braining.core.domain.clarify.ClarifySession
import com.braining.core.domain.clarify.ClarifyState
import com.braining.core.domain.clarify.ClarifyTurn
import com.braining.core.domain.clarify.ForgeEvent
import com.braining.core.domain.clarify.FrameworkOption
import com.braining.core.domain.clarify.PromptForge
import com.braining.core.domain.history.SessionRecord
import com.braining.core.domain.history.SessionRepository
import com.braining.core.domain.history.SessionSummary
import com.braining.core.domain.model.AiChunk
import com.braining.core.domain.model.AiError
import com.braining.core.domain.model.AiRequest
import com.braining.core.domain.model.ChatMessage
import com.braining.core.domain.model.MessageRole
import com.braining.core.domain.model.ProviderId
import com.braining.core.domain.model.RequestDiagnostics
import com.braining.core.domain.model.SttError
import com.braining.core.domain.provider.AiProvider
import com.braining.core.domain.routing.ModelRouter
import com.braining.core.domain.routing.RouteReason
import com.braining.core.domain.routing.RoutingDecision
import com.braining.core.domain.speech.SpeechToText
import com.braining.core.domain.speech.ReaderStatus
import com.braining.core.domain.speech.TextReader
import com.braining.core.domain.speech.TranscriptionEvent
import com.braining.core.domain.store.AppPreferences
import com.braining.core.domain.store.EncryptedKeyStore
import com.braining.ai.providers.toAiError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ClarifyUiState(
    /** The transcript the interrogation is about, exactly as the user left it. */
    val idea: String = "",
    val turns: List<ClarifyTurn> = emptyList(),
    /** The turn currently arriving, token by token. Empty when nothing is streaming. */
    val streaming: String = "",
    val state: ClarifyState = ClarifyState.ANALYZING,
    val inputText: String = "",
    val error: AiError? = null,
    val developerMode: Boolean = false,
    /**
     * How many times the engine has spoken. **The number the gate is read against**
     * (`docs/M3_DESIGN_NOTE.md` §5.3), which is why it is on screen behind Developer Mode
     * rather than left to be counted by hand — the same reason M2 published its segment count.
     */
    val engineTurns: Int = 0,
    val provider: ProviderId = ProviderId.DEEPSEEK,
    val model: String = "",
    /**
     * What the last turn actually sent and how it behaved. Developer Mode only.
     *
     * Survives a failed turn deliberately, the same way `ChatUiState.lastDiagnostics` does — a
     * request that failed is precisely when the endpoint and the outgoing body are worth reading.
     */
    val lastDiagnostics: RequestDiagnostics? = null,

    // ── FORGE ────────────────────────────────────────────────────────────────────────────
    /** True while the English prompt is being written. */
    val forging: Boolean = false,
    /** Known before the prompt finishes, so the user sees *why* before they see *what*. */
    val frameworkId: String? = null,
    val frameworkRationale: String = "",
    /**
     * The English prompt, **editable**.
     *
     * `ANSWERS.md` Part 7 §M3-5 and `docs/PROMPT_FRAMEWORKS.md` §3.7 both require it. It is held
     * as plain text rather than as [ForgedPrompt] because the moment the user edits a character
     * it is no longer what the model produced, and a type that claims otherwise would be lying.
     */
    val forgedPrompt: String = "",
    /** The user's framework choice. Null means the model picks. */
    val frameworkOverride: String? = null,

    /**
     * What «امسح» removed, held only so «تراجع» can put it back.
     *
     * **A one-tap delete of two thousand characters needs an undo, not a confirmation dialog.** A
     * dialog taxes every use to protect the rare mistake; an undo costs nothing until the mistake
     * happens. It is cleared the moment the user types something new, because at that point
     * restoring the old prompt would destroy what they just wrote — an undo that becomes a second
     * delete is worse than no undo.
     */
    val clearedPrompt: String = "",

    // ── VOICE ────────────────────────────────────────────────────────────────────────────
    val recording: Boolean = false,
    val amplitude: Float = 0f,
    /** The engine's unstable guess. Replaced, never appended — see `TranscriptionEvent`. */
    val partial: String = "",
    val voiceError: SttError? = null,
    /** False hides the microphone rather than showing a button that always fails. */
    val voiceAvailable: Boolean = false,

    // ── EXECUTE ──────────────────────────────────────────────────────────────────────────
    val executing: Boolean = false,
    /**
     * Seconds from opening the interrogation to «نضجت الفكرة», and to the answer finishing.
     *
     * **The gate's second number** (`docs/M3_GATE.md`). Measured by the device because
     * `2026-08-06-J` is an entry about exactly this: asking a human for a number the device could
     * hold is a measurement design error, and M2 got two consecutive dictation reports with no
     * duration attached before that lesson landed. Wall-clock on purpose — the question is
     * whether the interrogation was worth the user's time, and their thinking and typing are
     * part of what it cost.
     */
    val clarifySeconds: Int = 0,
    val totalSeconds: Int = 0,
    /**
     * The provider's answer to the forged prompt — **English**, by `ANSWERS.md` Part 7 §M3-2,
     * until M4 adds translation. The screen says so rather than leaving the user to wonder.
     */
    val result: String = "",

    // ── M4 · ROUTE, TRANSLATE, FEEDBACK ─────────────────────────────────────────────────
    /**
     * **Who answered, and why.** `BRAINING.md` §5 requires every routing decision to be visible;
     * this is that decision, held so the screen can name the provider that actually replied rather
     * than the one that was selected. The two differ exactly when a fallback happened, which is
     * the moment it matters most.
     */
    val route: RoutingDecision.Direct? = null,

    /** The answer as it arrived. Kept so a translation never destroys the original. */
    val originalResult: String = "",
    /** The Arabic translation, once asked for. Empty until then. */
    val translation: String = "",
    val translating: Boolean = false,
    /** Which of the two [result] is currently showing. */
    val showingTranslation: Boolean = false,

    /**
     * Follow-up rounds sent on this answer. Developer Mode.
     *
     * Published for the same reason M2 published its segment count before anyone knew how to read
     * it (§10 entry 9): the cost of a feedback loop is invisible otherwise, and "how many rounds
     * does a real answer take" is a question M5 will want answered from data.
     */
    val feedbackRounds: Int = 0,

    /**
     * Providers that could take over from the one that just failed, in the router's order.
     *
     * **Empty except immediately after a recoverable failure.** The owner reversed his own ruling
     * of 17 August on 2026-08-28: the app used to hop to the next provider by itself and announce
     * it; now it stops and asks. His reasoning is the one this project keeps arriving at — an
     * answer costs money and he wants to know *whose* model produced it before it is bought, not
     * after.
     *
     * Empty also covers the case where a fallback would be wrong: a missing key, a rejected key
     * or a dead network are facts about the user's setup, and `DefaultModelRouter.isRecoverable`
     * refuses to route around them. The screen then shows the error and no chips, which is
     * correct — there is nothing another provider could do.
     */
    val fallbackOptions: List<ProviderId> = emptyList(),

    // ── M5 · HISTORY AND READBACK ───────────────────────────────────────────────────────
    /**
     * True while the screen is restoring a saved session from history.
     *
     * Distinct from [forging] and [executing]: this one is waiting on a **local database**, not
     * on a provider, and a screen that said «أكتب البرومبت…» while reading a file would be
     * describing the wrong thing (`PROJECT_STATE.md` §10 entry 6).
     */
    val restoring: Boolean = false,
    /**
     * True when this run came out of history rather than out of a dictation.
     *
     * The screen uses it to say so. A user looking at a fully-formed prompt they did not just
     * write needs to know where it came from — the alternative is a screen that appears to have
     * invented an interrogation.
     */
    val fromHistory: Boolean = false,
    /**
     * The saved row could not be read — it was deleted between opening the list and tapping it.
     *
     * **A state of its own rather than a silent fallback.** The route carries a placeholder idea,
     * so quietly starting an interrogation would open one on `_` — a screen that appears to work
     * and is asking about nothing. §10 entry 13: the failure that looks like a success is the
     * dangerous one.
     */
    val restoreFailed: Boolean = false,
    /** The switch in Settings. The button exists only when the user turned readback on. */
    val ttsEnabled: Boolean = false,

    /**
     * What the speech engine knows about itself — **including why it could not speak.**
     *
     * The first build carried a bare `speaking: Boolean` here, and that is what hid the owner's
     * bug on 2026-08-28: a boolean can say *speaking* or *not speaking*, and has no way to say
     * *this phone has no Arabic voice*. The missing voice was therefore reported as an utterance
     * that started and finished in the same instant, which is exactly what he saw.
     */
    val reader: ReaderStatus = ReaderStatus(),

    /** The word being spoken, as a range into [result]. Null between words. */
    val spokenRange: IntRange? = null,

    /**
     * Whether the page still follows the spoken word.
     *
     * **False the moment the user touches the page, and it stays false until they ask for it
     * back.** The owner was explicit: the app must not "يستعصي" at the reading position. An
     * auto-scroll that reasserts itself makes re-reading a line impossible without stopping the
     * audio, so the rule is one-directional by design.
     */
    val followSpoken: Boolean = true,
)

/**
 * Which request `retry()` should repeat.
 *
 * Three different things on this screen can fail and each needs a different recovery. Deriving
 * that from the state flags instead would mean a chain of conditions that is one refactor away
 * from re-running the wrong one — and the wrong one here is destructive: re-opening the
 * interrogation throws the whole session away.
 */
/**
 * Which phase failed, and therefore what «أعد المحاولة» and the provider chips must re-run.
 *
 * **[REPLY] is separate from [CLARIFY] and the difference is destructive.** `start()` calls
 * `ClarifyEngine.open`, which **discards the session** and re-opens the interrogation from the
 * original idea. That is right for a first turn that failed and catastrophic for the twelfth:
 * retrying a failed answer would have thrown away every question and answer before it. [REPLY]
 * routes to `resume()` instead, which re-asks the same turn on the session as it stands.
 */
private enum class LastAction { CLARIFY, REPLY, FORGE, EXECUTE }

/**
 * Owns one interrogation.
 *
 * **The provider and the idea arrive as navigation arguments, not from a shared object.**
 * `ANSWERS.md` Part 7 §M3-3 rules that Clarify runs on the provider selected in the chat — and
 * that selection lives in `ChatViewModel`'s in-memory state, which a second ViewModel cannot
 * see. Passing it through the route makes the ruling literal: Clarify runs on whatever was
 * selected at the moment the button was pressed, and there is no second, hidden provider choice
 * anywhere.
 *
 * **A side effect worth knowing rather than discovering:** navigation saves its arguments, so
 * the *idea* survives process death while the *interrogation* does not. That is not a partial
 * implementation of §M3-4 — it is the ruling working out exactly as written. Nothing this class
 * holds is ever stored.
 */
@HiltViewModel
class ClarifyViewModel @Inject constructor(
    private val clarifyEngine: ClarifyEngine,
    private val promptForge: PromptForge,
    private val providers: Map<String, @JvmSuppressWildcards AiProvider>,
    private val router: ModelRouter,
    /**
     * Read for one purpose: **which providers the user actually has a key for.** A fallback to a
     * keyless provider trades one failure for a worse one — the second error names a missing key
     * and reads as if the user had misconfigured something.
     */
    private val keyStore: EncryptedKeyStore,
    private val appPreferences: AppPreferences,
    private val speechToText: SpeechToText,
    /**
     * M5. **This ViewModel is the only thing that writes history**, because it is the only object
     * that knows an answer completed. `ClarifyEngineImpl` reads summaries and never writes;
     * splitting it that way keeps "what gets saved" in one readable place instead of spread
     * across the engine, the forge and here.
     */
    private val sessions: SessionRepository,
    /** M5 readback. Off by default; the button appears only when the user turned it on. */
    private val textReader: TextReader,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    /**
     * The swap menu's options. Read straight from the library file, so adding a framework there
     * adds it here (`docs/PROMPT_FRAMEWORKS.md` §5).
     */
    val frameworks: List<FrameworkOption> get() = promptForge.frameworks

    private val _uiState = MutableStateFlow(ClarifyUiState())
    val uiState: StateFlow<ClarifyUiState> = _uiState.asStateFlow()

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
    private var lastAction: LastAction = LastAction.CLARIFY

    /**
     * The execution conversation: the forged prompt, the answer, and every round of feedback.
     *
     * **This is what "full session context" means in `BRAINING.md` §2.7.** The feedback loop is a
     * continuation, not a new request (owner's ruling, 2026-08-17): the model sees the prompt it
     * answered and the answer it gave, so "shorten it" refers to something. Rebuilt from scratch
     * on every fresh execute, appended to on every round.
     */
    private val exchange = mutableListOf<ChatMessage>()

    /** Every provider this execution has already spent. Guards the fallback against a loop. */
    private val triedThisExecution = mutableSetOf<ProviderId>()

    /**
     * The same guard for the interrogation and the forge.
     *
     * Kept apart from [triedThisExecution] deliberately. They have different lifetimes — one
     * turn versus one whole answer — and merging them would mean a provider spent on question
     * three could not be offered when the execution failed twenty minutes later. Each set is
     * cleared by the action that starts its own phase, and by nothing else.
     */
    private val triedThisTurn = mutableSetOf<ProviderId>()

    /**
     * Reset the turn's provider budget.
     *
     * **Called on success, and by the user starting a new turn — never on entry to `start()` or
     * `forge()`.** Those two are each reached three ways: fresh, from «أعد المحاولة», and from a
     * provider chip; only the first is a new turn. Clearing on entry would forget the provider
     * that had just failed and offer it straight back, which is the ping-pong `alreadyTried`
     * exists to prevent.
     */
    private fun resetTurnBudget() = triedThisTurn.clear()

    /** The provider whose failure produced [ClarifyUiState.fallbackOptions]. */
    private var lastFailedProvider: ProviderId? = null

    /** The Settings model overrides, read once in `init`. A fallback needs the map, not one name. */
    private var modelOverrides: Map<String, String> = emptyMap()

    /** `nanoTime`, not `currentTimeMillis`: a clock adjustment must not produce a negative age. */
    private val openedAt: Long = System.nanoTime()

    private fun elapsedSeconds(): Int = ((System.nanoTime() - openedAt) / 1_000_000_000L).toInt()

    /**
     * The history row this run occupies, once it has one.
     *
     * **Held so that refining an answer updates one row instead of appending a near-duplicate.**
     * A run whose answer is adjusted three times by feedback must end as a single session; three
     * rows would each hold a slice of the same conversation and would be indistinguishable in the
     * list.
     */
    private var recordId: Long = SessionRecord.NEW

    /**
     * The whole saved session, when this screen was opened from history.
     *
     * **Not just its turns.** `ClarifyEngineImpl` was never opened on a restored run, so its own
     * `session` is still empty — and `forge()` passes that session to FORGE. Holding only the
     * turns fixed what history stored and left «أعد الصياغة» and every framework chip silently
     * forging from an empty idea. Everything that needs "the session this screen is about" reads
     * this first and the engine second.
     */
    private var restoredSession: ClarifySession? = null

    /** The session FORGE and history should see: the restored one, or the live one. */
    private fun currentSession(): ClarifySession =
        restoredSession ?: clarifyEngine.session

    private val idea: String = savedStateHandle.get<String>(ARG_IDEA).orEmpty()
    private val provider: ProviderId = savedStateHandle.get<String>(ARG_PROVIDER)
        ?.let { name -> ProviderId.entries.firstOrNull { it.name == name } }
        ?: ProviderId.DEEPSEEK

    /** Non-zero when this screen was opened from the history list. */
    private val restoreId: Long = savedStateHandle.get<Long>(ARG_SESSION_ID) ?: SessionRecord.NEW

    init {
        // Asked once. A microphone button offered on a device with no engine is a button that
        // always fails, which `docs/M2_DESIGN_NOTE.md` §6 rules worse than no button at all.
        viewModelScope.launch {
            val available = speechToText.isAvailable()
            _uiState.update { it.copy(voiceAvailable = available) }
        }

        viewModelScope.launch {
            appPreferences.developerMode.collect { enabled ->
                _uiState.update { it.copy(developerMode = enabled) }
            }
        }

        // **The switch alone decides whether the button exists — not a pre-flight check.**
        //
        // The first build ANDed this with `isAvailable()`, which asks the engine whether it has
        // an Arabic voice. That question has an unreliable answer: engines routinely report a
        // language as available and then refuse it at `setLanguage`. The owner's phone said yes
        // and then produced silence.
        //
        // `docs/M2_DESIGN_NOTE.md` §6 still holds — do not offer a button that always fails — but
        // the way to keep it here is to **find out and then say so**, not to trust a check that
        // lies. The first press learns the truth, and `ReaderStatus.voiceUsable` carries it to a
        // sentence with somewhere to go.
        viewModelScope.launch {
            appPreferences.ttsEnabled.collect { enabled ->
                _uiState.update { it.copy(ttsEnabled = enabled) }
            }
        }

        viewModelScope.launch {
            textReader.status.collect { s -> _uiState.update { it.copy(reader = s) } }
        }

        viewModelScope.launch {
            textReader.spokenRange.collect { r -> _uiState.update { it.copy(spokenRange = r) } }
        }

        viewModelScope.launch {
            // Read the override BEFORE opening, not after.
            //
            // `ChatViewModel` collects this flow and re-resolves when it changes, which is right
            // for a screen that lives for a long time. An interrogation fires one request
            // immediately, so an override arriving a moment later would be a moment too late —
            // the first turn would have gone out on the default model while Settings said
            // otherwise. `ProviderId.defaultModel` remains the only place a model name is
            // written (`2026-08-03-A`).
            val overrides = appPreferences.selectedModels.first()
            modelOverrides = overrides
            val model = overrides[provider.name]?.takeIf { it.isNotBlank() } ?: provider.defaultModel

            // Developer Mode is read with `first()` too, for the same race. The collector above
            // keeps the flag live for the rest of the session, but it may not have delivered its
            // first value by the time the opening request goes out — and the one request whose
            // captured body matters most is the first one, because it carries the system prompt.
            val developerMode = appPreferences.developerMode.first()

            _uiState.update {
                it.copy(
                    idea = idea,
                    provider = provider,
                    model = model,
                    developerMode = developerMode,
                )
            }

            // Two entry points, and they must not both run.
            //
            // A fresh dictation opens an interrogation. A run restored from history already has
            // one — re-opening it would spend a request to ask questions about an idea the user
            // settled days ago, and would overwrite the prompt they came back for.
            if (restoreId != SessionRecord.NEW) restore(restoreId) else start()
        }
    }

    fun updateInput(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    /** Send whatever is in the input field. */
    fun reply() = replyWith(_uiState.value.inputText)

    /**
     * Send [answer] as the user's reply — used by the option buttons.
     *
     * The offered options and a typed answer go down **exactly the same path**. Tapping a button
     * is a shortcut for typing that sentence, not a different kind of input, so the transcript,
     * the session and everything FORGE later reads are identical either way. A parallel channel
     * for "structured" answers would have been the beginning of two histories to keep in sync.
     */
    fun replyWith(answer: String) {
        val text = answer.trim()
        if (text.isEmpty() || isBusy()) return
        val brain = brain() ?: return

        // The engine appends the UserReply to its session inside `reply()`, before it returns the
        // flow — so read the list back **now**, not when the next turn completes.
        //
        // **This is the bug the owner caught by eye.** `turns` was refreshed only on
        // `TurnCompleted`, so his answer stayed invisible until the model had finished writing
        // its *next* question, then appeared a fraction of a second after it. The data was never
        // wrong; the UI was reading it at the wrong moment.
        // REPLY, not CLARIFY: a retry of this turn must resume the session, never re-open it.
        lastAction = LastAction.REPLY
        resetTurnBudget()
        triedThisTurn += _uiState.value.provider
        val flow = clarifyEngine.reply(text, brain.first, brain.second, _uiState.value.developerMode)
        _uiState.update {
            it.copy(inputText = "", error = null, fallbackOptions = emptyList(), turns = clarifyEngine.session.turns)
        }
        collect(flow)
    }

    /**
     * Retry whatever just failed. Offered after a failure so a dead network is not a dead end.
     *
     * **Which one it retries depends on where the user is**, and getting that wrong would be
     * destructive rather than merely useless: after the idea is declared mature the failure is
     * the forge's, and blindly calling [start] would throw away the entire interrogation and
     * begin a fresh one on the original transcript.
     */
    fun retry() {
        if (isBusy()) return
        // The offer goes with the error it belonged to. Leaving chips on screen after the user
        // chose to retry the same provider would be a control describing a decision already made.
        _uiState.update { it.copy(error = null, fallbackOptions = emptyList()) }
        when (lastAction) {
            LastAction.CLARIFY -> start()
            // **Resume, not re-open.** See the note on `LastAction.REPLY`.
            LastAction.REPLY -> resumeClarify()
            LastAction.FORGE -> forge()
            // **Resume, not restart.** After a feedback round the exchange holds the prompt, the
            // answer and the note; `execute()` would clear all three and silently re-answer the
            // original question.
            LastAction.EXECUTE -> retryExecution()
        }
    }

    /**
     * The user declares the idea mature.
     *
     * The **only** path to [ClarifyState.READY] in the whole app. `BRAINING.md` §2.3 and
     * `ANSWERS.md` Part 7 §M3-1 both reserve this for the user, so no event, no timeout and no
     * model output may reach it — and there is deliberately nothing here that could fail.
     */
    fun declareReady() {
        currentJob?.cancel()
        clarifyEngine.declareReady()
        _uiState.update {
            it.copy(
                state = ClarifyState.READY,
                streaming = "",
                // Stamped here and never again. Re-forging or re-running does not change how long
                // the interrogation took, and a number that drifts after the event it names is
                // worse than no number.
                clarifySeconds = elapsedSeconds(),
            )
        }
        forge()
    }

    /** The user edited the prompt. From here on it is theirs, not the model's. */
    fun updateForgedPrompt(text: String) {
        // Typing invalidates the undo: see the note on `clearedPrompt`.
        _uiState.update {
            it.copy(
                forgedPrompt = text,
                clearedPrompt = if (text.isEmpty()) it.clearedPrompt else "",
            )
        }
    }

    /**
     * Empty the prompt box in one tap.
     *
     * The owner, 2026-08-18: editing and clearing a two-thousand-character monospace field on a
     * phone is "غير سلس". Selecting all of it by hand is the part that does not work — so the app
     * does it.
     */
    fun clearPrompt() {
        val current = _uiState.value.forgedPrompt
        if (current.isEmpty()) return
        _uiState.update { it.copy(forgedPrompt = "", clearedPrompt = current) }
    }

    /** Put back exactly what «امسح» took. */
    fun undoClearPrompt() {
        val previous = _uiState.value.clearedPrompt
        if (previous.isEmpty()) return
        _uiState.update { it.copy(forgedPrompt = previous, clearedPrompt = "") }
    }

    /**
     * Swap the framework and rewrite.
     *
     * Passing null re-runs with the model's own choice. `docs/PROMPT_FRAMEWORKS.md` §3.7:
     * "let the user edit or swap it, then regenerate".
     */
    fun swapFramework(id: String?) {
        _uiState.update { it.copy(frameworkOverride = id) }
        forge()
    }

    fun regenerate() = forge()

    /**
     * Write the English prompt from the whole interrogation.
     *
     * **Discards any edit the user had made, deliberately**, because every path here is an
     * explicit request for a new prompt — declaring the idea ready, swapping the framework, or
     * pressing regenerate. Merging an old edit into a newly written prompt would produce a
     * document neither the user nor the model wrote.
     */
    /**
     * Send the forged prompt and stream the answer back.
     *
     * **Run here rather than handed to the chat screen, and that is a decision worth the line.**
     * The chat would have given bubbles, history and diagnostics for free — but it would also
     * have prepended whatever conversation was already open, and a forged prompt is
     * self-contained by construction (`# ROLE`, `# CONTEXT`, `# INPUT`). Prior turns can only
     * contradict it. The alternative was clearing the user's chat first, which is destructive for
     * a reason they never asked about; `2026-08-07-F` is the entry about how sticky an unrelated
     * history turned out to be. Reversible on request.
     *
     * **No system prompt, deliberately.** The forged prompt *is* the instruction. Adding a second
     * one would put two voices in one request and quietly undo whatever the framework decided.
     */
    fun execute() {
        val prompt = _uiState.value.forgedPrompt.trim()
        if (prompt.isEmpty() || isBusy()) return

        lastAction = LastAction.EXECUTE
        currentJob?.cancel()
        // **Silence the reader first.** It is a system service and outlives this request: without
        // this, pressing «استمع» and then re-running, translating or sending feedback leaves the
        // phone reciting the *previous* answer while the new one streams in underneath, and the
        // stale word positions mark arbitrary words of text that was never spoken.
        textReader.stop()

        exchange.clear()
        exchange += ChatMessage(role = MessageRole.USER, content = prompt)
        triedThisExecution.clear()

        _uiState.update {
            it.copy(
                executing = true,
                error = null,
                result = "",
                originalResult = "",
                translation = "",
                showingTranslation = false,
                feedbackRounds = 0,
                route = null,
                fallbackOptions = emptyList(),
            )
        }

        currentJob = viewModelScope.launch { runExecution(_uiState.value.provider) }
    }

    /**
     * Run the answer again on a provider the user picked from the row under it.
     *
     * `BRAINING.md` §5: "Let the user override the model on any connected provider and re-run."
     * The override is **for this answer only** and does not touch the chat's selection — a control
     * on one screen that silently changes the setting behind another screen is the class of
     * surprise Developer Mode exists to prevent.
     */
    fun rerunWith(providerId: ProviderId) {
        if (isBusy() || exchange.isEmpty()) return
        lastAction = LastAction.EXECUTE
        currentJob?.cancel()
        textReader.stop()
        triedThisExecution.clear()
        _uiState.update {
            it.copy(
                executing = true,
                error = null,
                result = "",
                originalResult = "",
                translation = "",
                showingTranslation = false,
                route = null,
                fallbackOptions = emptyList(),
            )
        }
        currentJob = viewModelScope.launch { runExecution(providerId) }
    }

    /** Re-send the exchange exactly as it stands. See the note on `retry()`. */
    private fun retryExecution() {
        if (exchange.isEmpty()) {
            execute()
            return
        }
        currentJob?.cancel()
        textReader.stop()
        triedThisExecution.clear()
        _uiState.update {
            it.copy(executing = true, error = null, result = "", fallbackOptions = emptyList())
        }
        currentJob = viewModelScope.launch {
            runExecution(_uiState.value.route?.provider ?: _uiState.value.provider)
        }
    }

    /**
     * **The follow-up loop** — `BRAINING.md` §2.7, and the owner's ruling of 2026-08-17.
     *
     * The note is appended to the exchange and the whole thing is re-sent, so the model is
     * continuing its own answer rather than answering a fresh question about it. The alternative —
     * re-forging the English prompt from the note and executing from zero — was offered and
     * declined: it is more accurate for a change of direction and it throws away the answer the
     * user is looking at, which is the thing they asked to *adjust*.
     *
     * **Still no system prompt.** The forged prompt is in the conversation and it is the
     * instruction, including its Arabic OUTPUT CONTRACT. A second voice added here would quietly
     * argue with it — the same reason `execute()` sends none.
     */
    fun sendFeedback() {
        val note = _uiState.value.inputText.trim()
        if (note.isEmpty() || isBusy() || exchange.isEmpty()) return

        lastAction = LastAction.EXECUTE
        currentJob?.cancel()
        textReader.stop()
        exchange += ChatMessage(role = MessageRole.USER, content = note)
        triedThisExecution.clear()

        _uiState.update {
            it.copy(
                inputText = "",
                executing = true,
                error = null,
                result = "",
                originalResult = "",
                translation = "",
                showingTranslation = false,
                feedbackRounds = it.feedbackRounds + 1,
            )
        }
        currentJob = viewModelScope.launch {
            runExecution(_uiState.value.route?.provider ?: _uiState.value.provider)
        }
    }

    /**
     * Send [exchange] to [startProvider], and on a provider-side failure **try the next provider
     * and say so**.
     *
     * The owner's ruling of 2026-08-17: automatic fallback, with a visible notice naming both the
     * one that failed and the one that answered. Silent fallback was declined for the reason
     * Developer Mode exists — an answer that came from a model the user did not choose, with
     * nothing on screen saying so, is the failure this project spent §10 entry 5 learning to
     * surface.
     *
     * **Recursive, and bounded by [triedThisExecution].** Every hop records its provider, so the
     * ladder is at most one pass over the providers the user holds a key for. `DefaultModelRouter`
     * decides both the order and whether the error is worth a hop at all; both are pinned by
     * `DefaultModelRouterTest`.
     *
     * **Fallback belongs to this stage and not to plain chat.** Chat is the owner's instrument for
     * testing a provider, a key or a model (`ANSWERS.md` Part 7 §M3-1); an instrument that quietly
     * switches to a different provider when the one under test fails is measuring the wrong thing.
     */
    private suspend fun runExecution(startProvider: ProviderId, replacing: ProviderId? = null) {
        val model = modelFor(startProvider)
        val decision = router.route(startProvider, model)

        if (decision !is RoutingDecision.Direct) {
            // M6 territory. Nothing produces it yet; when something does, the screen needs a
            // sentence rather than a silent stall.
            _uiState.update {
                it.copy(
                    executing = false,
                    error = AiError.Unknown(startProvider, status = null, detail = "Path B is not built"),
                )
            }
            return
        }

        val instance = providers.values.find { it.id == startProvider }
        if (instance == null) {
            _uiState.update {
                it.copy(
                    executing = false,
                    error = AiError.Unknown(startProvider, status = null, detail = "Provider not found"),
                )
            }
            return
        }

        triedThisExecution += startProvider
        _uiState.update {
            it.copy(
                result = "",
                route = decision.copy(
                    reason = if (replacing == null) {
                        RouteReason.SELECTED_BY_USER
                    } else {
                        RouteReason.FALLBACK_AFTER_FAILURE
                    },
                    replacing = replacing,
                ),
            )
        }

        val request = AiRequest(
            model = decision.model,
            messages = exchange.toList(),
            maxTokens = 8192,
            diagnostics = _uiState.value.developerMode,
        )

        var failure: AiError? = null

        instance.complete(request)
            .catch { cause ->
                if (cause is CancellationException) throw cause
                emit(AiChunk.Error(cause.toAiError(instance.id)))
            }
            .collect { chunk ->
                when (chunk) {
                    is AiChunk.Token -> _uiState.update { it.copy(result = it.result + chunk.text) }

                    is AiChunk.Done -> {
                        val answer = _uiState.value.result
                        // The answer joins the conversation, so the next round of feedback is a
                        // continuation rather than a question about text the model cannot see.
                        exchange += ChatMessage(role = MessageRole.ASSISTANT, content = answer)
                        _uiState.update {
                            it.copy(
                                executing = false,
                                originalResult = answer,
                                totalSeconds = elapsedSeconds(),
                            )
                        }
                        // **M5. This is the one place a session enters history**, and it is here
                        // rather than in `execute()` because only a completed answer is a
                        // completed run. `decision.provider` and `decision.model` are recorded
                        // instead of the user's selection, so a session that fell back to a
                        // second provider says so in the list.
                        persist(answer, decision.provider, decision.model)
                    }

                    is AiChunk.Error -> failure = chunk.error

                    // Captured but not shown here: the diagnostics panel lives with the
                    // conversation above, and this request's body is the prompt the user is
                    // already looking at.
                    is AiChunk.Meta -> Unit
                }
            }

        val error = failure ?: return

        // **Stop and ask. Do not hop.**
        //
        // Until 2026-08-28 this recursed into the next provider automatically and announced the
        // substitution in red. The owner reversed that ruling: he wants the list of providers
        // that *could* answer, and he wants to pick. The reasoning behind the original — that a
        // silent switch is the failure Developer Mode exists to prevent — is untouched; what
        // changed is that announcing a purchase after the fact is not the same as being asked
        // before it.
        //
        // `fallbackCandidates` still decides **whether** a fallback is appropriate at all. An
        // empty list is not "you have no other keys" — it is the router refusing to route around
        // the user's own setup, and the screen then shows the error alone.
        val options = router.fallbackCandidates(
            failed = startProvider,
            error = error,
            keyed = keyedProviders(),
            alreadyTried = triedThisExecution,
        )
        lastFailedProvider = startProvider
        _uiState.update {
            it.copy(executing = false, error = error, fallbackOptions = options)
        }
    }

    /**
     * Continue this answer on a provider the user picked from the failure card.
     *
     * **Not the same as `rerunWith`**, which starts the answer again on a provider of the user's
     * choosing while nothing is wrong. This one carries `replacing`, so the line under the answer
     * names both providers — «تعذّر Google Gemini، فأجاب Claude» — which is the part of the 17
     * August ruling that survives: whatever happens, the screen says who actually answered.
     */
    fun chooseFallback(providerId: ProviderId) {
        if (isBusy()) return

        // **Which phase failed decides what happens next**, and the four answers are genuinely
        // different operations — not one operation with a flag. Until 2026-08-30 this method
        // assumed the execution had failed, because that was the only phase that ever offered
        // chips; now that the interrogation and the forge offer them too, the assumption would
        // have re-run the wrong thing on the user's chosen provider.
        when (lastAction) {
            LastAction.EXECUTE -> chooseFallbackForExecution(providerId)
            LastAction.CLARIFY -> switchBrainTo(providerId) { start() }
            // resume, never open — the twelve turns already on screen are not disposable.
            LastAction.REPLY -> switchBrainTo(providerId) { resumeClarify() }
            LastAction.FORGE -> switchBrainTo(providerId) { forge() }
        }
    }

    /** The original: continue the *answer* on someone else, naming both providers underneath. */
    private fun chooseFallbackForExecution(providerId: ProviderId) {
        if (exchange.isEmpty()) return
        val failed = lastFailedProvider
        lastFailedProvider = null
        lastAction = LastAction.EXECUTE
        currentJob?.cancel()
        textReader.stop()
        _uiState.update {
            it.copy(executing = true, error = null, fallbackOptions = emptyList(), result = "")
        }
        currentJob = viewModelScope.launch { runExecution(providerId, replacing = failed) }
    }

    /**
     * Move the whole screen to [providerId], then run [action].
     *
     * The interrogation and the forge read their provider from [ClarifyUiState.provider] through
     * `brain()` — there is no per-call provider argument the way `runExecution` has one — so a
     * fallback here is a **change of brain for the rest of the session**, not a detour for one
     * request. That is also the honest behaviour: an interrogation is a conversation, and a user
     * who was moved to Claude for one question and silently back to Gemini for the next would be
     * reading a dialogue with two authors and no way to tell which said what.
     *
     * The model moves with it. Sending Gemini's model name to Claude is a guaranteed failure and
     * would look exactly like the fallback itself not working.
     */
    private fun switchBrainTo(providerId: ProviderId, action: () -> Unit) {
        currentJob?.cancel()
        _uiState.update {
            it.copy(
                provider = providerId,
                model = modelFor(providerId),
                error = null,
                fallbackOptions = emptyList(),
            )
        }
        action()
    }

    /**
     * Re-ask the turn that failed, on whatever provider the screen now holds.
     *
     * Goes through `ClarifyEngine.resume`, which re-issues the current turn **without** appending
     * anything: `reply()` would file the user's answer a second time and `open()` would delete
     * the interrogation. Both were reachable from this button before the phase split.
     */
    private fun resumeClarify() {
        val brain = brain() ?: return
        lastAction = LastAction.REPLY
        triedThisTurn += _uiState.value.provider
        collect(clarifyEngine.resume(brain.first, brain.second, _uiState.value.developerMode))
    }

    /**
     * Who could take over the interrogation or the forge — the turn-scoped twin of the block in
     * `finishExecution`.
     *
     * Same router, same rules: an empty list means a hop would be wrong (a missing key, a
     * rejected key, a dead network, an unclassified failure), not that the user has no other
     * keys.
     */
    private suspend fun turnFallbackOptions(error: AiError): List<ProviderId> {
        val failed = _uiState.value.provider
        lastFailedProvider = failed
        return router.fallbackCandidates(
            failed = failed,
            error = error,
            keyed = keyedProviders(),
            alreadyTried = triedThisTurn,
        )
    }

    /**
     * Take the router's own first choice.
     *
     * The one-tap path for a user who does not care which provider answers, only that one does.
     * It goes through the same list the chips are built from, so the button can never pick
     * something the chips would not have offered.
     */
    fun tryAnyFallback() {
        val first = _uiState.value.fallbackOptions.firstOrNull() ?: return
        chooseFallback(first)
    }

    /**
     * Translate the answer into Arabic, **on demand**.
     *
     * `ANSWERS.md` Part 7 §M3-2 said M4 would translate every answer. That ruling was written when
     * answers came back in English; since 2026-08-07 the forged prompt requires Arabic, so a
     * mandatory step would spend a second call and several seconds on nearly every answer to
     * change nothing. The owner ruled on 2026-08-17: detect, and offer.
     *
     * The original is never destroyed — [ClarifyUiState.originalResult] holds it and the button
     * flips back.
     */
    fun translate() {
        val source = _uiState.value.originalResult.ifBlank { _uiState.value.result }
        if (source.isBlank() || isBusy()) return

        // Already translated once — show it again rather than paying for it twice.
        if (_uiState.value.translation.isNotBlank()) {
            _uiState.update { it.copy(result = it.translation, showingTranslation = true) }
            return
        }

        val providerId = _uiState.value.route?.provider ?: _uiState.value.provider
        val instance = providers.values.find { it.id == providerId } ?: return

        currentJob?.cancel()
        textReader.stop()
        _uiState.update { it.copy(translating = true, error = null, result = "") }

        currentJob = viewModelScope.launch {
            val request = AiRequest(
                model = modelFor(providerId),
                messages = listOf(ChatMessage(role = MessageRole.USER, content = source)),
                systemPrompt = TranslatePrompt.SYSTEM,
                maxTokens = 8192,
                diagnostics = _uiState.value.developerMode,
            )

            instance.complete(request)
                .catch { cause ->
                    if (cause is CancellationException) throw cause
                    emit(AiChunk.Error(cause.toAiError(instance.id)))
                }
                .collect { chunk ->
                    when (chunk) {
                        is AiChunk.Token -> _uiState.update { it.copy(result = it.result + chunk.text) }

                        is AiChunk.Done -> _uiState.update {
                            it.copy(
                                translating = false,
                                translation = it.result,
                                showingTranslation = true,
                            )
                        }

                        // The original is restored on failure. A half-streamed translation left on
                        // screen would replace a good answer with a broken one.
                        is AiChunk.Error -> _uiState.update {
                            it.copy(
                                translating = false,
                                error = chunk.error,
                                result = it.originalResult,
                            )
                        }

                        is AiChunk.Meta -> Unit
                    }
                }
        }
    }

    /** Flip back to the answer as it arrived. */
    fun showOriginal() {
        _uiState.update { it.copy(result = it.originalResult, showingTranslation = false) }
    }

    private fun modelFor(providerId: ProviderId): String =
        modelOverrides[providerId.name]?.takeIf { it.isNotBlank() } ?: providerId.defaultModel

    /** The providers the user holds a non-blank key for. Never throws; an unreadable store is an
     * empty set, which costs a fallback rather than the screen. */
    private suspend fun keyedProviders(): Set<ProviderId> = runCatching {
        keyStore.getAllKeys()
            .filterValues { it.isNotBlank() }
            .keys
            .mapNotNull { name -> ProviderId.entries.firstOrNull { it.name == name } }
            .toSet()
    }.getOrDefault(emptySet())

    private fun forge() {
        val brain = brain() ?: return
        lastAction = LastAction.FORGE
        triedThisTurn += _uiState.value.provider
        currentJob?.cancel()
        _uiState.update {
            it.copy(
                forging = true,
                error = null,
                fallbackOptions = emptyList(),
                forgedPrompt = "",
                clearedPrompt = "",
                frameworkId = null,
                frameworkRationale = "",
            )
        }

        currentJob = viewModelScope.launch {
            val startedAt = System.nanoTime()
            var firstDeltaAt: Long? = null
            var chunks = 0
            var endpoint = ""
            var requestBody = ""

            fun diagnostics(): RequestDiagnostics = RequestDiagnostics(
                endpoint = endpoint,
                requestBody = requestBody,
                firstChunkMillis = firstDeltaAt?.let { (it - startedAt) / 1_000_000 },
                totalMillis = (System.nanoTime() - startedAt) / 1_000_000,
                chunkCount = chunks,
            )

            promptForge.forge(
                // The restored session when this run came from history, the live one otherwise.
                // Passing `clarifyEngine.session` unconditionally forged from an empty idea on
                // every re-opened run — see `restoredSession`.
                session = currentSession(),
                provider = brain.first,
                model = brain.second,
                frameworkOverride = _uiState.value.frameworkOverride,
                diagnostics = _uiState.value.developerMode,
            ).collect { event ->
                when (event) {
                    is ForgeEvent.Meta -> {
                        endpoint = event.endpoint
                        requestBody = event.requestBody
                    }

                    is ForgeEvent.FrameworkChosen -> _uiState.update {
                        it.copy(
                            frameworkId = event.frameworkId,
                            frameworkRationale = event.rationale,
                        )
                    }

                    is ForgeEvent.Delta -> {
                        chunks++
                        if (firstDeltaAt == null) firstDeltaAt = System.nanoTime()
                        _uiState.update { it.copy(forgedPrompt = it.forgedPrompt + event.text) }
                    }

                    is ForgeEvent.Completed -> {
                        // Captured here and nowhere else. Re-forging or swapping the framework
                        // rewrites the prompt, so it rewrites the name with it — a session whose
                        // idea has been reframed should not keep the old label.
                        //
                        // **But only when there is a new name.** A model that ignores the marker
                        // returns an empty title, and assigning it unconditionally would blank
                        // the name a restored session already had — a re-run would silently
                        // downgrade a good row to its fallback. Absence is not an instruction.
                        if (event.prompt.title.isNotBlank()) sessionTitle = event.prompt.title
                        resetTurnBudget()
                        _uiState.update {
                            it.copy(
                                forging = false,
                                // Trimmed and taken from the terminal event rather than left as
                                // the accumulated deltas: the two agree, and taking the
                                // authoritative one means a future change to the reader cannot
                                // leave the field and the result quietly disagreeing.
                                forgedPrompt = event.prompt.english,
                                frameworkId = event.prompt.frameworkId,
                                frameworkRationale = event.prompt.rationale,
                                lastDiagnostics = if (it.developerMode) diagnostics() else null,
                            )
                        }
                    }

                    is ForgeEvent.Failed -> {
                        // Same offer as every other phase. The forge is one provider call like
                        // any other, and a user whose forge failed on a rate limit has exactly
                        // the same remedy available as one whose execution did.
                        val options = turnFallbackOptions(event.error)
                        _uiState.update {
                            it.copy(
                                forging = false,
                                error = event.error,
                                fallbackOptions = options,
                                lastDiagnostics = if (it.developerMode) diagnostics() else null,
                            )
                        }
                    }
                }
            }
        }
    }

    // ── M5 · HISTORY ─────────────────────────────────────────────────────────────────────

    /**
     * Re-open a saved run: its idea, its interrogation, and the prompt it produced.
     *
     * **It lands in [ClarifyState.READY] and asks nothing.** `BRAINING.md` §2.3 gives the
     * readiness decision to the user, and the user already made it — the day they pressed
     * «نضجت الفكرة» on this session. Re-asking would be the app forgetting a decision it
     * recorded.
     *
     * The answer is deliberately **not** restored into [ClarifyUiState.result]. Re-run means run
     * it again; showing the old answer under a screen that is about to produce a new one is the
     * label-that-outlived-its-behaviour defect this project has now fixed twice (§10 entry 14).
     */
    private fun restore(id: Long) {
        _uiState.update { it.copy(restoring = true, error = null) }
        viewModelScope.launch {
            val record = sessions.byId(id)
            if (record == null) {
                // Deleted between opening the list and tapping the row. Say so and stop — the
                // route's idea is a placeholder, so starting an interrogation here would ask
                // questions about nothing while looking exactly like a working screen.
                _uiState.update { it.copy(restoring = false, restoreFailed = true) }
                return@launch
            }
            recordId = record.id
            // Kept, or the next save would stamp the row with today's date, drop it to the
            // bottom of the list and label it «منذ ٦٨٥ شهر».
            createdAt = record.createdAt
            // Kept too: re-running a saved session must not blank the name it already has. It is
            // overwritten only if the user actually re-forges, which is where a new name is due.
            sessionTitle = record.title
            restoredSession = ClarifySession(
                originalIdea = record.idea,
                turns = record.turns,
                state = ClarifyState.READY,
                frameworkOverride = record.frameworkId.ifBlank { null },
            )

            // **The provider comes from the record, not from the route.** The history list
            // navigates with a placeholder because it does not read the database; this is where
            // the run's real provider is recovered, so re-running a Gemini session does not
            // silently execute on DeepSeek.
            val saved = ProviderId.entries.firstOrNull { it.name == record.providerName }
            val restoredProvider = saved ?: _uiState.value.provider
            val restoredModel = modelOverrides[restoredProvider.name]?.takeIf { it.isNotBlank() }
                ?: record.model.ifBlank { restoredProvider.defaultModel }

            _uiState.update {
                it.copy(
                    restoring = false,
                    fromHistory = true,
                    idea = record.idea,
                    turns = record.turns,
                    engineTurns = record.turns.count { t -> t !is ClarifyTurn.UserReply },
                    state = ClarifyState.READY,
                    forgedPrompt = record.forgedPrompt,
                    frameworkId = record.frameworkId.ifBlank { null },
                    frameworkOverride = record.frameworkId.ifBlank { null },
                    provider = restoredProvider,
                    model = restoredModel,
                )
            }
        }
    }

    /**
     * Write this run to history, or update the row it already occupies.
     *
     * **Called when an answer completes, and only then.** `docs/M5_DESIGN_NOTE.md` §2: a run that
     * produced no answer has nothing to re-run and nothing worth searching. That is a statement
     * about history, not about the value of an abandoned run — §10 entry 29 stands.
     *
     * **[ClarifyUiState.originalResult] is what is stored, never the translation.** The
     * translation is derived and can be re-derived; the answer as the model actually gave it
     * cannot. Storing the translation would make history disagree with what was really said.
     *
     * Failure is silent by design — `SessionRepositoryImpl` never throws, and a database that
     * cannot be written must cost the user their history, not the answer they are reading.
     */
    private fun persist(answer: String, provider: ProviderId, model: String) {
        if (answer.isBlank()) return
        val state = _uiState.value
        val turns = currentSession().turns
        val record = SessionRecord(
            id = recordId,
            // Stamped once, on first save. `currentTimeMillis` and not `nanoTime` — unlike the
            // elapsed timers above, this is a calendar date the user will read, not a duration.
            createdAt = if (recordId == SessionRecord.NEW) System.currentTimeMillis() else createdAt,
            idea = state.idea,
            turns = turns,
            frameworkId = state.frameworkId.orEmpty(),
            forgedPrompt = state.forgedPrompt,
            answer = answer,
            // `ProviderId.name`, not `displayName` — a stable key. The display string is a
            // user-facing label that may be reworded; the key is what lets a saved row be mapped
            // back to a provider, which is exactly what re-running one needs.
            providerName = provider.name,
            model = model,
            summary = SessionSummary.of(turns, state.idea),
            title = sessionTitle,
        )
        viewModelScope.launch {
            val id = sessions.save(record)
            if (recordId == SessionRecord.NEW) {
                recordId = id
                createdAt = record.createdAt
            }
        }
    }

    /** Held so an update does not re-stamp the row with today's date. */
    private var createdAt: Long = 0L

    /**
     * The model's short name for this session, from the forge call.
     *
     * A field rather than UI state: nothing on this screen shows it — it is written once and read
     * only by [persist]. Putting it in `ClarifyUiState` would publish a value no composable reads
     * and invite a future screen to display something the user cannot edit.
     */
    private var sessionTitle: String = ""

    // ── M5 · READBACK ────────────────────────────────────────────────────────────────────

    /**
     * Read the answer aloud, or stop if it is already reading.
     *
     * One button with two meanings rather than two buttons, because the second press of a
     * play-only button does nothing visible and teaches the user the control is broken (§10
     * entry 26).
     */
    fun toggleReadback() {
        if (_uiState.value.reader.speaking) {
            textReader.stop()
            return
        }
        val text = _uiState.value.result.ifBlank { _uiState.value.originalResult }
        if (text.isBlank()) return
        // Following resumes on every fresh press. Turning it back on is the one thing the user
        // cannot forget to do, because pressing play *is* the request to be taken to the words.
        _uiState.update { it.copy(followSpoken = true) }
        textReader.speak(text, "ar")
    }

    /**
     * The user took the scroll.
     *
     * Called from a touch listener on the page, not from a button — the gesture that means "I am
     * reading somewhere else" is a drag, and requiring a tap on a control first would make the
     * app fight for the scroll exactly once before giving up, which is worse than either policy.
     */
    fun stopFollowingSpoken() {
        if (_uiState.value.followSpoken) _uiState.update { it.copy(followSpoken = false) }
    }

    /** «تابع القراءة» — hand the page back to the reader. */
    fun resumeFollowingSpoken() = _uiState.update { it.copy(followSpoken = true) }

    /**
     * Silence the speaker when the screen goes away.
     *
     * `TextToSpeech` is a system service and outlives this ViewModel — without this, leaving the
     * screen mid-sentence leaves the phone talking about a screen the user has closed.
     */
    override fun onCleared() {
        textReader.stop()
        super.onCleared()
    }

    // ── VOICE ────────────────────────────────────────────────────────────────────────────
    //
    // **Answering by voice, not just asking by voice.** The owner's request of 2026-08-08: the
    // interrogation is a conversation, and typing every answer on a phone is what made a good
    // conversation feel like paperwork.
    //
    // The collection below mirrors `ChatViewModel`'s rather than sharing it, and that is a
    // deliberate trade recorded in §9: extracting it would mean refactoring the one voice path
    // M2 verified end to end on device, to save ~40 lines. The **UI** is shared — the sheet, the
    // waveform and the permission dialog all moved to `core-ui` — which is where duplication
    // would actually have hurt, because a second copy of the microphone rationale is a second
    // promise to keep true.

    /**
     * Segments land in [ClarifyUiState.inputText] — the same field the keyboard writes to, so
     * the answer stays editable before it is sent. `docs/M2_DESIGN_NOTE.md` §1's rule, applied to
     * the screen that needs it most: an interrogation answer is where a mis-heard word does the
     * most damage.
     */
    fun startVoice(languageTag: String) {
        if (_uiState.value.recording) return
        inputBeforeVoice = _uiState.value.inputText
        _uiState.update { it.copy(recording = true, partial = "", voiceError = null) }

        voiceJob = viewModelScope.launch {
            speechToText.transcribe(languageTag)
                .catch { cause ->
                    if (cause is CancellationException) throw cause
                    _uiState.update {
                        it.copy(
                            recording = false,
                            amplitude = 0f,
                            partial = "",
                            voiceError = SttError.EngineFailure(0),
                        )
                    }
                }
                .collect { event ->
                    when (event) {
                        is TranscriptionEvent.Amplitude ->
                            _uiState.update { it.copy(amplitude = event.level) }

                        is TranscriptionEvent.Partial ->
                            _uiState.update { it.copy(partial = event.text) }

                        is TranscriptionEvent.Segment -> _uiState.update { s ->
                            val joined = if (s.inputText.isBlank()) {
                                event.text
                            } else {
                                "${s.inputText.trimEnd()} ${event.text}"
                            }
                            s.copy(inputText = joined, partial = "")
                        }

                        is TranscriptionEvent.Failed -> _uiState.update {
                            it.copy(
                                recording = false,
                                amplitude = 0f,
                                partial = "",
                                voiceError = event.error,
                            )
                        }

                        TranscriptionEvent.Completed -> _uiState.update {
                            it.copy(recording = false, amplitude = 0f, partial = "")
                        }

                        // Which engine and dialect won. Chat surfaces it in Developer Mode; here
                        // the turn counter is already carrying that role, so it is not repeated.
                        is TranscriptionEvent.EngineConfig -> Unit
                    }
                }
        }
    }

    /** «تمّ» — let the engine deliver its last sentence. Not the same as abandoning the run. */
    fun stopVoice() = speechToText.stop()

    /**
     * «إلغاء» — abandon the run and **give the field back the way it was found**.
     *
     * Cancelling the job stops the microphone (`DeepgramSpeechToText` tears the recorder down in
     * `awaitClose`), but it does not un-write the segments that already arrived. Until 2026-08-18
     * it did not try to, so the button removed the panel and kept every word — the owner pressed
     * it and watched his dictation stay in the box.
     */
    fun cancelVoice() {
        voiceJob?.cancel()
        voiceJob = null
        _uiState.update {
            it.copy(
                recording = false,
                amplitude = 0f,
                partial = "",
                inputText = inputBeforeVoice,
            )
        }
    }

    fun dismissVoiceError() = _uiState.update { it.copy(voiceError = null) }

    fun onMicrophonePermissionDenied() =
        _uiState.update { it.copy(voiceError = SttError.PermissionDenied) }

    private fun start() {
        val brain = brain() ?: return
        lastAction = LastAction.CLARIFY
        triedThisTurn += _uiState.value.provider
        collect(
            clarifyEngine.open(
                _uiState.value.idea,
                brain.first,
                brain.second,
                _uiState.value.developerMode,
            ),
        )
    }

    /**
     * A turn is in flight.
     *
     * Deliberately keyed on state alone. An earlier version also required [ClarifyUiState.streaming]
     * to be non-empty, which made the gap between sending the request and the first token read as
     * *idle* — so a second tap during those seconds would have started a second turn on top of the
     * first. State is the whole truth here; the text is just what has arrived so far.
     */
    private fun isBusy(): Boolean =
        _uiState.value.state in BUSY_STATES ||
            _uiState.value.forging ||
            _uiState.value.executing ||
            _uiState.value.translating

    /** The provider instance plus its resolved model, or an error state if the map has no entry. */
    private fun brain(): Pair<AiProvider, String>? {
        val instance = providers.values.find { it.id == _uiState.value.provider }
        if (instance == null) {
            _uiState.update {
                it.copy(
                    state = ClarifyState.AWAITING_USER_DECISION,
                    error = AiError.Unknown(it.provider, status = null, detail = "Provider not found"),
                )
            }
            return null
        }
        return instance to _uiState.value.model
    }

    private fun collect(flow: Flow<ClarifyEvent>) {
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            // Timed here rather than in the engine, deliberately — the same split `2026-07-29-D`
            // made for chat. The provider knows what it sent; only this layer knows what the
            // user experienced. `nanoTime`, not `currentTimeMillis`, so a clock adjustment
            // mid-turn cannot produce a negative duration.
            val startedAt = System.nanoTime()
            var firstDeltaAt: Long? = null
            var chunks = 0
            var endpoint = ""
            var requestBody = ""

            fun diagnostics(): RequestDiagnostics = RequestDiagnostics(
                endpoint = endpoint,
                requestBody = requestBody,
                firstChunkMillis = firstDeltaAt?.let { (it - startedAt) / 1_000_000 },
                totalMillis = (System.nanoTime() - startedAt) / 1_000_000,
                chunkCount = chunks,
            )

            flow.collect { event ->
                when (event) {
                    is ClarifyEvent.StateChanged -> _uiState.update {
                        it.copy(
                            state = event.state,
                            streaming = if (event.state == ClarifyState.ANALYZING) "" else it.streaming,
                        )
                    }

                    is ClarifyEvent.Meta -> {
                        endpoint = event.endpoint
                        requestBody = event.requestBody
                    }

                    is ClarifyEvent.Delta -> {
                        chunks++
                        if (firstDeltaAt == null) firstDeltaAt = System.nanoTime()
                        _uiState.update { it.copy(streaming = it.streaming + event.text) }
                    }

                    is ClarifyEvent.TurnCompleted -> {
                        resetTurnBudget()
                        _uiState.update {
                            it.copy(
                                // Read back from the engine rather than appended here. The engine
                                // owns the session; two places building the same list is how the
                                // model name ended up wrong in three files at once.
                                turns = clarifyEngine.session.turns,
                                engineTurns = clarifyEngine.session.engineTurnCount,
                                streaming = "",
                                state = ClarifyState.AWAITING_USER_DECISION,
                                lastDiagnostics = if (it.developerMode) diagnostics() else null,
                            )
                        }
                    }

                    is ClarifyEvent.Failed -> {
                        // **The gap the owner found on 2026-08-30.** The chooser had been built
                        // for the execution phase only, so a provider that died mid-interrogation
                        // — which is where a user spends most of their time on this screen —
                        // produced a red card with nothing to do about it. Computed here, outside
                        // `update`, because it reads the key store.
                        val options = turnFallbackOptions(event.error)
                        _uiState.update {
                            it.copy(
                                error = event.error,
                                fallbackOptions = options,
                                streaming = "",
                                // Deliberately not left in ANALYZING: a failed turn must leave the
                                // user able to type, retry or declare the idea ready. A screen that
                                // strands someone on a spinner after a network blip is a worse
                                // outcome than the blip.
                                state = ClarifyState.AWAITING_USER_DECISION,
                                // Kept on failure too. A request that failed is exactly when the
                                // endpoint and the outgoing body are worth reading — the
                                // `lastDiagnostics` reasoning from `2026-07-29-D`, which existed
                                // because a failed request throws its bubble away and the
                                // diagnostics with it.
                                lastDiagnostics = if (it.developerMode) diagnostics() else null,
                            )
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val ARG_IDEA = "idea"
        const val ARG_PROVIDER = "provider"

        /**
         * The history row to re-open, or absent for a fresh dictation.
         *
         * **A query argument with a default, not a third path segment**, so the existing route
         * keeps working unchanged — a required third segment would have made every current
         * caller a compile error for no gain, and `Routes.clarify` would have had to invent a
         * value to pass. `SessionRecord.NEW` (0) means "none", which is also what Room never
         * assigns as a real id.
         */
        const val ARG_SESSION_ID = "session"

        private val BUSY_STATES = setOf(
            ClarifyState.ANALYZING,
            ClarifyState.ASKING,
            ClarifyState.SUGGESTING,
        )
    }
}
