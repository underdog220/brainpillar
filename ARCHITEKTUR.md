# BrainPillar - Architektur

## Moduluebersicht

```
brainpillar/
├── shared/                                # Geteilte Domain-Logik (Kotlin JVM, kein Android)
│   └── src/main/kotlin/com/brainpillar/shared/
│       └── simulator/
│           ├── SimulatorState.kt
│           ├── SimulatorEvent.kt
│           ├── SimulatorEffect.kt
│           ├── SimulatorStage.kt
│           ├── SimulationResult.kt
│           ├── SimulatorEngine.kt         # State Machine
│           ├── NetworkMode.kt
│           ├── QueuedAction.kt
│           ├── ExportState.kt
│           └── backend/
│               ├── BackendModels.kt       # API Request/Response
│               ├── BackendClient.kt       # Interface + Config
│               ├── StubBackendClient.kt   # Test/Demo Stub
│               └── EffectDispatcher.kt    # Effect->Backend Mapping
├── app-phone/                             # Phone Companion App (Material 3)
│   └── src/main/java/com/brainpillar/phone/
│       ├── MainActivity.kt               # Entry Point, Bottom Navigation (4 Tabs)
│       └── ui/
│           ├── theme/
│           │   └── Theme.kt              # BrainPillar Dark Theme (Material 3)
│           └── screens/
│               ├── ProjectListScreen.kt         # Projektliste mit Fortschrittsbalken
│               ├── TranscriptScreen.kt          # Transkript-Chunks mit farbigen Tags
│               ├── PhotoGalleryScreen.kt        # Foto-Grid (2 Spalten) mit Upload-Status
│               └── ExportDashboardScreen.kt     # Export-Status, Checkliste, KI-Bewertung
├── app-watch/                             # Wear OS Smartwatch App
│   └── src/main/java/com/brainpillar/watch/
│       ├── MainActivity.kt               # Entry Point, Demo-Workflow
│       ├── architecture/
│       │   └── simulator/
│       │       ├── adapter/
│       │       │   └── SimulatorToWatchHintMapper.kt  # Domain->Watch-UI Mapping
│       │       └── debug/
│       │           └── SimulatorDebugOverlay.kt
│       └── feature/
│           └── hints/                     # Watch UI Feature
│               ├── model/
│               │   ├── WatchHintModel.kt
│               │   └── WatchHintUiState.kt
│               └── ui/
│                   ├── HintCardScreen.kt
│                   ├── HintCard.kt
│                   └── HintIcon.kt
```

## Technologie-Stack

| Komponente | Version | Zweck |
|-----------|---------|-------|
| Android SDK (Watch) | Compile: 35, Min: 30, Target: 35 | Wear OS (API 30+) |
| Android SDK (Phone) | Compile: 35, Min: 26, Target: 35 | Phone App (API 26+) |
| Kotlin | 1.9.24 | Hauptsprache |
| Kotlin JVM (shared) | 1.9.24 | Shared-Modul (kein Android) |
| Compose BOM | 2024.06.00 | UI-Framework |
| Wear Compose Material 3 | 1.5.6 | Wear OS UI-Komponenten |
| Material 3 (Phone) | via BOM | Phone UI-Komponenten |
| Navigation Compose | 2.7.7 | Phone Screen-Navigation |
| AndroidX Core | 1.13.1 | Core Android |
| Activity Compose | 1.9.1 | Activity-Integration |
| JUnit 4 | 4.13.2 | Unit Testing |
| Java Target | 17 | Kompilierung |
| Gradle | mit Kotlin DSL | Build-System |

## Wichtige Architekturentscheidungen

### 1. Shared Kotlin-JVM-Modul
Die gesamte Domain-Logik (Simulator-Engine, Events, Effects, State, Backend-Abstraktion) lebt im `shared`-Modul als reines Kotlin-JVM-Modul ohne Android-Abhaengigkeiten. Dadurch koennen sowohl Watch als auch Phone App die gleiche Logik nutzen, und Tests laufen ohne Android-Emulator.

### 2. Android-freier Domain Layer
SimulatorEngine hat keine Android/Compose-Abhaengigkeiten. Dadurch sind reine JVM-Unit-Tests moeglich ohne Emulator oder Instrumentation.

### 3. Event-gesteuertes Design
Alle Zustandsaenderungen laufen ueber typisierte `SimulatorEvent`-Objekte. Das macht den Ablauf dokumentierbar und replay-faehig.

### 4. Effect-basierte Ausgaben
Statt direkter Seiteneffekte emittiert die Engine `SimulatorEffect`-Objekte (EmitHint, Log, Warning). Das entkoppelt Logik von UI und erleichtert Tests.

### 5. Adapter-Pattern fuer UI-Integration
`SimulatorToWatchHintMapper` bleibt in app-watch (Watch-UI Dependencies) und uebersetzt Domain-Effects in Watch-spezifische UI-Modelle:
```
SimulatorEffect.EmitHint -> WatchHintModel -> WatchHintUiState.Content -> HintCardScreen
```

### 6. Defensive State-Validierung
Ungueltige Zustandsuebergaenge erzeugen Warnings statt Exceptions. Die App bleibt stabil auch bei unerwarteten Events.

### 7. Phasenweise Entwicklung
Feature-Entwicklung folgt einem klaren Phasenplan (Analyse -> Design -> Core -> Demo -> Tests -> Debug -> Erweiterung).

### 8. Backend-Integration via EffectDispatcher
```
SimulatorEngine.transition()
   -> SimulatorEffect (ExportInProgress, FlushQueue, AiEvaluationTriggered, ...)
      -> EffectDispatcher.dispatch()
         -> BackendClient (Interface)
            -> StubBackendClient (Tests/Demo)
            -> HttpBackendClient (spaeter: echte HTTP-Aufrufe)
         -> DispatchResult (Success/Error pro Aktion)
```
Saubere Trennung: Engine kennt kein Backend, Dispatcher kennt keine UI.
