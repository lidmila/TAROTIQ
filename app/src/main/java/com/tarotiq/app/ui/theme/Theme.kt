package com.tarotiq.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val TarotIQDarkColorScheme = darkColorScheme(
    // Stitch AI — 1:1 color mapping
    primary = AstralPurple,                    // #edb1ff warm pink-lavender
    onPrimary = OnPrimaryCA,                   // #520070
    primaryContainer = PrimaryContainerCA,     // #9d50bb
    onPrimaryContainer = OnPrimaryContainerCA, // #fff4fc

    secondary = MysticTeal,                    // #c5c5d7 silver (Stitch secondary)
    onSecondary = OnSecondaryCA,               // #2e303d
    secondaryContainer = MysticTealDim,        // #464857
    onSecondaryContainer = OnSecondaryContainerCA, // #b6b7c9

    tertiary = CelestialGold,                  // #e9c349 gold (Stitch tertiary)
    onTertiary = OnTertiaryCA,                 // #3c2f00
    tertiaryContainer = CelestialGoldDim,      // #cca730
    onTertiaryContainer = OnTertiaryContainerCA, // #4f3d00

    background = CosmicDeep,                   // #131316
    onBackground = StarWhite,                  // #e4e1e6

    surface = CosmicDeep,                      // #131316
    onSurface = StarWhite,                     // #e4e1e6
    surfaceVariant = SurfaceContainerHighest,  // #353438
    onSurfaceVariant = MoonSilver,             // #c8c4d7
    surfaceTint = AstralGlow,                  // #edb1ff

    inverseSurface = Color(0xFFe4e1e6),
    inverseOnSurface = Color(0xFF303033),
    inversePrimary = AstralPurpleDim,          // #883ca6

    error = ErrorCrimson,                      // #ffb4ab
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000a),
    onErrorContainer = Color(0xFFffdad6),

    outline = DimSilver,                       // #928ea0
    outlineVariant = OutlineColor,             // #474554
    scrim = ScrimDark,

    surfaceBright = SurfaceBrightCA,           // #39393c
    surfaceDim = CosmicDeep,                   // #131316
    surfaceContainer = SurfaceContainerCA,     // #1f1f22
    surfaceContainerHigh = CosmicSurface,      // #2a2a2d
    surfaceContainerHighest = SurfaceContainerHighest, // #353438
    surfaceContainerLow = CosmicMid,           // #1b1b1e
    surfaceContainerLowest = VoidBlack         // #0e0e11
)

@Composable
@Suppress("UNUSED_PARAMETER")
fun TarotIQTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = TarotIQDarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            @Suppress("DEPRECATION")
            window.statusBarColor = VoidBlack.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor = VoidBlack.toArgb()
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
