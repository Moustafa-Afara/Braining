package com.braining.core.domain.clarify

import com.braining.core.domain.model.AiError
import com.braining.core.domain.provider.AiProvider
import kotlinx.coroutines.flow.Flow

/**
 * The CLARIFY stage: interrogate an idea until the user declares it mature.
 *
 * `BRAINING.md` §2.3 calls this the core stage and states the one rule the whole design turns
 * on: **it does not proceed until the user explicitly declares the idea ready** (نضجت الفكرة).
 * Nothing in this interface lets the engine advance itself past [ClarifyState.AWAITING_USER_DECISION];
 * [declareReady] is the only route to [ClarifyState.READY], and only the UI calls it.
 *
 * **Why the provider is a parameter and not a constructor dependency.** `ANSWERS.md` Part 7
 * §M3-3 rules that Clarify runs on whichever provider is selected in the chat, and that ruling
 * overrides `BRAINING.md` §5's "Claude is the default brain". A provider injected once at graph
 * construction would be fixed for the life of the process, so a user switching provider mid
 * session would keep talking to the old one — the identical defect `RoutingSpeechToText` was
 * created to avoid in `2026-08-06-O`, where a Hilt binding could not see a key entered after
 * startup. Resolve late, at the moment of the tap.
 */
interface ClarifyEngine {

    /**
     * Open an interrogation on [idea] and stream the first turn.
     *
     * @param idea the transcript as the user left it — **with its errors**. M2 closed with a
     *   third of the words missing on spontaneous speech and the owner's verdict that "the idea
     *   came through". The first question is asked about text that may have misheard a word, so
     *   correction is part of the conversation rather than a failure state
     *   (`docs/M3_DESIGN_NOTE.md` §2).
     * @param model the resolved model name. Never a literal — it comes from
     *   `ProviderId.defaultModel` or the user's Settings override, which is the single source
     *   established in `2026-08-03-A` after one retired model name was wrong in three places.
     */
    fun open(
        idea: String,
        provider: AiProvider,
        model: String,
        diagnostics: Boolean = false,
    ): Flow<ClarifyEvent>

    /** The user's answer to the current turn. Streams the next turn. */
    fun reply(
        text: String,
        provider: AiProvider,
        model: String,
        diagnostics: Boolean = false,
    ): Flow<ClarifyEvent>

    /**
     * Ask the current turn **again** — after it failed — without touching the session.
     *
     * The distinction from [reply] is the whole reason this exists: `reply` appends the user's
     * answer to the session *before* it returns the flow, so re-calling it after a network
     * failure would file the same answer twice. And [open] is worse: it **discards the session**
     * and restarts the interrogation from the original idea.
     *
     * Added 2026-08-30, when the fallback chooser reached this screen. A "try another provider"
     * button that silently threw away twelve turns of interrogation would be the most expensive
     * button in the app.
     *
     * @param provider may differ from the one that failed — that is the point.
     */
    fun resume(
        provider: AiProvider,
        model: String,
        diagnostics: Boolean = false,
    ): Flow<ClarifyEvent>

    /**
     * The user declares the idea mature. Moves to [ClarifyState.READY] and stops asking.
     *
     * Deliberately not a network call: this is a state transition the user owns, and making it
     * depend on a provider would mean a dead network could refuse to let someone finish.
     */
    fun declareReady()

    /** The evolving session — the full context FORGE will read. Never null after [open]. */
    val session: ClarifySession
}

/**
 * Everything one Clarify turn can report.
 *
 * The stream ends with exactly one of [TurnCompleted] or [Failed]. It may carry any number of
 * [StateChanged] and [Delta] events before that.
 *
 * **Shaped after `TranscriptionEvent`, on purpose.** One flow carrying state, text and failure
 * is the pattern this project has now used twice — `AiChunk.Meta` rode the token stream instead
 * of becoming a method on `AiProvider`, and amplitude rode the transcription stream for the same
 * reason. A second channel is a second thing to keep in sync by hand.
 */
sealed interface ClarifyEvent {

    /** The machine moved. Surfaced so the UI never has to infer state from text arriving. */
    data class StateChanged(val state: ClarifyState) : ClarifyEvent

    /**
     * A fragment of the current turn, token by token.
     *
     * Streaming is not decoration here. M1 spent `2026-07-29-A` proving genuine token-by-token
     * delivery and `2026-08-03-D` recovering it after thinking mode ate the budget. A Clarify
     * turn is longer than a chat reply, so a non-streaming turn would be a visible regression.
     */
    data class Delta(val text: String) : ClarifyEvent

    /**
     * The resolved endpoint and the exact bytes sent, **already redacted** at the provider.
     * Emitted before the first token, and only when the caller asked for diagnostics.
     *
     * The same shape as `AiChunk.Meta`, and for the same reason it exists there: the provider is
     * the only layer that knows what it actually sent, the ViewModel is the only one that can
     * time what the user experienced, and passing the two facts up the stream keeps that split
     * honest without adding a method to any interface.
     *
     * For CLARIFY this is not a nicety. The interrogation's system prompt never appears on any
     * screen — it is the single most likely thing in M3 to be silently wrong, and this is the
     * only place a human can ever read what was really asked.
     */
    data class Meta(val endpoint: String, val requestBody: String) : ClarifyEvent

    /** Terminal. The turn finished and is appended to [ClarifySession.turns]. */
    data class TurnCompleted(val turn: ClarifyTurn) : ClarifyEvent

    /**
     * Terminal. Classified, never phrased.
     *
     * Reuses `AiError` rather than inventing a `ClarifyError`. A3 (`2026-08-03-E`) put every
     * failure sentence in `core-ui` resources behind one typed hierarchy, and
     * `docs/M2_DESIGN_NOTE.md` §6 warns that rebuilding wording in a lower layer "would undo
     * the work within a day of finishing it". Clarify fails in exactly the ways a provider call
     * fails, so it fails in exactly the same vocabulary.
     */
    data class Failed(val error: AiError) : ClarifyEvent
}
