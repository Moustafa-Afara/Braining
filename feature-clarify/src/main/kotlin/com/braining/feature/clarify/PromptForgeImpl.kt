package com.braining.feature.clarify

import android.content.Context
import com.braining.ai.providers.toAiError
import com.braining.core.domain.clarify.ClarifySession
import com.braining.core.domain.clarify.ForgeEvent
import com.braining.core.domain.clarify.ForgedPrompt
import com.braining.core.domain.clarify.FrameworkOption
import com.braining.core.domain.clarify.PromptForge
import com.braining.core.domain.model.AiChunk
import com.braining.core.domain.model.AiError
import com.braining.core.domain.model.AiRequest
import com.braining.core.domain.model.ChatMessage
import com.braining.core.domain.model.MessageRole
import com.braining.core.domain.provider.AiProvider
import com.braining.core.domain.history.HistoryContext
import com.braining.core.domain.history.SessionRepository
import com.braining.core.domain.store.AppPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

/**
 * Reads the framework library, asks the model to pick and write, and parses what comes back.
 *
 * **Unscoped, like `ClarifyEngineImpl`** — see `ClarifyModule`. It holds no session state today,
 * but it is created beside one that does, and a `@Singleton` here would be an invitation to add
 * state later without noticing the change in lifetime.
 */
class PromptForgeImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appPreferences: AppPreferences,
    /** M5. Summaries only, read once per forge. See `ClarifyEngineImpl`'s field of the same name. */
    private val sessions: SessionRepository,
) : PromptForge {

    /**
     * Loaded once, lazily, off `res/raw`.
     *
     * `docs/PROMPT_FRAMEWORKS.md` §5: "keep the library data-driven so new frameworks can be
     * added without code changes". A `when` over framework names in Kotlin would have broken that
     * on the first day, and this project already knows what a value duplicated across files costs
     * (`2026-08-03-A`: one model name in three places, wrong in all three at once).
     *
     * **A malformed file must not be a crash.** It ships inside the APK so it cannot be corrupted
     * in the field — but it can be mistyped by whoever edits it next, which is the whole point of
     * it being editable. An empty list produces a forge that still runs and asks the model to
     * choose freely, which is a degraded result rather than a dead screen.
     */
    override val frameworks: List<FrameworkOption> by lazy {
        runCatching {
            val raw = context.resources.openRawResource(R.raw.prompt_frameworks)
                .bufferedReader()
                .use { it.readText() }

            Json.parseToJsonElement(raw).jsonObject["frameworks"]!!.jsonArray.map { element ->
                val obj = element.jsonObject
                FrameworkOption(
                    id = obj.str("id"),
                    arabicName = obj.str("arabicName"),
                    shape = obj.str("shape"),
                    bestFor = obj.str("bestFor"),
                    taskTypes = obj["taskTypes"]?.jsonArray
                        ?.mapNotNull { it.jsonPrimitive.contentOrNull }
                        .orEmpty(),
                )
            }
        }.getOrDefault(emptyList())
    }

    /**
     * `contentOrNull`, never `.content`.
     *
     * A JSON `null` parses to `JsonNull`, which is a real non-null `JsonPrimitive`, so `.content`
     * hands back the four-character string `"null"` and every safe-call operator sails past it.
     * That cost this project a day in `2026-08-03-C` and printed `nullnullnull…` in front of a
     * real answer on the owner's device. The same trap is still one keystroke away here.
     */
    private fun kotlinx.serialization.json.JsonObject.str(key: String): String =
        this[key]?.jsonPrimitive?.contentOrNull.orEmpty()

    override fun forge(
        session: ClarifySession,
        provider: AiProvider,
        model: String,
        frameworkOverride: String?,
        diagnostics: Boolean,
    ): Flow<ForgeEvent> = flow {
        // Same note the interrogation reads. FORGE gets it because the prompt it writes has to
        // decide who is being addressed and at what level — `ANSWERS.md` Part 8 §D3.
        val profile = appPreferences.userProfile.first()
        // Once per forge, not per turn — the forge runs once. Same source as CLARIFY's, so the
        // prompt is written with the context the interrogation was conducted under rather than
        // contradicting it.
        val history = HistoryContext.build(sessions.recent(HistoryContext.MAX_SESSIONS))

        val request = AiRequest(
            model = model,
            messages = listOf(
                ChatMessage(role = MessageRole.USER, content = ForgePrompt.material(session)),
            ),
            systemPrompt = ForgePrompt.system(frameworks, frameworkOverride, profile, history),
            // A filled skeleton is longer than a Clarify turn and longer than a chat reply. The
            // default 4096 truncates a thorough OUTPUT CONTRACT mid-sentence, and a prompt that
            // stops halfway is worse than a short one because it looks finished.
            maxTokens = 8192,
            diagnostics = diagnostics,
        )

        val reader = ForgeReader()

        provider.complete(request)
            .catch { cause ->
                if (cause is CancellationException) throw cause
                emit(AiChunk.Error(cause.toAiError(provider.id)))
            }
            .collect { chunk ->
                when (chunk) {
                    is AiChunk.Token -> {
                        val body = reader.feed(chunk.text)
                        if (reader.justChoseFramework) {
                            emit(
                                ForgeEvent.FrameworkChosen(
                                    frameworkId = reader.frameworkId,
                                    rationale = reader.rationale,
                                ),
                            )
                        }
                        if (body.isNotEmpty()) emit(ForgeEvent.Delta(body))
                    }

                    is AiChunk.Done -> {
                        val tail = reader.flush()
                        if (tail.isNotEmpty()) emit(ForgeEvent.Delta(tail))
                        val english = reader.body.trim()
                        if (english.isEmpty()) {
                            // Same guard as an empty Clarify turn, and the same history behind
                            // it: `2026-08-03-D`, HTTP 200 with no answer because reasoning
                            // consumed the whole token budget. A blank prompt on screen would
                            // look like a finished one.
                            emit(
                                ForgeEvent.Failed(
                                    AiError.Unknown(provider.id, null, "empty forged prompt"),
                                ),
                            )
                        } else {
                            emit(
                                ForgeEvent.Completed(
                                    ForgedPrompt(
                                        frameworkId = reader.frameworkId,
                                        rationale = reader.rationale,
                                        english = english,
                                        title = reader.title,
                                    ),
                                ),
                            )
                        }
                    }

                    is AiChunk.Error -> emit(ForgeEvent.Failed(chunk.error))

                    is AiChunk.Meta -> emit(ForgeEvent.Meta(chunk.endpoint, chunk.requestBody))
                }
            }
    }

}

/**
 * Splits the model's answer into framework · rationale · prompt.
 *
 * Line-oriented rather than JSON, for the reason `TurnKind` gives: a JSON envelope cannot be
 * shown until it closes, and the English prompt is the longest thing this app streams. The two
 * header lines are buffered — a few dozen characters — and everything after `[[PROMPT]]` flows
 * straight through.
 *
 * **It cannot stall and it cannot lose the prompt.** If the markers never appear, [flush] releases
 * the whole buffer as the prompt body and the framework reads "unknown". A model that ignored the
 * format still produced something worth showing, and `docs/PROMPT_FRAMEWORKS.md` §3.7 makes the
 * result editable anyway.
 */
private class ForgeReader {

    private val header = StringBuilder()
    private var inBody = false
    private val buffer = StringBuilder()

    /**
     * The prompt accumulated so far.
     *
     * A `String` getter and **not** the `StringBuilder` itself. Exposing the builder compiled
     * fine until a caller wrote `reader.body.trim()` and got back a `CharSequence` — because
     * `trim()` on a `StringBuilder` resolves to the `CharSequence` extension, not the `String`
     * one. That is a type error in the call site for a mistake in the API, and the fix belongs
     * here: hand out the finished value, not the thing still being built.
     */
    val body: String get() = buffer.toString()

    var frameworkId: String = UNKNOWN
        private set

    var rationale: String = ""
        private set

    /**
     * The session's name. Empty when the model omitted the marker — a normal outcome the caller
     * falls back from, never an error.
     */
    var title: String = ""
        private set

    /** True for exactly one [feed] — the one that crossed into the prompt body. */
    var justChoseFramework: Boolean = false
        private set

    fun feed(text: String): String {
        justChoseFramework = false
        if (inBody) {
            buffer.append(text)
            return text
        }

        header.append(text)
        val marker = header.indexOf(ForgePrompt.PROMPT_MARKER)
        if (marker < 0) {
            // Nothing to release yet. The cap stops a model that never emits the marker from
            // buffering its entire answer into memory before anything appears.
            if (header.length < CAP) return ""
            return crossIntoBody(header.toString(), rest = "", markerFound = false)
        }
        return crossIntoBody(
            headerText = header.substring(0, marker),
            rest = header.substring(marker + ForgePrompt.PROMPT_MARKER.length),
            markerFound = true,
        )
    }

    fun flush(): String {
        if (inBody) return ""
        return crossIntoBody(header.toString(), rest = "", markerFound = false)
    }

    /**
     * @param markerFound whether `[[PROMPT]]` was actually seen. **This is the whole difference
     *   between discarding a header and eating the answer.** When the marker is present, the text
     *   before it is genuinely header and is dropped. When it never came — a model that ignored
     *   the format — that same text is the prompt, and dropping it would silently delete the first
     *   400 characters of every such answer while looking like a model quirk. The identical trap
     *   was written and caught once already in `HeaderReader` (`2026-08-07-C`); writing it twice
     *   in two days is why it is spelled out here rather than left to the reader.
     */
    private fun crossIntoBody(headerText: String, rest: String, markerFound: Boolean): String {
        parseHeader(headerText)
        inBody = true
        justChoseFramework = true
        header.clear()

        val salvaged = if (markerFound) {
            ""
        } else {
            headerText.lineSequence()
                .filterNot { line ->
                    val t = line.trim()
                    t.startsWith(ForgePrompt.FRAMEWORK_MARKER) ||
                        t.startsWith(ForgePrompt.RATIONALE_MARKER) ||
                        t.startsWith(ForgePrompt.TITLE_MARKER)
                }
                .joinToString("\n")
        }

        val opening = (salvaged + rest).trimStart('\n', ' ')
        buffer.append(opening)
        return opening
    }

    private fun parseHeader(text: String) {
        text.lineSequence().forEach { line ->
            val trimmed = line.trim()
            when {
                trimmed.startsWith(ForgePrompt.FRAMEWORK_MARKER) ->
                    frameworkId = trimmed.removePrefix(ForgePrompt.FRAMEWORK_MARKER).trim()
                        .ifBlank { UNKNOWN }

                trimmed.startsWith(ForgePrompt.RATIONALE_MARKER) ->
                    rationale = trimmed.removePrefix(ForgePrompt.RATIONALE_MARKER).trim()

                trimmed.startsWith(ForgePrompt.TITLE_MARKER) ->
                    // Trailing punctuation stripped here rather than trusted to the prompt: a
                    // title is a label, and «خطة قراءة.» in a list reads as a truncated sentence.
                    title = trimmed.removePrefix(ForgePrompt.TITLE_MARKER)
                        .trim()
                        .trim('.', '،', '"', '«', '»')
                        .trim()
            }
        }
    }

    private companion object {
        const val UNKNOWN = "—"

        /** Two header lines plus slack. Beyond this the model is not following the format. */
        const val CAP = 400
    }
}
