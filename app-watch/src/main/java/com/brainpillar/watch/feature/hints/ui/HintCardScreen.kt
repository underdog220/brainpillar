package com.brainpillar.watch.feature.hints.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.union
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.brainpillar.watch.feature.hints.model.ConfidenceLabel
import com.brainpillar.watch.feature.hints.model.HintType
import com.brainpillar.watch.feature.hints.model.WatchHintModel
import com.brainpillar.watch.feature.hints.model.WatchHintUiState

/**
 * First real Wear OS MVP screen:
 * - One primary hint card
 * - Calm dark UI
 * - Round-screen safe area handling
 */
@Composable
fun HintCardScreen(
    state: WatchHintUiState,
    modifier: Modifier = Modifier
) {
    val insets = WindowInsets.safeDrawing.union(WindowInsets.navigationBars).asPaddingValues()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0E0F12), Color(0xFF08090B))
                )
            )
            .padding(insets)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            WatchHintUiState.Loading -> LoadingContent()
            is WatchHintUiState.Content -> HintCard(model = state.hint)
            WatchHintUiState.Empty -> EmptyContent()
            is WatchHintUiState.Error -> ErrorContent(message = state.message)
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CircularProgressIndicator()
        Text(
            text = "Hinweis wird geladen",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
private fun EmptyContent() {
    HintCard(
        model = WatchHintModel(
            hintType = HintType.FALLBACK,
            title = "Noch keine Infos",
            subtitle = "Frage bei Bedarf erneut"
        )
    )
}

@Composable
private fun ErrorContent(message: String) {
    // Keep error wording discreet and non-alarmist.
    HintCard(
        model = WatchHintModel(
            hintType = HintType.FALLBACK,
            title = message.ifBlank { "Hinweis nicht verfuegbar" },
            subtitle = "Bitte spaeter erneut"
        )
    )
}

/* ----------------------------- PREVIEWS ----------------------------- */

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun PreviewHint_Loading() {
    HintCardScreen(state = WatchHintUiState.Loading)
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun PreviewHint_PersonHigh() {
    HintCardScreen(
        state = WatchHintUiState.Content(
            WatchHintModel(
                hintType = HintType.PERSON,
                title = "Herr Meier",
                subtitle = "Dachsanierung"
            )
        )
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun PreviewHint_PersonMedium() {
    HintCardScreen(
        state = WatchHintUiState.Content(
            WatchHintModel(
                hintType = HintType.PERSON,
                title = "Herr Meier",
                subtitle = "Dachsanierung",
                confidenceLabel = ConfidenceLabel.PROBABLE
            )
        )
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun PreviewHint_Topic() {
    HintCardScreen(
        state = WatchHintUiState.Content(
            WatchHintModel(
                hintType = HintType.TOPIC,
                title = "Letztes Thema",
                subtitle = "Dachsanierung"
            )
        )
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun PreviewHint_Reminder() {
    HintCardScreen(
        state = WatchHintUiState.Content(
            WatchHintModel(
                hintType = HintType.REMINDER,
                title = "Offen",
                subtitle = "Rueckruf wegen Termin"
            )
        )
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun PreviewHint_Fallback() {
    HintCardScreen(state = WatchHintUiState.Empty)
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun PreviewHint_Error() {
    HintCardScreen(
        state = WatchHintUiState.Error("Hinweis nicht verfuegbar")
    )
}
