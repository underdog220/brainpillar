package com.brainpillar.shared.simulator.backend

import com.brainpillar.shared.simulator.QueuedAction
import com.brainpillar.shared.simulator.QueuedActionType
import com.brainpillar.shared.simulator.SimulatorEffect
import com.brainpillar.shared.simulator.SimulatorState

/**
 * Uebersetzt Simulator-Effects in Backend-Aufrufe.
 * Android-frei, synchron, testbar.
 *
 * Jeder dispatch-Aufruf gibt eine Liste von DispatchResult zurueck,
 * die der Aufrufer weiterverarbeiten kann (z.B. fuer UI-Updates oder Retry-Logik).
 */
class EffectDispatcher(private val client: BackendClient) {

    /**
     * Verarbeitet alle Effects einer Transition und fuehrt Backend-Aufrufe aus.
     * Gibt pro verarbeiteten Effect ein DispatchResult zurueck.
     */
    fun dispatch(effects: List<SimulatorEffect>, state: SimulatorState): List<DispatchResult> {
        return effects.mapNotNull { effect -> dispatchSingle(effect, state) }
    }

    private fun dispatchSingle(effect: SimulatorEffect, state: SimulatorState): DispatchResult? {
        return when (effect) {
            is SimulatorEffect.ExportInProgress -> {
                val request = ExportRequest(
                    projectId = state.projectId ?: "",
                    photoCount = state.photoCount,
                    transcriptionChunkCount = state.transcriptionChunkCount,
                    hasTranscription = state.hasTranscription
                )
                when (val result = client.exportProject(request)) {
                    is BackendResult.Success -> DispatchResult.ExportSuccess(result.data)
                    is BackendResult.Failure -> DispatchResult.ExportError(result.error)
                }
            }

            is SimulatorEffect.FlushQueue -> {
                val results = effect.actions.map { action -> dispatchQueuedAction(action, state) }
                DispatchResult.QueueFlushed(results)
            }

            is SimulatorEffect.EnqueueAction -> {
                // Nur loggen, wird spaeter bei FlushQueue ausgefuehrt
                DispatchResult.ActionQueued(effect.action)
            }

            is SimulatorEffect.AiEvaluationTriggered -> {
                val request = AiEvaluationRequest(
                    projectId = state.projectId ?: "",
                    evaluationType = effect.evaluationType.name,
                    contextSummary = effect.contextSummary
                )
                when (val result = client.requestEvaluation(request)) {
                    is BackendResult.Success -> DispatchResult.EvaluationSuccess(result.data)
                    is BackendResult.Failure -> DispatchResult.EvaluationError(result.error)
                }
            }

            // Effects ohne Backend-Aufruf
            is SimulatorEffect.EmitHint,
            is SimulatorEffect.Log,
            is SimulatorEffect.Warning,
            is SimulatorEffect.ChecklistResult,
            is SimulatorEffect.ExportDone,
            is SimulatorEffect.ScheduleRetry,
            is SimulatorEffect.ExportAborted -> null
        }
    }

    private fun dispatchQueuedAction(action: QueuedAction, state: SimulatorState): DispatchResult {
        return when (action.actionType) {
            QueuedActionType.PHOTO_UPLOAD -> {
                val request = PhotoUploadRequest(
                    projectId = state.projectId ?: "",
                    markerId = action.payload["markerId"],
                    timestampUtcMillis = action.queuedAtUtcMillis
                )
                when (val result = client.uploadPhoto(request)) {
                    is BackendResult.Success -> DispatchResult.PhotoUploaded(result.data)
                    is BackendResult.Failure -> DispatchResult.PhotoError(result.error)
                }
            }

            QueuedActionType.TRANSCRIPTION_SYNC -> {
                val request = TranscriptionSyncRequest(
                    projectId = state.projectId ?: "",
                    chunks = listOf(
                        TranscriptionChunk(
                            chunkIndex = 0,
                            text = action.payload["chunkText"] ?: "",
                            timestampUtcMillis = action.queuedAtUtcMillis
                        )
                    )
                )
                when (val result = client.syncTranscription(request)) {
                    is BackendResult.Success -> DispatchResult.TranscriptionSynced(result.data)
                    is BackendResult.Failure -> DispatchResult.TranscriptionError(result.error)
                }
            }

            QueuedActionType.EXPORT -> {
                val request = ExportRequest(
                    projectId = action.payload["projectId"] ?: state.projectId ?: "",
                    photoCount = state.photoCount,
                    transcriptionChunkCount = state.transcriptionChunkCount,
                    hasTranscription = action.payload["hasTranscription"]?.toBooleanStrictOrNull() ?: false
                )
                when (val result = client.exportProject(request)) {
                    is BackendResult.Success -> DispatchResult.ExportSuccess(result.data)
                    is BackendResult.Failure -> DispatchResult.ExportError(result.error)
                }
            }
        }
    }
}

/**
 * Ergebnis eines dispatched Effects.
 */
sealed interface DispatchResult {
    // Export
    data class ExportSuccess(val response: ExportResponse) : DispatchResult
    data class ExportError(val error: BackendError) : DispatchResult

    // Foto
    data class PhotoUploaded(val response: PhotoUploadResponse) : DispatchResult
    data class PhotoError(val error: BackendError) : DispatchResult

    // Transkript
    data class TranscriptionSynced(val response: TranscriptionSyncResponse) : DispatchResult
    data class TranscriptionError(val error: BackendError) : DispatchResult

    // KI-Bewertung
    data class EvaluationSuccess(val response: AiEvaluationResponse) : DispatchResult
    data class EvaluationError(val error: BackendError) : DispatchResult

    // Queue
    data class ActionQueued(val action: QueuedAction) : DispatchResult
    data class QueueFlushed(val results: List<DispatchResult>) : DispatchResult
}
