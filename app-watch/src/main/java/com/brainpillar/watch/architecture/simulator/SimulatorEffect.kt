package com.brainpillar.watch.architecture.simulator

/**
 * Domain-only effects/outputs emitted by the simulator.
 * These must not include Android/UI types.
 */
sealed interface SimulatorEffect {
    data class Log(val level: LogLevel, val message: String) : SimulatorEffect
    data class Warning(val message: String) : SimulatorEffect

    // For later integration: Adapter maps these to Watch UI hints.
    data class EmitHint(
        val hintType: SimulatorHintType,
        val title: String,
        val subtitle: String?,
        val confidenceLabel: SimulatorConfidenceLabel?,
        val isStale: Boolean,
        val ttlSec: Int?
    ) : SimulatorEffect
}

enum class LogLevel {
    DEBUG,
    INFO,
    WARN,
    ERROR
}

enum class SimulatorHintType {
    PERSON,
    TOPIC,
    REMINDER,
    FALLBACK
}

enum class SimulatorConfidenceLabel {
    PROBABLE,
    POSSIBLE
}

