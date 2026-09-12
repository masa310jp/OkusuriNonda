package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LocalIsDarkTheme = compositionLocalOf { false }

val CurrentDarkColorScheme = darkColorScheme(
    primary = Color(0xFF80CBC4),
    onPrimary = Color(0xFF00332C),
    primaryContainer = Color(0xFF004D40),
    onPrimaryContainer = Color(0xFFE0F2F1),
    secondary = Color(0xFF4DB6AC),
    onSecondary = Color(0xFF00332C),
    secondaryContainer = Color(0xFF004D40),
    onSecondaryContainer = Color(0xFFE0F2F1),
    tertiary = Color(0xFFFFB74D),
    onTertiary = Color(0xFF4E2600),
    background = Color(0xFF121414),
    onBackground = Color(0xFFE2E4E2),
    surface = Color(0xFF1A1D1C),
    onSurface = Color(0xFFE2E4E2),
    surfaceVariant = Color(0xFF262A29),
    onSurfaceVariant = Color(0xFFC4C8C6),
    outline = Color(0xFF3F4846)
)

private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = SurfaceCard,
    primaryContainer = TealLight,
    onPrimaryContainer = TealDark,
    secondary = TealAccent,
    onSecondary = SurfaceCard,
    secondaryContainer = TealLight,
    onSecondaryContainer = TealDark,
    tertiary = CoralWarning,
    onTertiary = SurfaceCard,
    tertiaryContainer = CoralLight,
    onTertiaryContainer = CoralWarning,
    background = SurfaceCream,
    onBackground = TextDark,
    surface = SurfaceCard,
    onSurface = TextDark,
    surfaceVariant = Color(0xFFEFF3F1),
    onSurfaceVariant = TextMedium,
    outline = DividerColor
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> CurrentDarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                // Keep status bar icons light/white in both light (Teal header) and dark mode,
                // so the status bar is never white.
                insetsController.isAppearanceLightStatusBars = false
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalIsDarkTheme provides darkTheme) {
        MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
    }
}

