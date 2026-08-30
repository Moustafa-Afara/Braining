package com.braining.core.domain.model

data class ProviderState(
    val providerId: ProviderId,
    val isEnabled: Boolean = false,
    /**
     * The API key as it should appear in the settings field. Held in memory only —
     * the persisted copy lives in the encrypted key store. The UI masks it behind a
     * PasswordVisualTransformation and reveals it on the eye toggle.
     */
    val apiKey: String = "",
    val hasKey: Boolean = false,
    val isValidating: Boolean = false,
    val isValid: Boolean? = null,
    val selectedModel: String = "",
    /**
     * The classified failure of the last [verify] attempt, resolved to a sentence by the
     * UI from string resources. Typed rather than pre-phrased for the same reason
     * `AiChunk.Error` is: the domain must not speak for the user interface.
     */
    val error: AiError? = null,
)
