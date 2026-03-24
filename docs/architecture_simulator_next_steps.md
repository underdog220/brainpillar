# Architecture Simulator - Nächste Schritte

## Phase 6 (optional): Debug-Integration
- Minimaler Developer/Debug-Entry:
  - Simulator-State anzeigen (z. B. in Textform)
  - Letztes Event und letzte Effects anzeigen
  - Buttons/Trigger nur intern (keine Navigation, kein Backend)

## Core erweitern (Phase 4/5 fortsetzen)
- Weitere Events modellieren:
  - `TranscriptionUpdated` wirklich in State/Effects einweben
  - `ChecklistRequested` und `AiEvaluationRequested`
  - `RequestExportPipeline` / Export-Status
  - Retry-Events inkl. Error-Informationen
  - Offline/Online/Hybrid: Blockieren vs. Queueing (domänenlogisch)

## Adapter erweitern
- `SimulatorToWatchHintMapper` erweitern um:
  - TTL-/Stale-Logik (falls im Simulator modelliert)
  - unterschiedliche Hint-Templates pro HintType

## Testabdeckung erhöhen
- Mehr Transition-Tests:
  - ungültige `PauseRecording/ResumeRecording` in falschen Stages
  - `FinishProject` in allen erlaubten/unerlaubten Stages
  - `NetworkModeChanged` prüfbar gegen State-Veränderungen

