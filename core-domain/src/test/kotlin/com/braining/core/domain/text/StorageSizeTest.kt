package com.braining.core.domain.text

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The storage readout, pinned.
 *
 * **This number stands in place of a size cap** (`ANSWERS.md` Part 1 §10), so it is a decision
 * aid rather than decoration — and a decision aid with an off-by-1024 in it is worse than none
 * (`PROJECT_STATE.md` §10 entry 6).
 */
class StorageSizeTest {

    @Test
    fun `small values are plain bytes`() {
        val f = StorageSize.format(700)
        assertEquals(StorageSize.Unit.BYTES, f.unit)
        assertEquals("700", f.value)
    }

    @Test
    fun `the boundary at one kilobyte is binary, not decimal`() {
        assertEquals(StorageSize.Unit.BYTES, StorageSize.format(1023).unit)
        assertEquals(StorageSize.Unit.KILOBYTES, StorageSize.format(1024).unit)
    }

    @Test
    fun `kilobytes below ten keep one decimal`() {
        val f = StorageSize.format(1536) // 1.5 KB
        assertEquals(StorageSize.Unit.KILOBYTES, f.unit)
        assertEquals("1.5", f.value)
    }

    @Test
    fun `kilobytes above ten drop the decimal`() {
        // Once the leading digits carry the information, a decimal is noise.
        val f = StorageSize.format(48L * 1024)
        assertEquals(StorageSize.Unit.KILOBYTES, f.unit)
        assertEquals("48", f.value)
    }

    @Test
    fun `megabytes are reported as megabytes`() {
        val f = StorageSize.format((1.2 * 1024 * 1024).toLong())
        assertEquals(StorageSize.Unit.MEGABYTES, f.unit)
        assertEquals("1.2", f.value)
    }

    @Test
    fun `the boundary at one megabyte`() {
        assertEquals(StorageSize.Unit.KILOBYTES, StorageSize.format(1024L * 1024 - 1).unit)
        assertEquals(StorageSize.Unit.MEGABYTES, StorageSize.format(1024L * 1024).unit)
    }

    @Test
    fun `gigabytes are reported as gigabytes`() {
        val f = StorageSize.format(3L * 1024 * 1024 * 1024)
        assertEquals(StorageSize.Unit.GIGABYTES, f.unit)
        assertEquals("3", f.value)
    }

    @Test
    fun `a whole number never shows a trailing zero`() {
        assertEquals("2", StorageSize.format(2L * 1024 * 1024).value)
    }

    @Test
    fun `zero is zero bytes and not an error`() {
        val f = StorageSize.format(0)
        assertEquals(StorageSize.Unit.BYTES, f.unit)
        assertEquals("0", f.value)
    }

    @Test
    fun `a negative size reads as zero rather than as a minus sign`() {
        // Nothing should produce one, but a readout that can print «منذ ٣- بايت» is a readout
        // that will one day print it in front of the user.
        assertEquals("0", StorageSize.format(-5).value)
    }

    @Test
    fun `digits are western in every locale because they are built by arithmetic`() {
        // `String.format` without an explicit locale emits Arabic-Indic digits on an Arabic
        // device, so the same build would produce two different strings and this test would pass
        // only on the machine that wrote it.
        assertEquals("1.5", StorageSize.format(1536).value)
    }
}
