package com.brainpillar.watch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.union
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.MaterialTheme
import com.brainpillar.watch.architecture.simulator.debug.SimulatorDebugOverlay
import com.brainpillar.watch.feature.hints.model.WatchHintUiState
import com.brainpillar.watch.feature.hints.ui.HintCardScreen
import com.brainpillar.watch.architecture.simulator.CapturePhoto
import com.brainpillar.watch.architecture.simulator.FinishProject
import com.brainpillar.watch.architecture.simulator.PauseRecording
import com.brainpillar.watch.architecture.simulator.ResumeRecording
import com.brainpillar.watch.architecture.simulator.NetworkMode
import com.brainpillar.watch.architecture.simulator.NetworkModeChanged
import com.brainpillar.watch.architecture.simulator.SimulatorEngine
import com.brainpillar.watch.architecture.simulator.SimulatorState
import com.brainpillar.watch.architecture.simulator.StartProject
import com.brainpillar.watch.architecture.simulator.StartRecording
import com.brainpillar.watch.architecture.simulator.TranscriptionUpdated
import com.brainpillar.watch.architecture.simulator.ChecklistRequested
import com.brainpillar.watch.architecture.simulator.AiEvaluationRequested
import com.brainpillar.watch.architecture.simulator.EvaluationType
import com.brainpillar.watch.architecture.simulator.ExportStarted
import com.brainpillar.watch.architecture.simulator.ExportFailed
import com.brainpillar.watch.architecture.simulator.ExportRetry
import com.brainpillar.watch.architecture.simulator.ExportCompleted
import com.brainpillar.watch.architecture.simulator.adapter.SimulatorToWatchHintMapper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Minimal demo integration: run one domain workflow in the simulator core
        // and map its last emitted hint to the existing Hint UI.
        val engine = SimulatorEngine()
        var simState = SimulatorState()
        val now = System.currentTimeMillis()
        var uiState: WatchHintUiState = WatchHintUiState.Empty

        // Phase 8: Demo mit Offline-Queueing und Online-Flush
        val events = listOf(
            StartProject(projectId = "demo-project", timestampUtcMillis = now),
            StartRecording(timestampUtcMillis = now + 1_000),
            CapturePhoto(markerId = "M1", timestampUtcMillis = now + 2_000),
            // Online-Transcriptions
            TranscriptionUpdated(chunkText = "Person: Dr. Mueller stellt sich vor", timestampUtcMillis = now + 5_000),
            TranscriptionUpdated(chunkText = "Thema: Statik und Tragwerksplanung", timestampUtcMillis = now + 10_000),
            // Netzwerk faellt aus
            NetworkModeChanged(mode = NetworkMode.Offline, timestampUtcMillis = now + 12_000),
            // Offline: Foto + Transcription werden gequeuet
            CapturePhoto(markerId = "M2", timestampUtcMillis = now + 15_000),
            TranscriptionUpdated(chunkText = "Deckenhoehe messen und dokumentieren", timestampUtcMillis = now + 18_000),
            // Checkliste waehrend Aufnahme pruefen
            ChecklistRequested(checklistId = "baucheck-1", timestampUtcMillis = now + 19_000),
            PauseRecording(timestampUtcMillis = now + 20_000),
            FinishProject(timestampUtcMillis = now + 22_000),
            // Netzwerk kehrt zurueck -> Queue wird geflusht
            NetworkModeChanged(mode = NetworkMode.Online, timestampUtcMillis = now + 30_000),
            // Phase 10: Export-Pipeline mit Retry
            ExportStarted(timestampUtcMillis = now + 31_000),
            ExportFailed(reason = "Timeout", isRetryable = true, timestampUtcMillis = now + 33_000),
            ExportRetry(timestampUtcMillis = now + 43_000),
            ExportCompleted(timestampUtcMillis = now + 45_000),
            // KI-Bewertung nach erfolgreichem Export
            AiEvaluationRequested(evaluationType = EvaluationType.QUALITY, timestampUtcMillis = now + 47_000)
        )

        val allWarnings = mutableListOf<String>()
        var lastEventLabel = "none"

        for (event in events) {
            lastEventLabel = event::class.simpleName ?: "Event"
            val result = engine.transition(simState, event)
            simState = result.newState
            uiState = SimulatorToWatchHintMapper.effectsToUiState(result.effects, simState)
            allWarnings += result.warnings
        }

        setContent {
            MaterialTheme {
                Box(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
                    // Make main content slightly more dominant.
                    HintCardScreen(
                        state = uiState,
                        modifier = Modifier.scale(1.07f)
                    )
                    if (BuildConfig.DEBUG) {
                        val hint = (uiState as? WatchHintUiState.Content)?.hint
                        val title = hint?.title
                        val subtitle = hint?.subtitle

                        val hintLine = when {
                            !title.isNullOrBlank() && !subtitle.isNullOrBlank() ->
                                "$title • $subtitle"
                            !title.isNullOrBlank() ->
                                title
                            !subtitle.isNullOrBlank() ->
                                subtitle
                            allWarnings.isNotEmpty() ->
                                "Warnings=${allWarnings.size}"
                            else ->
                                ""
                        }

                        val queueInfo = if (simState.hasPendingActions)
                            " Q=${simState.pendingQueue.size}" else ""
                        val debugLines: List<String> = when (simState.lastNetworkMode) {
                            NetworkMode.Offline -> listOfNotNull(
                                "Offline aktiv$queueInfo",
                                "Stage=${simState.stage} • Last=$lastEventLabel",
                                hintLine.takeIf { it.isNotBlank() }
                            )
                            else -> listOfNotNull(
                                "Stage=${simState.stage} • Net=${simState.lastNetworkMode}$queueInfo",
                                "Last=$lastEventLabel",
                                hintLine.takeIf { it.isNotBlank() }
                            )
                        }

                        val insets: androidx.compose.foundation.layout.PaddingValues = WindowInsets.safeDrawing
                            .union(WindowInsets.navigationBars)
                            .asPaddingValues()
                        SimulatorDebugOverlay(
                            lines = debugLines,
                            modifier = androidx.compose.ui.Modifier
                                .align(Alignment.TopCenter)
                                .padding(insets)
                                .padding(top = 6.dp)
                        )
                    }
                }
            }
        }
    }
}
