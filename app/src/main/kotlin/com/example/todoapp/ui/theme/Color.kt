package com.example.todoapp.ui.theme

import androidx.compose.ui.graphics.Color

// ══════════════════════════════════════════════════════════════════════════════
//  Modern Buddy Palette — Stitch design system (nature-green, earthy warmth)
// ══════════════════════════════════════════════════════════════════════════════

// ── Primary greens ────────────────────────────────────────────────────────────
val Primary            = Color(0xFF4E6356)   // #4e6356
val PrimaryContainer   = Color(0xFF8FA596)   // #8fa596
val OnPrimary          = Color(0xFFFFFFFF)
val OnPrimaryContainer = Color(0xFF273B2F)   // #273b2f
val PrimaryFixed       = Color(0xFFD1E8D8)   // #d1e8d8
val PrimaryFixedDim    = Color(0xFFB5CCBC)   // #b5ccbc
val InversePrimary     = Color(0xFFB5CCBC)

// ── Secondary greens ──────────────────────────────────────────────────────────
val Secondary            = Color(0xFF496455)   // #496455
val SecondaryContainer   = Color(0xFFCCEAD6)   // #ccead6
val OnSecondary          = Color(0xFFFFFFFF)
val OnSecondaryContainer = Color(0xFF4F6A5B)   // #4f6a5b
val SecondaryFixed       = Color(0xFFCCEAD6)
val SecondaryFixedDim    = Color(0xFFB0CDBB)

// ── Tertiary warm / earthy ────────────────────────────────────────────────────
val Tertiary            = Color(0xFF6E5B49)    // #6e5b49
val TertiaryContainer   = Color(0xFFB29C87)    // #b29c87
val OnTertiary          = Color(0xFFFFFFFF)
val OnTertiaryContainer = Color(0xFF433423)
val TertiaryFixed       = Color(0xFFF8DEC6)
val TertiaryFixedDim    = Color(0xFFDBC2AC)

// ── Surface / Background ──────────────────────────────────────────────────────
val Background              = Color(0xFFF4FBF6)   // #f4fbf6
val OnBackground            = Color(0xFF161D1A)
val Surface                 = Color(0xFFF4FBF6)
val SurfaceBright           = Color(0xFFF4FBF6)
val SurfaceContainerLowest  = Color(0xFFFFFFFF)
val SurfaceContainerLow     = Color(0xFFEEF5F0)   // #eef5f0
val SurfaceContainer        = Color(0xFFE9F0EB)   // #e9f0eb
val SurfaceContainerHigh    = Color(0xFFE3EAE5)   // #e3eae5
val SurfaceContainerHighest = Color(0xFFDDE4DF)   // #dde4df
val SurfaceDim              = Color(0xFFD5DCD7)   // #d5dcd7
val SurfaceVariant          = Color(0xFFDDE4DF)
val InverseSurface          = Color(0xFF2B322F)
val InverseOnSurface        = Color(0xFFEBF2ED)
val OnSurface               = Color(0xFF161D1A)
val OnSurfaceVariant        = Color(0xFF424844)

// ── Outline ───────────────────────────────────────────────────────────────────
val Outline        = Color(0xFF737873)   // #737873
val OutlineVariant = Color(0xFFC2C8C2)   // #c2c8c2

// ── Error ─────────────────────────────────────────────────────────────────────
val Error          = Color(0xFFBA1A1A)
val ErrorContainer = Color(0xFFFFDAD6)
val OnError        = Color(0xFFFFFFFF)
val OnErrorContainer = Color(0xFF93000A)

// ── Dark scheme ───────────────────────────────────────────────────────────────
val DarkPrimary            = Color(0xFFA8C5AF)   // soft sage green — readable on dark bg
val DarkOnPrimary          = Color(0xFF0D2619)   // deep forest
val DarkPrimaryContainer   = Color(0xFF243B2D)   // mid-depth green container
val DarkOnPrimaryContainer = Color(0xFFCEE8D5)   // pale mint label
val DarkSecondary          = Color(0xFF9EC4AB)   // muted teal-green
val DarkOnSecondary        = Color(0xFF0F2B1E)
val DarkSecondaryContainer = Color(0xFF1D3829)
val DarkOnSecondaryContainer = Color(0xFFBCDFC8)
val DarkTertiary            = Color(0xFFCDB69D)   // warm sand
val DarkOnTertiary          = Color(0xFF2C1E0D)
val DarkTertiaryContainer   = Color(0xFF40301F)
val DarkOnTertiaryContainer = Color(0xFFEED4BB)
val DarkBackground   = Color(0xFF0F1613)   // near-black green-tinted
val DarkOnBackground = Color(0xFFE2EBE4)   // high-contrast near-white
val DarkSurface          = Color(0xFF0F1613)
val DarkSurfaceVariant   = Color(0xFF2A322D)   // elevated card surface
val DarkOnSurface        = Color(0xFFE2EBE4)
val DarkOnSurfaceVariant = Color(0xFFB8C5BA)
val DarkOutline        = Color(0xFF6A736B)
val DarkOutlineVariant = Color(0xFF2A322D)
val DarkError   = Color(0xFFFFB4AB)
val DarkOnError = Color(0xFF690005)
val DarkErrorContainer   = Color(0xFF93000A)
val DarkOnErrorContainer = Color(0xFFFFDAD6)

// ── Semantic helpers ──────────────────────────────────────────────────────────
val SuccessGreen  = Color(0xFF2ECC71)
val WarningAmber  = Color(0xFFF39C12)
val ErrorRose     = Error

// ── Legacy aliases (keep for swipe-delete etc.) ───────────────────────────────
val Mint100 = SurfaceContainerLow
val Mint500 = Primary
val Coral100 = ErrorContainer
val Coral500 = Error
val BabyBlue200 = SurfaceContainerHighest
val Neutral200 = SurfaceContainerHighest
