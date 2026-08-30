package com.braining.core.domain.diagnostics

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Turns a captured request body into something a person can read.
 *
 * ### What this is, and what it is deliberately not
 *
 * Developer Mode shows **the exact bytes sent**, and that is why it has found three real faults
 * in this project. The owner's report of 2026-08-28 is nevertheless correct: what he sees is a
 * single JSON line where every newline is the two characters `\n`, English keys are wrapped
 * around Arabic text, and the whole thing runs together left-to-right.
 *
 * **So this does not replace the raw body — it sits above it.** `PROJECT_STATE.md` §10 entry 6:
 * a diagnostic that is confidently wrong is worse than none, and a prettified view is a *summary*
 * of what was sent, not the thing itself. A missing character would survive this function and
 * die in the raw. Both are on screen; this one is merely the one you read first.
 *
 * ### Why it lives in the domain
 *
 * It is pure string and JSON work, it has four provider shapes to get right, and getting one
 * wrong shows the user someone else's prompt. That is exactly the kind of logic §0 rule 11 wants
 * in `:core-domain` where a unit test can reach it without a phone, a key or a network.
 *
 * **It never throws.** A body it cannot parse — a truncated capture, a provider whose shape
 * changes — comes back as a single [Kind.RAW] section holding the original text, which is
 * strictly better than an empty panel that looks like nothing was captured.
 */
object PromptPreview {

    enum class Kind { MODEL, SYSTEM, USER, ASSISTANT, RAW }

    data class Section(
        val kind: Kind,
        val text: String,
        /**
         * Characters dropped from the end of [text] by the per-section cap, or 0.
         *
         * Reported rather than hidden: a preview that silently shortens is a preview that can
         * hide the very line being looked for, and the reader must know to open the raw body.
         */
        val truncated: Int = 0,
    )

    /**
     * The cap per section.
     *
     * CLARIFY's system prompt is several thousand characters. Showing all of it turns the panel
     * into the wall of text it is meant to replace; showing none of it hides the thing the panel
     * exists for. The first ~1500 characters carry the rules that are ever actually in doubt, and
     * the raw body is one tap away for the rest.
     */
    const val MAX_SECTION_CHARS = 1500

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    fun of(requestBody: String): List<Section> {
        val body = requestBody.trim()
        if (body.isEmpty()) return emptyList()

        val root = runCatching { json.parseToJsonElement(body).jsonObject }.getOrNull()
            ?: return listOf(Section(Kind.RAW, body))

        val out = mutableListOf<Section>()

        root["model"]?.let { m ->
            runCatching { m.jsonPrimitive.contentOrNull }.getOrNull()
                ?.let { out += Section(Kind.MODEL, it) }
        }

        // Anthropic puts the system prompt in a top-level `system`, which is a string on some
        // versions and a list of content blocks on others. Both shapes appear in the wild.
        root["system"]?.let { out += section(Kind.SYSTEM, flatten(it)) }

        // Gemini names the same thing `systemInstruction`, wrapped in `parts`.
        root["systemInstruction"]?.let { out += section(Kind.SYSTEM, flatten(it)) }

        // OpenAI, DeepSeek and Anthropic all use `messages` with a role per entry.
        root["messages"]?.let { messages ->
            runCatching { messages.jsonArray }.getOrNull()?.forEach { entry ->
                val obj = runCatching { entry.jsonObject }.getOrNull() ?: return@forEach
                val role = obj["role"]?.let {
                    runCatching { it.jsonPrimitive.contentOrNull }.getOrNull()
                }
                out += section(kindOf(role), flatten(obj["content"]))
            }
        }

        // Gemini's own name for the conversation.
        root["contents"]?.let { contents ->
            runCatching { contents.jsonArray }.getOrNull()?.forEach { entry ->
                val obj = runCatching { entry.jsonObject }.getOrNull() ?: return@forEach
                val role = obj["role"]?.let {
                    runCatching { it.jsonPrimitive.contentOrNull }.getOrNull()
                }
                out += section(kindOf(role), flatten(obj))
            }
        }

        // **Blank sections are kept, and that is the point of the file.**
        //
        // The first version filtered them out, so a genuinely empty message — or one whose shape
        // this reader does not understand — simply disappeared and the panel showed N-1 turns
        // with nothing to say one was missing. That is the exact failure this object's own KDoc
        // forbids: never lie about what was sent. A section that renders as empty tells the truth;
        // a section that is not there tells a different story.
        //
        // The whole-body fallback still applies when *nothing* was recognised, because an empty
        // panel reads as "no capture arrived", which is a separate and wrong claim.
        return out.ifEmpty { listOf(Section(Kind.RAW, body)) }
    }

    private fun kindOf(role: String?): Kind = when (role?.lowercase()) {
        "system" -> Kind.SYSTEM
        "assistant", "model" -> Kind.ASSISTANT
        else -> Kind.USER
    }

    private fun section(kind: Kind, text: String): Section {
        val clean = tidy(text)
        return if (clean.length <= MAX_SECTION_CHARS) {
            Section(kind, clean)
        } else {
            Section(kind, clean.take(MAX_SECTION_CHARS), clean.length - MAX_SECTION_CHARS)
        }
    }

    /**
     * Pull the text out of whatever container the provider used.
     *
     * A plain string, a `{"text": …}`, a `{"parts": [...]}`, or a list of any of those. Recursive
     * because Gemini nests two levels and Anthropic's block form nests one — writing four
     * separate readers would be four places for the same shape to be handled differently.
     */
    private fun flatten(element: kotlinx.serialization.json.JsonElement?): String {
        if (element == null) return ""
        return when (element) {
            is JsonArray -> element.joinToString("\n") { flatten(it) }
            is JsonObject -> {
                element["text"]?.let { return flatten(it) }
                element["content"]?.let { return flatten(it) }
                element["parts"]?.let { return flatten(it) }
                ""
            }
            else -> runCatching { element.jsonPrimitive.contentOrNull }.getOrNull().orEmpty()
        }
    }

    /**
     * Make the text readable without changing what it says.
     *
     * Three passes, and each is deliberately conservative — **this is a view of evidence, so
     * nothing here may invent, reorder or merge a line.** Runs of blank lines collapse to one,
     * trailing spaces go, and markdown emphasis markers are dropped because `**` around an Arabic
     * word is noise on a phone and carries no meaning the reader of a prompt needs.
     *
     * Sentences are **not** re-wrapped. The prompts already put one rule per line; splitting on
     * «.» would break decimals, abbreviations and Arabic punctuation, and a preview that mangles
     * its source is the diagnostic §10 entry 6 warns about.
     */
    private fun tidy(text: String): String = text
        .replace("\r\n", "\n")
        .replace("**", "")
        .lineSequence()
        .map { it.trimEnd() }
        .fold(mutableListOf<String>()) { acc, line ->
            if (line.isBlank() && acc.lastOrNull()?.isBlank() == true) acc else acc.apply { add(line) }
        }
        .joinToString("\n")
        .trim()
}
