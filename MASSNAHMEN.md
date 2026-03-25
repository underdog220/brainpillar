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
