package com.brainpillar.phone.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Demo-Datenmodell fuer ein Projekt
data class DemoProject(
    val name: String,
    val stage: String,
    val datum: String,
    val fortschritt: Float // 0.0 bis 1.0
)

// Hardcoded Demo-Projekte
private val demoProjects = listOf(
    DemoProject("Baustelle Mitte", "RECORDING", "2026-03-25", 0.35f),
    DemoProject("Brueckensanierung A7", "PAUSED", "2026-03-24", 0.60f),
    DemoProject("Dachinspektion Sued", "FINISHED", "2026-03-22", 1.0f),
    DemoProject("Fassade Nordturm", "IDLE", "2026-03-20", 0.0f),
    DemoProject("Kanalarbeiten Ost", "RECORDING", "2026-03-25", 0.15f),
    DemoProject("Fenstereinbau Block C", "PAUSED", "2026-03-23", 0.80f)
)

/**
 * Projektliste – zeigt alle Demo-Projekte mit Name, Stage, Datum und Fortschrittsbalken.
 * Klick auf ein Projekt zeigt vorerst einen Toast (Detail-Screen kommt spaeter).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectListScreen() {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Projekte") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(demoProjects) { project ->
                ProjectCard(
                    project = project,
                    onClick = {
                        Toast.makeText(
                            context,
                            "Detail: ${project.name} (kommt spaeter)",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
        }
    }
}

/**
 * Einzelne Projekt-Karte mit Name, Stage-Chip, Datum und Fortschrittsbalken.
 */
@Composable
private fun ProjectCard(
    project: DemoProject,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Zeile 1: Name und Stage-Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = project.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                StageChip(stage = project.stage)
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Zeile 2: Datum
            Text(
                text = project.datum,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Zeile 3: Fortschrittsbalken
            LinearProgressIndicator(
                progress = { project.fortschritt },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = fortschrittFarbe(project.fortschritt),
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Fortschritt in Prozent
            Text(
                text = "${(project.fortschritt * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

/**
 * Farbiger Chip fuer den Projekt-Stage.
 */
@Composable
private fun StageChip(stage: String) {
    val chipColor = when (stage) {
        "RECORDING" -> MaterialTheme.colorScheme.primary
        "PAUSED" -> MaterialTheme.colorScheme.tertiary
        "FINISHED" -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
    }

    Surface(
        color = chipColor.copy(alpha = 0.2f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = stage,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = chipColor
        )
    }
}

/**
 * Farbe je nach Fortschritt: rot -> gelb -> gruen.
 */
@Composable
private fun fortschrittFarbe(fortschritt: Float) = when {
    fortschritt >= 0.8f -> MaterialTheme.colorScheme.secondary
    fortschritt >= 0.4f -> MaterialTheme.colorScheme.tertiary
    else -> MaterialTheme.colorScheme.primary
}
