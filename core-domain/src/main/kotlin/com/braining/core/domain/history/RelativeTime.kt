package com.braining.core.domain.history

/**
 * How long ago, as a bucket the UI can phrase — never as a sentence.
 *
 * **The domain does not write user-facing text.** That rule produced `AiError` and it applies
 * here for a sharper reason than usual: Arabic has a **dual**, so «منذ يومين» is not «منذ ٢ أيام»,
 * and Android's `<plurals>` with `one` / `two` / `few` / `many` is the only thing that gets it
 * right in both locales. A domain object returning a formatted string would have to reimplement
 * that and would get it wrong in one of the two languages.
 */
object RelativeTime {

    enum class Bucket { TODAY, YESTERDAY, DAYS, WEEKS, MONTHS }

    data class Age(val bucket: Bucket, val count: Int)

    private const val DAY = 24L * 60 * 60 * 1000

    /**
     * @param now epoch millis, passed in rather than read from the clock so this is testable
     *   without freezing time — `PROJECT_STATE.md` §10 entry 8's principle applied in reverse:
     *   a value the caller already holds should not be re-derived where it cannot be checked.
     *
     * A record stamped in the future (a clock that moved backwards) reads as [Bucket.TODAY]
     * rather than as a negative age. It is the only harmless answer, and the alternative is a
     * label saying «منذ ٣- أيام».
     */
    fun of(now: Long, then: Long): Age {
        val days = ((now - then) / DAY).toInt()
        return when {
            days <= 0 -> Age(Bucket.TODAY, 0)
            days == 1 -> Age(Bucket.YESTERDAY, 1)
            days < 7 -> Age(Bucket.DAYS, days)
            days < 30 -> Age(Bucket.WEEKS, days / 7)
            else -> Age(Bucket.MONTHS, (days / 30).coerceAtLeast(1))
        }
    }
}
