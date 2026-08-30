package com.braining.core.domain.clarify

import com.braining.core.domain.model.AiError
import com.braining.core.domain.provider.AiProvider
import kotlinx.coroutines.flow.Flow

/**
 * The FORGE stage: turn a matured idea into a rigorous **English** prompt.
 *
 * `BRAINING.md` §2.4 and `docs/PROMPT_FRAMEWORKS.md`. Runs only after the user has declared the
 * idea mature — `ClarifyState.READY` is its precondition, and nothing here can reach that state.
 *
 * **Why the model writes the prompt rather than a local template filling a skeleton.** The
 * interrogation is Arabic and the output must be English, so a local template could only
 * concatenate Arabic text into English headings — a document in two languages that instructs
 * nobody. Choosing the framework, translating the matured idea and filling
 * `docs/PROMPT_FRAMEWORKS.md` §4's skeleton are one act of writing, not three mechanical steps.
 */
interface PromptForge {

    /** The catalogue, for the user's swap menu. `docs/PROMPT_FRAMEWORKS.md` §3.7 requires it. */
    val frameworks: List<FrameworkOption>

    /**
     * @param frameworkOverride the user's choice, when they have made one. Null means the model
     *   picks. `ANSWERS.md` Part 2 §6 approved `frameworkOverrides` on the session, and
     *   `docs/PROMPT_FRAMEWORKS.md` §1 is explicit that selection is "a **heuristic**, not a
     *   rigid rule" — this parameter is the whole mechanism of that promise.
     */
    fun forge(
        session: ClarifySession,
        provider: AiProvider,
        model: String,
        frameworkOverride: String? = null,
        diagnostics: Boolean = false,
    ): Flow<ForgeEvent>
}

/** One entry of the library in `feature-clarify/res/raw/prompt_frameworks.json`. */
data class FrameworkOption(
    val id: String,
    val arabicName: String,
    val shape: String,
    val bestFor: String,
    val taskTypes: List<String>,
)

/** The finished article. */
data class ForgedPrompt(
    val frameworkId: String,
    /** One line, **Arabic**: why this framework. `docs/PROMPT_FRAMEWORKS.md` §3.7. */
    val rationale: String,
    /** The prompt itself, **English** by rule (`docs/PROMPT_FRAMEWORKS.md` §5). */
    val english: String,
    /**
     * A short **Arabic** name for this session, for the history list. Empty when the model
     * ignored the marker.
     *
     * **It rides on the forge call rather than costing one of its own** — see
     * `ForgePrompt.TITLE_MARKER`. Empty is a normal outcome, not a failure: the list falls back
     * to the opening of the idea, which is what it showed before this existed.
     */
    val title: String = "",
)

sealed interface ForgeEvent {

    /**
     * The framework and its one-line reason, as soon as they are known — **before** the prompt
     * body finishes streaming.
     *
     * Emitted separately rather than only inside [Completed] because the choice is the first
     * thing the user wants to see and the prompt takes seconds to write. A user watching an
     * English wall of text arrive with no idea why *that* framework was picked has been shown
     * the output and denied the reasoning, which is the opposite of what §3.7 asks for.
     */
    data class FrameworkChosen(val frameworkId: String, val rationale: String) : ForgeEvent

    /** A fragment of the English prompt, token by token. */
    data class Delta(val text: String) : ForgeEvent

    /** Terminal. */
    data class Completed(val prompt: ForgedPrompt) : ForgeEvent

    /** Terminal. Classified, never phrased — the same `AiError` vocabulary as everything else. */
    data class Failed(val error: AiError) : ForgeEvent

    /** Developer Mode capture, already redacted. Same contract as `ClarifyEvent.Meta`. */
    data class Meta(val endpoint: String, val requestBody: String) : ForgeEvent
}
