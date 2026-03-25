package com.brainpillar.phone.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Upload-Status fuer Fotos
enum class UploadStatus {
    HOCHGELADEN,  // Gruen
    AUSSTEHEND,   // Gelb
    FEHLER        // Rot
}

// Demo-Datenmodell fuer ein Foto
data class DemoPhoto(
    val markerId: String,
    val zeitstempel: String,
    val uploadStatus: UploadStatus
)

// Hardcoded Demo-Fotos
private val demoPhotos = listOf(
    DemoPhoto("MRK-001", "10:05:15", UploadStatus.HOCHGELADEN),
    DemoPhoto("MRK-002", "10:06:30", UploadStatus.HOCHGELADEN),
    DemoPhoto("MRK-003", "10:07:45", UploadStatus.AUSSTEHEND),
    DemoPhoto("MRK-004", "10:08:12", UploadStatus.FEHLER),
    DemoPhoto("MRK-005", "10:10:00", UploadStatus.HOCHGELADEN),
    DemoPhoto("MRK-006", "10:12:22", UploadStatus.AUSSTEHEND),
    DemoPhoto("MRK-007", "10:15:05", UploadStatus.HOCHGELADEN),
    DemoPhoto("MRK-008", "10:18:30", UploadStatus.HOCHGELADEN)
)

/**
 * Foto-Galerie – Grid-Layout (2 Spalten) mit Foto-Platzhaltern.
 * Jedes Item zeigt Marker-ID, Zeitstempel und Upload-Status Icon.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoGalleryScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fotos") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(demoPhotos) { photo ->
                PhotoCard(photo)
            }
        }
    }
}

/**
 * Einzelne Foto-Karte mit Platzhalter-Bild, Marker-ID, Zeitstempel und Status-Icon.
 */
@Composable
private fun PhotoCard(photo: DemoPhoto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            // Bild-Platzhalter (graues Rechteck mit Icon)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "Foto-Platzhalter",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }

            // Info-Bereich
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                // Marker-ID und Status-Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = photo.markerId,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    UploadStatusIcon(photo.uploadStatus)
                }

                // Zeitstempel
                Text(
                    text = photo.zeitstempel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

/**
 * Upload-Status Icon mit Farbe je nach Status.
 */
@Composable
private fun UploadStatusIcon(status: UploadStatus) {
    val (icon, farbe, beschreibung) = when (status) {
        UploadStatus.HOCHGELADEN -> Triple(
            Icons.Default.CheckCircle, Color(0xFFA5D6A7), "Hochgeladen"
        )
        UploadStatus.AUSSTEHEND -> Triple(
            Icons.Default.CloudUpload, Color(0xFFFFCC80), "Ausstehend"
        )
        UploadStatus.FEHLER -> Triple(
            Icons.Default.Error, Color(0xFFEF9A9A), "Fehler"
        )
    }

    Icon(
        imageVector = icon,
        contentDescription = beschreibung,
        modifier = Modifier.size(18.dp),
        tint = farbe
    )
}
