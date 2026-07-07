package com.mazlabz.akari.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mazlabz.akari.Haptics
import com.mazlabz.akari.data.Entry
import com.mazlabz.akari.ui.components.Lantern
import com.mazlabz.akari.ui.theme.Washi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * The lantern turned down to embers. Four large soft buttons, one tap each,
 * silent confirmation — plus an optional guided box-breathing tool whose
 * rhythm is carried by gentle haptic ticks, so the screen can dim to black.
 */
@Composable
fun CrashScreen(
    remainingFraction: Float,
    onLog: (Entry) -> Unit,
    onExit: () -> Unit
) {
    val context = LocalContext.current
    var flash by remember { mutableStateOf<String?>(null) }
    var breathing by remember { mutableStateOf(false) }

    LaunchedEffect(flash) {
        if (flash != null) {
            delay(1600)
            flash = null
        }
    }

    if (breathing) {
        BreathingOverlay(onExit = { breathing = false })
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Washi.Night)
            .padding(horizontal = 22.dp, vertical = 26.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Lantern(level = remainingFraction * 0.3f, size = 100.dp, night = true)
        Text(
            "Rest is the practice now",
            color = Washi.NightInk,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Text(
            flash ?: " ",
            color = Washi.Moss,
            fontSize = 16.sp,
            modifier = Modifier.padding(vertical = 6.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            CrashButton("Crashing / PEM") {
                onLog(Entry(type = "pem")); Haptics.caution(context); flash = "noted — be gentle"
            }
            CrashButton("Resting now") {
                onLog(Entry(type = "rest")); Haptics.restPulse(context); flash = "rest logged"
            }
            CrashButton("Took meds") {
                onLog(Entry(type = "food", text = "Meds (crash mode)")); flash = "noted"
            }
            CrashButton("Drank water") {
                onLog(Entry(type = "food", text = "Water (crash mode)")); flash = "noted"
            }
            Button(
                onClick = { breathing = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 62.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Washi.Night,
                    contentColor = Washi.NightInk.copy(alpha = 0.85f)
                )
            ) {
                Text("Guided breathing · 4-4 box", fontSize = 17.sp)
            }
        }
        TextButton(onClick = onExit) {
            Text("bring the light back", color = Washi.NightInk.copy(alpha = 0.6f))
        }
    }
}

@Composable
private fun CrashButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 74.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Washi.NightCard,
            contentColor = Washi.NightInk
        )
    ) {
        Text(text, fontSize = 21.sp)
    }
}

/**
 * Box breathing (4 s in · 4 hold · 4 out · 4 hold) — slow, even breathing that
 * supports parasympathetic (rest-and-digest) activity. The timing loop runs on
 * plain delays and haptic ticks, deliberately independent of the display, so
 * the rhythm keeps going if the screen dims. The visual is a single dim ember
 * that swells and settles; tap anywhere to finish.
 */
@Composable
private fun BreathingOverlay(onExit: () -> Unit) {
    val context = LocalContext.current
    var phase by remember { mutableStateOf("breathe in") }
    val swell = remember { Animatable(0.45f) }

    // Haptic/timing loop — the source of truth, alive even when frames stop.
    LaunchedEffect(Unit) {
        while (true) {
            phase = "breathe in"; Haptics.tick(context)
            launch { swell.animateTo(1f, tween(4000, easing = LinearEasing)) }
            delay(4000)
            phase = "hold"; Haptics.tick(context)
            delay(4000)
            phase = "breathe out"; Haptics.tick(context)
            launch { swell.animateTo(0.45f, tween(4000, easing = LinearEasing)) }
            delay(4000)
            phase = "hold"; Haptics.tick(context)
            delay(4000)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) { detectTapGestures { onExit() } },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(220.dp)) {
            val r = size.minDimension / 2f * swell.value
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Washi.GlowWarm.copy(alpha = 0.30f),
                        Washi.Ember.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = Offset(size.width / 2f, size.height / 2f),
                    radius = r.coerceAtLeast(1f)
                ),
                radius = r.coerceAtLeast(1f),
                center = Offset(size.width / 2f, size.height / 2f)
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(phase, color = Washi.NightInk.copy(alpha = 0.5f), fontSize = 18.sp)
            Spacer(Modifier.height(6.dp))
            Text(
                "the pulses carry the rhythm — eyes can close",
                color = Washi.NightInk.copy(alpha = 0.3f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "tap anywhere to finish",
                color = Washi.NightInk.copy(alpha = 0.3f),
                fontSize = 13.sp
            )
        }
    }
}
