package com.braining.core.domain.provider

import com.braining.core.domain.model.AiChunk
import com.braining.core.domain.model.AiError
import com.braining.core.domain.model.AiRequest
import com.braining.core.domain.model.ProviderCapabilities
import com.braining.core.domain.model.ProviderId
import kotlinx.coroutines.flow.Flow

interface AiProvider {
    val id: ProviderId
    val capabilities: ProviderCapabilities
    fun complete(request: AiRequest): Flow<AiChunk>

    /**
     * Returns `null` when the key is valid, or the classified [AiError] otherwise.
     *
     * Typed on purpose: the old `Result<Unit>` failure carried a pre-phrased message, so
     * Settings could only show a String it had no hand in writing. A classified error lets
     * the UI resolve the wording from resources like every other failure.
     */
    suspend fun verify(apiKey: String): AiError?
}
