# BrainPillar - Architektur

## Moduluebersicht

```
brainpillar/
├── app-watch/                          # Einziges Modul: Wear OS App
│   └── src/main/java/com/brainpillar/watch/
│       ├── MainActivity.kt             # Entry Point, Demo-Workflow
│       ├── architecture/
│       │   └── simulator/              # Domain Layer (Android-frei)
│       │       ├── SimulatorState.kt
│       │       ├── SimulatorEvent.kt
│       │       ├── SimulatorEffect.kt
│       │       ├── SimulatorStage.kt
│       │       ├── SimulationResult.kt
│       │       ├── SimulatorEngine.kt  # State Machine
│       │       ├── NetworkMode.kt
│       │       ├── adapter/
│       │       │   └── SimulatorToWatchHintMapper.kt
│       │       └── debug/
│       │           └── SimulatorDebugOverlay.kt
│       └── feature/
│           └── hints/                  # Watch UI Feature
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
| Android SDK | Compile: 35, Min: 30, Target: 35 | Wear OS (API 30+) |
| Kotlin | 1.9.24 | Hauptsprache |
| Compose BOM | 2024.06.00 | UI-Framework |
| Wear Compose Material 3 | 1.5.6 | Wear OS UI-Komponenten |
| AndroidX Core | 1.13.1 | Core Android |
| Activity Compose | 1.9.1 | Activity-Integration |
| JUnit 4 | 4.13.2 | Unit Testing |
| Java Target | 17 | Kompilierung |
| Gradle | mit Kotlin DSL | Build-System |

## Wichtige Architekturentscheidungen

### 1. Android-freier Domain Layer
SimulatorEngine hat keine Android/Compose-Abhaengigkeiten. Dadurch sind reine JVM-Unit-Tests moeglich ohne Emulator oder Instrumentation.

### 2. Event-gesteuertes Design
Alle Zustandsaenderungen laufen ueber typisierte `SimulatorEvent`-Objekte. Das macht den Ablauf dokumentierbar und replay-faehig.

### 3. Effect-basierte Ausgaben
Statt direkter Seiteneffekte emittiert die Engine `SimulatorEffect`-Objekte (EmitHint, Log, Warning). Das entkoppelt Logik von UI und erleichtert Tests.

### 4. Adapter-Pattern fuer UI-Integration
`SimulatorToWatchHintMapper` uebersetzt Domain-Effects in Watch-spezifische UI-Modelle:
```
SimulatorEffect.EmitHint -> WatchHintModel -> WatchHintUiState.Content -> HintCardScreen
```

### 5. Defensive State-Validierung
Ungueltige Zustandsuebergaenge erzeugen Warnings statt Exceptions. Die App bleibt stabil auch bei unerwarteten Events.

### 6. Phasenweise Entwicklung
Feature-Entwicklung folgt einem klaren Phasenplan (Analyse -> Design -> Core -> Demo -> Tests -> Debug -> Erweiterung).
