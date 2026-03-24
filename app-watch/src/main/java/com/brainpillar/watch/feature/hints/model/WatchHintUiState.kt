package com.brainpillar.watch.feature.hints.model

/**
 * UI state for the first MVP hint screen.
 */
sealed interface WatchHintUiState {
    data object Loading : WatchHintUiState
    data class Content(val hint: WatchHintModel) : WatchHintUiState
    data object Empty : WatchHintUiState
    data class Error(val message: String = "Hinweis nicht verfuegbar") : WatchHintUiState
}
