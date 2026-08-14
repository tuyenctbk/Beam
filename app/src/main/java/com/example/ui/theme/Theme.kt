package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = BeamPrimary,
    onPrimary = BeamOnPrimary,
    primaryContainer = BeamPrimaryContainer,
    onPrimaryContainer = BeamOnPrimaryContainer,
    secondary = BeamSecondary,
    secondaryContainer = BeamSecondaryContainer,
    onSecondaryContainer = BeamOnSecondaryContainer,
    background = BeamBackground,
    onBackground = BeamOnBackground,
    surface = BeamSurface,
    onSurface = BeamOnSurface,
    outline = BeamOutline,
    error = BeamError
)

private val HighContrastDarkColorScheme = darkColorScheme(
    primary = Color(0xFF38BDF8),             // Electric Cyan
    onPrimary = Color(0xFF0F172A),           // Deep Dark
    primaryContainer = Color(0xFF1E293B),    // Slate 800
    onPrimaryContainer = Color(0xFFF8FAFC),  // Crisp White
    secondary = Color(0xFF94A3B8),          // Slate 400
    secondaryContainer = Color(0xFF334155),  // Slate 700
    onSecondaryContainer = Color(0xFFF1F5F9),// Slate 100
    background = Color(0xFF090D16),         // Pure High Contrast Dark Screen Canvas
    onBackground = Color(0xFFF8FAFC),       // Crisp White Text
    surface = Color(0xFF0F172A),            // Dark Slate Card
    onSurface = Color(0xFFFFFFFF),          // Pure White
    outline = Color(0xFF334155),            // High Contrast Slate Border
    error = BeamError
)

@Composable
fun BeamTheme(
    isHighContrastDark: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (isHighContrastDark) HighContrastDarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

