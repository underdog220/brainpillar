package com.brainpillar.watch.architecture.simulator

/**
 * Export-Pipeline Status fuer ein Projekt.
 */
data class ExportState(
    val status: ExportStatus = ExportStatus.IDLE,
    val retryCount: Int = 0,
    val lastAttemptAtUtc: Long? = null,
    val lastFailureReason: String? = null
) {
    companion object {
        const val MAX_RETRIES = 3
        /** Minimaler Abstand zwischen Retries in Millisekunden (exponentiell: 5s, 10s, 20s) */
        const val BASE_RETRY_DELAY_MS = 5_000L
    }

    /**
     * Berechnet die naechste Retry-Verzoegerung (exponentielles Backoff).
     */
    fun nextRetryDelayMs(): Long =
        BASE_RETRY_DELAY_MS * (1L shl retryCount.coerceAtMost(4))

    val canRetry: Boolean get() = retryCount < MAX_RETRIES
    val isTerminal: Boolean get() = status == ExportStatus.COMPLETED || status == ExportStatus.FAILED_PERMANENT
}

enum class ExportStatus {
    IDLE,
    EXPORTING,
    COMPLETED,
    FAILED_RETRYABLE,
    FAILED_PERMANENT,
    RETRY_SCHEDULED
}
