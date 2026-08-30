package com.braining.core.domain.text

/**
 * Is this text Arabic?
 *
 * **Why a detector exists at all.** `ANSWERS.md` Part 7 §M3-2 said the answer comes back in
 * English until M4 translates it. That stopped being true on 2026-08-07, when the forged prompt's
 * OUTPUT CONTRACT began requiring an Arabic reply — so the normal case now needs **no**
 * translation, and a mandatory translate step would spend a second call and several seconds on
 * every answer to change nothing (owner's ruling, 2026-08-17).
 *
 * What is left is the abnormal case: a model that ignores the contract and answers in English.
 * That is what this detects, and the user is offered a button rather than made to wait for a step
 * they usually do not need.
 *
 * **It counts letters, not words.** Word splitting is language-specific and would be one more
 * thing to be wrong about; letters are decided by the Unicode block and nothing else. Digits,
 * punctuation, code, URLs and emoji are ignored entirely — an Arabic answer full of English
 * product names and numbers must not read as English.
 */
object ScriptDetector {

    /** U+0600–U+06FF, U+0750–U+077F, U+08A0–U+08FF, U+FB50–U+FDFF, U+FE70–U+FEFF. */
    private fun Char.isArabicLetter(): Boolean = this in '\u0600'..'\u06FF' ||
        this in '\u0750'..'\u077F' ||
        this in '\u08A0'..'\u08FF' ||
        this in '\uFB50'..'\uFDFF' ||
        this in '\uFE70'..'\uFEFF'

    /**
     * Arabic letters as a share of all letters, 0f–1f. **Zero for text with no letters at all** —
     * a wall of code or numbers is not "0% Arabic prose", it is not prose, and [looksNonArabic]
     * refuses to judge it.
     */
    fun arabicRatio(text: String): Float {
        var arabic = 0
        var letters = 0
        for (c in text) {
            if (!c.isLetter()) continue
            letters++
            if (c.isArabicLetter()) arabic++
        }
        return if (letters == 0) 0f else arabic.toFloat() / letters
    }

    /**
     * True when the text is long enough to judge **and** mostly not Arabic.
     *
     * @param minLetters below this, say nothing. A three-word answer, a code block or a bare URL
     *   would otherwise light up the translate button on text there is nothing to translate.
     * @param threshold Arabic below this share reads as a non-Arabic answer. 0.2 is deliberately
     *   low: the cost of a missed offer is a button the user does not get, and the cost of a false
     *   one is a button offering to translate something already Arabic — the second is worse,
     *   because it makes the app look like it cannot read its own output.
     */
    fun looksNonArabic(
        text: String,
        minLetters: Int = 40,
        threshold: Float = 0.2f,
    ): Boolean {
        val letters = text.count { it.isLetter() }
        if (letters < minLetters) return false
        return arabicRatio(text) < threshold
    }
}
