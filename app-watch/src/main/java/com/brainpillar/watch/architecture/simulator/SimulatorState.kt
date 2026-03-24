package com.brainpillar.watch.architecture.simulator

/**
 * Domain-only simulation state.
 * No Android/Compose types here, so transition logic can be unit-tested.
 */
data class SimulatorState(
    val stage: SimulatorStage = SimulatorStage.Idle,
    val projectId: String? = null,
    val isRecording: Boolean = false,
    val lastPhotoMarkerId: String? = null,
    val lastNetworkMode: NetworkMode = NetworkMode.Online,
    val hasTranscription: Boolean = false,
    val lastError: String? = null
)

