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
    fun transcriptionUpdated_inRecordingActive_isAllowed_logs_noWarning_clearsLastError() {
        val initial = SimulatorState(
            stage = SimulatorStage.RecordingActive,
            isRecording = true,
            lastError = "stale-error"
        )

        val result = engine.transition(
            state = initial,
            event = TranscriptionUpdated(chunkText = "a", timestampUtcMillis = 0L)
        )

        assertEquals(SimulatorStage.RecordingActive, result.newState.stage)
        assertNull(result.newState.lastError)
        assertTrue(result.warnings.isEmpty())
        assertTrue(result.effects.any { it is SimulatorEffect.Log })
        assertFalse(result.effects.any { it is SimulatorEffect.Warning })
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

