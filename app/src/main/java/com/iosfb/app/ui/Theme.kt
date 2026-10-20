package com.iosfb.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object IosColors {
    val Blue = Color(0xFF1877F2)
    val Gray = Color(0xFF8E8E93)
}

private val Light = lightColorScheme(
    primary = IosColors.Blue,
    background = Color.White,
    surface = Color(0xFFF9F9F9),
    onBackground = Color.Black,
    onSurface = Color.Black,
    outlineVariant = Color(0xFFD1D1D6),
)

private val Dark = darkColorScheme(
    primary = IosColors.Blue,
    background = Color.Black,
    surface = Color(0xFF1C1C1E),
    onBackground = Color.White,
    onSurface = Color.White,
    outlineVariant = Color(0xFF38383A),
)

@Composable
fun IosFbTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) Dark else Light, content = content)
}
