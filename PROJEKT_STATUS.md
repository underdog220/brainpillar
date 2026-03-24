# BrainPillar - Projektstatus

## Aktueller Stand
Wear OS Smartwatch-App mit Architecture Simulator in Phase 6.2. Simulator-Core (State Machine), Hints-Feature (UI-Karten), Debug-Overlay und Unit-Tests sind implementiert. Demo-Workflow in MainActivity integriert.

## Aktuelle Version / Phase
Phase 6.2 (Debug: Offline-Sichtbarkeit)

## Naechste geplante Stufe
Phase 7 - Erweiterte Transcription-Integration (real-time Updates, TTL)

## Offene Punkte
- Adapter: TTL-/Stale-Logik, unterschiedliche Hint-Templates pro HintType
- Testabdeckung erhoehen: Pause/Resume in falschen Stages, FinishProject in allen Stages, NetworkModeChanged State-Pruefungen
- Offline/Online/Hybrid: Blockieren vs. Queueing domaenenlogisch modellieren
- ChecklistRequested und AiEvaluationRequested Events noch nicht modelliert
- Export-Pipeline und Retry-Logik ausstehend
- Backend-Integration noch nicht begonnen

## Letzte Aenderung
2026-03-24
