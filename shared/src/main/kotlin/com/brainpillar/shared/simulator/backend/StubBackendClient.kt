package com.brainpillar.shared.simulator.backend

/**
 * Stub-Implementierung fuer Tests und Demo.
 * Liefert deterministische Antworten, kein Netzwerk noetig.
 *
 * Ueber failNext* Felder koennen gezielt Fehler simuliert werden.
 */
class StubBackendClient : BackendClient {

    var failNextExport: BackendError? = null
    var failNextSync: BackendError? = null
    var failNextUpload: BackendError? = null
    var failNextEvaluation: BackendError? = null
    var failNextHealth: BackendError? = null

    /** Zaehler fuer Aufrufe (fuer Test-Assertions) */
    var exportCount = 0; private set
    var syncCount = 0; private set
    var uploadCount = 0; private set
    var evaluationCount = 0; private set
    var healthCount = 0; private set

    override fun health(): BackendResult<HealthResponse> {
        healthCount++
        failNextHealth?.let { err ->
            failNextHealth = null
            return BackendResult.Failure(err)
        }
        return BackendResult.Success(
            HealthResponse(
                status = "ok",
                version = "stub-1.0",
                timestampUtc = "2026-03-25T12:00:00Z"
            )
        )
    }

    override fun exportProject(request: ExportRequest): BackendResult<ExportResponse> {
        exportCount++
        failNextExport?.let { err ->
            failNextExport = null
            return BackendResult.Failure(err)
        }
        return BackendResult.Success(
            ExportResponse(
                exportId = "export-${request.projectId}-$exportCount",
                status = "completed",
                message = "Export erfolgreich"
            )
        )
    }

    override fun syncTranscription(request: TranscriptionSyncRequest): BackendResult<TranscriptionSyncResponse> {
        syncCount++
        failNextSync?.let { err ->
            failNextSync = null
            return BackendResult.Failure(err)
        }
        return BackendResult.Success(
            TranscriptionSyncResponse(
                syncedCount = request.chunks.size,
                status = "synced"
            )
        )
    }

    override fun uploadPhoto(request: PhotoUploadRequest): BackendResult<PhotoUploadResponse> {
        uploadCount++
        failNextUpload?.let { err ->
            failNextUpload = null
            return BackendResult.Failure(err)
        }
        return BackendResult.Success(
            PhotoUploadResponse(
                photoId = "photo-${request.markerId ?: "auto"}-$uploadCount",
                status = "uploaded"
            )
        )
    }

    override fun requestEvaluation(request: AiEvaluationRequest): BackendResult<AiEvaluationResponse> {
        evaluationCount++
        failNextEvaluation?.let { err ->
            failNextEvaluation = null
            return BackendResult.Failure(err)
        }
        return BackendResult.Success(
            AiEvaluationResponse(
                evaluationId = "eval-$evaluationCount",
                score = 85,
                verdict = "good",
                summary = "Projekt-Dokumentation vollstaendig und qualitativ hochwertig."
            )
        )
    }
}
