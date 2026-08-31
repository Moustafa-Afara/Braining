package com.braining.core.domain.provider

import com.braining.core.domain.model.ProviderId

/**
 * Where each provider's API key comes from, and what it costs to get one.
 *
 * ## Why this exists
 *
 * The owner's distribution plan is friends, not developers (`ANSWERS.md` Part 3 §A). A friend who
 * installs this app meets a screen asking for four API keys and has no idea what an API key is,
 * where to get one, or which of the four is free. **Every one of them has to ask him**, and he
 * answers the same four questions every time.
 *
 * There is a sharper reason too, and the 2026-08-30 Gemini incident is it: when a key fails, the
 * first question is always *"is the key wrong, or is my account wrong?"* — and that question is
 * unanswerable without knowing what the account was supposed to have in the first place.
 *
 * ## What is in here, and what deliberately is not
 *
 * **URLs and the shape of a key.** Those are stable, and when they do change the failure is
 * loud — a dead link is visible in one tap.
 *
 * **No prices, no quota numbers, no model lists.** Those change every few months, and a number
 * baked into an APK that a friend installed in August and opens in March is not out of date, it
 * is **wrong** — and it is wrong in the direction that costs money. The screen sends the user to
 * the provider's own page for anything that carries a figure. §10 entry 3's rule about confident
 * claims from stale evidence applies to shipped strings exactly as it applies to diagnoses.
 *
 * Facts below verified against each vendor's own documentation on **2026-08-30**. Re-verify
 * before changing them; do not "fix" one from memory.
 *
 * Pure, in `:core-domain`, unit-tested — no Android, no I/O.
 */
object ProviderGuide {

    /**
     * @param keyUrl the page that creates a key — the deepest link that survives, not a homepage.
     * @param docsUrl the page that answers "what do I need first?" — pricing, or the region list.
     * @param keyPrefix what a valid key starts with. Shown, never enforced: a provider may add a
     *   format tomorrow, and an app that refused the new one would be broken by someone else's
     *   improvement. Its job is to let a user notice they pasted OpenAI's key into Claude's card
     *   — the mistake that produces a 401 and reads as "my key is dead".
     * @param freeTier true when a working key can be had with **no payment method at all**.
     * @param regionLimited true when the provider refuses whole countries. Stated before the
     *   user spends time on a key, not after — `ANSWERS.md` Part 3 §B, amended 2026-08-03.
     */
    data class Guide(
        val keyUrl: String,
        val docsUrl: String,
        val keyPrefix: String,
        val freeTier: Boolean,
        val regionLimited: Boolean,
    )

    fun of(id: ProviderId): Guide = when (id) {
        // The only one of the four that gives a working key for nothing — and the only one that
        // refuses entire countries. Both facts are load-bearing for this app's users, and they
        // pull in opposite directions, so both are said.
        ProviderId.GEMINI -> Guide(
            keyUrl = "https://aistudio.google.com/apikey",
            docsUrl = "https://ai.google.dev/gemini-api/docs/available-regions",
            keyPrefix = "AIza",
            freeTier = true,
            regionLimited = true,
        )

        // console.anthropic.com now redirects here; the settings path is the stable one.
        ProviderId.ANTHROPIC -> Guide(
            keyUrl = "https://platform.claude.com/settings/keys",
            docsUrl = "https://platform.claude.com/docs/en/about-claude/pricing",
            keyPrefix = "sk-ant-",
            freeTier = false,
            regionLimited = false,
        )

        // Pricing, not the quickstart: `docsUrl` is the page behind «اقرأ الشروط», and the
        // thing this provider's user needs to check first is what a payment method commits
        // them to — not how to write their first request.
        ProviderId.OPENAI -> Guide(
            keyUrl = "https://platform.openai.com/api-keys",
            docsUrl = "https://openai.com/api/pricing/",
            keyPrefix = "sk-",
            freeTier = false,
            regionLimited = false,
        )

        ProviderId.DEEPSEEK -> Guide(
            keyUrl = "https://platform.deepseek.com/api_keys",
            docsUrl = "https://api-docs.deepseek.com/quick_start/pricing",
            keyPrefix = "sk-",
            freeTier = false,
            regionLimited = false,
        )
    }
}
