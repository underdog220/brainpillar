package com.brainpillar.watch.architecture.simulator.backend

/**
 * Backend-Client Interface fuer die Simulator-Domain.
 * Android-frei, damit Unit-Tests ohne Netzwerk moeglich sind.
 *
 * Implementierungen:
 * - StubBackendClient: Fuer Tests und Demo (deterministische Antworten)
 * - HttpBackendClient: Echte HTTP-Aufrufe (spaetere Phase, Android-Modul)
 */
interface BackendClient {

    /** Health-Check: Server erreichbar? */
    fun health(): BackendResult<HealthResponse>

    /** Projekt exportieren */
    fun exportProject(request: ExportRequest): BackendResult<ExportResponse>

    /** Transkript-Chunks synchronisieren */
    fun syncTranscription(request: TranscriptionSyncRequest): BackendResult<TranscriptionSyncResponse>

    /** Foto hochladen */
    fun uploadPhoto(request: PhotoUploadRequest): BackendResult<PhotoUploadResponse>

    /** KI-Bewertung anfordern */
    fun requestEvaluation(request: AiEvaluationRequest): BackendResult<AiEvaluationResponse>
}

/**
 * Ergebnis eines Backend-Aufrufs. Entweder Erfolg oder Fehler.
 */
sealed interface BackendResult<out T> {
    data class Success<T>(val data: T) : BackendResult<T>
    data class Failure(val error: BackendError) : BackendResult<Nothing>
}

/**
 * Backend-Konfiguration.
 */
data class BackendConfig(
    val baseUrl: String = "https://api.brainpillar.local",
    val apiToken: String = "",
    val timeoutMs: Long = 10_000,
    val maxRetries: Int = 3
)
