package com.mazlabz.akari.widget

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.mazlabz.akari.MainActivity
import com.mazlabz.akari.PacingGuide
import com.mazlabz.akari.R
import com.mazlabz.akari.data.AkariDatabase
import com.mazlabz.akari.data.Entry
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/* Washi palette mirrored for Glance (widget composition can't use the app theme). */
private val WPaper = Color(0xFFF6F2EA)
private val WCard = Color(0xFFFDFBF6)
private val WInk = Color(0xFF3B3A36)
private val WInkFaded = Color(0xFF6E6A61)
private val WMoss = Color(0xFF7C9A77)
private val WAmber = Color(0xFFD9A441)
private val WPersimmon = Color(0xFFC97B5D)
private val WNight = Color(0xFF181714)
private val WNightInk = Color(0xFFCFC9BE)

private data class WidgetState(
    val hasCheckin: Boolean,
    val remaining: Int,
    val fraction: Float
)

private fun loadState(context: Context): WidgetState {
    return try {
        val entries = kotlinx.coroutines.runBlocking {
            AkariDatabase.get(context).entryDao().snapshot()
        }
        val today = LocalDate.now()
        val zone = ZoneId.systemDefault()
        val todays = entries.filter {
            Instant.ofEpochMilli(it.ts).atZone(zone).toLocalDate() == today
        }
        val checkin = todays.lastOrNull { it.type == "checkin" }
        val spent = todays.filter { it.type == "activity" }.sumOf { it.cost ?: 0 }
        if (checkin?.battery == null) {
            WidgetState(false, 0, 0f)
        } else {
            val remaining = (checkin.battery - spent).coerceAtLeast(0)
            WidgetState(true, remaining, (remaining / 100f).coerceIn(0f, 1f))
        }
    } catch (t: Throwable) {
        WidgetState(false, 0, 0f)
    }
}

class AkariWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val state = loadState(context)
        provideContent { WidgetContent(state) }
    }
}

class AkariWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AkariWidget()
}

/** Called by the app after every diary write so the lantern never lies. */
object WidgetUpdater {
    suspend fun refresh(context: Context) {
        try {
            AkariWidget().updateAll(context)
        } catch (t: Throwable) {
            // widget may not be placed yet — never fail the diary for it
        }
    }
}

class LogRestAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        try {
            AkariDatabase.get(context).entryDao().insert(Entry(type = "rest"))
            com.mazlabz.akari.Haptics.restPulse(context)
            AkariWidget().update(context, glanceId)
        } catch (t: Throwable) { }
    }
}

class OpenCrashAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("crash_mode", true)
        }
        context.startActivity(intent)
    }
}

class OpenAppAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}

@androidx.compose.runtime.Composable
private fun WidgetContent(state: WidgetState) {
    val zoneColor = when {
        !state.hasCheckin -> WInkFaded
        state.fraction > PacingGuide.LOW_ZONE -> WMoss
        state.fraction > PacingGuide.REST_ZONE -> WAmber
        else -> WPersimmon
    }

    // zone-coloured halo border: outer box in zone colour, inner card inset
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(zoneColor))
            .cornerRadius(20.dp)
            .padding(3.dp)
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(WPaper))
                .cornerRadius(17.dp)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth().clickable(actionRunCallback<OpenAppAction>()),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_lantern),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(ColorProvider(zoneColor)),
                    modifier = GlanceModifier.size(22.dp)
                )
                Spacer(GlanceModifier.width(8.dp))
                Column {
                    Text(
                        text = if (state.hasCheckin) "${state.remaining}% · " +
                            PacingGuide.zoneLabel(state.fraction)
                        else "No check-in yet",
                        style = TextStyle(
                            color = ColorProvider(WInk),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = if (state.hasCheckin) PacingGuide.guidance(state.fraction)
                        else "Open Akari and set today's energy",
                        style = TextStyle(color = ColorProvider(WInkFaded), fontSize = 12.sp),
                        maxLines = 2
                    )
                }
            }
            Spacer(GlanceModifier.height(8.dp))
            Row(modifier = GlanceModifier.fillMaxWidth()) {
                Box(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .background(ColorProvider(WCard))
                        .cornerRadius(12.dp)
                        .padding(vertical = 9.dp)
                        .clickable(actionRunCallback<LogRestAction>()),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            provider = ImageProvider(R.drawable.ic_leaf),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(ColorProvider(WInk)),
                            modifier = GlanceModifier.size(16.dp)
                        )
                        Spacer(GlanceModifier.width(6.dp))
                        Text(
                            "Log rest",
                            style = TextStyle(color = ColorProvider(WInk), fontSize = 14.sp)
                        )
                    }
                }
                Spacer(GlanceModifier.width(8.dp))
                Box(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .background(ColorProvider(WNight))
                        .cornerRadius(12.dp)
                        .padding(vertical = 9.dp)
                        .clickable(actionRunCallback<OpenCrashAction>()),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            provider = ImageProvider(R.drawable.ic_moon),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(ColorProvider(WNightInk)),
                            modifier = GlanceModifier.size(16.dp)
                        )
                        Spacer(GlanceModifier.width(6.dp))
                        Text(
                            "Crash mode",
                            style = TextStyle(color = ColorProvider(WNightInk), fontSize = 14.sp)
                        )
                    }
                }
            }
        }
    }
}
