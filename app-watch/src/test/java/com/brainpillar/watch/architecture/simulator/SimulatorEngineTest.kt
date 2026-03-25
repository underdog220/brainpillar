package com.brainpillar.watch.architecture.simulator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Test

class SimulatorEngineTest {

    private val engine = SimulatorEngine()

    @Test
    fun startProject_fromIdle_emitsHint_andTransitionsStage() {
        val initial = SimulatorState(stage = SimulatorStage.Idle)
        val result = engine.transition(
            state = initial,
            event = StartProject(projectId = "p1", timestampUtcMillis = 0L)
        )

        assertEquals(SimulatorStage.ProjectRunning, result.newState.stage)
        assertEquals("p1", result.newState.projectId)
        assertTrue(result.warnings.isEmpty())

        val hints = result.effects.filterIsInstance<SimulatorEffect.EmitHint>()
        assertEquals(1, hints.size)
        assertEquals(SimulatorHintType.FALLBACK, hints.single().hintType)
    }

    @Test
    fun startRecording_whenNotInProjectRunning_isInvalid() {
        val initial = SimulatorState(stage = SimulatorStage.Idle)
        val result = engine.transition(
            state = initial,
            event = StartRecording(timestampUtcMillis = 1L)
        )

        assertEquals(SimulatorStage.Idle, result.newState.stage)
        assertTrue(result.warnings.isNotEmpty())
        assertTrue(result.effects.any { it is SimulatorEffect.Warning })
        assertFalse(result.effects.any { it is SimulatorEffect.EmitHint })
    }

    @Test
    fun capturePhoto_isAllowedDuringRecordingAndPaused() {
        val active = SimulatorState(
            stage = SimulatorStage.RecordingActive,
            isRecording = true
        )
        val activeResult = engine.transition(
            state = active,
            event = CapturePhoto(markerId = "M1", timestampUtcMillis = 2L)
        )

        assertEquals(SimulatorStage.RecordingActive, activeResult.newState.stage)
        assertEquals("M1", activeResult.newState.lastPhotoMarkerId)
        assertTrue(activeResult.effects.any { it is SimulatorEffect.EmitHint })

        val paused = SimulatorState(
            stage = SimulatorStage.Paused,
            isRecording = false
        )
        val pausedResult = engine.transition(
            state = paused,
            event = CapturePhoto(markerId = null, timestampUtcMillis = 3L)
        )

        assertEquals(SimulatorStage.Paused, pausedResult.newState.stage)
        assertEquals(null, pausedResult.newState.lastPhotoMarkerId)
        assertTrue(pausedResult.effects.any { it is SimulatorEffect.EmitHint })
    }

    @Test
    fun capturePhoto_whenIdle_isInvalid_andEmitsWarningButNoHint() {
        val initial = SimulatorState(
            stage = SimulatorStage.Idle,
            isRecording = false
        )

        val result = engine.transition(
            state = initial,
            event = CapturePhoto(markerId = "M1", timestampUtcMillis = 0L)
        )

        assertEquals(SimulatorStage.Idle, result.newState.stage)
        assertNull(result.newState.lastPhotoMarkerId)
        assertTrue(result.warnings.isNotEmpty())
        assertTrue(result.effects.any { it is SimulatorEffect.Warning })
        assertFalse(result.effects.any { it is SimulatorEffect.EmitHint })
    }

    @Test
    fun startProject_whenAlreadyRunning_isInvalid_andEmitsWarningButNoHint() {
        val initial = SimulatorState(
            stage = SimulatorStage.ProjectRunning,
            projectId = "p1"
        )

        val result = engine.transition(
            state = initial,
            event = StartProject(projectId = "p2", timestampUtcMillis = 0L)
        )

        assertEquals(SimulatorStage.ProjectRunning, result.newState.stage)
        assertEquals("p1", result.newState.projectId) // state is unchanged on invalid event
        assertTrue(result.warnings.isNotEmpty())
        assertTrue(result.effects.any { it is SimulatorEffect.Warning })
        assertFalse(result.effects.any { it is SimulatorEffect.EmitHint })
    }

    @Test
    fun transcriptionUpdated_inIdle_isInvalid_andEmitsWarningButNoHint() {
        val initial = SimulatorState(
            stage = SimulatorStage.Idle,
            lastError = null
        )

        val result = engine.transition(
            state = initial,
            event = TranscriptionUpdated(chunkText = "hello", timestampUtcMillis = 0L)
        )

        assertEquals(SimulatorStage.Idle, result.newState.stage)
        assertTrue(result.warnings.isNotEmpty())
        assertTrue(result.effects.any { it is SimulatorEffect.Warning })
        assertFalse(result.effects.any { it is SimulatorEffect.EmitHint })
    }

    @Test
    fun transcriptionUpdated_inRecordingActive_emitsHint_andUpdatesState() {
        val initial = SimulatorState(
            stage = SimulatorStage.RecordingActive,
            isRecording = true,
            lastError = "stale-error"
        )

        val result = engine.transition(
            state = initial,
            event = TranscriptionUpdated(chunkText = "Kurzer Text", timestampUtcMillis = 1000L)
        )

        assertEquals(SimulatorStage.RecordingActive, result.newState.stage)
        assertNull(result.newState.lastError)
        assertTrue(result.newState.hasTranscription)
        assertEquals(1, result.newState.transcriptionChunkCount)
        assertEquals(1000L, result.newState.lastTranscriptionAtUtc)
        assertTrue(result.warnings.isEmpty())
        assertTrue(result.effects.any { it is SimulatorEffect.EmitHint })
        assertTrue(result.effects.any { it is SimulatorEffect.Log })
    }

    @Test
    fun transcriptionUpdated_withPersonPrefix_emitsPersonHint() {
        val initial = SimulatorState(
            stage = SimulatorStage.RecordingActive,
            isRecording = true
        )

        val result = engine.transition(
            state = initial,
            event = TranscriptionUpdated(
                chunkText = "Person: Dr. Mueller stellt sich vor",
                timestampUtcMillis = 1000L
            )
        )

        val hint = result.effects.filterIsInstance<SimulatorEffect.EmitHint>().single()
        assertEquals(SimulatorHintType.PERSON, hint.hintType)
        assertEquals("Dr. Mueller stellt sich vor", hint.title)
        assertEquals("Person erkannt", hint.subtitle)
        assertEquals(SimulatorConfidenceLabel.PROBABLE, hint.confidenceLabel)
    }

    @Test
    fun transcriptionUpdated_withThemaPrefix_emitsTopicHint() {
        val initial = SimulatorState(
            stage = SimulatorStage.RecordingActive,
            isRecording = true
        )

        val result = engine.transition(
            state = initial,
            event = TranscriptionUpdated(
                chunkText = "Thema: Statik und Tragwerk",
                timestampUtcMillis = 1000L
            )
        )

        val hint = result.effects.filterIsInstance<SimulatorEffect.EmitHint>().single()
        assertEquals(SimulatorHintType.TOPIC, hint.hintType)
        assertEquals("Statik und Tragwerk", hint.title)
        assertEquals("Thema erkannt", hint.subtitle)
    }

    @Test
    fun transcriptionUpdated_withoutPrefix_emitsFallbackHint() {
        val initial = SimulatorState(
            stage = SimulatorStage.RecordingActive,
            isRecording = true
        )

        val result = engine.transition(
            state = initial,
            event = TranscriptionUpdated(
                chunkText = "Decke messen",
                timestampUtcMillis = 1000L
            )
        )

        val hint = result.effects.filterIsInstance<SimulatorEffect.EmitHint>().single()
        assertEquals(SimulatorHintType.FALLBACK, hint.hintType)
        assertEquals("Transkript", hint.title)
    }

    @Test
    fun transcriptionUpdated_shortChunk_hasPossibleConfidence() {
        val initial = SimulatorState(
            stage = SimulatorStage.RecordingActive,
            isRecording = true
        )

        val result = engine.transition(
            state = initial,
            event = TranscriptionUpdated(chunkText = "kurz", timestampUtcMillis = 1000L)
        )

        val hint = result.effects.filterIsInstance<SimulatorEffect.EmitHint>().single()
        assertEquals(SimulatorConfidenceLabel.POSSIBLE, hint.confidenceLabel)
    }

    @Test
    fun transcriptionUpdated_longChunk_hasProbableConfidence() {
        val initial = SimulatorState(
            stage = SimulatorStage.RecordingActive,
            isRecording = true
        )

        val result = engine.transition(
            state = initial,
            event = TranscriptionUpdated(
                chunkText = "Dies ist ein langer Transkript-Chunk mit mehr als zwanzig Zeichen",
                timestampUtcMillis = 1000L
            )
        )

        val hint = result.effects.filterIsInstance<SimulatorEffect.EmitHint>().single()
        assertEquals(SimulatorConfidenceLabel.PROBABLE, hint.confidenceLabel)
    }

    @Test
    fun transcriptionUpdated_incrementsChunkCount() {
        var state = SimulatorState(
            stage = SimulatorStage.RecordingActive,
            isRecording = true
        )

        val r1 = engine.transition(state, TranscriptionUpdated("Chunk 1", 1000L))
        state = r1.newState
        assertEquals(1, state.transcriptionChunkCount)

        val r2 = engine.transition(state, TranscriptionUpdated("Chunk 2", 2000L))
        state = r2.newState
        assertEquals(2, state.transcriptionChunkCount)
        assertEquals(2000L, state.lastTranscriptionAtUtc)
    }

    @Test
    fun transcriptionStale_whenTtlExceeded() {
        val state = SimulatorState(
            stage = SimulatorStage.RecordingActive,
            isRecording = true,
            lastTranscriptionAtUtc = 1000L
        )

        // 31s spaeter -> stale (TTL=30s)
        assertTrue(state.isTranscriptionStale(32_000L))
        // 29s spaeter -> noch frisch
        assertFalse(state.isTranscriptionStale(30_000L))
    }

    @Test
    fun transcriptionStale_whenNoTranscription() {
        val state = SimulatorState(
            stage = SimulatorStage.RecordingActive,
            isRecording = true,
            lastTranscriptionAtUtc = null
        )
        assertTrue(state.isTranscriptionStale(1000L))
    }

    @Test
    fun transcriptionUpdated_afterLongPause_marksHintAsStale() {
        var state = SimulatorState(
            stage = SimulatorStage.RecordingActive,
            isRecording = true
        )

        // Erster Chunk bei t=1000
        val r1 = engine.transition(state, TranscriptionUpdated("Erster", 1000L))
        state = r1.newState
        val hint1 = r1.effects.filterIsInstance<SimulatorEffect.EmitHint>().single()
        // Erster Chunk: kein vorheriger -> stale (keine lastTranscriptionAtUtc davor)
        assertTrue(hint1.isStale)

        // Zweiter Chunk bei t=2000 (1s spaeter -> frisch)
        val r2 = engine.transition(state, TranscriptionUpdated("Zweiter", 2000L))
        state = r2.newState
        val hint2 = r2.effects.filterIsInstance<SimulatorEffect.EmitHint>().single()
        assertFalse(hint2.isStale)

        // Dritter Chunk bei t=62000 (60s spaeter -> stale)
        val r3 = engine.transition(state, TranscriptionUpdated("Spaet", 62_000L))
        val hint3 = r3.effects.filterIsInstance<SimulatorEffect.EmitHint>().single()
        assertTrue(hint3.isStale)
    }

    @Test
    fun networkModeChanged_updatesState_andDoesNotEmitHintOrWarnings() {
        val initial = SimulatorState(
            stage = SimulatorStage.ProjectRunning,
            lastNetworkMode = NetworkMode.Offline
        )

        val result = engine.transition(
            state = initial,
            event = NetworkModeChanged(
                mode = NetworkMode.Hybrid,
                timestampUtcMillis = 0L
            )
        )

        assertEquals(NetworkMode.Hybrid, result.newState.lastNetworkMode)
        assertTrue(result.warnings.isEmpty())
        assertFalse(result.effects.any { it is SimulatorEffect.Warning })
        assertFalse(result.effects.any { it is SimulatorEffect.EmitHint })
        assertTrue(result.effects.any { it is SimulatorEffect.Log })
    }

    // ===== Phase 8: Offline/Online/Hybrid Queue-Tests =====

    @Test
    fun capturePhoto_offline_queuesPhotoUpload() {
        val state = SimulatorState(
            stage = SimulatorStage.RecordingActive,
            isRecording = true,
            lastNetworkMode = NetworkMode.Offline
        )

        val result = engine.transition(state, CapturePhoto(markerId = "M1", timestampUtcMillis = 1000L))

        assertTrue(result.newState.hasPendingActions)
        assertEquals(1, result.newState.pendingQueue.size)
        assertEquals(QueuedActionType.PHOTO_UPLOAD, result.newState.pendingQueue.first().actionType)
        assertTrue(result.effects.any { it is SimulatorEffect.EnqueueAction })
        // Hint wird trotzdem emittiert
        val hint = result.effects.filterIsInstance<SimulatorEffect.EmitHint>().single()
        assertTrue(hint.subtitle!!.contains("Upload wartet"))
    }

    @Test
    fun capturePhoto_online_doesNotQueue() {
        val state = SimulatorState(
            stage = SimulatorStage.RecordingActive,
            isRecording = true,
            lastNetworkMode = NetworkMode.Online
        )

        val result = engine.transition(state, CapturePhoto(markerId = "M1", timestampUtcMillis = 1000L))

        assertFalse(result.newState.hasPendingActions)
        assertFalse(result.effects.any { it is SimulatorEffect.EnqueueAction })
    }

    @Test
    fun finishProject_offline_queuesExport() {
        val state = SimulatorState(
            stage = SimulatorStage.RecordingActive,
            isRecording = true,
            lastNetworkMode = NetworkMode.Offline,
            hasTranscription = true
        )

        val result = engine.transition(state, FinishProject(timestampUtcMillis = 1000L))

        assertEquals(SimulatorStage.Completed, result.newState.stage)
        assertTrue(result.newState.hasPendingActions)
        assertEquals(QueuedActionType.EXPORT, result.newState.pendingQueue.first().actionType)
        assertTrue(result.effects.any { it is SimulatorEffect.EnqueueAction })
        val hint = result.effects.filterIsInstance<SimulatorEffect.EmitHint>().single()
        assertTrue(hint.isStale)
        assertTrue(hint.subtitle!!.contains("gequeuet"))
    }

    @Test
    fun finishProject_hybrid_queuesExport() {
        val state = SimulatorState(
            stage = SimulatorStage.ProjectRunning,
            lastNetworkMode = NetworkMode.Hybrid
        )

        val result = engine.transition(state, FinishProject(timestampUtcMillis = 1000L))

        assertEquals(SimulatorStage.Completed, result.newState.stage)
        assertTrue(result.newState.hasPendingActions)
        assertEquals(QueuedActionType.EXPORT, result.newState.pendingQueue.first().actionType)
    }

    @Test
    fun finishProject_online_doesNotQueue() {
        val state = SimulatorState(
            stage = SimulatorStage.RecordingActive,
            isRecording = true,
            lastNetworkMode = NetworkMode.Online
        )

        val result = engine.transition(state, FinishProject(timestampUtcMillis = 1000L))

        assertEquals(SimulatorStage.Completed, result.newState.stage)
        assertFalse(result.newState.hasPendingActions)
        assertFalse(result.effects.any { it is SimulatorEffect.EnqueueAction })
        val hint = result.effects.filterIsInstance<SimulatorEffect.EmitHint>().single()
        assertFalse(hint.isStale)
    }

    @Test
    fun transcriptionUpdated_offline_queuesSync() {
        val state = SimulatorState(
            stage = SimulatorStage.RecordingActive,
            isRecording = true,
            lastNetworkMode = NetworkMode.Offline
        )

        val result = engine.transition(
            state, TranscriptionUpdated("Offline-Text hier", timestampUtcMillis = 1000L)
        )

        assertTrue(result.newState.hasPendingActions)
        assertEquals(QueuedActionType.TRANSCRIPTION_SYNC, result.newState.pendingQueue.first().actionType)
        // Hint wird trotzdem lokal angezeigt
        val hint = result.effects.filterIsInstance<SimulatorEffect.EmitHint>().single()
        assertTrue(hint.subtitle!!.contains("lokal"))
    }

    @Test
    fun transcriptionUpdated_online_doesNotQueue() {
        val state = SimulatorState(
            stage = SimulatorStage.RecordingActive,
            isRecording = true,
            lastNetworkMode = NetworkMode.Online
        )

        val result = engine.transition(
            state, TranscriptionUpdated("Online-Text hier", timestampUtcMillis = 1000L)
        )

        assertFalse(result.newState.hasPendingActions)
        assertFalse(result.effects.any { it is SimulatorEffect.EnqueueAction })
    }

    @Test
    fun networkModeChanged_onlineReturn_flushesQueue() {
        val queued1 = QueuedAction(QueuedActionType.PHOTO_UPLOAD, "Foto", queuedAtUtcMillis = 100L)
        val queued2 = QueuedAction(QueuedActionType.EXPORT, "Export", queuedAtUtcMillis = 200L)
        val state = SimulatorState(
            stage = SimulatorStage.Completed,
            lastNetworkMode = NetworkMode.Offline,
            pendingQueue = listOf(queued1, queued2)
        )

        val result = engine.transition(
            state, NetworkModeChanged(mode = NetworkMode.Online, timestampUtcMillis = 1000L)
        )

        // Queue ist geleert
        assertFalse(result.newState.hasPendingActions)
        // FlushQueue-Effect mit beiden Aktionen
        val flush = result.effects.filterIsInstance<SimulatorEffect.FlushQueue>().single()
        assertEquals(2, flush.actions.size)
        // Hint zeigt Synchronisation
        val hint = result.effects.filterIsInstance<SimulatorEffect.EmitHint>().single()
        assertTrue(hint.subtitle!!.contains("2 Aktionen"))
    }

    @Test
    fun networkModeChanged_onlineReturn_emptyQueue_noFlush() {
        val state = SimulatorState(
            stage = SimulatorStage.Completed,
            lastNetworkMode = NetworkMode.Offline,
            pendingQueue = emptyList()
        )

        val result = engine.transition(
            state, NetworkModeChanged(mode = NetworkMode.Online, timestampUtcMillis = 1000L)
        )

        assertFalse(result.effects.any { it is SimulatorEffect.FlushQueue })
        val hint = result.effects.filterIsInstance<SimulatorEffect.EmitHint>().single()
        assertTrue(hint.subtitle!!.contains("wiederhergestellt"))
    }

    @Test
    fun offlineWorkflow_multipleActions_accumulate_thenFlush() {
        var state = SimulatorState(
            stage = SimulatorStage.RecordingActive,
            isRecording = true,
            lastNetworkMode = NetworkMode.Offline
        )

        // Foto offline
        val r1 = engine.transition(state, CapturePhoto(markerId = "M1", timestampUtcMillis = 100L))
        state = r1.newState
        assertEquals(1, state.pendingQueue.size)

        // Transcription offline
        val r2 = engine.transition(state, TranscriptionUpdated("Offline chunk", timestampUtcMillis = 200L))
        state = r2.newState
        assertEquals(2, state.pendingQueue.size)

        // Pause + FinishProject offline
        val r3 = engine.transition(state, PauseRecording(timestampUtcMillis = 300L))
        state = r3.newState
        val r4 = engine.transition(state, FinishProject(timestampUtcMillis = 400L))
        state = r4.newState
        assertEquals(3, state.pendingQueue.size)

        // Online-Rueckkehr: alles flushen
        val r5 = engine.transition(state, NetworkModeChanged(mode = NetworkMode.Online, timestampUtcMillis = 500L))
        state = r5.newState
        assertFalse(state.hasPendingActions)
        val flush = r5.effects.filterIsInstance<SimulatorEffect.FlushQueue>().single()
        assertEquals(3, flush.actions.size)
        assertEquals(QueuedActionType.PHOTO_UPLOAD, flush.actions[0].actionType)
        assertEquals(QueuedActionType.TRANSCRIPTION_SYNC, flush.actions[1].actionType)
        assertEquals(QueuedActionType.EXPORT, flush.actions[2].actionType)
    }

    @Test
    fun startProject_resetsQueue() {
        val state = SimulatorState(
            stage = SimulatorStage.Idle,
            pendingQueue = listOf(
                QueuedAction(QueuedActionType.EXPORT, "Alt", queuedAtUtcMillis = 0L)
            )
        )

        val result = engine.transition(state, StartProject("p2", timestampUtcMillis = 1000L))

        assertFalse(result.newState.hasPendingActions)
    }

    @Test
    fun multipleInvalidEvents_accumulateWarningsInCaller() {
        val initial = SimulatorState(stage = SimulatorStage.Idle)

        // First invalid: CapturePhoto in Idle
        val r1 = engine.transition(initial, CapturePhoto(markerId = "M1", timestampUtcMillis = 0L))
        // Second invalid: StartRecording in Idle (still invalid because project not running)
        val r2 = engine.transition(r1.newState, StartRecording(timestampUtcMillis = 1L))

        val allWarnings = r1.warnings + r2.warnings
        assertTrue(allWarnings.size >= 2)
        assertTrue(allWarnings.any { it.contains("CapturePhoto") })
        assertTrue(allWarnings.any { it.contains("StartRecording") })
        assertFalse((r1.effects + r2.effects).any { it is SimulatorEffect.EmitHint })
    }
}

