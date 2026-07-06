package com.example.keepsake.ui.theme

import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// --- KEEPSAKE BASE SYSTEM COLORS ---
val KeepBackgroundDark = Color(0xFF202124)
val KeepBackgroundLight = Color(0xFFFFFFFF)
val KeepBorderDark = Color(0xFF5F6368)
val KeepBorderLight = Color(0xFFE0E0E0)

// --- GOOGLE KEEP STYLE PASTEL NOTE COLORS ---

// 1. Dark Mode Deeper Variants
val NoteDarkRed = Color(0xFF5C2B29)
val NoteDarkOrange = Color(0xFF614A19)
val NoteDarkYellow = Color(0xFF635D19)
val NoteDarkGreen = Color(0xFF345920)
val NoteDarkTeal = Color(0xFF16504B)
val NoteDarkBlue = Color(0xFF2D555E)
val NoteDarkDarkBlue = Color(0xFF1E3A5F)
val NoteDarkPurple = Color(0xFF42275E)
val NoteDarkPink = Color(0xFF5B2245)
val NoteDarkBrown = Color(0xFF442F19)
val NoteDarkDefault = Color(0xFF202124)

// 2. Light Mode Bright Pastel Variants
val NoteLightRed = Color(0xFFF28B82)
val NoteLightOrange = Color(0xFFFBBC04)
val NoteLightYellow = Color(0xFFFFF475)
val NoteLightGreen = Color(0xFFCCFF90)
val NoteLightTeal = Color(0xFFA7FFEB)
val NoteLightBlue = Color(0xFFCBF0F8)
val NoteLightDarkBlue = Color(0xFFAECBFA)
val NoteLightPurple = Color(0xFFD7AEFB)
val NoteLightPink = Color(0xFFFDCFE8)
val NoteLightBrown = Color(0xFFE6C9A8)
val NoteLightDefault = Color(0xFFFFFFFF)
fun getThemeNoteColor(savedColor: Long, isDarkTheme: Boolean): Color {
    return if (isDarkTheme) {
        when (savedColor) {
            0xFFFFFFFF.toLong() -> Color(0xFF202124) // Light default transforms to Dark default
            0xFFF28B82.toLong() -> Color(0xFF5C2B29)
            0xFFFBBC04.toLong() -> Color(0xFF614A19)
            0xFFFFF475.toLong() -> Color(0xFF635D19)
            0xFFCCFF90.toLong() -> Color(0xFF345920)
            0xFFA7FFEB.toLong() -> Color(0xFF16504B)
            0xFFCBF0F8.toLong() -> Color(0xFF2D555E)
            0xFFAECBFA.toLong() -> Color(0xFF1E3A5F)
            0xFFD7AEFB.toLong() -> Color(0xFF42275E)
            0xFFFDCFE8.toLong() -> Color(0xFF5B2245)
            0xFFE6C9A8.toLong() -> Color(0xFF442F19)
            else -> Color(savedColor)
        }
    } else {
        when (savedColor) {
            0xFF202124.toLong() -> Color(0xFFFFFFFF) // Dark default transforms to Light default
            0xFF5C2B29.toLong() -> Color(0xFFF28B82)
            0xFF614A19.toLong() -> Color(0xFFFBBC04)
            0xFF635D19.toLong() -> Color(0xFFFFF475)
            0xFF345920.toLong() -> Color(0xFFCCFF90)
            0xFF16504B.toLong() -> Color(0xFFA7FFEB)
            0xFF2D555E.toLong() -> Color(0xFFCBF0F8)
            0xFF1E3A5F.toLong() -> Color(0xFFAECBFA)
            0xFF42275E.toLong() -> Color(0xFFD7AEFB)
            0xFF5B2245.toLong() -> Color(0xFFFDCFE8)
            0xFF442F19.toLong() -> Color(0xFFE6C9A8)
            else -> Color(savedColor)
        }
    }
}

// Color Picker palettes depend on Current Active Mode
val LightNoteColors = listOf(
    0xFFFFFFFF, 0xFFF28B82, 0xFFFBBC04, 0xFFFFF475,
    0xFFCCFF90, 0xFFA7FFEB, 0xFFCBF0F8, 0xFFAECBFA,
    0xFFD7AEFB, 0xFFFDCFE8, 0xFFE6C9A8
)

val DarkNoteColors = listOf(
    0xFF202124, 0xFF5C2B29, 0xFF614A19, 0xFF635D19,
    0xFF345920, 0xFF16504B, 0xFF2D555E, 0xFF1E3A5F,
    0xFF42275E, 0xFF5B2245, 0xFF442F19
)