package com.brainpillar.watch.feature.hints.model

/**
 * Hint categories shown on watch.
 */
enum class HintType {
    PERSON,
    TOPIC,
    REMINDER,
    FALLBACK
}

/**
 * Human-readable confidence labels for discreet UX.
 * Avoid percentages on watch.
 */
enum class ConfidenceLabel {
    PROBABLE,   // "Wahrscheinlich"
    POSSIBLE    // "Moeglicherweise"
}

/**
 * Small, watch-friendly UI model for a single hint card.
 */
data class WatchHintModel(
    val hintType: HintType,
    val title: String,
    val subtitle: String? = null,
    val confidenceLabel: ConfidenceLabel? = null,
    val isStale: Boolean = false,
    val generatedAtUtc: String? = null
)
