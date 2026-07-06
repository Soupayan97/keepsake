package com.example.keepsake.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFBBC04),
    background = KeepBackgroundDark,
    surface = KeepBackgroundDark,
    onBackground = Color(0xFFE8EAED),
    onSurface = Color(0xFFE8EAED),
    secondary = KeepBorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFF4B400),
    background = KeepBackgroundLight,
    surface = KeepBackgroundLight,
    onBackground = Color(0xFF202124),
    onSurface = Color(0xFF202124),
    secondary = KeepBorderLight
)

@Composable
fun KeepsakeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context.findActivity()
            activity?.window?.let { window ->
                val barColor = (if (darkTheme) KeepBackgroundDark else KeepBackgroundLight).toArgb()
                window.statusBarColor = barColor
                window.navigationBarColor = barColor
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

private fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}