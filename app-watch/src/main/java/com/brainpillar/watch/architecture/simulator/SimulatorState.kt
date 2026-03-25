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
    val transcriptionChunkCount: Int = 0,
    val lastTranscriptionAtUtc: Long? = null,
    val lastError: String? = null
) {
    /**
     * Prueft ob die letzte Transkription aelter als ttlMillis ist.
     * Gibt true zurueck wenn keine Transkription vorliegt oder TTL ueberschritten.
     */
    fun isTranscriptionStale(currentTimeUtcMillis: Long, ttlMillis: Long = 30_000L): Boolean {
        val lastAt = lastTranscriptionAtUtc ?: return true
        return (currentTimeUtcMillis - lastAt) > ttlMillis
    }
}

