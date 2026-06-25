package com.example.todoapp.ui.theme

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ══════════════════════════════════════════════════════════════════════════════
//  Material 3 Color Schemes — Modern Buddy (Stitch design)
// ══════════════════════════════════════════════════════════════════════════════

private val LightColorScheme = lightColorScheme(
    primary            = Primary,
    onPrimary          = OnPrimary,
    primaryContainer   = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,

    secondary            = Secondary,
    onSecondary          = OnSecondary,
    secondaryContainer   = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,

    tertiary            = Tertiary,
    onTertiary          = OnTertiary,
    tertiaryContainer   = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,

    background   = Background,
    onBackground = OnBackground,

    surface          = Surface,
    onSurface        = OnSurface,
    surfaceVariant   = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,

    outline        = Outline,
    outlineVariant = OutlineVariant,

    error            = Error,
    onError          = OnError,
    errorContainer   = ErrorContainer,
    onErrorContainer = OnErrorContainer,

    inverseSurface   = InverseSurface,
    inverseOnSurface = InverseOnSurface,
    inversePrimary   = InversePrimary,
)

private val DarkColorScheme = darkColorScheme(
    primary            = DarkPrimary,
    onPrimary          = DarkOnPrimary,
    primaryContainer   = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,

    secondary            = DarkSecondary,
    onSecondary          = DarkOnSecondary,
    secondaryContainer   = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,

    tertiary            = DarkTertiary,
    onTertiary          = DarkOnTertiary,
    tertiaryContainer   = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,

    background   = DarkBackground,
    onBackground = DarkOnBackground,

    surface          = DarkSurface,
    onSurface        = DarkOnSurface,
    surfaceVariant   = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,

    outline        = DarkOutline,
    outlineVariant = DarkOutlineVariant,

    error   = DarkError,
    onError = DarkOnError,
    errorContainer   = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
)

// ══════════════════════════════════════════════════════════════════════════════
//  TodoTheme composable
// ══════════════════════════════════════════════════════════════════════════════

/**
 * The root theme for the TodoApp — "Modern Buddy" style.
 *
 * @param darkTheme    Follow system dark-mode setting by default.
 * @param dynamicColor Use Android 12+ dynamic color (Material You).
 *                     Set false to always use the brand green palette.
 */
@Composable
fun TodoTheme(
    darkTheme: Boolean    = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else           dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    // Status bar
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = TodoTypography,
        shapes      = TodoShapes,
        content     = content,
    )
}
