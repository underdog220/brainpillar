package com.brainpillar.watch.architecture.simulator.adapter

import com.brainpillar.shared.simulator.SimulatorConfidenceLabel
import com.brainpillar.shared.simulator.SimulatorEffect
import com.brainpillar.shared.simulator.SimulatorHintType
import com.brainpillar.shared.simulator.SimulatorState
import com.brainpillar.watch.feature.hints.model.ConfidenceLabel
import com.brainpillar.watch.feature.hints.model.HintType
import com.brainpillar.watch.feature.hints.model.WatchHintModel
import com.brainpillar.watch.feature.hints.model.WatchHintUiState
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Thin mapping layer from simulator-domain outputs to watch UI state.
 * Keep this file minimal and side-effect free.
 */
object SimulatorToWatchHintMapper {

    private val utcFormatter = DateTimeFormatter.ISO_INSTANT

    /**
     * Konvertiert Effects zu UI-State. Optional: currentState fuer generatedAtUtc.
     */
    fun effectsToUiState(
        effects: List<SimulatorEffect>,
        currentState: SimulatorState? = null
    ): WatchHintUiState {
        val lastHint = effects.lastOrNull { it is SimulatorEffect.EmitHint } as? SimulatorEffect.EmitHint
        return if (lastHint != null) {
            val generatedAt = currentState?.lastTranscriptionAtUtc?.let { millis ->
                utcFormatter.format(Instant.ofEpochMilli(millis))
            }
            WatchHintUiState.Content(
                WatchHintModel(
                    hintType = lastHint.hintType.toWatchHintType(),
                    title = lastHint.title,
                    subtitle = lastHint.subtitle,
                    confidenceLabel = lastHint.confidenceLabel?.toWatchConfidenceLabel(),
                    isStale = lastHint.isStale,
                    generatedAtUtc = generatedAt
                )
            )
        } else {
            WatchHintUiState.Empty
        }
    }

    private fun SimulatorHintType.toWatchHintType(): HintType =
        when (this) {
            SimulatorHintType.PERSON -> HintType.PERSON
            SimulatorHintType.TOPIC -> HintType.TOPIC
            SimulatorHintType.REMINDER -> HintType.REMINDER
            SimulatorHintType.FALLBACK -> HintType.FALLBACK
        }

    private fun SimulatorConfidenceLabel.toWatchConfidenceLabel(): ConfidenceLabel =
        when (this) {
            SimulatorConfidenceLabel.PROBABLE -> ConfidenceLabel.PROBABLE
            SimulatorConfidenceLabel.POSSIBLE -> ConfidenceLabel.POSSIBLE
        }
}
