package com.brainpillar.phone.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Tag-Typen fuer Transkript-Chunks
enum class TagTyp {
    PERSON,  // Blau
    THEMA,   // Gruen
    FALLBACK // Grau
}

// Demo-Datenmodell fuer einen Transkript-Chunk
data class DemoTranscriptChunk(
    val zeitstempel: String,
    val text: String,
    val tagTyp: TagTyp,
    val tagLabel: String
)

// Hardcoded Demo-Transkriptionen
private val demoChunks = listOf(
    DemoTranscriptChunk(
        "10:05:12", "Hier sehen wir den Riss im Fundament, circa 3mm breit.",
        TagTyp.PERSON, "Bauleiter Mueller"
    ),
    DemoTranscriptChunk(
        "10:05:28", "Das muss vor dem naechsten Betoniervorgang saniert werden.",
        TagTyp.THEMA, "Betonsanierung"
    ),
    DemoTranscriptChunk(
        "10:06:01", "Ich empfehle eine Rissinjektion mit Epoxidharz.",
        TagTyp.PERSON, "Gutachter Schmidt"
    ),
    DemoTranscriptChunk(
        "10:06:15", "Die Bewehrung darunter scheint intakt zu sein.",
        TagTyp.FALLBACK, "Allgemein"
    ),
    DemoTranscriptChunk(
        "10:07:03", "Wir sollten auch die Abdichtung der Kelleraussenwand pruefen.",
        TagTyp.THEMA, "Abdichtung"
    ),
    DemoTranscriptChunk(
        "10:07:22", "Fotos von der Nordseite waeren hilfreich fuer den Bericht.",
        TagTyp.PERSON, "Bauleiter Mueller"
    ),
    DemoTranscriptChunk(
        "10:08:10", "Die Feuchtigkeit im Mauerwerk liegt bei 8 Prozent.",
        TagTyp.THEMA, "Feuchtemessung"
    ),
    DemoTranscriptChunk(
        "10:08:45", "Das ist noch im akzeptablen Bereich.",
        TagTyp.FALLBACK, "Allgemein"
    )
)

/**
 * Transkript-Screen – scrollbare Liste von Transkript-Chunks
 * mit farbigen Tags (Person=blau, Thema=gruen, Fallback=grau).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranscriptScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transkript") },
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
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(demoChunks) { chunk ->
                TranscriptChunkCard(chunk)
            }
        }
    }
}

/**
 * Einzelne Transkript-Karte mit Zeitstempel, farbigem Tag und Text.
 */
@Composable
private fun TranscriptChunkCard(chunk: DemoTranscriptChunk) {
    // Farbe je nach Tag-Typ
    val tagColor = when (chunk.tagTyp) {
        TagTyp.PERSON -> Color(0xFF90CAF9)   // Blau
        TagTyp.THEMA -> Color(0xFFA5D6A7)    // Gruen
        TagTyp.FALLBACK -> Color(0xFF9E9E9E) // Grau
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Zeile 1: Zeitstempel und Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = chunk.zeitstempel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                // Farbiger Tag-Chip
                Surface(
                    color = tagColor.copy(alpha = 0.2f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = chunk.tagLabel,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = tagColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Zeile 2: Transkript-Text
            Text(
                text = chunk.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
