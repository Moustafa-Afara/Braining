package com.braining.core.domain.model

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Turns a provider's model listing into the rows a picker shows.
 *
 * ## Why this is here and not in the provider that fetches it
 *
 * The HTTP call cannot be tested without a network; **the parsing can**, and the parsing is where
 * the mistakes live — a field renamed, a price arriving as `"0.0"` instead of `"0"`, a model with
 * no name. `:ai-providers` has no test source set at all, `:core-domain` has JUnit and
 * kotlinx-serialization already, and this file needs nothing else. The provider keeps the socket
 * and hands the bytes here.
 *
 * The same split `DefaultModelRouter` got, for the same reason (`ANSWERS.md` Part 1 §9): the
 * decisions go where they can be checked.
 */
object ModelCatalog {

    /** OpenRouter's own marker for a model that costs nothing. */
    private const val FREE_SUFFIX = ":free"

    /**
     * Parse an OpenAI-shaped `{"data": [...]}` model listing.
     *
     * Never throws. A listing that cannot be read is an empty picker, which costs the user a
     * convenience; an exception here would cost them the screen.
     *
     * Sorted free-first, then by id — so the list is predictable to scroll, and the row a
     * newcomer should start from is the one their eye lands on.
     */
    fun parse(root: JsonObject): List<RemoteModel> = runCatching {
        root["data"]
            ?.jsonArray
            ?.mapNotNull { element ->
                val model = element.jsonObject
                val id = model["id"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }
                    ?: return@mapNotNull null
                RemoteModel(
                    id = id,
                    // A model with no display name falls back to its id rather than to a blank
                    // row: an unlabelled row is a row nobody can choose.
                    label = model["name"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() } ?: id,
                    free = isFree(id, model),
                )
            }
            .orEmpty()
            .distinctBy { it.id }
            .sortedWith(compareByDescending<RemoteModel> { it.free }.thenBy { it.id })
    }.getOrDefault(emptyList())

    /**
     * Costs nothing to run.
     *
     * **Two independent signals**, because each alone has a failure: the `:free` suffix is
     * OpenRouter's convention and another provider may not use it, and a pricing block can be
     * absent entirely. Prices are read as **strings and parsed**, never compared to the literal
     * `"0"` — the day a provider starts sending `"0.0"` or `"0e0"` a literal test would silently
     * start calling every free model paid, and the user would be shown the more expensive list
     * with no sign anything had changed.
     */
    private fun isFree(id: String, model: JsonObject): Boolean {
        if (id.endsWith(FREE_SUFFIX)) return true
        val pricing = model["pricing"]?.jsonObject ?: return false
        // **`?: return false` is load-bearing, and not only for the missing case.** It makes both
        // values non-null `Double`, so `== 0.0` is IEEE comparison and `-0.0` counts as zero.
        // Written as `Double?` instead, Kotlin would compare through `Double.equals`, where
        // `-0.0 != 0.0` — and a price of "-0" would be read as paid. Do not relax these to
        // nullable without also changing the comparison.
        val prompt: Double = pricing["prompt"]?.jsonPrimitive?.contentOrNull?.toDoubleOrNull()
            ?: return false
        val completion: Double = pricing["completion"]?.jsonPrimitive?.contentOrNull?.toDoubleOrNull()
            ?: return false
        return prompt == 0.0 && completion == 0.0
    }
}
