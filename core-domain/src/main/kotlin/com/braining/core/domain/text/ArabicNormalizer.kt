package com.braining.core.domain.text

/**
 * Folds the spelling differences a user cannot reliably type, so search finds what they meant.
 *
 * **This is not cosmetic and it is not optional.** Arabic substring matching without it fails
 * silently: someone searching `احمد` does not find `أحمد`, someone searching `الاسئلة` does not
 * find `الأسئلة`, and the screen shows an empty result that looks exactly like an empty history.
 * `docs/M5_DESIGN_NOTE.md` §5 — and `PROJECT_STATE.md` §10 entry 7: a test whose pass and fail
 * look identical is not a test, and neither is a search.
 *
 * **Applied in exactly two places, and they must be the same function.** Once when a record is
 * written, into its `searchText` column, and once to the query before it is compared. The failure
 * mode of two nearly-identical normalizers is a search that works for whoever wrote it and not
 * for the user, with nothing on screen to say so — the same class of fault as one model name
 * living in three files (`2026-08-03-A`).
 *
 * Pure, in `:core-domain`, and unit-tested. No Android, no locale, no `String.lowercase(Locale)`
 * — the Turkish dotless-i problem is real and a locale-sensitive fold would make the index depend
 * on the phone's language setting, which is a bug that only appears on someone else's device.
 */
object ArabicNormalizer {

    /** Harakat, tanwin, shadda, sukun, superscript alef, and the tatweel stretch character. */
    private fun Char.isStrippable(): Boolean =
        this in 'ً'..'ٟ' || // fathatan … the combining marks block
            this == 'ـ' || // ـ tatweel — decorative stretch, never part of a word
            this == 'ٰ' || // superscript alef
            this in 'ۖ'..'ۜ' ||
            this in '۟'..'ۨ' ||
            this in '۪'..'ۭ'

    /**
     * The fold itself.
     *
     * `ة` → `ه` and `ى` → `ي` are the two that matter most in practice: they are the endings
     * people type by ear, and they end a large share of Arabic words.
     */
    private fun foldChar(c: Char): Char = when (c) {
        // The alef family. `ٱ` (wasla) included — it appears in pasted Qur'anic text.
        'أ', 'إ', 'آ', 'ٱ', 'ٲ', 'ٳ' -> 'ا'
        'ة' -> 'ه'
        'ى', 'ئ' -> 'ي'
        'ؤ' -> 'و'
        // Arabic-Indic and extended Arabic-Indic digits, so `٢٠٢٦` and `2026` are one thing.
        in '٠'..'٩' -> ('0' + (c - '٠'))
        in '۰'..'۹' -> ('0' + (c - '۰'))
        else -> c
    }

    /**
     * Normalized text, ready to be stored or compared.
     *
     * Whitespace is collapsed to single spaces so that a line break inside an answer cannot hide
     * a match that spans it, and the result is trimmed.
     */
    fun normalize(text: String): String {
        val sb = StringBuilder(text.length)
        var lastWasSpace = false
        for (raw in text) {
            if (raw.isStrippable()) continue
            val c = foldChar(raw)
            if (c.isWhitespace()) {
                if (!lastWasSpace && sb.isNotEmpty()) sb.append(' ')
                lastWasSpace = true
            } else {
                // Latin is folded with the root locale explicitly: see the class KDoc.
                sb.append(c.lowercaseChar())
                lastWasSpace = false
            }
        }
        return sb.toString().trim()
    }

    /**
     * Does [haystack] contain [needle], both normalized?
     *
     * Offered so no caller has to remember to normalize both sides — forgetting one is the whole
     * failure this object exists to prevent. Callers that store a pre-normalized column pass it
     * straight to SQL instead; [normalize] is then the only thing they need.
     */
    fun contains(haystack: String, needle: String): Boolean {
        val n = normalize(needle)
        if (n.isEmpty()) return true
        return normalize(haystack).contains(n)
    }
}
