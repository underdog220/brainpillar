package com.brainpillar.shared.simulator

/**
 * Transition result: new state + produced effects + diagnostics.
 */
data class SimulationResult(
    val newState: SimulatorState,
    val effects: List<SimulatorEffect>,
    val logs: List<String>,
    val warnings: List<String>
)
