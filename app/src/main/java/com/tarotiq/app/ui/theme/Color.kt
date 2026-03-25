package com.tarotiq.app.ui.theme

import androidx.compose.ui.graphics.Color

// ══════════════════════════════════════════════
// Stitch AI — Color System (1:1 match)
// ══════════════════════════════════════════════

// --- Cosmic Dark (backgrounds — neutral gray-black) ---
val VoidBlack = Color(0xFF0e0e11)          // surface-container-lowest
val CosmicDeep = Color(0xFF131316)         // surface / background
val CosmicMid = Color(0xFF1b1b1e)          // surface-container-low
val CosmicSurface = Color(0xFF2a2a2d)      // surface-container-high

// --- Primary (warm pink-lavender) ---
val AstralPurple = Color(0xFFedb1ff)       // primary
val AstralPurpleLight = Color(0xFFf9d8ff)  // primary-fixed
val AstralPurpleDim = Color(0xFF883ca6)    // inverse-primary
val AstralGlow = Color(0xFFedb1ff)         // surface-tint

// --- Tertiary (celestial gold) — Stitch: tertiary ---
val CelestialGold = Color(0xFFe9c349)      // tertiary
val CelestialGoldLight = Color(0xFFffe088) // tertiary-fixed
val CelestialGoldDim = Color(0xFFcca730)   // tertiary-container
val GoldGlow = Color(0x40e9c349)           // gold glow effect

// --- Secondary (silver / neutral) — Stitch: secondary ---
val MysticTeal = Color(0xFFc5c5d7)         // secondary
val MysticTealDim = Color(0xFF464857)      // secondary-container

// --- Text (cool ethereal) ---
val StarWhite = Color(0xFFe4e1e6)          // on-surface
val MoonSilver = Color(0xFFc8c4d7)         // on-surface-variant
val DimSilver = Color(0xFF928ea0)          // outline

// --- Glass / Frost (ghost borders) ---
val GlassWhite = Color(0x99353438)         // 60% surface-variant
val GlassBorder = Color(0x26474554)        // outline-variant 15%
val GlassBorderFocus = Color(0x50e9c349)   // gold focus
val GlassBackground = Color(0xFF1b1b1e)    // surface-container-low

// --- Semantic ---
val ErrorCrimson = Color(0xFFffb4ab)
val SuccessEmerald = Color(0xFF34D399)
val WarningAmber = Color(0xFFFBBF24)
val InfoSky = Color(0xFF60A5FA)

// --- Card colors ---
val CardReversed = Color(0xFFFF6B8A)
val CardUpright = Color(0xFF34D399)

// --- Glow / Effects ---
val PurpleGlow = Color(0x30edb1ff)         // primary glow
val TealGlow = Color(0x30c5c5d7)           // secondary glow
val GoldShimmer = Color(0x30e9c349)        // gold shimmer

// --- Scrim / Overlay ---
val ScrimDark = Color(0xE6050510)
val ScrimMedium = Color(0x99050510)

// --- Stitch — Extended palette ---
val NebulaGradientStart = Color(0xFF9d50bb)   // primary-container
val SurfaceContainerCA = Color(0xFF1f1f22)     // surface-container
val SurfaceContainerHighest = Color(0xFF353438)
val SurfaceBrightCA = Color(0xFF39393c)        // surface-bright
val OnPrimaryCA = Color(0xFF520070)            // on-primary
val PrimaryContainerCA = Color(0xFF9d50bb)     // primary-container
val OnPrimaryContainerCA = Color(0xFFfff4fc)   // on-primary-container
val OnSecondaryCA = Color(0xFF2e303d)          // on-secondary
val OnSecondaryContainerCA = Color(0xFFb6b7c9) // on-secondary-container

// --- Stitch — Additional Material3 colors ---
val OnTertiaryCA = Color(0xFF3c2f00)           // on-tertiary
val OnTertiaryContainerCA = Color(0xFF4f3d00)  // on-tertiary-container
val OnPrimaryFixedCA = Color(0xFF320046)       // on-primary-fixed
val OnPrimaryFixedVariantCA = Color(0xFF6e208c) // on-primary-fixed-variant
val PrimaryFixedDimCA = Color(0xFFedb1ff)      // primary-fixed-dim
val SecondaryFixedCA = Color(0xFFe1e1f3)       // secondary-fixed
val SecondaryFixedDimCA = Color(0xFFc5c5d7)    // secondary-fixed-dim
val OnSecondaryFixedCA = Color(0xFF191b28)     // on-secondary-fixed
val OnSecondaryFixedVariantCA = Color(0xFF444654) // on-secondary-fixed-variant
val TertiaryFixedCA = Color(0xFFffe088)        // tertiary-fixed
val TertiaryFixedDimCA = Color(0xFFe9c349)     // tertiary-fixed-dim
val OnTertiaryFixedCA = Color(0xFF241a00)      // on-tertiary-fixed
val OnTertiaryFixedVariantCA = Color(0xFF574500) // on-tertiary-fixed-variant

// ══════════════════════════════
// Compatibility aliases
// (keeps all existing screens compiling)
// ══════════════════════════════
val NightBg1 = VoidBlack
val NightBg2 = CosmicDeep
val NightBg3 = CosmicMid
val EmeraldPrimary = AstralPurple
val EmeraldLight = AstralPurpleLight
val EmeraldDark = AstralPurpleDim
val AntiqueGold = CelestialGold
val AntiqueGoldLight = CelestialGoldLight
val AntiqueGoldDark = CelestialGoldDim
val Amethyst = AstralPurple
val AmethystLight = AstralPurpleLight
val AmethystDark = AstralPurpleDim
val CreamText = StarWhite
val CreamTextDim = MoonSilver
val DarkRuby = Color(0xFF93000a)           // error-container
val DarkRubyLight = Color(0xFFffb4ab)      // error
val GoldLine = CelestialGold
val GoldLineSubtle = Color(0x66e9c349)
val FrameBg = CosmicDeep
val FrameBgElevated = CosmicMid
val SurfaceWarm = Color(0xFF1f1f22)         // surface-container
val SurfaceCool = Color(0xFF1b1b1e)         // surface-container-low
val GoldGradientStart = CelestialGoldLight
val GoldGradientEnd = CelestialGoldDim
val ErrorRuby = ErrorCrimson
val WarningGold = WarningAmber
val InfoBlue = InfoSky
val OrnamentBorder = GlassBorderFocus
val OrnamentBorderActive = Color(0x60e9c349)
val OutlineColor = Color(0xFF474554)        // outline-variant
val OutlineVariantColor = Color(0xFF353438) // surface-variant
val DividerColor = Color(0x1A474554)        // ghost divider
val ScrimColor = ScrimDark
val OnPrimaryColor = StarWhite
val OnBackgroundColor = StarWhite
val OnSurfaceColor = StarWhite
val OnSurfaceVariantColor = MoonSilver

// Deep aliases for old code
val MysticPrimary = AstralPurple
val MysticPrimaryVariant = AstralPurpleDim
val MysticDark = AstralPurpleDim
val MysticLight = AstralPurpleLight
val GoldSecondary = CelestialGold
val GoldSecondaryVariant = CelestialGoldDim
val GoldLight = CelestialGoldLight
val TealTertiary = MysticTeal
val TealLight = MysticTeal
val TealDark = MysticTealDim
val MidnightBg1 = VoidBlack
val MidnightBg2 = CosmicDeep
val MidnightBg3 = CosmicMid
val SurfaceDark = CosmicDeep
val SurfaceVariant = CosmicMid
val SurfaceBright = CosmicSurface
val TextPrimary = StarWhite
val TextSecondary = MoonSilver
val OnPrimary = StarWhite
val OnBackground = StarWhite
val OnSurface = StarWhite
val OnSurfaceVariant = MoonSilver
val Accent = CelestialGold
val AccentLight = CelestialGoldLight
val Gold = CelestialGold
val Silver = MoonSilver
val CrystalBlue = InfoSky
val NebulaBlue = CosmicMid
val NebulaPurple = AstralPurpleDim
val CandleFlicker = CelestialGoldLight
val ErrorColor = ErrorCrimson
val WarningColor = WarningAmber
val SuccessColor = SuccessEmerald
val InfoColor = InfoSky
val Outline = OutlineColor
val OutlineVariant = OutlineVariantColor
val Divider = DividerColor
val Scrim = ScrimDark
val PurplePrimary = AstralPurple
val PurplePrimaryVariant = AstralPurpleDim
val PurpleDark = AstralPurpleDim
val PurpleLight = AstralPurpleLight
val BackgroundDark = VoidBlack
val DeepOcean1 = VoidBlack
val DeepOcean2 = CosmicDeep
