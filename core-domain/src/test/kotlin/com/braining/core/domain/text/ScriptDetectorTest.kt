package com.braining.core.domain.text

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The Arabic detector, pinned.
 *
 * It decides one thing: whether to offer the «ترجم» button. **A false offer is the expensive
 * failure** — a button offering to translate text that is already Arabic makes the app look as if
 * it cannot read its own output. A missed offer costs one tap of the user's own judgement.
 */
class ScriptDetectorTest {

    private val arabic = "هذه فقرة عربية كاملة تتحدث عن موضوع ما بتفصيل كافٍ لتجاوز الحد الأدنى"
    private val english = "This is an ordinary English paragraph, long enough to be judged fairly."

    @Test
    fun `pure Arabic is all Arabic`() {
        assertEquals(1f, ScriptDetector.arabicRatio(arabic), 0.001f)
    }

    @Test
    fun `pure English is no Arabic`() {
        assertEquals(0f, ScriptDetector.arabicRatio(english), 0.001f)
    }

    @Test
    fun `digits and punctuation are not letters and do not shift the ratio`() {
        val withNoise = "سؤال 1234 — (٥٠٪) ... !!! 99"
        assertEquals(1f, ScriptDetector.arabicRatio(withNoise), 0.001f)
    }

    @Test
    fun `text with no letters at all is not judged`() {
        assertEquals(0f, ScriptDetector.arabicRatio("1234 5678 :: ---"), 0.001f)
        assertFalse(ScriptDetector.looksNonArabic("1234 5678 :: ---"))
    }

    @Test
    fun `empty text never offers a translation`() {
        assertFalse(ScriptDetector.looksNonArabic(""))
    }

    @Test
    fun `a long English answer is offered a translation`() {
        assertTrue(ScriptDetector.looksNonArabic(english))
    }

    @Test
    fun `a long Arabic answer is never offered a translation`() {
        assertFalse(ScriptDetector.looksNonArabic(arabic))
    }

    @Test
    fun `Arabic carrying English product names stays Arabic`() {
        // The case that matters most: this app's answers are full of Anthropic, DeepSeek, API,
        // Android. A word-counting detector would have flipped on these.
        val mixed = "استعمل Anthropic أو DeepSeek عبر واجهة API على نظام Android لتنفيذ الطلب " +
            "ثم راجع النتيجة بنفسك قبل الاعتماد عليها في أي قرار مهم"
        assertFalse(ScriptDetector.looksNonArabic(mixed))
    }

    @Test
    fun `a short English fragment is below the judging threshold`() {
        // "OK, done." is not an English answer — it is too short to mean anything.
        assertFalse(ScriptDetector.looksNonArabic("OK, done."))
    }

    @Test
    fun `the threshold is a share of letters, not of characters`() {
        val halfAndHalf = "هذه جملة عربية قصيرة نسبيا This is an English sentence of similar size"
        val ratio = ScriptDetector.arabicRatio(halfAndHalf)
        assertTrue("ratio was $ratio", ratio in 0.3f..0.7f)
        assertFalse(ScriptDetector.looksNonArabic(halfAndHalf))
    }

    @Test
    fun `documented limitation - an answer that is only English code reads as English`() {
        // Accepted, and written down rather than discovered: a reply that is nothing but a code
        // block will offer a translation. The button is optional and does no harm; a detector
        // that tried to recognise code would be a second thing to be wrong about.
        val code = "fun main() { val result = compute(input); println(result.toString()) }"
        assertTrue(ScriptDetector.looksNonArabic(code))
    }
}
