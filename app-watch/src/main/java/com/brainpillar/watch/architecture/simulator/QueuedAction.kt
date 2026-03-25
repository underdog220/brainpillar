package com.brainpillar.watch.architecture.simulator

/**
 * Aktion die bei Offline/Hybrid gepuffert wird und bei Netzwerk-Rueckkehr
 * automatisch ausgefuehrt werden soll.
 */
data class QueuedAction(
    val actionType: QueuedActionType,
    val label: String,
    val payload: Map<String, String> = emptyMap(),
    val queuedAtUtcMillis: Long
)

enum class QueuedActionType {
    /** Projekt-Export (FinishProject bei Offline) */
    EXPORT,
    /** Foto-Upload */
    PHOTO_UPLOAD,
    /** Transkript-Sync */
    TRANSCRIPTION_SYNC
}
