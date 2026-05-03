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

// Warna khusus tema navy app
private val AppNavyDeep    = Color(0xFF0A1F5C)
private val AppNavyMid     = Color(0xFF0D3B8E)
private val AppNavyCard    = Color(0xFF112870)
private val AppNavyBorder  = Color(0xFF1E3FA0)
private val AppWaterBlue   = Color(0xFF4FC3F7)
private val AppWaterMid    = Color(0xFF29B6F6)
private val AppTextPrimary = Color(0xFFFFFFFF)
private val AppTextSub     = Color(0xFFB0C4E8)
private val AppOrange      = Color(0xFFFF6D00)

// ─────────────────────────────────────────
// NAVY COLOR SCHEME (dipakai untuk kedua mode agar konsisten)
// ─────────────────────────────────────────
private val NavyColorScheme = darkColorScheme(
    primary              = AppWaterBlue,
    onPrimary            = AppNavyDeep,
    primaryContainer     = AppNavyMid,
    onPrimaryContainer   = AppTextPrimary,

    secondary            = AppWaterMid,
    onSecondary          = AppNavyDeep,
    secondaryContainer   = AppNavyCard,
    onSecondaryContainer = AppTextPrimary,

    tertiary             = AppOrange,
    onTertiary           = Color.White,
    tertiaryContainer    = Color(0xFF1A3A7A),
    onTertiaryContainer  = AppTextPrimary,

    // Background utama — navy gelap
    background           = AppNavyDeep,
    onBackground         = AppTextPrimary,

    // Surface card
    surface              = AppNavyCard,
    onSurface            = AppTextPrimary,

    surfaceVariant       = Color(0xFF0D3B8E),
    onSurfaceVariant     = AppTextSub,

    outline              = AppNavyBorder,
    outlineVariant       = Color(0xFF1A3580),

    error                = Color(0xFFF87171),
    onError              = Color(0xFF7F1D1D),
    errorContainer       = Color(0xFF991B1B),
    onErrorContainer     = Color(0xFFFECACA),

    scrim                = Color(0x73000000),
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
    // Selalu pakai NavyColorScheme agar konsisten dengan design
    val colorScheme = NavyColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Status bar ikut warna navy
            window.statusBarColor         = AppNavyDeep.toArgb()
            window.navigationBarColor     = AppNavyDeep.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = false  // icon status bar putih
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