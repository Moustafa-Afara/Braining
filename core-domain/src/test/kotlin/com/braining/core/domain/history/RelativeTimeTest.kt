package com.braining.core.domain.history

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The age bucket, pinned.
 *
 * The wording lives in `<plurals>` because Arabic has a dual; this only has to get the **bucket
 * and the count** right, and the boundaries are where it could quietly be wrong.
 */
class RelativeTimeTest {

    private val day = 24L * 60 * 60 * 1000
    private val now = 1_000_000_000_000L

    @Test
    fun `a few hours ago is today`() {
        assertEquals(RelativeTime.Bucket.TODAY, RelativeTime.of(now, now - 3 * 60 * 60 * 1000).bucket)
    }

    @Test
    fun `one day ago is yesterday`() {
        assertEquals(RelativeTime.Bucket.YESTERDAY, RelativeTime.of(now, now - day).bucket)
    }

    @Test
    fun `two days ago is the dual, which is why the count travels`() {
        val age = RelativeTime.of(now, now - 2 * day)
        assertEquals(RelativeTime.Bucket.DAYS, age.bucket)
        assertEquals(2, age.count)
    }

    @Test
    fun `six days is still days and seven is weeks`() {
        assertEquals(RelativeTime.Bucket.DAYS, RelativeTime.of(now, now - 6 * day).bucket)
        assertEquals(RelativeTime.Bucket.WEEKS, RelativeTime.of(now, now - 7 * day).bucket)
    }

    @Test
    fun `weeks are counted, not just named`() {
        assertEquals(2, RelativeTime.of(now, now - 14 * day).count)
    }

    @Test
    fun `twenty-nine days is weeks and thirty is months`() {
        assertEquals(RelativeTime.Bucket.WEEKS, RelativeTime.of(now, now - 29 * day).bucket)
        assertEquals(RelativeTime.Bucket.MONTHS, RelativeTime.of(now, now - 30 * day).bucket)
    }

    @Test
    fun `months are never reported as zero`() {
        // `days / 30` at exactly 30 days is 1; the coerce guards the arithmetic against a future
        // edit to the boundary producing «منذ ٠ أشهر».
        assertEquals(1, RelativeTime.of(now, now - 30 * day).count)
    }

    @Test
    fun `a timestamp in the future reads as today, never as a negative age`() {
        // A phone whose clock moved backwards. The only harmless answer — the alternative is a
        // label saying «منذ ٣- أيام».
        assertEquals(RelativeTime.Bucket.TODAY, RelativeTime.of(now, now + 5 * day).bucket)
    }
}
