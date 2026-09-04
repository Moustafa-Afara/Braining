package com.braining.core.domain.model

/**
 * One model a provider says it can run, as the picker needs it.
 *
 * Three fields and no more, on purpose. A provider's model listing carries a dozen — context
 * length, modality, per-token prices, moderation flags — and every one of them would be a number
 * compiled into a screen that goes stale (`ANSWERS.md` Part 14 §N2). What the user is choosing
 * between is a **name**, and the only property that changes that choice for the friends this app
 * is built for is whether it costs anything.
 *
 * @param id what goes in the request. Namespaced at OpenRouter (`qwen/qwen3-8b`), bare at Ollama.
 * @param label what the human reads. Falls back to [id] when the provider offers no better name.
 * @param free costs nothing to run. Sorted to the top of the picker.
 */
data class RemoteModel(
    val id: String,
    val label: String,
    val free: Boolean = false,
)
