package com.mazlabz.akari.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Washi / Akari palette — light, warmth, ink. No pure white, no alarm colors.
 *
 * `night` switches the whole app into a dim, warm low-light palette (photophobia
 * and sensory sensitivity are core ME/CFS symptoms — a bright screen is itself
 * an energy cost). Every consumer reads these properties inside composition, so
 * flipping the flag recomposes the entire app.
 */
object Washi {
    var night by mutableStateOf(false)

    val Paper: Color get() = if (night) Color(0xFF161513) else Color(0xFFF6F2EA)
    val Card: Color get() = if (night) Color(0xFF211F1B) else Color(0xFFFDFBF6)
    val Ink: Color get() = if (night) Color(0xFFD8D2C6) else Color(0xFF3B3A36)
    // day value darkened from 0xFF8D887E for WCAG-friendlier contrast on paper
    val InkFaded: Color get() = if (night) Color(0xFF97917F) else Color(0xFF6E6A61)
    val Line: Color get() = if (night) Color(0xFF35322C) else Color(0xFFE7E1D4)

    val Moss = Color(0xFF7C9A77)
    val Persimmon = Color(0xFFC97B5D)
    val Amber = Color(0xFFD9A441)
    val GlowCore = Color(0xFFFFE9B8)
    val GlowWarm = Color(0xFFF4C67A)
    val Night = Color(0xFF181714)
    val NightInk = Color(0xFFCFC9BE)
    val NightCard = Color(0xFF24221E)
    val Ember = Color(0xFF8A5A3B)

    // Readable text-grade accents: deep on the day palette, brightened for night,
    // so accent-coloured text and filled accent buttons meet WCAG AA in both modes.
    val MossText: Color get() = if (night) Color(0xFF9DBE97) else Color(0xFF4C6746)
    val AmberText: Color get() = if (night) Color(0xFFE3B968) else Color(0xFF875510)
    val PersimmonText: Color get() = if (night) Color(0xFFDD9C82) else Color(0xFF9C4526)

    // Pale tint fills behind chips and labels, in their accent hue.
    val MossTint: Color get() = if (night) Color(0xFF26302A) else Color(0xFFE7EEE3)
    val AmberTint: Color get() = if (night) Color(0xFF322A1A) else Color(0xFFF6E9D2)
    val PersimmonTint: Color get() = if (night) Color(0xFF33241E) else Color(0xFFF5E2D8)
}

private val AkariType = Typography(
    displaySmall = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 40.sp,
        lineHeight = 44.sp
    ),
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

/** One spacing scale, for consistent vertical rhythm across screens. */
object Space {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
}

@Composable
fun AkariTheme(content: @Composable () -> Unit) {
    // built per-composition so the low-light flag re-themes everything
    val colors = lightColorScheme(
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
    MaterialTheme(
        colorScheme = colors,
        typography = AkariType,
        shapes = AkariShapes,
        content = content
    )
}
