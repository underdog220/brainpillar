package com.brainpillar.shared.simulator

/**
 * Domain-only simulation engine.
 * Deterministic transition: state + event => newState + effects.
 */
class SimulatorEngine {

    companion object {
        /** TTL fuer Transcription-Hints in Sekunden */
        const val TRANSCRIPTION_HINT_TTL_SEC = 30
    }

    /**
     * Simuliert Chunk-Parsing: erkennt Praefix-basiert Person/Thema,
     * sonst Fallback. In spaeterer Phase durch echtes NLP ersetzt.
     */
    private data class ParsedChunk(
        val hintType: SimulatorHintType,
        val title: String,
        val subtitle: String?
    )

    private fun parseTranscriptionChunk(chunkText: String): ParsedChunk {
        val trimmed = chunkText.trim()
        return when {
            trimmed.startsWith("Person:", ignoreCase = true) -> ParsedChunk(
                hintType = SimulatorHintType.PERSON,
                title = trimmed.removePrefix("Person:").trim().take(30).ifBlank { "Erkannt" },
                subtitle = "Person erkannt"
            )
            trimmed.startsWith("Thema:", ignoreCase = true) -> ParsedChunk(
                hintType = SimulatorHintType.TOPIC,
                title = trimmed.removePrefix("Thema:").trim().take(30).ifBlank { "Neues Thema" },
                subtitle = "Thema erkannt"
            )
            else -> ParsedChunk(
                hintType = SimulatorHintType.FALLBACK,
                title = "Transkript",
                subtitle = trimmed.take(40).let { if (trimmed.length > 40) "$it..." else it }
            )
        }
    }

    /**
     * Baut eine Checkliste basierend auf dem aktuellen Projekt-State.
     */
    private fun buildChecklist(state: SimulatorState): List<ChecklistItem> = listOf(
        ChecklistItem("Projekt angelegt", state.projectId != null),
        ChecklistItem("Aufnahme gestartet", state.stage != SimulatorStage.ProjectRunning),
        ChecklistItem("Mindestens 1 Foto", state.photoCount > 0),
        ChecklistItem("Transkription vorhanden", state.hasTranscription),
        ChecklistItem("Mindestens 3 Chunks", state.transcriptionChunkCount >= 3),
        ChecklistItem("Projekt abgeschlossen", state.stage == SimulatorStage.Completed),
        ChecklistItem("Alle Uploads synchronisiert", !state.hasPendingActions),
        ChecklistItem("Export erfolgreich", state.exportState.status == ExportStatus.COMPLETED)
    )

    /**
     * Baut eine Kontext-Zusammenfassung fuer die KI-Bewertung.
     */
    private fun buildEvaluationContext(state: SimulatorState): String = buildString {
        append("Projekt: ${state.projectId ?: "unbekannt"}")
        append(", Stage: ${state.stage}")
        append(", Fotos: ${state.photoCount}")
        append(", Transkript-Chunks: ${state.transcriptionChunkCount}")
        append(", Netzwerk: ${state.lastNetworkMode}")
        if (state.hasPendingActions) {
            append(", Wartend: ${state.pendingQueue.size}")
        }
    }

    fun transition(state: SimulatorState, event: SimulatorEvent): SimulationResult {
        val effects = mutableListOf<SimulatorEffect>()
        val logs = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        fun emitHint(
            hintType: SimulatorHintType,
            title: String,
            subtitle: String?,
            confidenceLabel: SimulatorConfidenceLabel? = null,
            isStale: Boolean = false,
            ttlSec: Int? = 20
        ) {
            effects += SimulatorEffect.EmitHint(
                hintType = hintType,
                title = title,
                subtitle = subtitle,
                confidenceLabel = confidenceLabel,
                isStale = isStale,
                ttlSec = ttlSec
            )
        }

        fun log(level: LogLevel, message: String) {
            effects += SimulatorEffect.Log(level = level, message = message)
            logs += message
        }

        fun warn(message: String) {
            effects += SimulatorEffect.Warning(message = message)
            warnings += message
        }

        val currentStage = state.stage
        val result = when (event) {
            is StartProject -> {
                if (currentStage != SimulatorStage.Idle) {
                    warn("StartProject is not allowed in stage=$currentStage")
                    state
                } else {
                    log(LogLevel.INFO, "Project started: ${event.projectId}")
                    emitHint(
                        hintType = SimulatorHintType.FALLBACK,
                        title = "Projekt gestartet",
                        subtitle = "Start bereit"
                    )
                    state.copy(
                        stage = SimulatorStage.ProjectRunning,
                        projectId = event.projectId,
                        isRecording = false,
                        lastPhotoMarkerId = null,
                        hasTranscription = false,
                        transcriptionChunkCount = 0,
                        lastTranscriptionAtUtc = null,
                        pendingQueue = emptyList(),
                        lastChecklistId = null,
                        lastEvaluationType = null,
                        photoCount = 0,
                        exportState = ExportState(),
                        lastError = null
                    )
                }
            }

            is StartRecording -> {
                if (currentStage != SimulatorStage.ProjectRunning) {
                    warn("StartRecording is not allowed in stage=$currentStage")
                    state
                } else {
                    log(LogLevel.INFO, "Recording started")
                    emitHint(
                        hintType = SimulatorHintType.REMINDER,
                        title = "Aufnahme laeuft",
                        subtitle = null
                    )
                    state.copy(
                        stage = SimulatorStage.RecordingActive,
                        isRecording = true,
                        lastError = null
                    )
                }
            }

            is PauseRecording -> {
                if (currentStage != SimulatorStage.RecordingActive) {
                    warn("PauseRecording is not allowed in stage=$currentStage")
                    state
                } else {
                    log(LogLevel.INFO, "Recording paused")
                    emitHint(
                        hintType = SimulatorHintType.REMINDER,
                        title = "Pause",
                        subtitle = "Mitschrift pausiert"
                    )
                    state.copy(
                        stage = SimulatorStage.Paused,
                        isRecording = false,
                        lastError = null
                    )
                }
            }

            is ResumeRecording -> {
                if (currentStage != SimulatorStage.Paused) {
                    warn("ResumeRecording is not allowed in stage=$currentStage")
                    state
                } else {
                    log(LogLevel.INFO, "Recording resumed")
                    emitHint(
                        hintType = SimulatorHintType.REMINDER,
                        title = "Weiter",
                        subtitle = "Aufnahme laeuft"
                    )
                    state.copy(
                        stage = SimulatorStage.RecordingActive,
                        isRecording = true,
                        lastError = null
                    )
                }
            }

            is CapturePhoto -> {
                when (currentStage) {
                    SimulatorStage.RecordingActive,
                    SimulatorStage.Paused -> {
                        log(LogLevel.INFO, "Photo captured (markerId=${event.markerId ?: "null"})")
                        val isOffline = state.lastNetworkMode == NetworkMode.Offline
                        val subtitle = event.markerId
                            ?.takeIf { it.isNotBlank() }
                            ?.let { markerId -> "Marker: ${markerId.take(6)}" }
                            ?: "Foto markiert"

                        if (isOffline) {
                            // Offline: Foto lokal speichern, Upload queuen
                            val queued = QueuedAction(
                                actionType = QueuedActionType.PHOTO_UPLOAD,
                                label = "Foto-Upload: ${event.markerId ?: "ohne Marker"}",
                                payload = mapOf("markerId" to (event.markerId ?: "")),
                                queuedAtUtcMillis = event.timestampUtcMillis
                            )
                            effects += SimulatorEffect.EnqueueAction(queued)
                            emitHint(
                                hintType = SimulatorHintType.REMINDER,
                                title = "Foto gespeichert",
                                subtitle = "$subtitle (Upload wartet)"
                            )
                            state.copy(
                                lastPhotoMarkerId = event.markerId,
                                photoCount = state.photoCount + 1,
                                pendingQueue = state.pendingQueue + queued,
                                lastError = null
                            )
                        } else {
                            emitHint(
                                hintType = SimulatorHintType.REMINDER,
                                title = "Foto gespeichert",
                                subtitle = subtitle
                            )
                            state.copy(
                                lastPhotoMarkerId = event.markerId,
                                photoCount = state.photoCount + 1,
                                lastError = null
                            )
                        }
                    }

                    SimulatorStage.Idle,
                    SimulatorStage.ProjectRunning,
                    SimulatorStage.Completed -> {
                        warn("CapturePhoto is not allowed in stage=$currentStage")
                        state
                    }
                }
            }

            is FinishProject -> {
                when (currentStage) {
                    SimulatorStage.ProjectRunning,
                    SimulatorStage.RecordingActive,
                    SimulatorStage.Paused -> {
                        log(LogLevel.INFO, "Project finished")
                        val isOffline = state.lastNetworkMode == NetworkMode.Offline
                        val isHybrid = state.lastNetworkMode == NetworkMode.Hybrid

                        if (isOffline || isHybrid) {
                            // Offline/Hybrid: Export queuen statt sofort ausfuehren
                            val queued = QueuedAction(
                                actionType = QueuedActionType.EXPORT,
                                label = "Export: ${state.projectId ?: "unbekannt"}",
                                payload = mapOf(
                                    "projectId" to (state.projectId ?: ""),
                                    "hasTranscription" to state.hasTranscription.toString()
                                ),
                                queuedAtUtcMillis = event.timestampUtcMillis
                            )
                            effects += SimulatorEffect.EnqueueAction(queued)
                            val pendingCount = state.pendingQueue.size + 1
                            val subtitle = when {
                                isOffline && state.hasTranscription ->
                                    "Offline+Transkript (Export gequeuet, $pendingCount wartend)"
                                isOffline ->
                                    "Offline-Export gequeuet ($pendingCount wartend)"
                                state.hasTranscription ->
                                    "Hybrid: Export gequeuet ($pendingCount wartend)"
                                else ->
                                    "Hybrid: Export gequeuet ($pendingCount wartend)"
                            }
                            emitHint(
                                hintType = SimulatorHintType.FALLBACK,
                                title = "Abgeschlossen",
                                subtitle = subtitle,
                                isStale = true
                            )
                            state.copy(
                                stage = SimulatorStage.Completed,
                                isRecording = false,
                                pendingQueue = state.pendingQueue + queued,
                                lastError = null
                            )
                        } else {
                            val subtitle = if (state.hasTranscription)
                                "Export mit Transkript" else "Export bereit"
                            emitHint(
                                hintType = SimulatorHintType.FALLBACK,
                                title = "Abgeschlossen",
                                subtitle = subtitle,
                                isStale = false
                            )
                            state.copy(
                                stage = SimulatorStage.Completed,
                                isRecording = false,
                                lastError = null
                            )
                        }
                    }

                    SimulatorStage.Idle,
                    SimulatorStage.Completed -> {
                        warn("FinishProject is not allowed in stage=$currentStage")
                        state
                    }
                }
            }

            is NetworkModeChanged -> {
                log(LogLevel.DEBUG, "Network mode changed: ${event.mode}")
                val wasOffline = state.lastNetworkMode == NetworkMode.Offline
                val wasHybrid = state.lastNetworkMode == NetworkMode.Hybrid
                val nowOffline = event.mode == NetworkMode.Offline
                val nowOnline = event.mode == NetworkMode.Online

                when {
                    // Uebergang zu Offline
                    nowOffline && !wasOffline -> {
                        emitHint(
                            hintType = SimulatorHintType.REMINDER,
                            title = "Offline",
                            subtitle = "Verbindung eingeschraenkt"
                        )
                    }
                    // Rueckkehr zu Online: Queue flushen
                    nowOnline && (wasOffline || wasHybrid) && state.hasPendingActions -> {
                        log(LogLevel.INFO, "Online: ${state.pendingQueue.size} gepufferte Aktionen werden ausgefuehrt")
                        effects += SimulatorEffect.FlushQueue(state.pendingQueue)
                        emitHint(
                            hintType = SimulatorHintType.REMINDER,
                            title = "Online",
                            subtitle = "${state.pendingQueue.size} Aktionen synchronisiert"
                        )
                    }
                    // Rueckkehr zu Online ohne Queue
                    nowOnline && (wasOffline || wasHybrid) -> {
                        emitHint(
                            hintType = SimulatorHintType.REMINDER,
                            title = "Online",
                            subtitle = "Verbindung wiederhergestellt"
                        )
                    }
                }
                state.copy(
                    lastNetworkMode = event.mode,
                    // Queue leeren bei Online-Rueckkehr
                    pendingQueue = if (nowOnline) emptyList() else state.pendingQueue
                )
            }

            is TranscriptionUpdated -> {
                when (currentStage) {
                    SimulatorStage.RecordingActive,
                    SimulatorStage.Paused -> {
                        val chunkCount = state.transcriptionChunkCount + 1
                        val isStale = state.isTranscriptionStale(event.timestampUtcMillis)
                        val isOffline = state.lastNetworkMode == NetworkMode.Offline
                        log(LogLevel.DEBUG, "Transcription updated (len=${event.chunkText.length}, chunk=#$chunkCount)")

                        // Hint-Typ aus Chunk-Inhalt ableiten (simuliert)
                        val parsed = parseTranscriptionChunk(event.chunkText)
                        val confidence = if (event.chunkText.length >= 20)
                            SimulatorConfidenceLabel.PROBABLE else SimulatorConfidenceLabel.POSSIBLE

                        // Hint wird immer lokal angezeigt (auch offline)
                        val subtitleSuffix = if (isOffline) " (lokal)" else ""
                        emitHint(
                            hintType = parsed.hintType,
                            title = parsed.title,
                            subtitle = (parsed.subtitle ?: "") + subtitleSuffix,
                            confidenceLabel = confidence,
                            isStale = isStale,
                            ttlSec = TRANSCRIPTION_HINT_TTL_SEC
                        )

                        // Offline: Transkript-Sync queuen
                        val newQueue = if (isOffline) {
                            val queued = QueuedAction(
                                actionType = QueuedActionType.TRANSCRIPTION_SYNC,
                                label = "Transkript-Sync: Chunk #$chunkCount",
                                payload = mapOf("chunkText" to event.chunkText.take(100)),
                                queuedAtUtcMillis = event.timestampUtcMillis
                            )
                            effects += SimulatorEffect.EnqueueAction(queued)
                            state.pendingQueue + queued
                        } else {
                            state.pendingQueue
                        }

                        state.copy(
                            hasTranscription = true,
                            transcriptionChunkCount = chunkCount,
                            lastTranscriptionAtUtc = event.timestampUtcMillis,
                            pendingQueue = newQueue,
                            lastError = null
                        )
                    }

                    SimulatorStage.Idle,
                    SimulatorStage.ProjectRunning,
                    SimulatorStage.Completed -> {
                        warn("TranscriptionUpdated is not allowed in stage=$currentStage")
                        state
                    }
                }
            }

            is ChecklistRequested -> {
                // Erlaubt in allen aktiven Stages (nicht Idle)
                when (currentStage) {
                    SimulatorStage.ProjectRunning,
                    SimulatorStage.RecordingActive,
                    SimulatorStage.Paused,
                    SimulatorStage.Completed -> {
                        log(LogLevel.INFO, "Checklist requested: ${event.checklistId}")

                        // Checkliste basierend auf aktuellem State generieren
                        val items = buildChecklist(state)
                        effects += SimulatorEffect.ChecklistResult(
                            checklistId = event.checklistId,
                            items = items
                        )

                        val checkedCount = items.count { it.checked }
                        emitHint(
                            hintType = SimulatorHintType.REMINDER,
                            title = "Checkliste",
                            subtitle = "$checkedCount/${items.size} erledigt"
                        )

                        state.copy(
                            lastChecklistId = event.checklistId,
                            lastError = null
                        )
                    }

                    SimulatorStage.Idle -> {
                        warn("ChecklistRequested is not allowed in stage=$currentStage")
                        state
                    }
                }
            }

            is ExportStarted -> {
                when {
                    currentStage != SimulatorStage.Completed -> {
                        warn("ExportStarted is not allowed in stage=$currentStage")
                        state
                    }
                    state.exportState.status == ExportStatus.EXPORTING -> {
                        warn("Export laeuft bereits")
                        state
                    }
                    state.exportState.isTerminal -> {
                        warn("Export bereits abgeschlossen oder endgueltig fehlgeschlagen")
                        state
                    }
                    state.lastNetworkMode == NetworkMode.Offline -> {
                        warn("Export nicht moeglich: Offline")
                        emitHint(
                            hintType = SimulatorHintType.REMINDER,
                            title = "Export",
                            subtitle = "Offline - nicht moeglich",
                            isStale = true
                        )
                        state.copy(lastError = "Export requires network")
                    }
                    else -> {
                        log(LogLevel.INFO, "Export started for project: ${state.projectId}")
                        effects += SimulatorEffect.ExportInProgress(state.projectId ?: "")
                        emitHint(
                            hintType = SimulatorHintType.FALLBACK,
                            title = "Export",
                            subtitle = "Wird hochgeladen..."
                        )
                        state.copy(
                            exportState = state.exportState.copy(
                                status = ExportStatus.EXPORTING,
                                lastAttemptAtUtc = event.timestampUtcMillis
                            ),
                            lastError = null
                        )
                    }
                }
            }

            is ExportCompleted -> {
                if (state.exportState.status != ExportStatus.EXPORTING) {
                    warn("ExportCompleted ohne laufenden Export")
                    state
                } else {
                    log(LogLevel.INFO, "Export completed successfully")
                    effects += SimulatorEffect.ExportDone(state.projectId ?: "")
                    emitHint(
                        hintType = SimulatorHintType.FALLBACK,
                        title = "Export fertig",
                        subtitle = "Erfolgreich hochgeladen"
                    )
                    state.copy(
                        exportState = state.exportState.copy(
                            status = ExportStatus.COMPLETED,
                            lastFailureReason = null
                        ),
                        lastError = null
                    )
                }
            }

            is ExportFailed -> {
                if (state.exportState.status != ExportStatus.EXPORTING) {
                    warn("ExportFailed ohne laufenden Export")
                    state
                } else {
                    val newRetryCount = state.exportState.retryCount + 1
                    log(LogLevel.WARN, "Export failed: ${event.reason} (attempt $newRetryCount)")

                    if (event.isRetryable && newRetryCount < ExportState.MAX_RETRIES) {
                        // Retry planen
                        val updatedExport = state.exportState.copy(
                            status = ExportStatus.RETRY_SCHEDULED,
                            retryCount = newRetryCount,
                            lastFailureReason = event.reason
                        )
                        val delayMs = updatedExport.nextRetryDelayMs()
                        effects += SimulatorEffect.ScheduleRetry(
                            delayMs = delayMs,
                            attempt = newRetryCount + 1
                        )
                        emitHint(
                            hintType = SimulatorHintType.REMINDER,
                            title = "Export fehlgeschlagen",
                            subtitle = "Retry #${newRetryCount + 1} in ${delayMs / 1000}s"
                        )
                        state.copy(
                            exportState = updatedExport,
                            lastError = event.reason
                        )
                    } else {
                        // Endgueltig fehlgeschlagen
                        effects += SimulatorEffect.ExportAborted(
                            reason = event.reason,
                            attempts = newRetryCount
                        )
                        emitHint(
                            hintType = SimulatorHintType.FALLBACK,
                            title = "Export abgebrochen",
                            subtitle = "Nach $newRetryCount Versuchen: ${event.reason}",
                            isStale = true
                        )
                        state.copy(
                            exportState = state.exportState.copy(
                                status = ExportStatus.FAILED_PERMANENT,
                                retryCount = newRetryCount,
                                lastFailureReason = event.reason
                            ),
                            lastError = event.reason
                        )
                    }
                }
            }

            is ExportRetry -> {
                when {
                    state.exportState.status != ExportStatus.RETRY_SCHEDULED -> {
                        warn("ExportRetry ohne geplanten Retry")
                        state
                    }
                    state.lastNetworkMode == NetworkMode.Offline -> {
                        warn("ExportRetry nicht moeglich: Offline")
                        emitHint(
                            hintType = SimulatorHintType.REMINDER,
                            title = "Retry verschoben",
                            subtitle = "Warte auf Netzwerk"
                        )
                        state.copy(lastError = "Retry postponed: offline")
                    }
                    else -> {
                        log(LogLevel.INFO, "Export retry #${state.exportState.retryCount + 1}")
                        effects += SimulatorEffect.ExportInProgress(state.projectId ?: "")
                        emitHint(
                            hintType = SimulatorHintType.FALLBACK,
                            title = "Export Retry",
                            subtitle = "Versuch #${state.exportState.retryCount + 1}..."
                        )
                        state.copy(
                            exportState = state.exportState.copy(
                                status = ExportStatus.EXPORTING,
                                lastAttemptAtUtc = event.timestampUtcMillis
                            ),
                            lastError = null
                        )
                    }
                }
            }

            is AiEvaluationRequested -> {
                // Erlaubt in RecordingActive, Paused und Completed
                when (currentStage) {
                    SimulatorStage.RecordingActive,
                    SimulatorStage.Paused,
                    SimulatorStage.Completed -> {
                        val isOffline = state.lastNetworkMode == NetworkMode.Offline
                        log(LogLevel.INFO, "AI evaluation requested: ${event.evaluationType}")

                        if (isOffline) {
                            // Offline: KI-Bewertung nicht moeglich
                            warn("AiEvaluation nicht moeglich: Offline")
                            emitHint(
                                hintType = SimulatorHintType.REMINDER,
                                title = "KI-Bewertung",
                                subtitle = "Offline - nicht verfuegbar",
                                isStale = true
                            )
                            state.copy(lastError = "AI evaluation requires network")
                        } else {
                            // Online/Hybrid: Bewertung triggern
                            val contextSummary = buildEvaluationContext(state)
                            effects += SimulatorEffect.AiEvaluationTriggered(
                                evaluationType = event.evaluationType,
                                contextSummary = contextSummary
                            )

                            val typeLabel = when (event.evaluationType) {
                                EvaluationType.QUALITY -> "Qualitaet"
                                EvaluationType.COMPLETENESS -> "Vollstaendigkeit"
                                EvaluationType.SUMMARY -> "Zusammenfassung"
                            }
                            emitHint(
                                hintType = SimulatorHintType.TOPIC,
                                title = "KI: $typeLabel",
                                subtitle = "Bewertung gestartet...",
                                confidenceLabel = SimulatorConfidenceLabel.PROBABLE
                            )

                            state.copy(
                                lastEvaluationType = event.evaluationType,
                                lastError = null
                            )
                        }
                    }

                    SimulatorStage.Idle,
                    SimulatorStage.ProjectRunning -> {
                        warn("AiEvaluationRequested is not allowed in stage=$currentStage")
                        state
                    }
                }
            }
        }

        return SimulationResult(
            newState = result,
            effects = effects.toList(),
            logs = logs.toList(),
            warnings = warnings.toList()
        )
    }
}
