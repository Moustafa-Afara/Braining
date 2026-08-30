package com.braining.core.domain.clarify

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * One exchange inside an interrogation.
 *
 * **Why this is not `ChatMessage`.** `ChatMessage` carries the provider's own three roles —
 * `SYSTEM` / `USER` / `ASSISTANT` — and has no vocabulary at all for the distinction that makes
 * Clarify worth building: a **question** demands an answer, a **suggestion** may be taken or
 * left, and a **caveat** is a warning the user should weigh before declaring the idea mature.
 * Folding them into one role would either lose that distinction or pad `ChatMessage` with
 * fields meaningless to nine-tenths of its uses. The two lists stay separate;
 * `ClarifySession.toProviderHistory` is the single place they are translated
 * (`docs/M3_DESIGN_NOTE.md` §3.2).
 *
 * **Every implementation carries an explicit `@SerialName`, and that is a release-blocker
 * rather than a nicety.** Polymorphic JSON writes a discriminator, and its default value is the
 * **fully-qualified class name** — which R8 renames in the release build. A history written by a
 * debug build would then be unreadable by the release APK, and vice versa, with the failure
 * appearing as sessions that have lost their interrogation rather than as an error anyone could
 * trace. The short names below never change, whatever the obfuscator does. **Never rename one**:
 * the value is on disk in every user's history.
 *
 * **`@Serializable` since M5 — see `ClarifyState` for why it waited.** The interrogation is now
 * stored inside `SessionRecord`, which is what makes a saved session re-openable rather than a
 * title and an answer with nothing between them. A **sealed** hierarchy is what lets that JSON
 * round-trip without a hand-written discriminator: every implementation is known at compile time,
 * so adding a sixth turn kind changes the schema in one place and the compiler finds every reader.
 */
@Serializable
sealed interface ClarifyTurn {

    val text: String

    /**
     * The engine asked something. The expected next event is a [UserReply].
     *
     * @param options the likely answers, when the question has a small closed set of them.
     *   Empty for an open question. **The list is never the only way to answer** — the text field
     *   stays, because the owner's most valuable replies in gate run 1 were the ones that were
     *   not on any list. Offering choices must not narrow what can be said.
     */
    @Serializable
    @SerialName("question")
    data class Question(
        override val text: String,
        val options: List<String> = emptyList(),
    ) : ClarifyTurn

    /** The engine proposed a direction. Optional to act on. */
    @Serializable
    @SerialName("suggestion")
    data class Suggestion(override val text: String) : ClarifyTurn

    /** The engine raised a risk or a missing piece. Not an error — a warning worth reading. */
    @Serializable
    @SerialName("caveat")
    data class Caveat(override val text: String) : ClarifyTurn

    /**
     * The engine has nothing further to ask.
     *
     * **Added 2026-08-07 because the interrogation would not stop.** The owner's report was
     * "أسئلته التي لا تنتهي", which is the exact risk `docs/M3_DESIGN_NOTE.md` §9 named as the
     * highest in this milestone: a feature users learn to route around is dead however well it
     * works.
     *
     * **This does not move the machine and must never be made to.** `BRAINING.md` §2.3 and
     * `ANSWERS.md` Part 7 §M3-1 give the readiness decision to the user without conditions. This
     * is the engine *saying* it is out of questions; pressing «نضجت الفكرة» remains the only way
     * to reach [ClarifyState.READY]. The fix for an endless interrogation is to let it admit it
     * is finished, not to let it finish on the user's behalf.
     */
    @Serializable
    @SerialName("enough")
    data class Enough(override val text: String) : ClarifyTurn

    /** The user answered. */
    @Serializable
    @SerialName("reply")
    data class UserReply(override val text: String) : ClarifyTurn
}

/**
 * How the engine labels the turn it is about to write.
 *
 * The model is asked to open every turn with one of these markers on its own first line; the
 * implementation strips it before a single character reaches the screen.
 *
 * **Why a leading marker and not JSON.** Structured output would be easier to parse and would
 * cost the thing M1 spent two work units securing: `2026-07-29-A` and `2026-08-03-D` were both
 * about genuine token-by-token streaming, and a JSON envelope cannot be shown until it closes.
 * A marker on the first line is read once, discarded, and everything after it streams normally.
 *
 * **And it degrades safely.** An absent or unrecognised marker resolves to [QUESTION] — the
 * commonest turn, and the one whose UI affordance (an input field) is correct even when the
 * classification is not. The failure mode of a wrong guess here is a suggestion that looks like
 * a question, which costs nothing; the failure mode of a hard parse would be a dead screen.
 */
enum class TurnKind(val marker: String) {
    QUESTION("[[سؤال]]"),
    SUGGESTION("[[اقتراح]]"),
    CAVEAT("[[تنبيه]]"),
    ENOUGH("[[كافٍ]]"),
    ;

    companion object {
        /** Never throws. See the class KDoc: an unknown marker is a [QUESTION]. */
        fun fromMarker(line: String): TurnKind =
            entries.firstOrNull { line.trim().startsWith(it.marker) } ?: QUESTION
    }
}
