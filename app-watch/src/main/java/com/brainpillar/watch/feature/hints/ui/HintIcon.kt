package com.brainpillar.watch.feature.hints.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.brainpillar.watch.feature.hints.model.HintType

/**
 * Minimal, calm icon token for quick recognition on small round displays.
 * Uses neutral dark palette with subtle per-type differentiation.
 */
@Composable
fun HintIcon(
    hintType: HintType,
    modifier: Modifier = Modifier
) {
    val token = when (hintType) {
        HintType.PERSON -> "P"
        HintType.TOPIC -> "T"
        HintType.REMINDER -> "!"
        HintType.FALLBACK -> "?"
    }

    val backgroundColor = when (hintType) {
        HintType.PERSON -> Color(0xFF1E3A5F)
        HintType.TOPIC -> Color(0xFF2A3B2D)
        HintType.REMINDER -> Color(0xFF4A3A1B)
        HintType.FALLBACK -> Color(0xFF2F2F35)
    }

    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = token,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )
    }
}
