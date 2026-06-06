package paulokat.de.docspellviewer.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Textfarbe für Inhalte (Dokumentnamen, Links) – neutral, nicht schema-gefärbt. */
@Composable
fun contentTextColor(): Color = MaterialTheme.colorScheme.onSurface

/** Textfarbe für sekundäre Inhalte. */
@Composable
fun contentTextColorMuted(): Color = MaterialTheme.colorScheme.onSurfaceVariant
