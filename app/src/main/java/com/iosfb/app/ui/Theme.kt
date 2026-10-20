package com.iosfb.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/** iOS system colors (UIColor.systemBlue / systemGray etc.). */
object IosColors {
    val Blue = Color(0xFF007AFF)
    val BlueDark = Color(0xFF0A84FF)
    val FbBlue = Color(0xFF1877F2) // only for the "facebook" wordmark
    val Gray = Color(0xFF8E8E93)
}

private val Light = lightColorScheme(
    primary = IosColors.Blue,
    background = Color.White,              // systemBackground
    surface = Color(0xFFF2F2F7),           // systemGroupedBackground
    onBackground = Color.Black,
    onSurface = Color.Black,
    outlineVariant = Color(0xFFC6C6C8),    // separator
)

private val Dark = darkColorScheme(
    primary = IosColors.BlueDark,
    background = Color.Black,
    surface = Color(0xFF1C1C1E),
    onBackground = Color.White,
    onSurface = Color.White,
    outlineVariant = Color(0xFF38383A),
)

/** Colors for the iOS 26 "Liquid Glass" controls (translucent fill + specular edge). */
@Immutable
class GlassColors(
    val fill: Color,
    val selected: Color,
    val stroke: Color,
    val shadow: Color,
)

@Composable
fun glassColors(): GlassColors =
    if (isSystemInDarkTheme()) {
        GlassColors(
            fill = Color(0xFF2C2C2E).copy(alpha = 0.80f),
            selected = Color.White.copy(alpha = 0.16f),
            stroke = Color.White.copy(alpha = 0.16f),
            shadow = Color.Black.copy(alpha = 0.55f),
        )
    } else {
        GlassColors(
            fill = Color(0xFFF7F7FA).copy(alpha = 0.82f),
            selected = Color.Black.copy(alpha = 0.07f),
            stroke = Color.Black.copy(alpha = 0.09f),
            shadow = Color.Black.copy(alpha = 0.30f),
        )
    }

@Composable
fun IosFbTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) Dark else Light, content = content)
}
