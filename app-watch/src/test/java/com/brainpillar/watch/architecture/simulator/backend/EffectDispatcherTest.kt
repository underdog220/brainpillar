package com.brainpillar.watch.architecture.simulator.backend

import com.brainpillar.watch.architecture.simulator.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class EffectDispatcherTest {

    private lateinit var client: StubBackendClient
    private lateinit var dispatcher: EffectDispatcher
    private val baseState = SimulatorState(
        stage = SimulatorStage.Completed,
        projectId = "p1",
        photoCount = 2,
        transcriptionChunkCount = 5,
        hasTranscription = true,
        lastNetworkMode = NetworkMode.Online
    )

    @Before
    fun setup() {
        client = StubBackendClient()
        dispatcher = EffectDispatcher(client)
    }

    // ===== ExportInProgress =====

    @Test
    fun exportInProgress_callsBackend_returnsSuccess() {
        val effects = listOf(SimulatorEffect.ExportInProgress("p1"))
        val results = dispatcher.dispatch(effects, baseState)

        assertEquals(1, client.exportCount)
        val success = results.single() as DispatchResult.ExportSuccess
        assertEquals("completed", success.response.status)
        assertTrue(success.response.exportId.contains("p1"))
    }

    @Test
    fun exportInProgress_onFailure_returnsError() {
        client.failNextExport = BackendError("TIMEOUT", "Server timeout", isRetryable = true)
        val effects = listOf(SimulatorEffect.ExportInProgress("p1"))
        val results = dispatcher.dispatch(effects, baseState)

        val error = results.single() as DispatchResult.ExportError
        assertEquals("TIMEOUT", error.error.code)
        assertTrue(error.error.isRetryable)
    }

    // ===== FlushQueue =====

    @Test
    fun flushQueue_dispatchesAllActions() {
        val actions = listOf(
            QueuedAction(QueuedActionType.PHOTO_UPLOAD, "Foto", mapOf("markerId" to "M1"), 100L),
            QueuedAction(QueuedActionType.TRANSCRIPTION_SYNC, "Sync", mapOf("chunkText" to "text"), 200L),
            QueuedAction(QueuedActionType.EXPORT, "Export", mapOf("projectId" to "p1", "hasTranscription" to "true"), 300L)
        )
        val effects = listOf(SimulatorEffect.FlushQueue(actions))
        val results = dispatcher.dispatch(effects, baseState)

        val flushed = results.single() as DispatchResult.QueueFlushed
        assertEquals(3, flushed.results.size)
        assertTrue(flushed.results[0] is DispatchResult.PhotoUploaded)
        assertTrue(flushed.results[1] is DispatchResult.TranscriptionSynced)
        assertTrue(flushed.results[2] is DispatchResult.ExportSuccess)
        assertEquals(1, client.uploadCount)
        assertEquals(1, client.syncCount)
        assertEquals(1, client.exportCount)
    }

    @Test
    fun flushQueue_partialFailure_reportsErrors() {
        client.failNextUpload = BackendError("NET_ERROR", "Verbindungsfehler", isRetryable = true)
        val actions = listOf(
            QueuedAction(QueuedActionType.PHOTO_UPLOAD, "Foto", mapOf("markerId" to "M1"), 100L),
            QueuedAction(QueuedActionType.TRANSCRIPTION_SYNC, "Sync", mapOf("chunkText" to "text"), 200L)
        )
        val effects = listOf(SimulatorEffect.FlushQueue(actions))
        val results = dispatcher.dispatch(effects, baseState)

        val flushed = results.single() as DispatchResult.QueueFlushed
        assertTrue(flushed.results[0] is DispatchResult.PhotoError)
        assertTrue(flushed.results[1] is DispatchResult.TranscriptionSynced)
    }

    // ===== EnqueueAction =====

    @Test
    fun enqueueAction_returnsActionQueued_noBackendCall() {
        val action = QueuedAction(QueuedActionType.PHOTO_UPLOAD, "Foto", queuedAtUtcMillis = 100L)
        val effects = listOf(SimulatorEffect.EnqueueAction(action))
        val results = dispatcher.dispatch(effects, baseState)

        val queued = results.single() as DispatchResult.ActionQueued
        assertEquals(QueuedActionType.PHOTO_UPLOAD, queued.action.actionType)
        // Kein Backend-Aufruf
        assertEquals(0, client.uploadCount)
    }

    // ===== AiEvaluationTriggered =====

    @Test
    fun aiEvaluation_callsBackend_returnsSuccess() {
        val effects = listOf(
            SimulatorEffect.AiEvaluationTriggered(
                evaluationType = EvaluationType.QUALITY,
                contextSummary = "Projekt p1, 2 Fotos"
            )
        )
        val results = dispatcher.dispatch(effects, baseState)

        assertEquals(1, client.evaluationCount)
        val success = results.single() as DispatchResult.EvaluationSuccess
        assertEquals(85, success.response.score)
        assertEquals("good", success.response.verdict)
    }

    @Test
    fun aiEvaluation_onFailure_returnsError() {
        client.failNextEvaluation = BackendError("AI_UNAVAILABLE", "KI nicht verfuegbar")
        val effects = listOf(
            SimulatorEffect.AiEvaluationTriggered(
                evaluationType = EvaluationType.COMPLETENESS,
                contextSummary = "context"
            )
        )
        val results = dispatcher.dispatch(effects, baseState)

        val error = results.single() as DispatchResult.EvaluationError
        assertEquals("AI_UNAVAILABLE", error.error.code)
    }

    // ===== Non-Backend Effects =====

    @Test
    fun nonBackendEffects_areIgnored() {
        val effects = listOf(
            SimulatorEffect.EmitHint(SimulatorHintType.FALLBACK, "Test", null, null, false, null),
            SimulatorEffect.Log(LogLevel.INFO, "Log"),
            SimulatorEffect.Warning("Warnung"),
            SimulatorEffect.ExportDone("p1"),
            SimulatorEffect.ScheduleRetry(5000L, 2),
            SimulatorEffect.ExportAborted("Grund", 3)
        )
        val results = dispatcher.dispatch(effects, baseState)

        assertTrue(results.isEmpty())
        assertEquals(0, client.exportCount)
        assertEquals(0, client.healthCount)
    }

    // ===== Mehrere Effects =====

    @Test
    fun multipleEffects_dispatchedInOrder() {
        val effects = listOf(
            SimulatorEffect.ExportInProgress("p1"),
            SimulatorEffect.AiEvaluationTriggered(EvaluationType.SUMMARY, "ctx"),
            SimulatorEffect.EmitHint(SimulatorHintType.FALLBACK, "T", null, null, false, null)
        )
        val results = dispatcher.dispatch(effects, baseState)

        assertEquals(2, results.size) // Hint wird ignoriert
        assertTrue(results[0] is DispatchResult.ExportSuccess)
        assertTrue(results[1] is DispatchResult.EvaluationSuccess)
    }

    // ===== StubBackendClient =====

    @Test
    fun stubClient_health_works() {
        val result = client.health()
        assertTrue(result is BackendResult.Success)
        assertEquals("ok", (result as BackendResult.Success).data.status)
        assertEquals(1, client.healthCount)
    }

    @Test
    fun stubClient_failOnce_thenSucceed() {
        client.failNextExport = BackendError("ERR", "Einmalig")

        val r1 = client.exportProject(ExportRequest("p1", 0, 0, false))
        assertTrue(r1 is BackendResult.Failure)

        val r2 = client.exportProject(ExportRequest("p1", 0, 0, false))
        assertTrue(r2 is BackendResult.Success)

        assertEquals(2, client.exportCount)
    }
}
