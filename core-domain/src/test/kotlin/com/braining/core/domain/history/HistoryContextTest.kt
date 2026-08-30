package com.braining.core.domain.history

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The block that rides on every Clarify turn, pinned.
 *
 * Three properties are load-bearing and each has a check below: **nothing at all when there is
 * no history**, **a budget that binds**, and **the prohibitions that stop background becoming the
 * subject** (`PROJECT_STATE.md` §10 entry 32).
 */
class HistoryContextTest {

    private fun record(summary: String, idea: String = "فكرة", id: Long = 1) = SessionRecord(
        id = id,
        createdAt = 0L,
        idea = idea,
        summary = summary,
    )

    @Test
    fun `no history produces nothing at all`() {
        // Not a heading with no entries under it. A model shown "previous sessions:" followed by
        // silence will reason about the silence.
        assertEquals("", HistoryContext.build(emptyList()))
    }

    @Test
    fun `records with nothing to say produce nothing`() {
        assertEquals("", HistoryContext.build(listOf(record(summary = "", idea = "   "))))
    }

    @Test
    fun `a single session produces a block containing its summary`() {
        val out = HistoryContext.build(listOf(record("خطة قراءة لطفل في السادسة")))
        assertTrue(out.contains("خطة قراءة لطفل في السادسة"))
    }

    @Test
    fun `a record with no summary falls back to its idea`() {
        val out = HistoryContext.build(listOf(record(summary = "", idea = "فكرة عن مشروع صغير")))
        assertTrue(out.contains("فكرة عن مشروع صغير"))
    }

    @Test
    fun `no more than MAX_SESSIONS reach the model`() {
        val many = (1..20).map { record("ملخص رقم $it", id = it.toLong()) }
        val out = HistoryContext.build(many)
        val entries = out.lines().count { it.startsWith("- ") }
        assertEquals(HistoryContext.MAX_SESSIONS, entries)
    }

    @Test
    fun `the newest survive the cap, not an arbitrary prefix`() {
        // Callers pass newest-first. The newest sessions say the most about who is speaking now.
        val many = (1..20).map { record("ملخص رقم $it", id = it.toLong()) }
        val out = HistoryContext.build(many)
        assertTrue(out.contains("ملخص رقم 1"))
        assertFalse(out.contains("ملخص رقم 20"))
    }

    @Test
    fun `the block respects its character budget`() {
        val fat = (1..5).map { record("ملخص طويل جداً ".repeat(60), id = it.toLong()) }
        val out = HistoryContext.build(fat)
        assertTrue(
            "block was ${out.length} chars, budget is ${HistoryContext.MAX_CHARS}",
            out.length <= HistoryContext.MAX_CHARS + SessionSummary.MAX_CHARS,
        )
    }

    @Test
    fun `one oversized summary still produces a block rather than nothing`() {
        // The budget must never silence history entirely: the first entry is always kept, or a
        // user whose last session was long would get no context at all and never know why.
        val out = HistoryContext.build(listOf(record("ملخص ".repeat(500))))
        assertTrue(out.isNotEmpty())
        assertEquals(1, out.lines().count { it.startsWith("- ") })
    }

    @Test
    fun `the prohibitions ship with the block`() {
        val out = HistoryContext.build(listOf(record("ملخص")))
        // Background that is merely present reads as background that is relevant. These three
        // sentences are what stop five old topics becoming the subject of a new question.
        assertTrue(out.contains("لا تفترض أن الفكرة الجديدة امتدادٌ لجلسة سابقة"))
        assertTrue(out.contains("لا تقتبس"))
        assertTrue(out.contains("خلفية تُسكِت أسئلة"))
    }

    @Test
    fun `the block is numbered ten so it cannot collide with an existing rule`() {
        // `ClarifyPrompt`'s rules run to ٨ and the profile block is ٩. Its other rules refer to
        // each other by number, so a collision would make one of those references ambiguous.
        val out = HistoryContext.build(listOf(record("ملخص")))
        assertTrue(out.trimStart().startsWith("١٠."))
    }
}
