package com.brainpillar.watch.architecture.simulator

import com.brainpillar.watch.architecture.simulator.adapter.SimulatorToWatchHintMapper
import com.brainpillar.watch.architecture.simulator.SimulatorConfidenceLabel
import com.brainpillar.watch.architecture.simulator.SimulatorEffect
import com.brainpillar.watch.architecture.simulator.SimulatorHintType
import com.brainpillar.watch.architecture.simulator.SimulatorEffect.EmitHint
import com.brainpillar.watch.feature.hints.model.ConfidenceLabel
import com.brainpillar.watch.feature.hints.model.HintType
import com.brainpillar.watch.feature.hints.model.WatchHintUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SimulatorToWatchHintMapperTest {

    @Test
    fun effectsWithoutEmitHint_mapsToEmpty() {
        val effects = listOf(
            SimulatorEffect.Warning(message = "warn1"),
            SimulatorEffect.Log(level = LogLevel.INFO, message = "log1")
        )

        val uiState = SimulatorToWatchHintMapper.effectsToUiState(effects)
        assertTrue(uiState is WatchHintUiState.Empty)
    }

    @Test
    fun longHintTitleAndSubtitle_mapThroughWithoutLosingData() {
        val longTitle = "Herr Meier mit sehr langem Titel damit Wrapping getestet werden kann"
        val longSubtitle = "Dachsanierung und weitere Details (sehr lang) zur Textbreite"

        val effects = listOf(
            EmitHint(
                hintType = SimulatorHintType.PERSON,
                title = longTitle,
                subtitle = longSubtitle,
                confidenceLabel = SimulatorConfidenceLabel.PROBABLE,
                isStale = true,
                ttlSec = null
            )
        )

        val uiState = SimulatorToWatchHintMapper.effectsToUiState(effects)
        require(uiState is WatchHintUiState.Content)

        assertEquals(HintType.PERSON, uiState.hint.hintType)
        assertEquals(longTitle, uiState.hint.title)
        assertEquals(longSubtitle, uiState.hint.subtitle)
        assertEquals(ConfidenceLabel.PROBABLE, uiState.hint.confidenceLabel)
        assertEquals(true, uiState.hint.isStale)
    }

    @Test
    fun nullSubtitle_mapsToNullSubtitleInUiModel() {
        val effects = listOf(
            EmitHint(
                hintType = SimulatorHintType.TOPIC,
                title = "Letztes Thema",
                subtitle = null,
                confidenceLabel = null,
                isStale = false,
                ttlSec = null
            )
        )

        val uiState = SimulatorToWatchHintMapper.effectsToUiState(effects)
        require(uiState is WatchHintUiState.Content)

        assertEquals("Letztes Thema", uiState.hint.title)
        assertEquals(null, uiState.hint.subtitle)
    }
}

