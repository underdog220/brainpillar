package com.brainpillar.watch.architecture.simulator

/**
 * Domain-only effects/outputs emitted by the simulator.
 * These must not include Android/UI types.
 */
sealed interface SimulatorEffect {
    data class Log(val level: LogLevel, val message: String) : SimulatorEffect
    data class Warning(val message: String) : SimulatorEffect

    data class EmitHint(
        val hintType: SimulatorHintType,
        val title: String,
        val subtitle: String?,
        val confidenceLabel: SimulatorConfidenceLabel?,
        val isStale: Boolean,
        val ttlSec: Int?
    ) : SimulatorEffect

    /** Aktion wird offline gepuffert statt sofort ausgefuehrt */
    data class EnqueueAction(val action: QueuedAction) : SimulatorEffect

    /** Alle gepufferten Aktionen sollen jetzt ausgefuehrt werden (Netzwerk wieder da) */
    data class FlushQueue(val actions: List<QueuedAction>) : SimulatorEffect

    /** Checklisten-Ergebnis: Liste von Pruefpunkten mit Status */
    data class ChecklistResult(
        val checklistId: String,
        val items: List<ChecklistItem>
    ) : SimulatorEffect

    /** KI-Bewertung angefordert (wird asynchron verarbeitet) */
    data class AiEvaluationTriggered(
        val evaluationType: EvaluationType,
        val contextSummary: String
    ) : SimulatorEffect
}

data class ChecklistItem(
    val label: String,
    val checked: Boolean
)

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

