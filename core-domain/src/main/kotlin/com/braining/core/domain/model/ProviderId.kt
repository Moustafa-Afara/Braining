package com.braining.core.domain.model

import kotlinx.serialization.Serializable

/**
 * The providers the app can talk to, and the model each one uses unless the user overrides
 * it in Settings.
 *
 * [defaultModel] lives here because it was previously written out by hand in three places —
 * `ChatViewModel.selectProvider`, `SettingsViewModel.init` and `GeminiProvider.DEFAULT_MODEL`.
 * Vendors retire model names on their own schedule, so a name that is duplicated is a name
 * that will eventually be updated in two places out of three and left broken in the third.
 * One constant per provider, here, is the fix.
 */
/**
 * GitHub Models was removed on the owner's ruling of 2026-08-17 — `ANSWERS.md` Part 8 §D1.
 * It was a stub that could never answer, and a dead entry in the provider list is a control
 * that teaches the user the list cannot be trusted. This **overrides** `ANSWERS.md` Part 1 §3,
 * which had asked for it to stay as a labelled non-blocking stub. Do not restore it from that
 * section without a new ruling.
 */
enum class ProviderId(val displayName: String, val defaultModel: String) {
    ANTHROPIC("Claude (Anthropic)", "claude-sonnet-5"),
    OPENAI("ChatGPT (OpenAI)", "gpt-4o"),

    // `deepseek-chat` was shut down on 2026-07-24 (announced 2026-04-24). It had pointed at
    // DeepSeek-V4-Flash's non-thinking mode, so `deepseek-v4-flash` is the like-for-like
    // replacement and DeepSeek's own recommendation for chat workloads.
    // Ref: https://api-docs.deepseek.com/updates
    DEEPSEEK("DeepSeek", "deepseek-v4-flash"),

    // Stable and not on Google's deprecation schedule as of 2026-07-30.
    // Ref: https://ai.google.dev/gemini-api/docs/deprecations
    GEMINI("Google Gemini", "gemini-3.5-flash"),

    /**
     * Models running on the user's **own** computer, reached over the local network.
     *
     * The odd one out in three ways, and each one cost a hook somewhere else:
     *
     * 1. **No API key.** Ollama authenticates nobody, so `BaseHttpProvider.requiresKey` is false
     *    for it. Inventing a placeholder key to store would have been a lie in a key store.
     * 2. **No fixed address.** It lives at whatever the user's PC is, typed in Settings, so it
     *    resolves its base URL per request through `BaseHttpProvider.resolveBaseUrl`.
     * 3. **It can simply be switched off.** Every other provider either answers or fails; this
     *    one has a third state — the PC is asleep — and the fallback list has to ask before it
     *    offers it (`ANSWERS.md` Part 15 §O3).
     *
     * `defaultModel` is a starting point, not a promise: the real list is whatever the user has
     * actually pulled, fetched from their machine and shown as a picker.
     */
    // `displayName` is a plain constant, not a resource — it cannot follow the app's language.
    // An Arabic parenthetical here would have read «Ollama (حاسوبك)» inside every English
    // sentence in the app: the provider menu, the fallback chips, every error. The Arabic
    // belongs in `ollama_title`, which is a string resource and has both locales.
    OLLAMA("Ollama", "llama3.2")
}
