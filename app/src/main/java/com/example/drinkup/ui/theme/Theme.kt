package com.example.drinkup.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ─────────────────────────────────────────────────────────────────────────────
// PALETTE — selaras dengan SettingsScreen & EditProfileScreen
// ─────────────────────────────────────────────────────────────────────────────

// Background
val AppBgDeep        = Color(0xFF060D1F)   // background utama terdalam
val AppBgMid         = Color(0xFF0A1428)   // layer tengah
val AppBgSurface     = Color(0xFF0B1A35)   // layer paling atas (gradient ujung)

// Card / Container
val AppNavyCard      = Color(0xFF0D1E38)   // card biasa
val AppNavyCardAlt   = Color(0xFF112244)   // card alternatif / lebih terang
val AppNavyAccent    = Color(0xFF152A50)   // icon background, pressed state
val AppBorderSubtle  = Color(0xFF162845)   // border halus antar elemen

// Primary — Teal
val AppTealPrimary   = Color(0xFF00D4AA)   // warna utama app
val AppTealLight     = Color(0xFF26E5BC)   // hover / highlight
val AppTealDark      = Color(0xFF00B894)   // gradient end tombol simpan

// Accent — Cyan & Blue
val AppCyanAccent    = Color(0xFF00BFFF)   // aksen sekunder
val AppBlueGradStart = Color(0xFF005FA3)   // daily target card gradient start
val AppBlueGradMid   = Color(0xFF0099CC)   // daily target card gradient mid
val AppBlueGradEnd   = Color(0xFF00C4E0)   // daily target card gradient end

// Text
val AppTextWhite     = Color(0xFFFFFFFF)   // teks utama
val AppTextSub       = Color(0xFF7A9BBF)   // teks sekunder / placeholder aktif
val AppTextMuted     = Color(0xFF3D5A7A)   // teks disabled / label ringan

// Semantic
val AppGreenOnline   = Color(0xFF1DDB8B)   // dot online, sukses
val AppDangerRed     = Color(0xFFFF4D6A)   // delete / error / destructive
val AppPinkFemale    = Color(0xFFFF6B9D)   // badge gender perempuan
val AppProgressEmpty = Color(0xFF182C48)   // progress bar fill kosong

// ─────────────────────────────────────────────────────────────────────────────
// COLOR SCHEME — dark, konsisten dengan desain navy
// ─────────────────────────────────────────────────────────────────────────────
private val AppColorScheme = darkColorScheme(

    // Primary = Teal (tombol utama, aksen, highlight)
    primary              = AppTealPrimary,
    onPrimary            = AppBgDeep,
    primaryContainer     = AppNavyCard,
    onPrimaryContainer   = AppTealPrimary,

    // Secondary = Cyan (aksen pendukung)
    secondary            = AppCyanAccent,
    onSecondary          = AppBgDeep,
    secondaryContainer   = AppNavyCardAlt,
    onSecondaryContainer = AppCyanAccent,

    // Tertiary = Danger Red (delete, error state)
    tertiary             = AppDangerRed,
    onTertiary           = Color.White,
    tertiaryContainer    = AppDangerRed.copy(alpha = 0.12f),
    onTertiaryContainer  = AppDangerRed,

    // Background
    background           = AppBgDeep,
    onBackground         = AppTextWhite,

    // Surface (card, bottom sheet, dialog)
    surface              = AppNavyCard,
    onSurface            = AppTextWhite,
    surfaceVariant       = AppNavyCardAlt,
    onSurfaceVariant     = AppTextSub,

    // Outline
    outline              = AppBorderSubtle,
    outlineVariant       = AppNavyAccent,

    // Error
    error                = AppDangerRed,
    onError              = Color.White,
    errorContainer       = AppDangerRed.copy(alpha = 0.14f),
    onErrorContainer     = AppDangerRed,

    // Scrim (overlay modal)
    scrim                = Color(0x80000000),
)

// ─────────────────────────────────────────────────────────────────────────────
// MAIN THEME
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun DrinkUpTheme(
    darkTheme    : Boolean = isSystemInDarkTheme(),
    dynamicColor : Boolean = false,   // selalu false — pakai palette custom
    content      : @Composable () -> Unit
) {
    val colorScheme = AppColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor     = AppBgDeep.toArgb()
            window.navigationBarColor = AppBgDeep.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = false   // icon status bar putih
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = DrinkUpTypography,
        content     = content
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// CUSTOM COLORS — akses via MaterialTheme.drinkUpColors
// ─────────────────────────────────────────────────────────────────────────────
data class DrinkUpColors(
    // Background layers
    val bgDeep        : Color = AppBgDeep,
    val bgMid         : Color = AppBgMid,
    val bgSurface     : Color = AppBgSurface,

    // Card / container
    val navyCard      : Color = AppNavyCard,
    val navyCardAlt   : Color = AppNavyCardAlt,
    val navyAccent    : Color = AppNavyAccent,
    val borderSubtle  : Color = AppBorderSubtle,

    // Primary teal
    val tealPrimary   : Color = AppTealPrimary,
    val tealLight     : Color = AppTealLight,
    val tealDark      : Color = AppTealDark,

    // Accent
    val cyanAccent    : Color = AppCyanAccent,

    // Daily target gradient
    val blueGradStart : Color = AppBlueGradStart,
    val blueGradMid   : Color = AppBlueGradMid,
    val blueGradEnd   : Color = AppBlueGradEnd,

    // Text
    val textWhite     : Color = AppTextWhite,
    val textSub       : Color = AppTextSub,
    val textMuted     : Color = AppTextMuted,

    // Semantic
    val greenOnline   : Color = AppGreenOnline,
    val dangerRed     : Color = AppDangerRed,
    val pinkFemale    : Color = AppPinkFemale,
    val progressEmpty : Color = AppProgressEmpty,
)

val LocalDrinkUpColors = staticCompositionLocalOf { DrinkUpColors() }

val MaterialTheme.drinkUpColors: DrinkUpColors
    @Composable get() = LocalDrinkUpColors.current