package com.braining.core.domain.text

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FileRequestDetectorTest {

    @Test
    fun `arabic report request is file shaped`() {
        assertTrue(FileRequestDetector.isFileShaped("اكتب لي تقريراً عن الذكاء الاصطناعي"))
    }

    @Test
    fun `arabic file word with the definite article is file shaped`() {
        assertTrue(FileRequestDetector.isFileShaped("أنشئ الملف كما اتفقنا"))
    }

    @Test
    fun `arabic table request is file shaped`() {
        assertTrue(FileRequestDetector.isFileShaped("أعطني جدولاً بالنتائج"))
    }

    @Test
    fun `ordinary arabic question is not file shaped`() {
        assertFalse(FileRequestDetector.isFileShaped("ما هو الطقس اليوم؟"))
    }

    @Test
    fun `english report request is file shaped`() {
        assertTrue(FileRequestDetector.isFileShaped("write me a report about solar power"))
    }

    @Test
    fun `english table request is file shaped`() {
        assertTrue(FileRequestDetector.isFileShaped("Make a TABLE of the results"))
    }

    /**
     * The reason `LATIN` uses word boundaries. `profile` ends in `file`; a substring test would
     * nudge every message that mentions one.
     */
    @Test
    fun `profile does not count as file`() {
        assertFalse(FileRequestDetector.isFileShaped("please update my profile"))
    }

    @Test
    fun `filename inside a word does not count`() {
        assertFalse(FileRequestDetector.isFileShaped("the classifier is unprofitable"))
    }

    @Test
    fun `blank is not file shaped`() {
        assertFalse(FileRequestDetector.isFileShaped("   "))
    }
}
