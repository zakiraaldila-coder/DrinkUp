package com.example.drinkup.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────
// FONT FAMILY
//
// Sekarang pakai FontFamily.Default supaya bisa langsung compile.
// Kalau mau pakai Nunito:
//   1. Download: https://fonts.google.com/specimen/Nunito
//   2. Taruh di res/font/: nunito_light.ttf, nunito_regular.ttf,
//      nunito_medium.ttf, nunito_semibold.ttf, nunito_bold.ttf, nunito_extrabold.ttf
//   3. Ganti baris di bawah dengan blok yang ada di komentar
// ─────────────────────────────────────────

// AKTIF: langsung compile tanpa file font
val NunitoFamily: FontFamily = FontFamily.Default

/* Aktifkan setelah file font ada di res/font/:
import androidx.compose.ui.text.font.Font
val NunitoFamily = FontFamily(
    Font(com.example.drinkup.R.font.nunito_light,     FontWeight.Light),
    Font(com.example.drinkup.R.font.nunito_regular,   FontWeight.Normal),
    Font(com.example.drinkup.R.font.nunito_medium,    FontWeight.Medium),
    Font(com.example.drinkup.R.font.nunito_semibold,  FontWeight.SemiBold),
    Font(com.example.drinkup.R.font.nunito_bold,      FontWeight.Bold),
    Font(com.example.drinkup.R.font.nunito_extrabold, FontWeight.ExtraBold),
)
*/

// ─────────────────────────────────────────
// TYPOGRAPHY SCALE
// ─────────────────────────────────────────
val DrinkUpTypography = Typography(

    displayLarge = TextStyle(
        fontFamily    = NunitoFamily,
        fontWeight    = FontWeight.ExtraBold,
        fontSize      = 56.sp,
        lineHeight    = 64.sp,
        letterSpacing = (-1).sp
    ),
    displayMedium = TextStyle(
        fontFamily    = NunitoFamily,
        fontWeight    = FontWeight.ExtraBold,
        fontSize      = 44.sp,
        lineHeight    = 52.sp,
        letterSpacing = (-0.5).sp
    ),
    displaySmall = TextStyle(
        fontFamily    = NunitoFamily,
        fontWeight    = FontWeight.Bold,
        fontSize      = 36.sp,
        lineHeight    = 44.sp,
        letterSpacing = (-0.25).sp
    ),

    headlineLarge = TextStyle(
        fontFamily    = NunitoFamily,
        fontWeight    = FontWeight.Bold,
        fontSize      = 28.sp,
        lineHeight    = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily    = NunitoFamily,
        fontWeight    = FontWeight.Bold,
        fontSize      = 24.sp,
        lineHeight    = 32.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily    = NunitoFamily,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 20.sp,
        lineHeight    = 28.sp,
        letterSpacing = 0.sp
    ),

    titleLarge = TextStyle(
        fontFamily    = NunitoFamily,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 18.sp,
        lineHeight    = 26.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily    = NunitoFamily,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 16.sp,
        lineHeight    = 24.sp,
        letterSpacing = 0.sp
    ),
    titleSmall = TextStyle(
        fontFamily    = NunitoFamily,
        fontWeight    = FontWeight.Medium,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.sp
    ),

    bodyLarge = TextStyle(
        fontFamily    = NunitoFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 16.sp,
        lineHeight    = 24.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily    = NunitoFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 14.sp,
        lineHeight    = 22.sp,
        letterSpacing = 0.sp
    ),
    bodySmall = TextStyle(
        fontFamily    = NunitoFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 12.sp,
        lineHeight    = 18.sp,
        letterSpacing = 0.sp
    ),

    labelLarge = TextStyle(
        fontFamily    = NunitoFamily,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily    = NunitoFamily,
        fontWeight    = FontWeight.Medium,
        fontSize      = 12.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily    = NunitoFamily,
        fontWeight    = FontWeight.Medium,
        fontSize      = 10.sp,
        lineHeight    = 14.sp,
        letterSpacing = 0.5.sp
    ),
)

// ─────────────────────────────────────────
// CUSTOM TEXT STYLES
// ─────────────────────────────────────────

val WaterDisplayStyle = TextStyle(
    fontFamily    = NunitoFamily,
    fontWeight    = FontWeight.ExtraBold,
    fontSize      = 64.sp,
    lineHeight    = 72.sp,
    letterSpacing = (-2).sp
)

val WaterUnitStyle = TextStyle(
    fontFamily    = NunitoFamily,
    fontWeight    = FontWeight.Medium,
    fontSize      = 13.sp,
    lineHeight    = 18.sp,
    letterSpacing = 1.sp
)

val GreetingSubStyle = TextStyle(
    fontFamily    = NunitoFamily,
    fontWeight    = FontWeight.Normal,
    fontSize      = 14.sp,
    lineHeight    = 20.sp,
    letterSpacing = 0.sp
)

val GreetingNameStyle = TextStyle(
    fontFamily    = NunitoFamily,
    fontWeight    = FontWeight.ExtraBold,
    fontSize      = 26.sp,
    lineHeight    = 32.sp,
    letterSpacing = (-0.5).sp
)

val ChipLabelStyle = TextStyle(
    fontFamily    = NunitoFamily,
    fontWeight    = FontWeight.Bold,
    fontSize      = 10.sp,
    lineHeight    = 14.sp,
    letterSpacing = 0.8.sp
)

val ChipValueStyle = TextStyle(
    fontFamily    = NunitoFamily,
    fontWeight    = FontWeight.ExtraBold,
    fontSize      = 16.sp,
    lineHeight    = 22.sp,
    letterSpacing = 0.sp
)

val StreakNumberStyle = TextStyle(
    fontFamily    = NunitoFamily,
    fontWeight    = FontWeight.ExtraBold,
    fontSize      = 22.sp,
    lineHeight    = 28.sp,
    letterSpacing = (-0.5).sp
)

val QuoteStyle = TextStyle(
    fontFamily    = NunitoFamily,
    fontWeight    = FontWeight.Medium,
    fontStyle     = FontStyle.Italic,
    fontSize      = 14.sp,
    lineHeight    = 22.sp,
    letterSpacing = 0.sp
)