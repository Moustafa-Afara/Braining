package com.braining.core.domain.text

/**
 * Bytes as a short human string: `٤٨٠ ك.ب`, `١٫٢ م.ب`.
 *
 * **Why this is in the domain and not in a composable.** `ANSWERS.md` Part 1 §10 replaces a hard
 * size cap with a readout the user is trusted to act on, which makes this number a decision aid
 * rather than decoration — and a decision aid with an off-by-1024 in it is worse than none
 * (`PROJECT_STATE.md` §10 entry 6). Pure, here, unit-tested.
 *
 * **Binary units, and the unit word comes from resources.** This returns the number and a unit
 * *key*; the screen resolves the key to Arabic or English. A domain object that returned «ميغابايت»
 * would be phrasing user-facing text, which is the thing `AiError` was built to stop.
 */
object StorageSize {

    enum class Unit { BYTES, KILOBYTES, MEGABYTES, GIGABYTES }

    data class Formatted(val value: String, val unit: Unit)

    private const val K = 1024.0

    /**
     * One decimal place above a kilobyte, none below it.
     *
     * `٧٠٠ بايت` needs no decimal; `1.2 م.ب` does, because the difference between 1 MB and 1.9 MB
     * is the difference between "ignore this" and "look at this". The decimal is dropped again
     * once the leading digits carry the information — `24 م.ب`, not `24.0`.
     */
    fun format(bytes: Long): Formatted {
        val b = if (bytes < 0) 0L else bytes
        return when {
            b < 1024 -> Formatted(b.toString(), Unit.BYTES)
            b < 1024L * 1024 -> Formatted(round(b / K, b / K < 10), Unit.KILOBYTES)
            b < 1024L * 1024 * 1024 -> Formatted(round(b / (K * K), b / (K * K) < 10), Unit.MEGABYTES)
            else -> Formatted(round(b / (K * K * K), true), Unit.GIGABYTES)
        }
    }

    /**
     * Rounded to one decimal or to none.
     *
     * Built by integer arithmetic rather than `String.format`, deliberately: `String.format`
     * without an explicit locale emits Arabic-Indic digits on an Arabic device and Western ones
     * elsewhere, so the same build would produce two different strings and any test pinning one
     * of them would pass only on the machine that wrote it.
     */
    private fun round(value: Double, withDecimal: Boolean): String {
        if (!withDecimal) return (value + 0.5).toInt().toString()
        val tenths = ((value * 10) + 0.5).toLong()
        val whole = tenths / 10
        val frac = tenths % 10
        return if (frac == 0L) whole.toString() else "$whole.$frac"
    }
}
