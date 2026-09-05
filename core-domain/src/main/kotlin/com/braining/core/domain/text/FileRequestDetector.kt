package com.braining.core.domain.text

/**
 * Is the user asking for something **file-shaped** — a report, a document, a table?
 *
 * M6 adds one system line telling the model to answer in clean Markdown, and this decides when to
 * add it. `docs/M6_FILE_GENERATION.md` §2.
 *
 * **Why this is allowed to exist at all.** `PROJECT_STATE.md` §8 records a deliberate rule: plain
 * chat sends **no** system prompt, because "an instrument that accumulates state measures something
 * different every time it is used." That rule is about *accumulated* state. This is a pure function
 * of the message being sent right now — the same input always produces the same request, and
 * nothing is remembered between turns. The instrument stays honest.
 *
 * **Latin needs word boundaries; Arabic does not.** `profile` contains `file`, and nudging every
 * message that mentions a profile would be a bug. Arabic attaches its articles and suffixes to the
 * stem — «الملف», «ملفاً», «تقريري» — so a substring test on the stem is the correct instrument
 * there, not a defect. Getting this backwards is the whole reason it is tested.
 */
object FileRequestDetector {

    /** Latin stems, matched on word boundaries with an optional plural. */
    private val LATIN = Regex(
        "\\b(file|files|report|reports|table|tables|document|documents|markdown|spreadsheet|spreadsheets)\\b",
        RegexOption.IGNORE_CASE,
    )

    /** Arabic stems, matched as substrings so prefixes and suffixes come along for free. */
    private val ARABIC = listOf("ملف", "تقرير", "جدول", "وثيقة", "مستند")

    fun isFileShaped(text: String): Boolean {
        if (text.isBlank()) return false
        if (LATIN.containsMatchIn(text)) return true
        return ARABIC.any { text.contains(it) }
    }
}
