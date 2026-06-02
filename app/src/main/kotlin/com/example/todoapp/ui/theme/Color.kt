package com.example.todoapp.ui.theme

import androidx.compose.ui.graphics.Color

// ══════════════════════════════════════════════════════════════════════════════
//  Brand / Signature Palette  — Baemin-inspired pastel playfulness
// ══════════════════════════════════════════════════════════════════════════════

// ── Mint Green (primary) ──────────────────────────────────────────────────────
val Mint100   = Color(0xFFE8FAF6)
val Mint200   = Color(0xFFC5F0E5)
val Mint300   = Color(0xFF96E4CE)
val Mint400   = Color(0xFF5DD5B3)
val Mint500   = Color(0xFF2BBFA0)   // ← brand primary
val Mint600   = Color(0xFF1FA080)
val Mint700   = Color(0xFF157A5F)

// ── Coral / Peach Orange (secondary / accent) ─────────────────────────────────
val Coral100  = Color(0xFFFFF0EC)
val Coral200  = Color(0xFFFFD5C8)
val Coral300  = Color(0xFFFFB39E)
val Coral400  = Color(0xFFFF8C72)
val Coral500  = Color(0xFFFF6B4E)   // ← brand accent
val Coral600  = Color(0xFFE04E33)
val Coral700  = Color(0xFFB83420)

// ── Baby Blue (tertiary) ──────────────────────────────────────────────────────
val BabyBlue100 = Color(0xFFE8F4FF)
val BabyBlue200 = Color(0xFFC2DCFF)
val BabyBlue300 = Color(0xFF91BFFF)
val BabyBlue400 = Color(0xFF5C9EFF)
val BabyBlue500 = Color(0xFF3282FF)

// ── Lavender (container surface) ─────────────────────────────────────────────
val Lavender100 = Color(0xFFF4F0FF)
val Lavender200 = Color(0xFFE3D9FF)
val Lavender300 = Color(0xFFCAB8FF)

// ── Lemon Yellow (done state / chip) ─────────────────────────────────────────
val Lemon100  = Color(0xFFFFFBE8)
val Lemon300  = Color(0xFFFFE680)
val Lemon500  = Color(0xFFFFD700)

// ── Neutral / Surface shades ──────────────────────────────────────────────────
val Neutral50   = Color(0xFFFAFBFC)
val Neutral100  = Color(0xFFF2F4F6)
val Neutral200  = Color(0xFFE5E9EE)
val Neutral300  = Color(0xFFCDD3DA)
val Neutral400  = Color(0xFFADB6C0)
val Neutral500  = Color(0xFF8993A0)
val Neutral600  = Color(0xFF636D78)
val Neutral700  = Color(0xFF434B54)
val Neutral800  = Color(0xFF272D34)
val Neutral900  = Color(0xFF131619)

// ── Semantic states ───────────────────────────────────────────────────────────
val SuccessGreen  = Color(0xFF2ECC71)
val WarningAmber  = Color(0xFFF39C12)
val ErrorRose     = Color(0xFFE74C3C)
val InfoBlue      = Color(0xFF3498DB)

// ══════════════════════════════════════════════════════════════════════════════
//  Light Theme tokens
// ══════════════════════════════════════════════════════════════════════════════
val LightPrimary            = Mint600 // Increased from Mint500 for AAA contrast with White text
val LightOnPrimary          = Color.White
val LightPrimaryContainer   = Mint100
val LightOnPrimaryContainer = Mint700

val LightSecondary            = Coral600 // Increased from Coral500 for AAA contrast
val LightOnSecondary          = Color.White
val LightSecondaryContainer   = Coral100
val LightOnSecondaryContainer = Coral700

val LightTertiary            = BabyBlue500 // Increased from BabyBlue400
val LightOnTertiary          = Color.White
val LightTertiaryContainer   = BabyBlue100
val LightOnTertiaryContainer = BabyBlue500

val LightBackground   = Neutral50
val LightOnBackground = Neutral900

val LightSurface          = Color.White
val LightOnSurface        = Neutral800
val LightSurfaceVariant   = Neutral100
val LightOnSurfaceVariant = Neutral600

val LightOutline         = Neutral300
val LightOutlineVariant  = Neutral200

val LightError   = ErrorRose
val LightOnError = Color.White

// ══════════════════════════════════════════════════════════════════════════════
//  Dark Theme tokens
// ══════════════════════════════════════════════════════════════════════════════
val DarkPrimary            = Mint300
val DarkOnPrimary          = Mint700
val DarkPrimaryContainer   = Mint600
val DarkOnPrimaryContainer = Mint100

val DarkSecondary            = Coral300
val DarkOnSecondary          = Coral700
val DarkSecondaryContainer   = Coral600
val DarkOnSecondaryContainer = Coral100

val DarkTertiary            = BabyBlue300
val DarkOnTertiary          = BabyBlue500
val DarkTertiaryContainer   = BabyBlue500
val DarkOnTertiaryContainer = BabyBlue100

val DarkBackground   = Neutral900
val DarkOnBackground = Neutral100

val DarkSurface          = Neutral800
val DarkOnSurface        = Neutral200
val DarkSurfaceVariant   = Neutral700
val DarkOnSurfaceVariant = Neutral400

val DarkOutline        = Neutral600
val DarkOutlineVariant = Neutral700

val DarkError   = Color(0xFFFF6B6B)
val DarkOnError = Color(0xFF690000)
