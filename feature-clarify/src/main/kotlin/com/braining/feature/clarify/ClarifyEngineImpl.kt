package com.braining.feature.clarify

import com.braining.ai.providers.toAiError
import com.braining.core.domain.clarify.ClarifyEngine
import com.braining.core.domain.clarify.ClarifyEvent
import com.braining.core.domain.clarify.ClarifySession
import com.braining.core.domain.clarify.ClarifyState
import com.braining.core.domain.clarify.ClarifyTurn
import com.braining.core.domain.clarify.TurnKind
import com.braining.core.domain.history.HistoryContext
import com.braining.core.domain.history.SessionRepository
import com.braining.core.domain.model.AiChunk
import com.braining.core.domain.model.AiError
import com.braining.core.domain.model.AiRequest
import com.braining.core.domain.provider.AiProvider
import com.braining.core.domain.store.AppPreferences
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Drives the CLARIFY state machine over whichever [AiProvider] the caller hands it.
 *
 * **Not `@Singleton`, and that is load-bearing.** This object holds the session. A singleton
 * would outlive the ViewModel that opened it, so the next interrogation would begin with the
 * previous one's turns still attached — and `ANSWERS.md` Part 7 §M3-4 rules that a session does
 * not survive its owner in M3. The Hilt binding is deliberately unscoped; see `ClarifyModule`.
 */
class ClarifyEngineImpl @Inject constructor(
    private val appPreferences: AppPreferences,
    /**
     * M5. Read for one thing only: the last few session **summaries**, which become the context
     * that stops the engine asking questions it has no business asking (`ANSWERS.md` Part 11 §K1).
     *
     * The engine never writes here. Saving a finished run belongs to `ClarifyViewModel`, which is
     * the only object that knows an answer completed — this one's job ends when the interrogation
     * does.
     */
    private val sessions: SessionRepository,
) : ClarifyEngine {

    override var session: ClarifySession = ClarifySession(originalIdea = "")
        private set

    override fun open(
        idea: String,
        provider: AiProvider,
        model: String,
        diagnostics: Boolean,
    ): Flow<ClarifyEvent> {
        session = ClarifySession(originalIdea = idea, state = ClarifyState.ANALYZING)
        return runTurn(provider, model, diagnostics)
    }

    override fun reply(
        text: String,
        provider: AiProvider,
        model: String,
        diagnostics: Boolean,
    ): Flow<ClarifyEvent> {
        session = session.copy(
            turns = session.turns + ClarifyTurn.UserReply(text),
            state = ClarifyState.ANALYZING,
        )
        return runTurn(provider, model, diagnostics)
    }

    /**
     * Re-ask the current turn. See [ClarifyEngine.resume].
     *
     * `session.copy(state = …)` and **not** `session = ClarifySession(...)`: every turn already
     * recorded stays exactly where it was, and only the state moves back to ANALYZING so the
     * screen shows a request in flight again.
     */
    override fun resume(
        provider: AiProvider,
        model: String,
        diagnostics: Boolean,
    ): Flow<ClarifyEvent> {
        session = session.copy(state = ClarifyState.ANALYZING)
        return runTurn(provider, model, diagnostics)
    }

    /**
     * The user declared the idea mature.
     *
     * No network, no provider, no failure path. `BRAINING.md` §2.3 makes this the user's alone,
     * and a version that could fail would mean a dropped connection refusing to let someone
     * finish thinking.
     */
    override fun declareReady() {
        session = session.copy(state = ClarifyState.READY)
    }

    private fun runTurn(
        provider: AiProvider,
        model: String,
        diagnostics: Boolean,
    ): Flow<ClarifyEvent> = flow {
        emit(ClarifyEvent.StateChanged(ClarifyState.ANALYZING))

        // Read per turn rather than injected once, because the note can be edited in Settings
        // between two turns of the same interrogation. `first()` on a StateFlow returns the
        // value that is already there — §10 entry 21 is about a flow still warming up, and this
        // one is warm at construction.
        val profile = appPreferences.userProfile.first()

        // Read per turn, like the profile, and for the same reason: a session finished in another
        // tab between two turns of this one should be visible to the next question. It is a local
        // database read of at most five rows — cheaper than the request it is about to ride on.
        // A failure here costs the context, never the turn (`SessionRepositoryImpl` never throws).
        val history = HistoryContext.build(sessions.recent(HistoryContext.MAX_SESSIONS))

        val request = AiRequest(
            model = model,
            messages = session.toProviderHistory(),
            // The path that has existed since M1 and has never been used: all four providers
            // already read this field, and Anthropic correctly maps it to the top-level
            // `system` parameter — the only place `/v1/messages` accepts it. Putting these
            // instructions in `messages` as a SYSTEM role would be rejected there.
            systemPrompt = ClarifyPrompt.system(profile, history),
            // Gated on Developer Mode, never on by default: capture means holding the whole
            // prompt in memory for the life of the session, which `AiRequest.diagnostics`'s own
            // KDoc calls pointless for an ordinary user and a needlessly wider surface for a key
            // to be exposed on.
            diagnostics = diagnostics,
        )

        // Reads the marker on the first line, then gets out of the way. Everything after the
        // header streams through untouched.
        val header = HeaderReader()
        val body = StringBuilder()

        provider.complete(request)
            .catch { cause ->
                // Cancellation is the user leaving the screen or pressing stop. It is NOT a
                // failure, and swallowing it here would show an error card for an action the
                // user deliberately took — the defect already fixed once in `ChatViewModel`
                // and once in `BaseHttpProvider`. Rethrow first, classify second.
                if (cause is CancellationException) throw cause

                // Transport failures arrive here, exactly as they do in ChatViewModel. The
                // `catch` in BaseHttpProvider was deleted in 2026-08-03-E precisely so socket
                // errors would reach a collector that can classify them.
                emit(AiChunk.Error(cause.toAiError(provider.id)))
            }
            .collect { chunk ->
                when (chunk) {
                    is AiChunk.Token -> {
                        val out = header.feed(chunk.text)
                        if (header.justResolved) {
                            emit(ClarifyEvent.StateChanged(header.kind.toState()))
                        }
                        if (out.isNotEmpty()) {
                            body.append(out)
                            emit(ClarifyEvent.Delta(out))
                        }
                    }

                    is AiChunk.Done -> {
                        val tail = header.flush()
                        if (tail.isNotEmpty()) {
                            body.append(tail)
                            emit(ClarifyEvent.Delta(tail))
                        }
                        val text = body.toString().trim()

                        // An empty turn is a failure, not a turn. This is not defensive
                        // padding: `2026-08-03-D` is a whole entry about DeepSeek returning
                        // HTTP 200, one chunk and **no answer** after 38 seconds, because
                        // thinking tokens had consumed the entire `max_tokens` budget. Appending
                        // a blank turn would put an empty bubble on screen and leave the machine
                        // waiting for the user to answer nothing.
                        if (text.isEmpty()) {
                            emit(
                                ClarifyEvent.Failed(
                                    AiError.Unknown(
                                        provider = provider.id,
                                        status = null,
                                        detail = "empty clarify turn",
                                    ),
                                ),
                            )
                            return@collect
                        }

                        val turn = header.kind.toTurn(text)
                        session = session.copy(
                            turns = session.turns + turn,
                            state = ClarifyState.AWAITING_USER_DECISION,
                        )
                        emit(ClarifyEvent.StateChanged(ClarifyState.AWAITING_USER_DECISION))
                        emit(ClarifyEvent.TurnCompleted(turn))
                    }

                    is AiChunk.Error -> emit(ClarifyEvent.Failed(chunk.error))

                    // Arrives before the first token, and only under Developer Mode. Passed
                    // straight up: the interrogation's system prompt appears on no screen, so
                    // this is the only way a human ever sees what was actually asked.
                    is AiChunk.Meta -> emit(
                        ClarifyEvent.Meta(chunk.endpoint, chunk.requestBody),
                    )
                }
            }
    }
}

private fun TurnKind.toState(): ClarifyState = when (this) {
    TurnKind.QUESTION -> ClarifyState.ASKING
    // ENOUGH is not a state of its own. The machine still rests at AWAITING_USER_DECISION when
    // the turn ends, because the user is exactly who is now being waited on.
    TurnKind.SUGGESTION, TurnKind.CAVEAT, TurnKind.ENOUGH -> ClarifyState.SUGGESTING
}

private fun TurnKind.toTurn(text: String): ClarifyTurn = when (this) {
    TurnKind.QUESTION -> splitOptions(text).let { (body, options) ->
        ClarifyTurn.Question(body, options)
    }
    TurnKind.SUGGESTION -> ClarifyTurn.Suggestion(text)
    TurnKind.CAVEAT -> ClarifyTurn.Caveat(text)
    TurnKind.ENOUGH -> ClarifyTurn.Enough(text)
}

/**
 * Pulls a trailing bullet list off a question and returns it as tappable options.
 *
 * **Plain `- ` bullets rather than another `[[…]]` marker, and that is the whole design.** The
 * options arrive token by token like everything else, and a marker would sit visibly in the
 * stream as `[[خيار]] نعم` for a second before the turn completed and it could be stripped.
 * A bullet list *reads correctly while it is still arriving* and needs no cleanup — the parse
 * happens at the end, and until then the user is looking at a perfectly normal list.
 *
 * **Only trailing, only consecutive, only in a question, only 2 to [MAX_OPTIONS].** One bullet is
 * not a choice; a long list is a form. And a bullet in the middle of a question is prose, not an
 * option — a model listing considerations mid-sentence must not have them turned into buttons.
 *
 * When the shape does not match, the text is returned untouched and the question is simply open.
 * **Failing to a plain question is always safe**; the text field is there in every case.
 */
private fun splitOptions(text: String): Pair<String, List<String>> {
    val lines = text.lines()
    val options = ArrayDeque<String>()
    var end = lines.size

    while (end > 0) {
        val line = lines[end - 1].trim()
        if (line.isEmpty()) {
            end--
            continue
        }
        val option = stripBullet(line) ?: break
        options.addFirst(option)
        end--
    }

    if (options.size !in 2..MAX_OPTIONS || options.any { it.isBlank() }) return text to emptyList()
    return lines.subList(0, end).joinToString("\n").trim() to options.toList()
}

/**
 * Returns the option text if [line] is a dashed list item, or null if it is anything else.
 *
 * **Dashes only — numbers are deliberately NOT accepted, and that reverses a change made hours
 * earlier.** `2026-08-07-P` widened this to `١.` and `1)` because models write numbers unprompted
 * and no options were appearing. The owner's ruling of the same day then made a *batch of
 * questions* a first-class turn shape, and batches are numbered. Accepting numbers here would
 * turn three questions into three answer buttons — a far worse failure than no buttons at all.
 *
 * **So the two shapes are now split by syntax:** numbers mean questions, dashes mean answers. The
 * prompt says so explicitly, and Developer Mode reports what the model actually wrote when the
 * buttons do not appear, so the next fix is made from evidence rather than from another guess.
 */
private fun stripBullet(line: String): String? =
    BULLETS.firstOrNull { line.startsWith(it) }?.let { line.removePrefix(it).trim() }

private val BULLETS = listOf("- ", "– ", "— ", "• ", "* ")
private const val MAX_OPTIONS = 4

/**
 * Consumes the marker line at the head of a streamed turn and passes the rest straight through.
 *
 * **Why buffering at all, when buffering is what streaming exists to avoid.** The marker tells
 * the UI whether it is looking at a question, a suggestion or a caveat, and it has to be read
 * before it is shown — otherwise `[[سؤال]]` flashes on screen for a few hundred milliseconds and
 * then vanishes. The buffer is bounded to the length of the longest marker plus a little slack,
 * so the delay is a handful of characters, not a turn.
 *
 * **It cannot stall.** If the model forgets the marker entirely, [CAP] releases the buffer and
 * the kind falls back to [TurnKind.QUESTION] — the commonest turn, and the one whose affordance
 * (an input field) is right even when the guess is wrong. A hard parse would have made a missing
 * marker a dead screen, which is the trade `docs/M3_DESIGN_NOTE.md` §3.2 refuses.
 */
private class HeaderReader {

    private val buffer = StringBuilder()
    private var resolved = false

    var kind: TurnKind = TurnKind.QUESTION
        private set

    /** True for exactly one [feed] call — the one that decided the kind. */
    var justResolved: Boolean = false
        private set

    fun feed(text: String): String {
        justResolved = false
        if (resolved) return text

        buffer.append(text)
        val newline = buffer.indexOf("\n")
        if (newline < 0 && buffer.length < CAP) return ""

        return resolve(
            head = if (newline >= 0) buffer.substring(0, newline) else buffer.toString(),
            rest = if (newline >= 0) buffer.substring(newline + 1) else "",
        )
    }

    /** The stream ended. Release whatever is still held, whether or not a marker was seen. */
    fun flush(): String {
        if (resolved) return ""
        val newline = buffer.indexOf("\n")
        return resolve(
            head = if (newline >= 0) buffer.substring(0, newline) else buffer.toString(),
            rest = if (newline >= 0) buffer.substring(newline + 1) else "",
        )
    }

    private fun resolve(head: String, rest: String): String {
        kind = TurnKind.fromMarker(head)
        resolved = true
        justResolved = true

        // Strip the marker, not the line.
        //
        // The first version dropped the whole head whenever a marker was found, which is
        // correct only when the model obeys rule 6 and puts the marker alone on its own line.
        // When it writes `[[سؤال]] ما هو هدفك من هذا؟` on ONE line, [CAP] fires before any
        // newline arrives and the head is 32 characters of real question — so dropping it
        // would silently eat the opening of every turn the model formatted slightly wrong.
        // Removing only the marker is right in both cases.
        val trimmedHead = head.trimStart()
        val matched = TurnKind.entries.firstOrNull { trimmedHead.startsWith(it.marker) }
        val salvaged = if (matched != null) trimmedHead.removePrefix(matched.marker) else head

        buffer.clear()
        return (salvaged + rest).trimStart('\n', ' ')
    }

    private companion object {
        /** Longest marker is 9 characters; 32 leaves room for stray whitespace. */
        const val CAP = 32
    }
}
