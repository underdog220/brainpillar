package com.brainpillar.watch.architecture.simulator

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
                        // Photo capture can happen during active recording or immediately before/after pause.
                        log(LogLevel.INFO, "Photo captured (markerId=${event.markerId ?: "null"})")
                        val subtitle = event.markerId
                            ?.takeIf { it.isNotBlank() }
                            ?.let { markerId -> "Marker: ${markerId.take(6)}" }
                            ?: "Foto markiert"
                        emitHint(
                            hintType = SimulatorHintType.REMINDER,
                            title = "Foto gespeichert",
                            subtitle = subtitle
                        )
                        state.copy(
                            lastPhotoMarkerId = event.markerId,
                            lastError = null
                        )
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
                        val subtitle = when {
                            isOffline && state.hasTranscription ->
                                "Offline+Transkript"
                            isOffline ->
                                "Offline-Export"
                            state.hasTranscription ->
                                "Export mit Transkript"
                            else ->
                                "Export bereit"
                        }
                        emitHint(
                            hintType = SimulatorHintType.FALLBACK,
                            title = "Abgeschlossen",
                            subtitle = subtitle,
                            isStale = isOffline
                        )
                        state.copy(
                            stage = SimulatorStage.Completed,
                            isRecording = false,
                            lastError = null
                        )
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
                val nowOffline = event.mode == NetworkMode.Offline
                // Kleiner Reminder nur beim Uebergang zu Offline (nicht bei jedem erneuten Offline-Event).
                if (nowOffline && !wasOffline) {
                    emitHint(
                        hintType = SimulatorHintType.REMINDER,
                        title = "Offline",
                        subtitle = "Verbindung eingeschraenkt"
                    )
                }
                state.copy(
                    lastNetworkMode = event.mode
                )
            }

            is TranscriptionUpdated -> {
                when (currentStage) {
                    SimulatorStage.RecordingActive,
                    SimulatorStage.Paused -> {
                        val chunkCount = state.transcriptionChunkCount + 1
                        val isStale = state.isTranscriptionStale(event.timestampUtcMillis)
                        log(LogLevel.DEBUG, "Transcription updated (len=${event.chunkText.length}, chunk=#$chunkCount)")

                        // Hint-Typ aus Chunk-Inhalt ableiten (simuliert)
                        val parsed = parseTranscriptionChunk(event.chunkText)
                        val confidence = if (event.chunkText.length >= 20)
                            SimulatorConfidenceLabel.PROBABLE else SimulatorConfidenceLabel.POSSIBLE

                        emitHint(
                            hintType = parsed.hintType,
                            title = parsed.title,
                            subtitle = parsed.subtitle,
                            confidenceLabel = confidence,
                            isStale = isStale,
                            ttlSec = TRANSCRIPTION_HINT_TTL_SEC
                        )

                        state.copy(
                            hasTranscription = true,
                            transcriptionChunkCount = chunkCount,
                            lastTranscriptionAtUtc = event.timestampUtcMillis,
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
        }

        return SimulationResult(
            newState = result,
            effects = effects.toList(),
            logs = logs.toList(),
            warnings = warnings.toList()
        )
    }
}

