package com.brainpillar.phone.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Demo-Daten fuer Export-Status
private data class DemoExportStatus(
    val status: String,
    val retryCount: Int,
    val maxRetries: Int,
    val letzteAktion: String,
    val zeitstempel: String
)

// Demo-Daten fuer Checklisten-Punkt
private data class DemoChecklistItem(
    val label: String,
    val erfuellt: Boolean
)

// Demo-Daten fuer KI-Bewertung
private data class DemoKiBewertung(
    val score: Float,
    val verdict: String,
    val zusammenfassung: String
)

// Hardcoded Demo-Export-Status
private val demoExportStatus = DemoExportStatus(
    status = "COMPLETED",
    retryCount = 1,
    maxRetries = 3,
    letzteAktion = "Export erfolgreich abgeschlossen",
    zeitstempel = "2026-03-25 10:22:00"
)

// Hardcoded Checkliste (8 Punkte aus Phase 10)
private val demoChecklist = listOf(
    DemoChecklistItem("Projekt gestartet", true),
    DemoChecklistItem("Transkription vorhanden", true),
    DemoChecklistItem("Mindestens 1 Foto aufgenommen", true),
    DemoChecklistItem("Netzwerk Online", true),
    DemoChecklistItem("Queue leer (alle Aktionen synchronisiert)", true),
    DemoChecklistItem("Export gestartet", true),
    DemoChecklistItem("Export erfolgreich abgeschlossen", true),
    DemoChecklistItem("KI-Bewertung angefordert", false)
)

// Hardcoded KI-Bewertung
private val demoKiBewertung = DemoKiBewertung(
    score = 0.85f,
    verdict = "GUT",
    zusammenfassung = "Die Dokumentation ist weitgehend vollstaendig. " +
            "Transkriptionen decken die wichtigsten Gewerke ab. " +
            "Empfehlung: Zusaetzliche Fotos der Nordseite wuerden die Bewertung verbessern."
)

/**
 * Export-Dashboard – zeigt Export-Status, Checkliste und KI-Bewertung.
 * Alle Daten sind vorerst hardcoded (shared-Modul kommt spaeter).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportDashboardScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Export") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // 1. Export-Status Karte
            ExportStatusKarte()

            // 2. Checkliste
            ChecklisteKarte()

            // 3. KI-Bewertung
            KiBewertungKarte()

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Karte mit aktuellem Export-Status, Retry-Count und letzter Aktion.
 */
@Composable
private fun ExportStatusKarte() {
    val statusFarbe = when (demoExportStatus.status) {
        "COMPLETED" -> Color(0xFFA5D6A7)
        "IN_PROGRESS" -> Color(0xFF90CAF9)
        "FAILED" -> Color(0xFFEF9A9A)
        else -> Color(0xFF9E9E9E)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Export-Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Status-Zeile
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Status:", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                Surface(
                    color = statusFarbe.copy(alpha = 0.2f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = demoExportStatus.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = statusFarbe
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Retry-Count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Retries:", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                Text(
                    text = "${demoExportStatus.retryCount} / ${demoExportStatus.maxRetries}",
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Letzte Aktion
            Text(
                text = demoExportStatus.letzteAktion,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = demoExportStatus.zeitstempel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}

/**
 * Checkliste mit 8 Pruefpunkten aus Phase 10.
 */
@Composable
private fun ChecklisteKarte() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Checkliste",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Fortschrittsanzeige
            val erfuellt = demoChecklist.count { it.erfuellt }
            Text(
                text = "$erfuellt / ${demoChecklist.size} erfuellt",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Einzelne Punkte
            demoChecklist.forEach { item ->
                ChecklistZeile(item)
            }
        }
    }
}

/**
 * Einzelne Zeile in der Checkliste mit Icon und Label.
 */
@Composable
private fun ChecklistZeile(item: DemoChecklistItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = if (item.erfuellt)
                Icons.Default.CheckBox
            else
                Icons.Default.CheckBoxOutlineBlank,
            contentDescription = if (item.erfuellt) "Erfuellt" else "Offen",
            modifier = Modifier.size(20.dp),
            tint = if (item.erfuellt)
                Color(0xFFA5D6A7)
            else
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
        Text(
            text = item.label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (item.erfuellt)
                MaterialTheme.colorScheme.onSurface
            else
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

/**
 * KI-Bewertung Karte mit Score, Verdict und Zusammenfassung.
 */
@Composable
private fun KiBewertungKarte() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Titel mit KI-Icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = "KI-Bewertung",
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "KI-Bewertung",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Score als Prozent-Balken
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Score:", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                Text(
                    text = "${(demoKiBewertung.score * 100).toInt()}%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = { demoKiBewertung.score },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Verdict-Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Verdict:", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                Surface(
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = demoKiBewertung.verdict,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Zusammenfassung
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = demoKiBewertung.zusammenfassung,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }
    }
}
