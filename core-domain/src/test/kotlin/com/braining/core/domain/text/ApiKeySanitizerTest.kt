package com.braining.core.domain.text

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The key repair, pinned — and the first test in this project written from a real incident with
 * the provider's own error message in hand.
 *
 * 2026-08-30: Google refused the owner's key with `Unexpected char 0x2014 at 37`. `0x2014` is an
 * em dash where a hyphen belonged. Every case below is a way that damage happens.
 */
class ApiKeySanitizerTest {

    // ── the incident itself ─────────────────────────────────────────────────────────────

    @Test
    fun `the em dash that broke Gemini becomes a hyphen`() {
        val broken = "AQ.Ab8RN6LnfQodqvGvR_xkd_vy8rtXChdC73—zPzW299m"
        val out = ApiKeySanitizer.sanitize(broken)
        assertFalse(out.key.contains('—'))
        assertTrue(out.key.contains("C73-zPzW"))
        assertTrue(out.repaired)
    }

    @Test
    fun `the repair names the character and where it was`() {
        // Without the position, "I fixed something" is not a report — the user cannot check it.
        val out = ApiKeySanitizer.sanitize("ab—cd")
        val fix = out.fixes.filterIsInstance<ApiKeySanitizer.Fix.Replaced>().single()
        assertEquals('—', fix.from)
        assertEquals('-', fix.to)
        assertEquals(2, fix.at)
    }

    @Test
    fun `every dash look-alike folds to a plain hyphen`() {
        for (dash in listOf('—', '–', '‒', '―', '‑', '−', '­')) {
            assertEquals("a-b", ApiKeySanitizer.sanitize("a${dash}b").key)
        }
    }

    // ── the invisible ones, which nobody finds by reading ───────────────────────────────

    @Test
    fun `a right-to-left mark is removed`() {
        // The Arabic-first hazard: copying near Arabic text drags one of these along, and the
        // key then looks pixel-identical to a correct one in every font.
        val out = ApiKeySanitizer.sanitize("sk-abc‏def")
        assertEquals("sk-abcdef", out.key)
        assertTrue(out.fixes.any { it is ApiKeySanitizer.Fix.RemovedInvisible })
    }

    @Test
    fun `zero-width characters and the byte-order mark are removed`() {
        assertEquals("abcd", ApiKeySanitizer.sanitize("﻿ab​c‍d").key)
    }

    @Test
    fun `bidi isolates and overrides are removed`() {
        assertEquals("abc", ApiKeySanitizer.sanitize("⁦a‮b⁩c").key)
    }

    // ── ordinary paste damage ───────────────────────────────────────────────────────────

    @Test
    fun `surrounding whitespace and newlines go`() {
        assertEquals("sk-abc", ApiKeySanitizer.sanitize("  sk-abc\n").key)
    }

    @Test
    fun `a line-wrapped paste is rejoined`() {
        assertEquals("sk-abcdef", ApiKeySanitizer.sanitize("sk-abc\ndef").key)
    }

    @Test
    fun `an interior line break is not reported as a repair`() {
        // It is a line break, not a corrupted character. Reporting it would bury the ones that
        // actually changed a value.
        assertTrue(ApiKeySanitizer.sanitize("sk-abc def").fixes.isEmpty())
    }

    @Test
    fun `curly quotes become straight ones`() {
        assertEquals("a\"b'c", ApiKeySanitizer.sanitize("a“b’c").key)
    }

    @Test
    fun `arabic-indic digits become western ones`() {
        assertEquals("key2026", ApiKeySanitizer.sanitize("key٢٠٢٦").key)
    }

    // ── the line it must not cross ──────────────────────────────────────────────────────

    @Test
    fun `an arabic letter inside a key is reported and NOT deleted`() {
        // **The rule that keeps this honest.** There is no defensible guess for an arbitrary
        // letter in a credential, and quietly deleting it would turn a legible failure into a
        // mysterious one.
        val out = ApiKeySanitizer.sanitize("sk-abمcd")
        assertTrue(out.key.contains('م'))
        assertTrue(out.suspicious)
        val fix = out.fixes.filterIsInstance<ApiKeySanitizer.Fix.Suspicious>().single()
        assertEquals(0x0645, fix.code)
        assertEquals(5, fix.at)
    }

    @Test
    fun `a healthy key is returned untouched and reports nothing`() {
        // The commonest case by far, and the one a repair must never disturb.
        val key = "sk-ant-api03_AbC-123.xyz~456/789+0=="
        val out = ApiKeySanitizer.sanitize(key)
        assertEquals(key, out.key)
        assertTrue(out.fixes.isEmpty())
        assertFalse(out.repaired)
        assertFalse(out.suspicious)
    }

    @Test
    fun `an empty key stays empty rather than throwing`() {
        assertEquals("", ApiKeySanitizer.sanitize("   ").key)
    }

    @Test
    fun `several kinds of damage in one key are all reported`() {
        val out = ApiKeySanitizer.sanitize(" sk—ab‏cمd \n")
        assertEquals("sk-abcمd", out.key)
        assertEquals(1, out.fixes.filterIsInstance<ApiKeySanitizer.Fix.Replaced>().size)
        assertEquals(1, out.fixes.filterIsInstance<ApiKeySanitizer.Fix.RemovedInvisible>().size)
        assertEquals(1, out.fixes.filterIsInstance<ApiKeySanitizer.Fix.Suspicious>().size)
    }
}
