package com.example.moa_project.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val MoaLightColorScheme = lightColorScheme(
    primary = MoaBlue,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    secondary = MoaBlue,
    background = MoaScreenBackground,
    onBackground = MoaTextPrimary,
    surface = androidx.compose.ui.graphics.Color.White,
    onSurface = MoaTextPrimary,
    outline = MoaInputBorder,
)

@Composable
fun Moa_ProjectTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = MoaLightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = MoaScreenBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
