package com.braining.core.domain.history

import com.braining.core.domain.clarify.ClarifySession
import com.braining.core.domain.clarify.ClarifyTurn

/**
 * The one or two Arabic lines that stand for a finished session in everything that reads history
 * back — the list, and the context CLARIFY receives.
 *
 * **The engine already wrote this, and that is the whole design.** `ClarifyTurn.Enough` is the
 * `[[كافٍ]]` turn: the engine's own summary of the matured idea, in Arabic, produced at the exact
 * moment the idea settled. It has existed since M3 and nothing has ever read it back.
 *
 * **The alternative was rejected on cost and on principle.** Asking a model to summarize each
 * session is one extra call per run, spent on text the user will rarely read, on the milestone
 * whose point is that the app stops paying the network for things it already knows
 * (`docs/M5_DESIGN_NOTE.md` §4).
 *
 * The fallback — the opening of the idea itself — is not a degraded mode. It is what a session
 * looks like when the user declared the idea mature before the engine ran out of questions, which
 * the owner does routinely and which `BRAINING.md` §2.3 explicitly permits.
 */
object SessionSummary {

    /**
     * How much of a summary is kept. Two lines of Arabic sit comfortably inside this; the cap
     * exists so that a model which ignores «سطرين» cannot put a paragraph into the budget
     * [HistoryContext] is trying to hold.
     */
    const val MAX_CHARS = 240

    /** From a live session, at the moment it is saved. */
    fun of(session: ClarifySession): String = of(session.turns, session.originalIdea)

    /**
     * From the parts, so a saved record can be re-summarized without rebuilding a
     * [ClarifySession] around it.
     *
     * The **last** `Enough` turn wins, not the first: swapping the framework or answering more
     * questions can produce a second one, and the later one describes the idea as it finally
     * stood.
     */
    fun of(turns: List<ClarifyTurn>, idea: String): String {
        val enough = turns.lastOrNull { it is ClarifyTurn.Enough }?.text?.trim()
        val source = if (!enough.isNullOrBlank()) enough else idea.trim()
        return truncate(source, MAX_CHARS)
    }

    /**
     * A title for the list — short enough for one line on a phone.
     *
     * Taken from the **idea**, never from the summary: the list is how the user finds a session
     * they remember starting, and what they remember is what they said, not what the engine
     * concluded.
     */
    fun titleOf(idea: String, max: Int = 60): String = truncate(idea.trim(), max)

    /**
     * Cap arbitrary text to a summary's length.
     *
     * Public so that [HistoryContext] can enforce the budget on text it did **not** produce.
     * A record written by an older build, or one whose summary was edited by hand, must not be
     * able to spend the whole prompt budget on its own — and re-implementing the cut there would
     * be the two-nearly-identical-functions failure that `ArabicNormalizer` exists to avoid.
     */
    fun cap(text: String, max: Int = MAX_CHARS): String = truncate(text, max)

    /**
     * Cut at a word boundary where one is near the limit, and mark the cut.
     *
     * Cutting mid-word produces text that reads as corrupt rather than as abbreviated — and in
     * Arabic a truncated word can be a different, real word, which is worse than an obvious
     * stump.
     */
    private fun truncate(text: String, max: Int): String {
        val flat = text.replace(Regex("\\s+"), " ").trim()
        if (flat.length <= max) return flat
        val cut = flat.take(max)
        val lastSpace = cut.lastIndexOf(' ')
        val body = if (lastSpace > max * 2 / 3) cut.take(lastSpace) else cut
        return body.trimEnd() + "…"
    }
}
