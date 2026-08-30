package com.braining.core.domain.text

/**
 * Repairs an API key damaged in transit between the provider's website and this app.
 *
 * ## The incident this exists for
 *
 * 2026-08-30. The owner pasted a Google key and Gemini refused it. The app said «حدث خطأ غير
 * متوقّع», three causes were plausible, and none could be told apart — until Developer Mode
 * printed what Google had actually said:
 *
 * > `Unexpected char 0x2014 at 37 in x-goog-api-key value`
 *
 * `0x2014` is an **em dash**. Somewhere between Google's page and the text field, a plain `-`
 * became `—`. The key was not wrong, not expired and not unentitled: it was **one character
 * corrupted by copy-and-paste**, and nothing in the app could see it, because an em dash and a
 * hyphen are two pixels apart on a phone screen.
 *
 * ## Why this is a first-class problem and not an edge case
 *
 * The APK is meant to be handed to friends (`ANSWERS.md` Part 3 §A). They will copy keys on
 * phones, through messaging apps, out of PDFs and screenshots — every one of which is a place
 * where "smart" punctuation, bidirectional marks and zero-width characters get inserted. An
 * Arabic-first app carries an extra hazard nobody else has: copying anything near Arabic text
 * can drag an invisible RTL mark along with it, and that mark will then sit inside the HTTP
 * header forever, unseen.
 *
 * **An API key is always plain ASCII.** Every provider issues them from `[A-Za-z0-9._~+/=-]`.
 * That makes the damage detectable with certainty rather than by guesswork.
 *
 * ## The two rules this object follows
 *
 * **1. Repair only what has exactly one correct answer.** An em dash inside a key can only ever
 * have been a hyphen; a zero-width joiner can only ever be noise. Those are fixed. Anything else
 * non-ASCII is **reported and left alone** — silently deleting a character from a credential to
 * make it look valid would turn a legible failure into a mysterious one.
 *
 * **2. Say what was changed.** A key that is quietly rewritten is a key the user cannot reason
 * about when it still fails. Every repair is returned with its position, so the screen can name
 * it.
 *
 * Pure, in `:core-domain`, unit-tested — no Android, no I/O.
 */
object ApiKeySanitizer {

    /** What happened to one character. */
    sealed interface Fix {
        /** A look-alike replaced by the character it must have been. */
        data class Replaced(val from: Char, val to: Char, val at: Int) : Fix

        /** An invisible character deleted: zero-width, or a bidirectional control. */
        data class RemovedInvisible(val code: Int, val at: Int) : Fix

        // `at` on all three is a position in the **cleaned** key, not in what was pasted.
        // That is the only index the user can act on: the field they are looking at holds the
        // cleaned key, so an index into the original would point at the wrong character by
        // however many were removed before it. For a deletion, `at` is where the character
        // used to be — i.e. between the two characters now sitting either side of it.

        /**
         * Non-ASCII, and **not** repaired.
         *
         * There is no defensible guess for an arbitrary letter inside a credential. Reported so
         * the user knows exactly where to look, and left in place so the key still fails
         * honestly rather than failing differently.
         */
        data class Suspicious(val code: Int, val at: Int) : Fix
    }

    data class Result(val key: String, val fixes: List<Fix>) {
        val repaired: Boolean get() = fixes.any { it !is Fix.Suspicious }
        val suspicious: Boolean get() = fixes.any { it is Fix.Suspicious }
    }

    /**
     * Look-alikes with exactly one possible original.
     *
     * The dashes are the ones that actually bite — every provider's key format uses `-`, and
     * every editor with typographic substitution turns it into `—` or `–`. The quotes come from
     * keys pasted out of prose. The Arabic-Indic digits come from an Arabic keyboard's
     * autocorrect touching a key that contains numbers.
     */
    private val REPLACEMENTS: Map<Char, Char> = buildMap {
        // Dashes: em, en, figure, horizontal bar, non-breaking hyphen, minus, soft hyphen.
        listOf('—', '–', '‒', '―', '‑', '−', '­')
            .forEach { put(it, '-') }
        // Curly quotes and primes.
        listOf('“', '”', '″').forEach { put(it, '"') }
        listOf('‘', '’', '′').forEach { put(it, '\'') }
        // Arabic-Indic and extended Arabic-Indic digits.
        for (i in 0..9) {
            put('٠' + i, '0' + i)
            put('۰' + i, '0' + i)
        }
        // Full-width forms, from CJK input methods.
        put('－', '-')
        put('＿', '_')
        put('．', '.')
    }

    /**
     * Characters that carry no information and must simply go.
     *
     * **The bidirectional controls are the dangerous ones here.** They are invisible by
     * definition, the system inserts them wherever Arabic and Latin text meet, and a key carrying
     * one looks *identical* to a correct key in every font at every size. Nobody has ever found
     * one by reading.
     */
    private fun Char.isInvisible(): Boolean =
        this == '﻿' || // BOM / zero-width no-break space
            this in '​'..'‏' || // zero-width space … RLM
            this in '‪'..'‮' || // bidi embedding / override
            this in '⁠'..'⁤' || // word joiner and invisible operators
            this in '⁦'..'⁩' // bidi isolates

    /**
     * Clean [raw] and report every change.
     *
     * Surrounding whitespace is trimmed first: a pasted key routinely carries a trailing newline,
     * and that has produced auth failures in this project before — the reason every key field
     * already trims. Whitespace **inside** the key is removed too, because no provider issues a
     * key containing a space, and a line-wrapped paste is the commonest way one gets there.
     */
    fun sanitize(raw: String): Result {
        val fixes = mutableListOf<Fix>()
        val out = StringBuilder(raw.length)

        raw.trim().forEach { c ->
            // `out.length` — the position this character occupies in the cleaned key — and not
            // the loop index. See the note on Fix: the screen shows the cleaned key.
            when {
                c.isInvisible() -> fixes += Fix.RemovedInvisible(c.code, out.length)

                // Interior whitespace: dropped without a fix entry. A wrapped paste is not a
                // corrupted character, it is a line break, and reporting it as a repair would
                // bury the ones that matter.
                c.isWhitespace() -> Unit

                REPLACEMENTS.containsKey(c) -> {
                    val to = REPLACEMENTS.getValue(c)
                    fixes += Fix.Replaced(c, to, out.length)
                    out.append(to)
                }

                c.code > 127 -> {
                    // Reported, kept. See Fix.Suspicious.
                    fixes += Fix.Suspicious(c.code, out.length)
                    out.append(c)
                }

                else -> out.append(c)
            }
        }

        return Result(out.toString(), fixes)
    }
}
