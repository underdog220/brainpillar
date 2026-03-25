# BrainPillar - Projektstatus

## Aktueller Stand
Wear OS Smartwatch-App mit Architecture Simulator in Phase 7. TranscriptionUpdated emittiert jetzt echte Hints (PERSON/TOPIC/FALLBACK) mit Chunk-Parsing, Confidence-Labels und TTL/Stale-Logik. Adapter liefert generatedAtUtc. Demo-Workflow zeigt mehrere Transcription-Typen.

## Aktuelle Version / Phase
Phase 7 (Erweiterte Transcription-Integration)

## Naechste geplante Stufe
Phase 8 - Offline/Online/Hybrid-Logik (Blocking vs. Queueing)

## Offene Punkte
- Unterschiedliche Hint-Templates pro HintType im UI
- Testabdeckung erhoehen: Pause/Resume in falschen Stages, FinishProject in allen Stages
- Offline/Online/Hybrid: Blockieren vs. Queueing domaenenlogisch modellieren
- ChecklistRequested und AiEvaluationRequested Events noch nicht modelliert
- Export-Pipeline und Retry-Logik ausstehend
- Backend-Integration noch nicht begonnen

## Letzte Aenderung
2026-03-25
