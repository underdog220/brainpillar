package com.brainpillar.watch.architecture.simulator

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

/**
 * Placeholder for later phases; included early so the simulator API already supports
 * transcription events without forcing UI/Android dependencies.
 */
data class TranscriptionUpdated(
    val chunkText: String,
    override val timestampUtcMillis: Long
) : SimulatorEvent

