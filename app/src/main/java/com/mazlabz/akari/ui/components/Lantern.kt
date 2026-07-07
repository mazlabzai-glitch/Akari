package com.mazlabz.akari.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import kotlin.random.Random

/** One fibre of procedurally generated washi paper. */
private data class Fibre(
    val u: Float,   // position within the body oval, -1..1
    val v: Float,
    val len: Float, // relative length
    val angle: Float,
    val alpha: Float,
    val bright: Boolean // occasional lighter fleck
)

private fun generateFibres(seed: Int = 41, count: Int = 110): List<Fibre> {
    val rnd = Random(seed)
    val fibres = mutableListOf<Fibre>()
    while (fibres.size < count) {
        val u = rnd.nextFloat() * 2f - 1f
        val v = rnd.nextFloat() * 2f - 1f
        if (u * u + v * v > 0.92f) continue // stay inside the oval
        fibres.add(
            Fibre(
                u = u, v = v,
                len = 0.03f + rnd.nextFloat() * 0.09f,
                angle = rnd.nextFloat() * (Math.PI.toFloat()),
                alpha = 0.025f + rnd.nextFloat() * 0.055f,
                bright = rnd.nextFloat() < 0.18f
            )
        )
    }
    return fibres
}

/**
 * The Akari lantern: the day's energy envelope as living, paper-diffused light.
 *
 *  - The glow breathes like a real flame (slow 4 s pulse, gentle by design).
 *  - Zone colour cross-fades smoothly instead of jumping when a log lands.
 *  - A procedurally generated washi fibre texture gives the paper its grain.
 *  - Ring zones: green = Steady, amber = Getting low, warm red = Rest zone,
 *    with a marker dot at the current level.
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
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "lanternLevel"
    )

    // the flame breathes — a slow, subtle pulse, never a flicker
    val flameTransition = rememberInfiniteTransition(label = "flame")
    val flame by flameTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flamePulse"
    )

    val targetZone = when {
        animated > PacingGuide.LOW_ZONE -> Washi.Moss
        animated > PacingGuide.REST_ZONE -> Washi.Amber
        else -> Washi.Persimmon
    }
    val zoneColor by animateColorAsState(
        targetValue = targetZone,
        animationSpec = tween(durationMillis = 900),
        label = "zoneColor"
    )

    val fibres = remember { generateFibres() }

    Box(modifier = modifier.size(size)) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            val cx = w / 2f
            val cy = h / 2f
            val r = min(w, h) / 2f

            val glow = (0.15f + 0.85f * animated) * flame

            // ---- outer halo: light bleeding through paper, breathing ----
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Washi.GlowWarm.copy(alpha = (0.55f * glow).coerceIn(0f, 1f)),
                        Washi.GlowWarm.copy(alpha = (0.18f * glow).coerceIn(0f, 1f)),
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
                        Washi.GlowCore.copy(alpha = (0.35f + 0.65f * glow).coerceIn(0f, 1f)),
                        Washi.GlowWarm.copy(alpha = (0.30f + 0.45f * glow).coerceIn(0f, 1f)),
                        (if (night) Washi.NightCard else Washi.Card).copy(alpha = 0.9f)
                    ),
                    center = Offset(cx, cy - bodyH * 0.08f),
                    radius = bodyH * 0.62f
                ),
                topLeft = Offset(cx - bodyW / 2f, bodyTop),
                size = Size(bodyW, bodyH)
            )

            // ---- procedural washi grain: fibres and flecks ----
            val fibreInk = if (night) Washi.NightInk else Washi.Ink
            fibres.forEach { f ->
                val fx = cx + f.u * bodyW / 2f
                val fy = cy + f.v * bodyH / 2f
                val half = f.len * r
                val dx = cos(f.angle) * half
                val dy = sin(f.angle) * half * 0.4f // fibres lie mostly horizontal, like laid paper
                drawLine(
                    color = if (f.bright)
                        Color.White.copy(alpha = f.alpha * (0.6f + glow))
                    else
                        fibreInk.copy(alpha = f.alpha),
                    start = Offset(fx - dx, fy - dy),
                    end = Offset(fx + dx, fy + dy),
                    strokeWidth = 1.4f,
                    cap = StrokeCap.Round
                )
            }

            // ---- washi ribs ----
            val ribColor = fibreInk.copy(alpha = 0.14f)
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

            // ---- inner flame core, breathing with the halo ----
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = (0.85f * glow).coerceIn(0f, 1f)),
                        Washi.GlowCore.copy(alpha = (0.55f * glow).coerceIn(0f, 1f)),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy),
                    radius = r * 0.36f * flame
                ),
                radius = r * 0.36f * flame,
                center = Offset(cx, cy)
            )

            // ---- zoned gauge ring ----
            val ringR = r * 0.97f
            val ringRect = Offset(cx - ringR, cy - ringR)
            val ringSize = Size(ringR * 2f, ringR * 2f)
            val stroke = Stroke(width = 9f, cap = StrokeCap.Round)
            val faint = if (night) 0.28f else 0.22f

            val restSweep = 360f * PacingGuide.REST_ZONE
            val lowSweep = 360f * (PacingGuide.LOW_ZONE - PacingGuide.REST_ZONE)
            val steadySweep = 360f * (1f - PacingGuide.LOW_ZONE)
            drawArc(Washi.Persimmon.copy(alpha = faint), -90f, restSweep, false, ringRect, ringSize, style = stroke)
            drawArc(Washi.Amber.copy(alpha = faint), -90f + restSweep, lowSweep, false, ringRect, ringSize, style = stroke)
            drawArc(Washi.Moss.copy(alpha = faint), -90f + restSweep + lowSweep, steadySweep, false, ringRect, ringSize, style = stroke)

            drawArc(
                color = zoneColor.copy(alpha = 0.95f),
                startAngle = -90f,
                sweepAngle = 360f * animated,
                useCenter = false,
                topLeft = ringRect,
                size = ringSize,
                style = Stroke(width = 9f, cap = StrokeCap.Round)
            )

            val angleRad = Math.toRadians((-90f + 360f * animated).toDouble())
            val mx = cx + ringR * cos(angleRad).toFloat()
            val my = cy + ringR * sin(angleRad).toFloat()
            drawCircle(color = if (night) Washi.NightInk else Washi.Card, radius = 13f, center = Offset(mx, my))
            drawCircle(color = zoneColor, radius = 9f, center = Offset(mx, my))
        }
    }
}
