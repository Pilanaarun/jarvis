package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView

private val DarkColorScheme = darkColorScheme(
    primary = IceBluePrimary,
    onPrimary = OnIceBlueText,
    primaryContainer = DeepBlueContainer,
    onPrimaryContainer = IceBluePrimary,
    secondary = SlateGray,
    onSecondary = TextPrimary,
    tertiary = MutedGold,
    background = GeometricBg,
    onBackground = TextPrimary,
    surface = GeometricSurface,
    onSurface = TextPrimary,
    surfaceVariant = GeometricSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = GeometricBorder,
    error = AlertRed
)

@Composable
fun JarvisTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = GeometricBg.toArgb()
            window.navigationBarColor = GeometricBg.toArgb()
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
