package com.brainpillar.watch.architecture.simulator.backend

/**
 * Backend API Request/Response-Modelle.
 * Android-frei, reine Datenklassen fuer Serialisierung.
 */

// ===== Export =====

data class ExportRequest(
    val projectId: String,
    val photoCount: Int,
    val transcriptionChunkCount: Int,
    val hasTranscription: Boolean,
    val metadata: Map<String, String> = emptyMap()
)

data class ExportResponse(
    val exportId: String,
    val status: String,
    val message: String? = null
)

// ===== Transkript-Sync =====

data class TranscriptionSyncRequest(
    val projectId: String,
    val chunks: List<TranscriptionChunk>
)

data class TranscriptionChunk(
    val chunkIndex: Int,
    val text: String,
    val timestampUtcMillis: Long
)

data class TranscriptionSyncResponse(
    val syncedCount: Int,
    val status: String
)

// ===== Foto-Upload =====

data class PhotoUploadRequest(
    val projectId: String,
    val markerId: String?,
    val timestampUtcMillis: Long
)

data class PhotoUploadResponse(
    val photoId: String,
    val status: String
)

// ===== KI-Bewertung =====

data class AiEvaluationRequest(
    val projectId: String,
    val evaluationType: String,
    val contextSummary: String
)

data class AiEvaluationResponse(
    val evaluationId: String,
    val score: Int?,
    val verdict: String,
    val summary: String
)

// ===== Health =====

data class HealthResponse(
    val status: String,
    val version: String,
    val timestampUtc: String
)

// ===== Gemeinsam =====

data class BackendError(
    val code: String,
    val message: String,
    val isRetryable: Boolean = false
)
