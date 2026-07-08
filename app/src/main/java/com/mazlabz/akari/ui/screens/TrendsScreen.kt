package com.mazlabz.akari.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextAlign
import com.mazlabz.akari.DaySummary
import com.mazlabz.akari.LoadState
import com.mazlabz.akari.ui.components.CardTitle
import com.mazlabz.akari.ui.components.SectionCard
import com.mazlabz.akari.ui.components.SectionLabel
import com.mazlabz.akari.ui.theme.Washi

@Composable
fun TrendsScreen(
    trend: List<DaySummary>,
    load: LoadState,
    sleep: Pair<Int, Int>?,
    triggers: List<Pair<String, Int>>,
    symptomFreq: List<Pair<String, Int>>
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        SectionCard {
            Text("The last 14 days", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
            TrendChart(trend)
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                LegendDot(Washi.Moss); Text("  energy set   ", style = MaterialTheme.typography.bodyMedium, color = Washi.InkFaded)
                LegendDot(Washi.Persimmon.copy(alpha = 0.7f)); Text("  energy used   ", style = MaterialTheme.typography.bodyMedium, color = Washi.InkFaded)
                LegendDot(Washi.Night); Text("  crash day", style = MaterialTheme.typography.bodyMedium, color = Washi.InkFaded)
            }
        }


        sleep?.let { (poor, decent) ->
            SectionCard {
                Text("Sleep and the envelope", style = MaterialTheme.typography.titleLarge)
                Text(
                    "After poor sleep her mornings start around $poor% — after okay or good " +
                        "sleep, around $decent%. Unrefreshing sleep is part of ME/CFS itself, " +
                        "but on poor-sleep mornings it's worth setting the lantern lower than " +
                        "instinct suggests.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Washi.InkFaded,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        SectionCard {
            Text("3-day load · body, brain, heart", style = MaterialTheme.typography.titleLarge)
            Text(
                "Cumulative energy spent over the last 72 hours, split across the three " +
                    "kinds of effort. PEM is delayed, so this window — not just today — " +
                    "is where crash risk builds.",
                style = MaterialTheme.typography.bodyMedium,
                color = Washi.InkFaded,
                modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
            )
            val maxLoad = maxOf(60, load.physical, load.cognitive, load.emotional)
            LoadBar("Body", load.physical, maxLoad, Washi.Moss)
            LoadBar("Brain", load.cognitive, maxLoad, Washi.Amber)
            LoadBar("Heart", load.emotional, maxLoad, Washi.Persimmon)
            if (load.baseline > 0f) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "Total ${load.total}% · your usual 3-day load is about ${load.baseline.toInt()}%" +
                        if (load.spiking) " — running high, plan extra rest" else "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (load.spiking) Washi.PersimmonText else Washi.InkFaded
                )
            }
        }

        SectionCard {
            Text("What came before the crashes", style = MaterialTheme.typography.titleLarge)
            Text(
                "Activities in the 48 hours before each PEM flag. PEM is delayed — patterns show up here before they can be felt.",
                style = MaterialTheme.typography.bodyMedium,
                color = Washi.InkFaded,
                modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
            )
            if (triggers.isEmpty()) {
                Text(
                    "No crashes flagged yet. When one comes, a single tap on \"I'm crashing\" starts building this picture.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Washi.InkFaded
                )
            } else {
                triggers.forEach { (name, count) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        Text(
                            "before $count crash" + if (count > 1) "es" else "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Washi.InkFaded
                        )
                    }
                    HorizontalDivider(color = Washi.Line)
                }
            }
        }

        SectionCard {
            CardTitle("Most frequent symptoms")
            Text(
                "Last 30 days",
                style = MaterialTheme.typography.bodyMedium,
                color = Washi.InkFaded,
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
            )
            if (symptomFreq.isEmpty()) {
                Text("No symptoms logged yet.", style = MaterialTheme.typography.bodyMedium, color = Washi.InkFaded)
            } else {
                symptomFreq.forEach { (name, count) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Text(name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        Text("$count×", style = MaterialTheme.typography.bodyMedium, color = Washi.InkFaded)
                    }
                }
            }
        }
    }
}


@Composable
private fun LoadBar(label: String, value: Int, max: Int, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(56.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .background(Washi.Line, RoundedCornerShape(5.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((value.toFloat() / max).coerceIn(0f, 1f))
                    .height(10.dp)
                    .background(color, RoundedCornerShape(5.dp))
            )
        }
        Text(
            " $value%",
            style = MaterialTheme.typography.bodyMedium,
            color = Washi.InkFaded,
            modifier = Modifier.width(56.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun LegendDot(color: androidx.compose.ui.graphics.Color) {
    Canvas(modifier = Modifier.width(10.dp).height(10.dp)) {
        drawCircle(color = color)
    }
}

@Composable
private fun TrendChart(trend: List<DaySummary>) {
    val gridColor = Washi.Line
    val moss = Washi.Moss
    val used = Washi.Persimmon.copy(alpha = 0.7f)
    val crash = Washi.Night
    val fadedArgb = Washi.InkFaded.toArgb()
    val inkArgb = Washi.Ink.toArgb()
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(172.dp)
    ) {
        if (trend.isEmpty()) return@Canvas
        val w = size.width
        val h = size.height
        val labelH = 16f
        val bottom = h - labelH - 4f
        val usable = bottom - 6f
        val slot = w / trend.size
        val todayIdx = trend.lastIndex

        // soft band marking today
        drawRect(
            color = gridColor,
            topLeft = Offset(todayIdx * slot, 0f),
            size = Size(slot, bottom + labelH)
        )

        // faint baseline / 50% / 100% gridlines
        listOf(0f, 0.5f, 1f).forEach { frac ->
            val y = bottom - usable * frac
            drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
        }

        val labelPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = 10.sp.toPx()
            color = fadedArgb
        }
        val todayPaint = android.graphics.Paint(labelPaint).apply {
            color = inkArgb
            isFakeBoldText = true
        }

        trend.forEachIndexed { i, d ->
            val x = i * slot
            d.battery?.let { b ->
                val bh = usable * (b / 100f)
                drawRoundRect(
                    color = moss,
                    topLeft = Offset(x + slot * 0.14f, bottom - bh),
                    size = Size(slot * 0.30f, bh),
                    cornerRadius = CornerRadius(6f, 6f)
                )
            }
            if (d.spent > 0) {
                val sh = usable * (d.spent.coerceAtMost(100) / 100f)
                drawRoundRect(
                    color = used,
                    topLeft = Offset(x + slot * 0.52f, bottom - sh),
                    size = Size(slot * 0.30f, sh),
                    cornerRadius = CornerRadius(6f, 6f)
                )
            }
            if (d.pem) {
                drawCircle(
                    color = crash,
                    radius = 4f,
                    center = Offset(x + slot / 2f, bottom - usable - 1f)
                )
            }
            val letter = d.date.dayOfWeek.getDisplayName(
                java.time.format.TextStyle.NARROW, java.util.Locale.getDefault()
            )
            drawContext.canvas.nativeCanvas.drawText(
                letter,
                x + slot / 2f,
                h - 4f,
                if (i == todayIdx) todayPaint else labelPaint
            )
        }
    }
}
