package com.brainpillar.shared.simulator

/**
 * Domain-only simulation events.
 * Keep timestamps as millis to avoid formatting decisions in the core.
 */
sealed interface SimulatorEvent {
    val timestampUtcMillis: Long
}

data class StartProject(
    val projectId: String,
    override val timestampUtcMillis: Long
) : SimulatorEvent

data class StartRecording(
    override val timestampUtcMillis: Long
) : SimulatorEvent

data class PauseRecording(
    override val timestampUtcMillis: Long
) : SimulatorEvent

data class ResumeRecording(
    override val timestampUtcMillis: Long
) : SimulatorEvent

data class CapturePhoto(
    val markerId: String?,
    override val timestampUtcMillis: Long
) : SimulatorEvent

data class FinishProject(
    override val timestampUtcMillis: Long
) : SimulatorEvent

data class NetworkModeChanged(
    val mode: NetworkMode,
    override val timestampUtcMillis: Long
) : SimulatorEvent

data class TranscriptionUpdated(
    val chunkText: String,
    override val timestampUtcMillis: Long
) : SimulatorEvent

/**
 * Checklisten-Pruefung anfordern. Typisch: Nutzer tippt auf Watch
 * um den aktuellen Fortschritt zu pruefen.
 */
data class ChecklistRequested(
    val checklistId: String,
    override val timestampUtcMillis: Long
) : SimulatorEvent

/**
 * KI-Bewertung anfordern. Typisch: Nach Projektabschluss oder
 * auf Nutzer-Anforderung fuer Qualitaetsbewertung.
 */
data class AiEvaluationRequested(
    val evaluationType: EvaluationType = EvaluationType.QUALITY,
    override val timestampUtcMillis: Long
) : SimulatorEvent

/**
 * Export manuell starten oder nach Queue-Flush automatisch.
 */
data class ExportStarted(
    override val timestampUtcMillis: Long
) : SimulatorEvent

/**
 * Export erfolgreich abgeschlossen.
 */
data class ExportCompleted(
    override val timestampUtcMillis: Long
) : SimulatorEvent

/**
 * Export fehlgeschlagen. Engine entscheidet ob Retry moeglich.
 */
data class ExportFailed(
    val reason: String,
    val isRetryable: Boolean = true,
    override val timestampUtcMillis: Long
) : SimulatorEvent

/**
 * Retry eines fehlgeschlagenen Exports ausfuehren.
 */
data class ExportRetry(
    override val timestampUtcMillis: Long
) : SimulatorEvent

enum class EvaluationType {
    /** Qualitaetsbewertung der Aufnahme/Dokumentation */
    QUALITY,
    /** Vollstaendigkeitspruefung */
    COMPLETENESS,
    /** Zusammenfassung des Projekts */
    SUMMARY
}
