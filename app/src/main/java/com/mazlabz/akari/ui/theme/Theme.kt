package com.mazlabz.akari.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Washi / Akari palette — light, warmth, ink. No pure white, no alarm colors.
object Washi {
    val Paper = Color(0xFFF6F2EA)        // warm off-white background
    val Card = Color(0xFFFDFBF6)         // slightly lighter card
    val Ink = Color(0xFF3B3A36)          // soft charcoal
    val InkFaded = Color(0xFF8D887E)     // secondary text
    val Line = Color(0xFFE7E1D4)         // hairline dividers
    val Moss = Color(0xFF7C9A77)         // calm green accent
    val Persimmon = Color(0xFFC97B5D)    // muted warm accent (never shouting)
    val Amber = Color(0xFFD9A441)
    val GlowCore = Color(0xFFFFE9B8)     // lantern inner glow
    val GlowWarm = Color(0xFFF4C67A)     // lantern mid glow
    val Night = Color(0xFF181714)        // crash mode background
    val NightInk = Color(0xFFCFC9BE)     // crash mode text
    val NightCard = Color(0xFF24221E)    // crash mode buttons
    val Ember = Color(0xFF8A5A3B)        // crash mode accent
}

private val AkariColors = lightColorScheme(
    primary = Washi.Ink,
    onPrimary = Washi.Paper,
    secondary = Washi.Moss,
    onSecondary = Washi.Paper,
    tertiary = Washi.Persimmon,
    background = Washi.Paper,
    onBackground = Washi.Ink,
    surface = Washi.Card,
    onSurface = Washi.Ink,
    surfaceVariant = Washi.Card,
    onSurfaceVariant = Washi.InkFaded,
    outline = Washi.Line,
    error = Washi.Persimmon,
    onError = Washi.Paper
)

private val AkariType = Typography(
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        lineHeight = 32.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 21.sp,
        lineHeight = 27.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(fontSize = 17.sp, lineHeight = 25.sp),
    bodyMedium = TextStyle(fontSize = 15.sp, lineHeight = 22.sp),
    labelMedium = TextStyle(
        fontSize = 12.5.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.8.sp
    )
)

private val AkariShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(26.dp)
)

@Composable
fun AkariTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AkariColors,
        typography = AkariType,
        shapes = AkariShapes,
        content = content
    )
}
