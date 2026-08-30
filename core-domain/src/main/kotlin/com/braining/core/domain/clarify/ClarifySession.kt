package com.braining.core.domain.clarify

import com.braining.core.domain.model.ChatMessage
import com.braining.core.domain.model.MessageRole
import kotlinx.serialization.Serializable

/**
 * The evolving context of one interrogation — the "context engineering" object of
 * `BRAINING.md` §6, cut down to what M3 is actually allowed to hold.
 *
 * **What is deliberately missing, and why.** `docs/ARCHITECTURE.md` §5 sketches a much larger
 * `Session` "persisted to Room". Three of its fields have no place here yet:
 *
 *  - `originalAudioRef` — **it can never be filled.** `ANSWERS.md` Part 6 §M2-10 deletes raw
 *    audio the moment the transcript returns, and the streaming Deepgram build never writes it
 *    to storage at all. Filed against `docs/ARCHITECTURE.md` in `PROJECT_STATE.md` §9.
 *  - `routingDecision`, `translatedResult` — M4.
 *
 * **Persistence arrived in M5, and `@Serializable` with it.** The first draft carried the
 * annotation speculatively and it was removed; this file then said M5 would "annotate these
 * against a real schema — a cheaper change than a wrong one". That is what has now happened.
 * `SessionRecord.turns` is the schema, and `ANSWERS.md` Part 7 §M3-4 is unchanged: what is
 * written down is a **finished** run, and an interrogation interrupted mid-flight still dies with
 * its ViewModel. This object is still built and thrown away in memory; it is only *copied* into
 * history when an answer completes.
 */
@Serializable
data class ClarifySession(
    /** The transcript as the user left it — errors included. Never rewritten. */
    val originalIdea: String,
    val turns: List<ClarifyTurn> = emptyList(),
    val state: ClarifyState = ClarifyState.ANALYZING,
    /**
     * The user's framework choice, when they have made one.
     *
     * `ANSWERS.md` Part 2 §6 approved `frameworkOverrides` on the session, and
     * `docs/PROMPT_FRAMEWORKS.md` §1 is explicit that framework selection is "a **heuristic**,
     * not a rigid rule — always show the chosen framework to the user and let them edit or swap
     * it". Null means FORGE picks. This field is the whole mechanism of that promise.
     */
    val frameworkOverride: String? = null,
) {

    /**
     * The interrogation as the provider needs to see it.
     *
     * **This is the single place the two message vocabularies are translated**, and it exists so
     * the mapping cannot drift into two copies — the failure `ProviderId.defaultModel` was
     * created to prevent after one model name lived in three places and was wrong in all of them
     * at once (`2026-08-03-A`).
     *
     * The turn markers are **not** re-sent. They are the engine's instruction to the model for
     * the turn it is writing, not part of the conversation's meaning, and echoing them back
     * would teach the model that its own markers are content worth imitating inside a turn.
     */
    fun toProviderHistory(): List<ChatMessage> = buildList {
        add(ChatMessage(role = MessageRole.USER, content = originalIdea))
        turns.forEach { turn ->
            add(
                ChatMessage(
                    role = when (turn) {
                        is ClarifyTurn.UserReply -> MessageRole.USER
                        else -> MessageRole.ASSISTANT
                    },
                    content = turn.text,
                ),
            )
        }
    }

    /** How many times the engine has spoken. The number the gate is judged on — §5.3 of the note. */
    val engineTurnCount: Int get() = turns.count { it !is ClarifyTurn.UserReply }
}
