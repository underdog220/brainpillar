# Architecture Simulator - Status

## Phase 1
- Analyse-Dokument: `docs/architecture_simulator_analysis.md` erstellt
- Build-Check: `:app-watch:assembleDebug` erfolgreich

## Phase 2
- Design-Dokument: `docs/architecture_simulator_design.md` erstellt
- Build-Check: `:app-watch:assembleDebug` erfolgreich

## Phase 3
- Simulator-Core (State/Event/Effect/Engine) im neuen Package eingefuehrt
- Build-Check: `:app-watch:assembleDebug` erfolgreich

## Phase 4
- Demo-Workflow (Projektstart -> Aufnahme -> Foto -> Pause/Resume -> Abschluss) via SimulatorEngine komplett modelliert
- Minimaler UI-Adapter + Integration in `MainActivity` (keine Navigation/ViewModel/Backend)
- Build-Check: `:app-watch:assembleDebug` erfolgreich

## Phase 5
- Unit-Tests fuer SimulatorEngine (gueltige sowie ungueltige Zustandsuebergaenge, Effects, Warnungen)
- Build-Check: `:app-watch:testDebugUnitTest` erfolgreich

## Phase 5.2 / Next
- zusaetzliche Transition-Tests fuer ungültige Events (CapturePhoto/StartProject/TranscriptionUpdated)
- Erweiterte Tests fuer TranscriptionUpdated und NetworkModeChanged (State-Updates, Logs, keine Hint/Warnings)
- Mapper-Tests fuer lange Hint-Titel/Subtitles und null Subtitle

## Phase 6
- Minimaler Debug-Overlay im Watch-Startpunkt (`MainActivity`) der Simulator-State/letztes Event/letzte Hint-Details anzeigt (nur in Debug/„debug“ Build.TYPE)
- Neue UI-Komponente: `SimulatorDebugOverlay`
- Build-Check: `:app-watch:assembleDebug` erfolgreich

### Phase 6.1 (kleine Funktionalintegration)
- `TranscriptionUpdated` setzt jetzt `hasTranscription=true` im Simulator-State
- `FinishProject` erzeugt den Abschluss-Hinweis dynamisch abhängig von `lastNetworkMode` und `hasTranscription` (z. B. „Offline+Transkript“)

### Phase 6.2 (Debug: Offline-Sichtbarkeit)
- Bei `lastNetworkMode == Offline` zeigt das Debug-Overlay in Zeile 1 dauerhaft `Offline aktiv`; Zeile 2 fasst `Stage` und `Last=` zusammen (weiterhin max. 3 Zeilen mit Hint-Zeile). Demo-Eventfolge und Hint-Priorisierung unveraendert.

