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
import com.mazlabz.akari.ui.theme.Washi
import kotlin.math.min

/**
 * The Akari lantern: her energy for the day, drawn as paper-diffused light.
 * Full battery = a warm, full glow; as energy is spent the lantern dims
 * toward embers. `level` is 0f..1f (remaining / 100).
 */
@Composable
fun Lantern(
    level: Float,
    modifier: Modifier = Modifier,
    size: Dp = 220.dp,
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

            val glow = 0.15f + 0.85f * animated  // never fully dark: embers remain

            // Outer halo — light bleeding through paper
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

            // Lantern body — soft vertical oval
            val bodyW = r * 1.15f
            val bodyH = r * 1.45f
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

            // Washi ribs — the horizontal bamboo lines of an Akari lamp
            val ribColor = (if (night) Washi.NightInk else Washi.Ink).copy(alpha = 0.14f)
            val ribs = 7
            for (i in 1 until ribs) {
                val t = i.toFloat() / ribs
                val y = bodyTop + bodyH * t
                // width of the oval at this height (ellipse equation)
                val dy = (y - cy) / (bodyH / 2f)
                val halfW = bodyW / 2f * kotlin.math.sqrt((1f - dy * dy).coerceAtLeast(0f))
                drawLine(
                    color = ribColor,
                    start = Offset(cx - halfW, y),
                    end = Offset(cx + halfW, y),
                    strokeWidth = 2f,
                    cap = StrokeCap.Round
                )
            }

            // Inner flame core
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.85f * glow),
                        Washi.GlowCore.copy(alpha = 0.55f * glow),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy),
                    radius = r * 0.38f
                ),
                radius = r * 0.38f,
                center = Offset(cx, cy)
            )

            // Level ring — a thin arc tracing remaining energy
            val ringColor = when {
                animated > 0.4f -> Washi.Moss
                animated > 0.15f -> Washi.Amber
                else -> Washi.Persimmon
            }
            drawArc(
                color = ringColor.copy(alpha = 0.9f),
                startAngle = -90f,
                sweepAngle = 360f * animated,
                useCenter = false,
                topLeft = Offset(cx - r * 0.98f, cy - r * 0.98f),
                size = Size(r * 1.96f, r * 1.96f),
                style = Stroke(width = 7f, cap = StrokeCap.Round)
            )
        }
    }
}
