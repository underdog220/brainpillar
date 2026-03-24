package com.brainpillar.watch.architecture.simulator.adapter

import com.brainpillar.watch.architecture.simulator.SimulatorConfidenceLabel
import com.brainpillar.watch.architecture.simulator.SimulatorEffect
import com.brainpillar.watch.architecture.simulator.SimulatorHintType
import com.brainpillar.watch.feature.hints.model.ConfidenceLabel
import com.brainpillar.watch.feature.hints.model.HintType
import com.brainpillar.watch.feature.hints.model.WatchHintModel
import com.brainpillar.watch.feature.hints.model.WatchHintUiState

/**
 * Thin mapping layer from simulator-domain outputs to watch UI state.
 * Keep this file minimal and side-effect free.
 */
object SimulatorToWatchHintMapper {

    fun effectsToUiState(effects: List<SimulatorEffect>): WatchHintUiState {
        val lastHint = effects.lastOrNull { it is SimulatorEffect.EmitHint } as? SimulatorEffect.EmitHint
        return if (lastHint != null) {
            WatchHintUiState.Content(
                WatchHintModel(
                    hintType = lastHint.hintType.toWatchHintType(),
                    title = lastHint.title,
                    subtitle = lastHint.subtitle,
                    confidenceLabel = lastHint.confidenceLabel?.toWatchConfidenceLabel(),
                    isStale = lastHint.isStale,
                    generatedAtUtc = null
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

