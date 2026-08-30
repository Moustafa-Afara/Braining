package com.braining.core.domain.model

enum class CostTier { FREE, LOW, MEDIUM, HIGH }

data class ProviderCapabilities(
    val streaming: Boolean = true,
    val maxContextTokens: Int = 8192,
    val costTier: CostTier = CostTier.MEDIUM,
    val supportsVision: Boolean = false,
    val supportsTools: Boolean = false,
)
