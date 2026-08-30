package com.braining.core.domain.history

import com.braining.core.domain.clarify.ClarifyTurn
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * What stands for a finished session, pinned.
 *
 * The design in one line (`docs/M5_DESIGN_NOTE.md` §4): **the engine already wrote the summary**
 * — its `[[كافٍ]]` turn — so nothing has to be paid for to produce one.
 */
class SessionSummaryTest {

    private val idea = "أريد خطة لتعليم ابني القراءة بالعربية"

    @Test
    fun `the enough turn is the summary`() {
        val turns = listOf(
            ClarifyTurn.Question("ما عمر ابنك؟"),
            ClarifyTurn.UserReply("ست سنوات"),
            ClarifyTurn.Enough("خطة قراءة عربية لطفل في السادسة، عملية وقصيرة."),
        )
        assertEquals("خطة قراءة عربية لطفل في السادسة، عملية وقصيرة.", SessionSummary.of(turns, idea))
    }

    @Test
    fun `the last enough turn wins`() {
        // Swapping the framework or answering more questions can produce a second one; the later
        // one describes the idea as it finally stood.
        val turns = listOf(
            ClarifyTurn.Enough("الصياغة الأولى"),
            ClarifyTurn.UserReply("لا، بل هكذا"),
            ClarifyTurn.Enough("الصياغة الأخيرة"),
        )
        assertEquals("الصياغة الأخيرة", SessionSummary.of(turns, idea))
    }

    @Test
    fun `with no enough turn the idea is the summary`() {
        // The user declared the idea mature before the engine ran out of questions. This is
        // routine, not a degraded mode — `BRAINING.md` §2.3 explicitly permits it.
        val turns = listOf(ClarifyTurn.Question("ما عمر ابنك؟"))
        assertEquals(idea, SessionSummary.of(turns, idea))
    }

    @Test
    fun `an interrogation with no turns at all falls back to the idea`() {
        assertEquals(idea, SessionSummary.of(emptyList(), idea))
    }

    @Test
    fun `a blank enough turn does not win over the idea`() {
        val turns = listOf(ClarifyTurn.Enough("   "))
        assertEquals(idea, SessionSummary.of(turns, idea))
    }

    @Test
    fun `a long summary is capped`() {
        val long = "كلمة ".repeat(200)
        val out = SessionSummary.of(listOf(ClarifyTurn.Enough(long)), idea)
        assertTrue(out.length <= SessionSummary.MAX_CHARS + 1)
        assertTrue(out.endsWith("…"))
    }

    @Test
    fun `truncation does not cut a word in half`() {
        // In Arabic a truncated word can be a different, real word — worse than an obvious stump.
        val long = "كلمة ".repeat(200)
        val out = SessionSummary.of(listOf(ClarifyTurn.Enough(long)), idea)
        assertFalse(out.removeSuffix("…").endsWith("كلم"))
        assertTrue(out.removeSuffix("…").trimEnd().endsWith("كلمة"))
    }

    @Test
    fun `newlines are flattened so a summary is one line`() {
        val out = SessionSummary.of(listOf(ClarifyTurn.Enough("سطر\nثانٍ")), idea)
        assertEquals("سطر ثانٍ", out)
    }

    // ── the list title ──────────────────────────────────────────────────────────────────

    @Test
    fun `the title comes from the idea, not the summary`() {
        // The list is how the user finds a session they remember *starting*, and what they
        // remember is what they said.
        assertEquals(idea, SessionSummary.titleOf(idea))
    }

    @Test
    fun `a long idea makes a short title`() {
        val long = "كلمة ".repeat(80)
        val title = SessionSummary.titleOf(long)
        assertTrue(title.length <= 61)
        assertTrue(title.endsWith("…"))
    }

    @Test
    fun `an empty idea makes an empty title rather than an ellipsis`() {
        assertEquals("", SessionSummary.titleOf("   "))
    }
}
