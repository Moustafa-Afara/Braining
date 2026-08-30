package com.braining.feature.clarify

import com.braining.core.domain.clarify.ClarifySession
import com.braining.core.domain.clarify.ClarifyTurn
import com.braining.core.domain.clarify.FrameworkOption

/**
 * The instructions that turn a matured Arabic idea into a rigorous English prompt.
 *
 * Like `ClarifyPrompt`, this is **not a UI string** and does not belong in `res/values`: it is
 * sent to a model and never rendered, and localising it would let the app's English toggle change
 * how the prompt is written. Unlike `ClarifyPrompt` it is mostly English, because it is describing
 * an English deliverable to the model.
 */
internal object ForgePrompt {

    /** The markers the answer is parsed on. Same technique as `TurnKind`, same reasons. */
    const val FRAMEWORK_MARKER = "[[FRAMEWORK]]"
    const val RATIONALE_MARKER = "[[السبب]]"

    /**
     * **The session's name in the history list — and it costs nothing.**
     *
     * The owner, 2026-08-28: the list was titled with the first sixty characters of the
     * transcript, which is a dictation, not a name. He asked for a short name written by the
     * model.
     *
     * **A separate call was rejected.** One title request per session is one more round trip and
     * one more bill for a line the user reads for half a second. The forge is already reading
     * the whole matured idea, is already writing Arabic in the rationale line, and already runs
     * exactly once per session — so the title rides on a request that was happening anyway.
     * `PROJECT_STATE.md` §10 entry 35 in miniature: the cheapest work is the work you stop doing.
     *
     * It is placed **before** `[[PROMPT]]` deliberately: everything after that marker streams
     * straight through untouched, so anything the reader must parse has to arrive first.
     */
    const val TITLE_MARKER = "[[العنوان]]"

    const val PROMPT_MARKER = "[[PROMPT]]"

    fun system(
        frameworks: List<FrameworkOption>,
        override: String?,
        profile: String,
        history: String = "",
    ): String {
        val catalogue = frameworks.joinToString("\n") { f ->
            "- ${f.id} — ${f.shape}. Best for: ${f.bestFor}"
        }

        val choice = if (override != null) {
            "The user has CHOSEN the framework: $override. Use it even if you would have " +
                "picked differently, and let the rationale line say what it gives up."
        } else {
            "Choose the single most suitable framework from the catalogue."
        }

        // The "about me" note (`ANSWERS.md` Part 8 §D3), when the user has written one. Built by
        // concatenation, not interpolated into the template below: the note's own lines carry no
        // indentation, and `trimIndent()` would then find a common indent of zero and leave the
        // whole prompt indented. The prohibition matters as much as the text — a background fact
        // given to a model with nothing else to hold onto becomes the subject of the work.
        val about = if (profile.isBlank()) {
            ""
        } else {
            "ABOUT THE USER — written once by them, in Arabic, about themselves:\n" +
                profile.trim() + "\n\n" +
                "Use it ONLY where it changes the prompt you are writing: who the prompt should " +
                "address, what level to pitch it at, what constraints it implies. Do not copy it " +
                "into the prompt as a block, do not describe the user in it, and do not treat any " +
                "detail in it as part of the task they asked for.\n"
        }

        // M5. The same summaries CLARIFY reads, in the same words, because a prompt written
        // without the context the interrogation had would contradict it. Arabic, deliberately
        // — it is the user's own material and translating it here would add a paraphrase step
        // whose errors nobody would ever see.
        val past = if (history.isBlank()) "" else history.trim() + "\n"

        return """
            You turn a matured idea into a professional English prompt.

            The material below is an interrogation conducted in Arabic: an original idea, then
            questions the user answered. The user has declared the idea mature. Your job is not
            to answer the idea — it is to write the prompt that will be sent to another model.

            FRAMEWORK CATALOGUE
            $catalogue

            $choice

            $about
            $past
            SKELETON — fill every heading that applies, in this order, and omit none that does:
            # ROLE
            # CONTEXT
            # OBJECTIVE
            # CONSTRAINTS
            # INPUT
            # REASONING GUIDANCE
            # OUTPUT CONTRACT
            # EXAMPLES   (only if worked examples genuinely help)

            RULES
            0. **You are writing the prompt. Never write a prompt that asks for a prompt.**
               If the user's idea says "write me a professional prompt", "use prompt-engineering
               frameworks", or anything of that shape, that is them describing the job YOU are
               doing — it is not the deliverable they want back. Their real goal is the thing the
               prompt is for. Build a prompt that performs that task directly, and never one whose
               objective is to construct, design or return another prompt.
            1. The prompt is written entirely in ENGLISH. No Arabic inside it — except where
               rule 4 requires the OUTPUT CONTRACT to name Arabic as the answer's language.
            2. Use everything the interrogation established. Facts the user gave in their answers
               belong in CONTEXT or CONSTRAINTS — do not discard them and do not invent any.
            3. Where the interrogation left something genuinely unknown, say so in CONSTRAINTS
               rather than filling the gap with a plausible guess.
            4. OUTPUT CONTRACT must be specific: format, structure, and length — and it MUST
               require that **the reply produced when this prompt is run is written in Arabic**.
               Say it about the immediate answer itself, in those words. Do not say it about some
               document, report or message that the answer might in turn produce: an instruction
               that lands one level too deep leaves the reader holding English.
            5. Answer in exactly this shape, each marker on its own line:

            $FRAMEWORK_MARKER <the framework id, exactly as written in the catalogue>
            $RATIONALE_MARKER <one line, in ARABIC, saying why this framework suits this task>
            $TITLE_MARKER <a SHORT name for this session, in ARABIC — see rule 6>
            $PROMPT_MARKER
            <the English prompt, and nothing after it>

            6. The $TITLE_MARKER line names the session in the user's saved history. **Two to five
               Arabic words. A name, not a sentence.** It must say what the idea is ABOUT, the way
               a person would title a note to themselves — «خطة قراءة لطفل في السادسة», not «طلب
               المستخدم خطة». Never repeat the user's opening words back verbatim, never end it
               with a full stop, and never write it in English.
        """.trimIndent()
    }

    /**
     * The interrogation, flattened for the forge.
     *
     * Sent as one user message rather than replayed as a conversation, deliberately: the forge is
     * not continuing that dialogue, it is reading a finished transcript. Replaying it as
     * user/assistant turns invites the model to answer the last question instead of writing a
     * prompt — the request's shape teaches it what is being asked as much as the instructions do.
     */
    fun material(session: ClarifySession): String = buildString {
        appendLine("ORIGINAL IDEA (Arabic, as dictated — it may contain transcription errors):")
        appendLine(session.originalIdea)
        if (session.turns.isNotEmpty()) {
            appendLine()
            appendLine("THE INTERROGATION:")
            session.turns.forEach { turn ->
                val label = when (turn) {
                    is ClarifyTurn.Question -> "Q"
                    is ClarifyTurn.Suggestion -> "SUGGESTION"
                    is ClarifyTurn.Caveat -> "CAVEAT"
                    // The engine's own summary of the matured idea. Labelled distinctly because
                    // it is the closest thing to a settled statement of what the user wants.
                    is ClarifyTurn.Enough -> "SUMMARY"
                    is ClarifyTurn.UserReply -> "USER"
                }
                appendLine("[$label] ${turn.text}")
            }
        }
    }
}
