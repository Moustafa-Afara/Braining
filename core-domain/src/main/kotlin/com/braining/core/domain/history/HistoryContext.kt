package com.braining.core.domain.history

/**
 * Turns the last few sessions into the block that is appended to CLARIFY's and FORGE's system
 * prompts.
 *
 * **This is the permanent answer to the over-questioning problem** — `PROJECT_STATE.md` §7's
 * sharpest diagnosis, and `ANSWERS.md` Part 11 §K1. The engine over-specifies because it has no
 * idea who it is talking to; the «نبذة عنك» note was the cheap half of the fix and this is the
 * half that grows on its own.
 *
 * ### Three properties, each of which is a decision
 *
 * **1. Summaries, never full sessions.** K1. Five full interrogations on every turn would cost a
 * fortune and bury the current question in noise. [SessionSummary] is what is stored and what is
 * read.
 *
 * **2. A budget, in one place.** This text rides on **every Clarify turn**, exactly like the
 * «نبذة عنك» note whose `MAX_PROFILE_LENGTH` exists for the same reason. [MAX_SESSIONS] and
 * [MAX_CHARS] are that budget, and the oldest entries drop first when it binds — the newest
 * sessions say the most about who is speaking now.
 *
 * **3. Prohibitions ride with it, and they are not padding.** `PROJECT_STATE.md` §10 entry 32:
 * *a fact handed to a model that has nothing else to hold onto becomes the subject.* The note
 * needed three explicit prohibitions for exactly this reason. **History is the same hazard,
 * larger** — five previous topics are five ways to drag a new question somewhere it never asked
 * to go. A user whose last session was about a car must not be asked about the car when they
 * arrive with a question about their child.
 */
object HistoryContext {

    /** How many past sessions reach the model. "The last few" — `ANSWERS.md` Part 11 §K1. */
    const val MAX_SESSIONS = 5

    /**
     * The ceiling on the whole block, in characters.
     *
     * Roughly twice `AppPreferences.MAX_PROFILE_LENGTH`, and chosen the same way: this is a token
     * budget spent on every turn of every interrogation, and background that costs more than the
     * question it is helping to ask is background that is not earning its place.
     */
    const val MAX_CHARS = 1200

    /**
     * The block, or an empty string when there is nothing to say.
     *
     * **Empty is the fresh-install case and it must produce nothing at all** — not a heading with
     * no entries under it. A model shown "previous sessions:" followed by silence will reason
     * about the silence.
     *
     * @param records newest first, as [SessionRepository.recent] returns them.
     */
    fun build(records: List<SessionRecord>): String {
        val lines = records
            .asSequence()
            .map { it.summary.ifBlank { SessionSummary.titleOf(it.idea) } }
            .map { it.trim() }
            // Capped here as well as at save time. A row written by an older build, or one whose
            // summary grew for any reason, must not be able to spend the whole budget alone.
            .map { SessionSummary.cap(it) }
            .filter { it.isNotEmpty() }
            .take(MAX_SESSIONS)
            .toList()

        if (lines.isEmpty()) return ""

        // Built oldest-dropped-first: entries are added newest first and the loop stops when the
        // budget is spent, so what survives is always the most recent, never an arbitrary prefix.
        val body = StringBuilder()
        var used = HEADER.length + RULES.length
        for (line in lines) {
            val entry = "- $line\n"
            if (used + entry.length > MAX_CHARS && body.isNotEmpty()) break
            body.append(entry)
            used += entry.length
        }

        if (body.isEmpty()) return ""
        return HEADER + "\n\n" + body.toString().trimEnd() + "\n\n" + RULES
    }

    /**
     * Numbered ١٠ because `ClarifyPrompt`'s rules run to ٨ and the «نبذة عنك» block is ٩.
     * The numbering is not decoration: the prompt's other rules refer to each other by number
     * ("القاعدة ٤"), so a block that collided with an existing number would make one of those
     * references ambiguous.
     */
    private val HEADER: String = """
        ١٠. **ملخّصات جلسات سابقة لهذا المستخدم، الأحدث أولاً. اقرأها قبل أن تسأل.**
    """.trimIndent()

    /**
     * The same three prohibitions the «نبذة عنك» note carries, worded for history — and one more
     * that only history needs: **a new idea is not a continuation of an old one unless the user
     * says so.** Without it, a user who spent yesterday on a business plan gets today's question
     * about a school trip folded into the business plan.
     */
    private val RULES: String = """
        استعملها لتعرف من تخاطب ولتحذف كل سؤال صار جوابه معروفاً منها.

        **ولا تفعل بها هذا:** لا تفترض أن الفكرة الجديدة امتدادٌ لجلسة سابقة — هي فكرة مستقلّة
        حتى يقول هو غير ذلك. ولا تقتبس هذه الملخّصات ولا تعلّق عليها، ولا تجرّ الحديث إلى موضوع
        ورد فيها ولم يذكره الآن. هي خلفية تُسكِت أسئلة، وليست موضوعاً يُضاف.
    """.trimIndent()
}
