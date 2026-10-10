package com.appretodos.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta: blanco, gris y verde
private val EsquemaVerde = lightColorScheme(
    primary = Color(0xFF2E7D32),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDF0DE),
    onPrimaryContainer = Color(0xFF1B5E20),
    secondary = Color(0xFF616161),
    onSecondary = Color.White,
    background = Color.White,
    onBackground = Color(0xFF212121),
    surface = Color.White,
    onSurface = Color(0xFF212121),
    surfaceVariant = Color(0xFFF1F1F1),
    onSurfaceVariant = Color(0xFF616161),
    outline = Color(0xFFBDBDBD)
)

@Composable
fun AppRetoDosTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaVerde,
        typography = Typography,
        content = content
    )
}