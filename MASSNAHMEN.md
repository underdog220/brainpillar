# BrainPillar - Massnahmen

## Chronologische Liste

[Phase 1] Analyse-Dokument `docs/architecture_simulator_analysis.md` erstellt, Build-Check erfolgreich
[Phase 2] Design-Dokument `docs/architecture_simulator_design.md` erstellt, Build-Check erfolgreich
[Phase 3] Simulator-Core (State/Event/Effect/Engine) im Package `architecture.simulator` eingefuehrt, Build-Check erfolgreich
[Phase 4] Demo-Workflow komplett modelliert (Projektstart -> Aufnahme -> Foto -> Pause/Resume -> Abschluss), minimaler UI-Adapter + Integration in MainActivity
[Phase 5] Unit-Tests fuer SimulatorEngine (gueltige/ungueltige Zustandsuebergaenge, Effects, Warnungen), Build-Check erfolgreich
[Phase 5.2] Zusaetzliche Transition-Tests (CapturePhoto/StartProject/TranscriptionUpdated ungueltig), erweiterte Tests fuer TranscriptionUpdated/NetworkModeChanged, Mapper-Tests fuer Rand-Faelle
[Phase 6] Debug-Overlay `SimulatorDebugOverlay` eingefuehrt - zeigt Simulator-State/letztes Event/Hint-Details (nur Debug-Build)
[Phase 6.1] TranscriptionUpdated setzt hasTranscription=true, FinishProject-Hinweis dynamisch abhaengig von lastNetworkMode und hasTranscription
[Phase 6.2] Debug-Overlay: Bei Offline dauerhaft "Offline aktiv" in Zeile 1, Stage+Last in Zeile 2 zusammengefasst (max 3 Zeilen)
[2026-03-24] PROJEKT_STATUS.md, MASSNAHMEN.md, ARCHITEKTUR.md angelegt (Pflichtstruktur)
[2026-03-25] [Phase 7] Erweiterte Transcription-Integration: SimulatorState um transcriptionChunkCount/lastTranscriptionAtUtc/isTranscriptionStale erweitert, TranscriptionUpdated emittiert Hints (Person/Thema/Fallback via Chunk-Parsing), Confidence-Labels (PROBABLE/POSSIBLE nach Chunk-Laenge), TTL/Stale-Logik (30s), Adapter liefert generatedAtUtc, Demo-Workflow mit 4 Transcription-Chunks, 10 neue Unit-Tests
[2026-03-25] [Phase 8] Offline/Online/Hybrid-Logik: QueuedAction-Modell (EXPORT/PHOTO_UPLOAD/TRANSCRIPTION_SYNC), neue Effects EnqueueAction/FlushQueue, State um pendingQueue erweitert. CapturePhoto/FinishProject/TranscriptionUpdated queuen bei Offline. NetworkModeChanged Online flusht Queue automatisch. Hybrid-Modus queued Export. Demo-Workflow mit Offline-Szenario und Online-Flush. Debug-Overlay zeigt Queue-Groesse. 12 neue Unit-Tests
[2026-03-25] [Phase 9] Checklist & AI-Evaluation Events: ChecklistRequested/AiEvaluationRequested Events, EvaluationType (QUALITY/COMPLETENESS/SUMMARY), ChecklistItem-Modell, Effects ChecklistResult/AiEvaluationTriggered. Dynamische Checkliste aus State (7 Pruefpunkte). KI-Bewertung mit Offline-Blockierung. photoCount im State. Demo mit Checklist + AI-Evaluation. 11 neue Unit-Tests
[2026-03-25] [Phase 10] Export-Pipeline & Retry-Logik: ExportState-Modell (6 Status-Werte), ExportStarted/ExportCompleted/ExportFailed/ExportRetry Events, ExportInProgress/ExportDone/ScheduleRetry/ExportAborted Effects. Exponentielles Backoff (5s Basis), max 3 Retries, Offline-Blockierung fuer Export und Retry. Checkliste um Export-Status erweitert (8 Pruefpunkte). Demo mit Start-Fail-Retry-Complete Workflow. 15 neue Unit-Tests
