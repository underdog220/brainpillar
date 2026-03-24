package com.brainpillar.watch.feature.hints.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CardDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.brainpillar.watch.feature.hints.model.ConfidenceLabel
import com.brainpillar.watch.feature.hints.model.WatchHintModel

/**
 * Single-card content optimized for <2s readability.
 * - No scrolling
 * - Defensive, short text
 * - Calm visual hierarchy
 */
@Composable
fun HintCard(
    model: WatchHintModel,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = { /* MVP: no navigation/action */ },
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.ui.graphics.Color(0xFF17181C)
        )
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            HintIcon(hintType = model.hintType)

            // Main line (max 1 line) - strong and immediate.
            Text(
                text = buildTitle(model),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Secondary line (max 1 line).
            model.subtitle
                ?.takeIf { it.isNotBlank() }
                ?.let { subtitle ->
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

            if (model.isStale) {
                Text(
                    text = "Nicht aktuell",
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun buildTitle(model: WatchHintModel): String {
    val prefix = when (model.confidenceLabel) {
        ConfidenceLabel.PROBABLE -> "Wahrscheinlich: "
        ConfidenceLabel.POSSIBLE -> "Moeglicherweise: "
        null -> ""
    }
    return prefix + model.title
}
