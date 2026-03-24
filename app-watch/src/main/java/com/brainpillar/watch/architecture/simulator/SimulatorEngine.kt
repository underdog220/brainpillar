package com.brainpillar.watch.architecture.simulator

/**
 * Domain-only simulation engine.
 * Deterministic transition: state + event => newState + effects.
 */
class SimulatorEngine {

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
                // Not used yet for state changes, but supported by the API for later phases.
                when (currentStage) {
                    SimulatorStage.RecordingActive,
                    SimulatorStage.Paused -> {
                        log(LogLevel.DEBUG, "Transcription updated (len=${event.chunkText.length})")
                        state.copy(
                            hasTranscription = true,
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

