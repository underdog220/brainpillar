package com.brainpillar.phone.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// BrainPillar Farbpalette – passend zum Watch Dark Theme
private val BrainPillarPrimary = Color(0xFF90CAF9)       // Helles Blau
private val BrainPillarOnPrimary = Color(0xFF003258)
private val BrainPillarPrimaryContainer = Color(0xFF004880)
private val BrainPillarOnPrimaryContainer = Color(0xFFD1E4FF)

private val BrainPillarSecondary = Color(0xFFA5D6A7)     // Helles Gruen
private val BrainPillarOnSecondary = Color(0xFF003910)
private val BrainPillarSecondaryContainer = Color(0xFF005318)
private val BrainPillarOnSecondaryContainer = Color(0xFFC8E6C9)

private val BrainPillarTertiary = Color(0xFFFFCC80)       // Helles Orange
private val BrainPillarOnTertiary = Color(0xFF4A2800)
private val BrainPillarTertiaryContainer = Color(0xFF6A3C00)
private val BrainPillarOnTertiaryContainer = Color(0xFFFFE0B2)

private val BrainPillarError = Color(0xFFEF9A9A)
private val BrainPillarBackground = Color(0xFF121212)
private val BrainPillarSurface = Color(0xFF1E1E1E)
private val BrainPillarOnBackground = Color(0xFFE0E0E0)
private val BrainPillarOnSurface = Color(0xFFE0E0E0)

// Dunkles Farbschema – Standarddarstellung fuer BrainPillar
private val DarkColorScheme = darkColorScheme(
    primary = BrainPillarPrimary,
    onPrimary = BrainPillarOnPrimary,
    primaryContainer = BrainPillarPrimaryContainer,
    onPrimaryContainer = BrainPillarOnPrimaryContainer,
    secondary = BrainPillarSecondary,
    onSecondary = BrainPillarOnSecondary,
    secondaryContainer = BrainPillarSecondaryContainer,
    onSecondaryContainer = BrainPillarOnSecondaryContainer,
    tertiary = BrainPillarTertiary,
    onTertiary = BrainPillarOnTertiary,
    tertiaryContainer = BrainPillarTertiaryContainer,
    onTertiaryContainer = BrainPillarOnTertiaryContainer,
    error = BrainPillarError,
    background = BrainPillarBackground,
    surface = BrainPillarSurface,
    onBackground = BrainPillarOnBackground,
    onSurface = BrainPillarOnSurface
)

// Helles Farbschema – Fallback (nicht primaer genutzt)
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1565C0),
    secondary = Color(0xFF388E3C),
    tertiary = Color(0xFFEF6C00)
)

/**
 * BrainPillar Material 3 Theme.
 * Standardmaessig Dark Theme, passend zur Wear OS Watch App.
 */
@Composable
fun BrainPillarPhoneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
