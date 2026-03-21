package com.tarotiq.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val TarotIQDarkColorScheme = darkColorScheme(
    primary = MysticPrimary,
    onPrimary = OnPrimary,
    primaryContainer = MysticPrimaryVariant,
    onPrimaryContainer = TextPrimary,

    secondary = GoldSecondary,
    onSecondary = OnPrimary,
    secondaryContainer = GoldSecondaryVariant,
    onSecondaryContainer = TextPrimary,

    tertiary = TealTertiary,
    onTertiary = TextPrimary,
    tertiaryContainer = TealDark,
    onTertiaryContainer = TextPrimary,

    background = MidnightBg1,
    onBackground = OnBackground,

    surface = SurfaceDark,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    surfaceTint = MysticPrimary,

    inverseSurface = TextPrimary,
    inverseOnSurface = SurfaceDark,
    inversePrimary = MysticDark,

    error = ErrorColor,
    onError = OnPrimary,
    errorContainer = ErrorColor,
    onErrorContainer = TextPrimary,

    outline = Outline,
    outlineVariant = OutlineVariant,
    scrim = Scrim,

    surfaceBright = SurfaceBright,
    surfaceDim = MidnightBg1,
    surfaceContainer = SurfaceDark,
    surfaceContainerHigh = SurfaceVariant,
    surfaceContainerHighest = SurfaceBright,
    surfaceContainerLow = SurfaceDark,
    surfaceContainerLowest = MidnightBg1
)

@Composable
fun TarotIQTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = TarotIQDarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TarotTypography,
        content = content
    )
}
