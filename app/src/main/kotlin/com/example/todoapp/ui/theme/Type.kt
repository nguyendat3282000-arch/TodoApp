package com.example.todoapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.example.todoapp.R

// ══════════════════════════════════════════════════════════════════════════════
//  Google Fonts provider
// ══════════════════════════════════════════════════════════════════════════════

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage   = "com.google.android.gms",
    certificates      = R.array.com_google_android_gms_fonts_certs,
)

// ── Plus Jakarta Sans — body, labels ─────────────────────────────────────────
private val PlusJakartaSansFont = GoogleFont("Plus Jakarta Sans")

val PlusJakartaSansFamily = FontFamily(
    Font(googleFont = PlusJakartaSansFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = PlusJakartaSansFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = PlusJakartaSansFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = PlusJakartaSansFont, fontProvider = provider, weight = FontWeight.Bold),
)

// ── Quicksand — display, headlines ───────────────────────────────────────────
private val QuicksandFont = GoogleFont("Quicksand")

val QuicksandFamily = FontFamily(
    Font(googleFont = QuicksandFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = QuicksandFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = QuicksandFont, fontProvider = provider, weight = FontWeight.Bold),
)

// ══════════════════════════════════════════════════════════════════════════════
//  Material 3 Typography — matches Stitch design tokens
//  Display / Headline → Quicksand (chunky, rounded, friendly)
//  Title / Body / Label → Plus Jakarta Sans (clean, modern)
// ══════════════════════════════════════════════════════════════════════════════

val TodoTypography = Typography(

    // ── Display ───────────────────────────────────────────────────────────────
    displayLarge = TextStyle(
        fontFamily    = QuicksandFamily,
        fontWeight    = FontWeight.Bold,
        fontSize      = 57.sp,
        lineHeight    = 64.sp,
        letterSpacing = (-0.25).sp,
    ),
    displayMedium = TextStyle(
        fontFamily    = QuicksandFamily,
        fontWeight    = FontWeight.Bold,
        fontSize      = 45.sp,
        lineHeight    = 52.sp,
        letterSpacing = 0.sp,
    ),
    displaySmall = TextStyle(
        fontFamily    = QuicksandFamily,
        fontWeight    = FontWeight.Bold,
        fontSize      = 32.sp,      // display-lg in Stitch
        lineHeight    = 40.sp,
        letterSpacing = (-0.64).sp, // -0.02em at 32px
    ),

    // ── Headline ──────────────────────────────────────────────────────────────
    headlineLarge = TextStyle(
        fontFamily    = QuicksandFamily,
        fontWeight    = FontWeight.Bold,
        fontSize      = 32.sp,
        lineHeight    = 40.sp,
        letterSpacing = 0.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily    = QuicksandFamily,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 24.sp,      // headline-md in Stitch
        lineHeight    = 32.sp,
        letterSpacing = 0.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily    = QuicksandFamily,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 20.sp,      // headline-sm in Stitch
        lineHeight    = 28.sp,
        letterSpacing = 0.2.sp,
    ),

    // ── Title ─────────────────────────────────────────────────────────────────
    titleLarge = TextStyle(
        fontFamily    = PlusJakartaSansFamily,
        fontWeight    = FontWeight.Bold,
        fontSize      = 22.sp,
        lineHeight    = 28.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily    = PlusJakartaSansFamily,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 16.sp,
        lineHeight    = 24.sp,
        letterSpacing = 0.15.sp,
    ),
    titleSmall = TextStyle(
        fontFamily    = PlusJakartaSansFamily,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.1.sp,
    ),

    // ── Body ──────────────────────────────────────────────────────────────────
    bodyLarge = TextStyle(
        fontFamily    = PlusJakartaSansFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 16.sp,      // body-lg in Stitch
        lineHeight    = 24.sp,
        letterSpacing = 0.16.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily    = PlusJakartaSansFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 14.sp,      // body-md in Stitch
        lineHeight    = 20.sp,
        letterSpacing = 0.14.sp,
    ),
    bodySmall = TextStyle(
        fontFamily    = PlusJakartaSansFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 12.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.4.sp,
    ),

    // ── Label ─────────────────────────────────────────────────────────────────
    labelLarge = TextStyle(
        fontFamily    = PlusJakartaSansFamily,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily    = PlusJakartaSansFamily,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 12.sp,      // label-md in Stitch
        lineHeight    = 16.sp,
        letterSpacing = 0.6.sp,
    ),
    labelSmall = TextStyle(
        fontFamily    = PlusJakartaSansFamily,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 11.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.5.sp,
    ),
)
