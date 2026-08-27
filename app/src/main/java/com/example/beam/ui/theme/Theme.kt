package com.example.beam.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class ThemeMode {
    DARK,
    LIGHT,
    DYNAMIC
}

private val DarkColorScheme = darkColorScheme(
    primary = BeamFocusCyanBright,
    secondary = BeamNeonBlue,
    tertiary = BeamPurple,
    background = BeamDarkCanvas,
    surface = BeamCardBg,
    surfaceVariant = BeamSurfaceElevated,
    onPrimary = BeamDarkCanvas,
    onSecondary = BeamDarkCanvas,
    onBackground = BeamTextPrimary,
    onSurface = BeamTextPrimary,
    onSurfaceVariant = BeamTextSecondary,
    outline = BeamBorder,
    outlineVariant = BeamBorderLight,
    error = BeamRose
)

private val LightColorScheme = lightColorScheme(
    primary = BeamNeonBlue,
    secondary = BeamFocusCyan,
    tertiary = BeamIndigo,
    background = BeamLightCanvas,
    surface = BeamLightCardBg,
    surfaceVariant = BeamLightSurfaceElevated,
    onPrimary = BeamLightCardBg,
    onSecondary = BeamLightCardBg,
    onBackground = BeamLightTextPrimary,
    onSurface = BeamLightTextPrimary,
    onSurfaceVariant = BeamLightTextSecondary,
    outline = BeamLightBorder,
    outlineVariant = BeamLightBorderLight,
    error = BeamRose
)

@Composable
fun BeamTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemDark = isSystemInDarkTheme()

    val isDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.DYNAMIC -> systemDark
    }

    val colorScheme = when {
        themeMode == ThemeMode.DYNAMIC && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !isDark
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
