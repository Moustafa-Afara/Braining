package com.braining.core.domain.text

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The search fold, pinned.
 *
 * **Every case below is a real way a user types a word they will not otherwise find.** Without
 * the fold, search returns nothing and the screen shows an empty history — a failure whose pass
 * and fail look identical (`PROJECT_STATE.md` §10 entry 7), which is why these checks exist at
 * all rather than being left to "it obviously works".
 */
class ArabicNormalizerTest {

    // ── the alef family ─────────────────────────────────────────────────────────────────

    @Test
    fun `hamza on alef folds to bare alef`() {
        assertEquals(ArabicNormalizer.normalize("احمد"), ArabicNormalizer.normalize("أحمد"))
    }

    @Test
    fun `hamza under alef folds to bare alef`() {
        assertEquals(ArabicNormalizer.normalize("اسلام"), ArabicNormalizer.normalize("إسلام"))
    }

    @Test
    fun `madda folds to bare alef`() {
        assertEquals(ArabicNormalizer.normalize("الان"), ArabicNormalizer.normalize("الآن"))
    }

    @Test
    fun `every alef form reaches the same normal form`() {
        val forms = listOf("أ", "إ", "آ", "ٱ", "ا")
        val normalized = forms.map { ArabicNormalizer.normalize(it) }.toSet()
        assertEquals(1, normalized.size)
        assertEquals("ا", normalized.first())
    }

    // ── the endings people type by ear ──────────────────────────────────────────────────

    @Test
    fun `ta marbuta folds to ha`() {
        assertEquals(ArabicNormalizer.normalize("مدرسه"), ArabicNormalizer.normalize("مدرسة"))
    }

    @Test
    fun `alef maqsura folds to ya`() {
        assertEquals(ArabicNormalizer.normalize("علي"), ArabicNormalizer.normalize("على"))
    }

    @Test
    fun `hamza on waw and on ya fold to their carriers`() {
        assertEquals(ArabicNormalizer.normalize("سوال"), ArabicNormalizer.normalize("سؤال"))
        assertEquals(ArabicNormalizer.normalize("مسيول"), ArabicNormalizer.normalize("مسئول"))
    }

    // ── marks that carry no search meaning ──────────────────────────────────────────────

    @Test
    fun `diacritics are stripped`() {
        assertEquals("كتب", ArabicNormalizer.normalize("كَتَبَ"))
    }

    @Test
    fun `shadda and sukun are stripped`() {
        assertEquals("مدرس", ArabicNormalizer.normalize("مُدَرِّسْ"))
    }

    @Test
    fun `tatweel is stripped`() {
        assertEquals(ArabicNormalizer.normalize("كتاب"), ArabicNormalizer.normalize("كــتــاب"))
    }

    // ── digits and Latin ────────────────────────────────────────────────────────────────

    @Test
    fun `arabic-indic digits fold to western ones`() {
        assertEquals("2026", ArabicNormalizer.normalize("٢٠٢٦"))
    }

    @Test
    fun `latin is case folded`() {
        assertEquals("gemini api", ArabicNormalizer.normalize("Gemini API"))
    }

    // ── shape ───────────────────────────────────────────────────────────────────────────

    @Test
    fun `runs of whitespace collapse to one space`() {
        assertEquals("سطر اخر", ArabicNormalizer.normalize("سطر\n\n   آخر"))
    }

    @Test
    fun `leading and trailing whitespace is trimmed`() {
        // **Written wrong the first time, and the runner caught it.** The original asserted the
        // literal «فكرة» — forgetting that this object folds ة to ه, which the test three rows
        // up asserts on purpose. The implementation was right; the expectation carried a rule the
        // person writing it had not applied.
        //
        // Comparing against `normalize` of the trimmed form is the better test anyway: it pins
        // **only** the property in its name. An expectation spelled out by hand quietly restates
        // every other rule in the object, so a deliberate change to any of them breaks a test
        // that was never about them.
        assertEquals(
            ArabicNormalizer.normalize("فكرة"),
            ArabicNormalizer.normalize("   فكرة \n"),
        )
    }

    @Test
    fun `empty stays empty`() {
        assertEquals("", ArabicNormalizer.normalize(""))
        assertEquals("", ArabicNormalizer.normalize("   \n\t "))
    }

    // ── the thing the whole object exists for ───────────────────────────────────────────

    @Test
    fun `a query typed without hamzas finds text written with them`() {
        val haystack = "أريد خطة لتعليم ابني القراءة"
        assertTrue(ArabicNormalizer.contains(haystack, "اريد"))
        assertTrue(ArabicNormalizer.contains(haystack, "ابني"))
    }

    @Test
    fun `a match that spans a line break is still found`() {
        assertTrue(ArabicNormalizer.contains("خطة\nلتعليم", "خطة لتعليم"))
    }

    @Test
    fun `an empty query matches everything rather than nothing`() {
        // A blank search box must not empty the list — the caller short-circuits it, and this
        // pins the contract so a future caller that forgets cannot silently hide the history.
        assertTrue(ArabicNormalizer.contains("أي نص", ""))
    }

    @Test
    fun `a word that is genuinely absent is not found`() {
        assertFalse(ArabicNormalizer.contains("خطة لتعليم القراءة", "سيارة"))
    }
}
