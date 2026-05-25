package com.example.corsa.ui.theme

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ── Colour schemes ────────────────────────────────────────────────────────────

private val StrideDarkColorScheme = darkColorScheme(
    primary          = StrideLime,          // main accent – buttons, FABs, active nav
    onPrimary        = StrideDarkBg,        // content drawn ON primary (black text on lime)
    secondary        = StrideTrackBg,       // icon button backgrounds, progress tracks
    onSecondary      = StrideTextPrimary,
    tertiary         = StrideTextMuted,     // muted labels / inactive icons
    onTertiary       = StrideTextPrimary,
    background       = StrideDarkBg,        // scaffold / screen background
    onBackground     = StrideTextPrimary,
    surface          = StrideCardBg,        // card / sheet surfaces
    onSurface        = StrideTextPrimary,
    surfaceVariant   = StrideTrackBg,       // subtle variant surfaces
    onSurfaceVariant = StrideTextMuted,
)

// Light scheme kept as a sensible inverse; the app is dark-first.
private val StrideLightColorScheme = lightColorScheme(
    primary          = StrideLime,
    onPrimary        = StrideDarkBg,
    secondary        = StrideTrackBg,
    onSecondary      = StrideTextPrimary,
    tertiary         = StrideTextMuted,
    onTertiary       = StrideDarkBg,
    background       = StrideTextPrimary,
    onBackground     = StrideDarkBg,
    surface          = Color(0xFFE8E8E8),   // light card surface
    onSurface        = StrideDarkBg,
    surfaceVariant   = Color(0xFFD0D0D0),
    onSurfaceVariant = StrideTextMuted,
)

// ── Theme entry point ─────────────────────────────────────────────────────────

@Composable
fun CorsaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic colour is available on Android 12+; disable to always use brand colours.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> StrideDarkColorScheme
        else      -> StrideLightColorScheme
    }

    // Colour the status bar to match the background.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content,
    )
}