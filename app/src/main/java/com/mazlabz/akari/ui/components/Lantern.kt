package com.mazlabz.akari.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mazlabz.akari.PacingGuide
import com.mazlabz.akari.ui.theme.Washi
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * The Akari lantern: the day's energy envelope drawn as paper-diffused light,
 * wrapped in a zoned gauge so the number always comes with a judgment.
 *
 * Ring zones (drawn faintly behind the level arc):
 *   green  = Steady  (above 40 %)
 *   amber  = Getting low (15–40 %)
 *   warm red = Rest zone (below 15 %)
 * A marker dot sits at the current level so the eye finds "where am I" instantly.
 */
@Composable
fun Lantern(
    level: Float,
    modifier: Modifier = Modifier,
    size: Dp = 230.dp,
    night: Boolean = false
) {
    val animated by animateFloatAsState(
        targetValue = level.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 900),
        label = "lantern"
    )

    Box(modifier = modifier.size(size)) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            val cx = w / 2f
            val cy = h / 2f
            val r = min(w, h) / 2f

            val glow = 0.15f + 0.85f * animated  // embers always remain

            // ---- outer halo: light bleeding through paper ----
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Washi.GlowWarm.copy(alpha = 0.55f * glow),
                        Washi.GlowWarm.copy(alpha = 0.18f * glow),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy),
                    radius = r
                ),
                radius = r,
                center = Offset(cx, cy)
            )

            // ---- lantern body ----
            val bodyW = r * 1.08f
            val bodyH = r * 1.38f
            val bodyTop = cy - bodyH / 2f

            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Washi.GlowCore.copy(alpha = 0.35f + 0.65f * glow),
                        Washi.GlowWarm.copy(alpha = 0.30f + 0.45f * glow),
                        (if (night) Washi.NightCard else Washi.Card).copy(alpha = 0.9f)
                    ),
                    center = Offset(cx, cy - bodyH * 0.08f),
                    radius = bodyH * 0.62f
                ),
                topLeft = Offset(cx - bodyW / 2f, bodyTop),
                size = Size(bodyW, bodyH)
            )

            // ---- washi ribs ----
            val ribColor = (if (night) Washi.NightInk else Washi.Ink).copy(alpha = 0.14f)
            val ribs = 7
            for (i in 1 until ribs) {
                val t = i.toFloat() / ribs
                val y = bodyTop + bodyH * t
                val dy = (y - cy) / (bodyH / 2f)
                val halfW = bodyW / 2f * sqrt((1f - dy * dy).coerceAtLeast(0f))
                drawLine(
                    color = ribColor,
                    start = Offset(cx - halfW, y),
                    end = Offset(cx + halfW, y),
                    strokeWidth = 2f,
                    cap = StrokeCap.Round
                )
            }

            // ---- inner flame core ----
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.85f * glow),
                        Washi.GlowCore.copy(alpha = 0.55f * glow),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy),
                    radius = r * 0.36f
                ),
                radius = r * 0.36f,
                center = Offset(cx, cy)
            )

            // ---- zoned gauge ring ----
            val ringR = r * 0.97f
            val ringRect = Offset(cx - ringR, cy - ringR)
            val ringSize = Size(ringR * 2f, ringR * 2f)
            val stroke = Stroke(width = 9f, cap = StrokeCap.Round)
            val faint = if (night) 0.28f else 0.22f

            // background zones: start at 12 o'clock, clockwise
            val restSweep = 360f * PacingGuide.REST_ZONE
            val lowSweep = 360f * (PacingGuide.LOW_ZONE - PacingGuide.REST_ZONE)
            val steadySweep = 360f * (1f - PacingGuide.LOW_ZONE)
            drawArc(Washi.Persimmon.copy(alpha = faint), -90f, restSweep, false, ringRect, ringSize, style = stroke)
            drawArc(Washi.Amber.copy(alpha = faint), -90f + restSweep, lowSweep, false, ringRect, ringSize, style = stroke)
            drawArc(Washi.Moss.copy(alpha = faint), -90f + restSweep + lowSweep, steadySweep, false, ringRect, ringSize, style = stroke)

            // level arc in the current zone's colour
            val zoneColor = when {
                animated > PacingGuide.LOW_ZONE -> Washi.Moss
                animated > PacingGuide.REST_ZONE -> Washi.Amber
                else -> Washi.Persimmon
            }
            drawArc(
                color = zoneColor.copy(alpha = 0.95f),
                startAngle = -90f,
                sweepAngle = 360f * animated,
                useCenter = false,
                topLeft = ringRect,
                size = ringSize,
                style = Stroke(width = 9f, cap = StrokeCap.Round)
            )

            // marker dot at the current level
            val angleRad = Math.toRadians((-90f + 360f * animated).toDouble())
            val mx = cx + ringR * cos(angleRad).toFloat()
            val my = cy + ringR * sin(angleRad).toFloat()
            drawCircle(color = if (night) Washi.NightInk else Washi.Card, radius = 13f, center = Offset(mx, my))
            drawCircle(color = zoneColor, radius = 9f, center = Offset(mx, my))
        }
    }
}
