package com.example.drinkup.ui.theme

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
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ─────────────────────────────────────────
// LIGHT COLOR SCHEME
// ─────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary            = Navy800,
    onPrimary          = White,
    primaryContainer   = Navy100,
    onPrimaryContainer = Navy900,

    secondary            = Teal500,
    onSecondary          = White,
    secondaryContainer   = Teal50,
    onSecondaryContainer = Teal600,

    tertiary            = WaterMid,
    onTertiary          = White,
    tertiaryContainer   = WaterBg,
    onTertiaryContainer = WaterDeep,

    background   = BgPage,
    onBackground = TextPrimary,
    surface      = BgSurface,
    onSurface    = TextPrimary,

    surfaceVariant   = BgSurfaceAlt,
    onSurfaceVariant = TextSecondary,

    outline        = BorderLight,
    outlineVariant = BorderMedium,

    error            = Error500,
    onError          = White,
    errorContainer   = Error100,
    onErrorContainer = Error500,

    scrim = Color(0x52000000),
)

// ─────────────────────────────────────────
// DARK COLOR SCHEME
// ─────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary            = Navy200,
    onPrimary          = Navy900,
    primaryContainer   = Navy700,
    onPrimaryContainer = Navy100,

    secondary            = Teal300,
    onSecondary          = Teal600,
    secondaryContainer   = Color(0xFF0F4A3F),
    onSecondaryContainer = Teal200,

    tertiary            = WaterLight,
    onTertiary          = WaterDeep,
    tertiaryContainer   = Color(0xFF0D3A52),
    onTertiaryContainer = WaterSurface,

    background   = DarkBgPage,
    onBackground = White,
    surface      = DarkBgSurface,
    onSurface    = White,

    surfaceVariant   = DarkBgSurfaceAlt,
    onSurfaceVariant = Neutral300,

    outline        = Color(0xFF2E4A6E),
    outlineVariant = Color(0xFF1E3A6E),

    error            = Color(0xFFF87171),
    onError          = Color(0xFF7F1D1D),
    errorContainer   = Color(0xFF991B1B),
    onErrorContainer = Color(0xFFFECACA),

    scrim = Color(0x73000000),
)

// ─────────────────────────────────────────
// MAIN THEME COMPOSABLE
// ─────────────────────────────────────────
@Composable
fun DrinkUpTheme(
    darkTheme    : Boolean = isSystemInDarkTheme(),
    dynamicColor : Boolean = false,
    content      : @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = DrinkUpTypography,
        content     = content
    )
}

// ─────────────────────────────────────────
// CUSTOM COLORS EXTENSION
// Pakai: MaterialTheme.drinkUpColors.waterMid
// ─────────────────────────────────────────
data class DrinkUpColors(
    val waterDeep   : Color = WaterDeep,
    val waterMid    : Color = WaterMid,
    val waterLight  : Color = WaterLight,
    val waterSurface: Color = WaterSurface,
    val waterBg     : Color = WaterBg,
    val streakOrange: Color = StreakOrange,
    val streakBg    : Color = StreakBg,
    val streakText  : Color = StreakText,
    val navActive   : Color = NavActive,
    val navInactive : Color = NavInactive,
    val teal50      : Color = Teal50,
    val teal100     : Color = Teal100,
    val teal600     : Color = Teal600,
    val navy50      : Color = Navy50,
    val navy100     : Color = Navy100,
    val bgOverlay   : Color = BgOverlay,
    val textTeal    : Color = TextTeal,
    val textNavy    : Color = TextNavy,
)

val LocalDrinkUpColors = staticCompositionLocalOf { DrinkUpColors() }

val MaterialTheme.drinkUpColors: DrinkUpColors
    @Composable get() = LocalDrinkUpColors.current