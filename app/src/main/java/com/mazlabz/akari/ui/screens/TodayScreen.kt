package com.mazlabz.akari.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.mazlabz.akari.GuideTopic
import com.mazlabz.akari.Haptics
import com.mazlabz.akari.LoadState
import com.mazlabz.akari.MainViewModel
import com.mazlabz.akari.PacingGuide
import com.mazlabz.akari.TodayState
import com.mazlabz.akari.data.Entry
import com.mazlabz.akari.data.Settings
import com.mazlabz.akari.health.HealthSnapshot
import com.mazlabz.akari.ui.components.GentleButton
import com.mazlabz.akari.ui.components.InfoDot
import com.mazlabz.akari.ui.components.InfoSheet
import com.mazlabz.akari.ui.components.Lantern
import com.mazlabz.akari.ui.components.SectionCard
import com.mazlabz.akari.ui.components.SectionLabel
import com.mazlabz.akari.ui.theme.Washi
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val timeFmt = DateTimeFormatter.ofPattern("h:mm a")

private fun entryIcon(type: String) = when (type) {
    "checkin" -> "🏮"
    "activity" -> "⚡"
    "rest" -> "🍃"
    "symptom" -> "🌡"
    "food" -> "🍵"
    "vitals" -> "💓"
    "pem" -> "🌙"
    else -> "•"
}

private fun describe(e: Entry): String = when (e.type) {
    "checkin" -> "Morning check-in: ${e.battery}% energy" +
        (e.sleepQ?.let { " · sleep " + listOf("", "poor", "okay", "good")[it] } ?: "")
    "activity" -> "${e.name} · used ${e.cost}% (${e.kind})"
    "rest" -> "Rest — logged as an action, because it is one"
    "symptom" -> "${e.name} · " + listOf("", "mild", "moderate", "severe")[e.sev ?: 2]
    "food" -> e.text ?: ""
    "vitals" -> listOfNotNull(
        e.hr?.let { "HR $it" }, e.bp?.let { "BP $it" }, e.spo2?.let { "SpO2 $it%" }
    ).joinToString(" · ")
    "pem" -> "Crash / PEM flagged — the app is learning from this"
    else -> e.type
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TodayScreen(
    vm: MainViewModel,
    today: TodayState,
    settings: Settings,
    health: HealthSnapshot?,
    load: LoadState,
    onEnterCrashMode: () -> Unit
) {
    var sheet by remember { mutableStateOf<String?>(null) }
    var info by remember { mutableStateOf<GuideTopic?>(null) }
    val hapticContext = LocalContext.current

    // Cautionary double-pulse on crossing into the Rest zone
    var lastZone by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(today.fraction, today.checkin?.id) {
        if (today.checkin != null) {
            val z = PacingGuide.zoneLabel(today.fraction)
            if (lastZone != null && z == "Rest zone" && lastZone != "Rest zone") {
                Haptics.caution(hapticContext)
            }
            lastZone = z
        }
    }

    // Cautionary double-pulse on breaching the heart-rate pacing ceiling
    var hrWasOver by remember { mutableStateOf(false) }
    LaunchedEffect(health?.latestHr, settings.pacingCeiling) {
        val ceiling = settings.pacingCeiling
        val hr = health?.latestHr
        val over = ceiling != null && hr != null && hr > ceiling
        if (over && !hrWasOver) Haptics.caution(hapticContext)
        hrWasOver = over
    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        if (today.checkin == null) {
            CheckinCard(
                onInfo = { info = PacingGuide.ENVELOPE },
                onSave = { battery, sleepQ ->
                    vm.add(Entry(type = "checkin", battery = battery, sleepQ = sleepQ))
                }
            )
        } else {
            SectionCard {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Lantern(level = today.fraction)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${today.remaining}%", style = MaterialTheme.typography.headlineMedium)
                        InfoDot { info = PacingGuide.ZONES }
                    }
                    val zone = PacingGuide.zoneLabel(today.fraction)
                    Text(
                        zone,
                        style = MaterialTheme.typography.titleMedium,
                        color = when (zone) {
                            "Steady" -> Washi.Moss
                            "Getting low" -> Washi.Amber
                            else -> Washi.Persimmon
                        }
                    )
                    Spacer(Modifier.height(4.dp))
                    // Guidance strip: always answers "what should I do now?"
                    Text(
                        PacingGuide.guidance(today.fraction),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Started at ${today.checkin.battery}% · used ${today.spent}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Washi.InkFaded
                    )
                    settings.pacingCeiling?.let { ceiling ->
                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider(color = Washi.Line)
                        Spacer(Modifier.height(8.dp))
                        val hrNow = health?.latestHr
                        val over = hrNow != null && hrNow > ceiling
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                if (hrNow != null)
                                    "Heart rate $hrNow · ceiling $ceiling bpm" +
                                        (if (over) " — above the threshold, stop and rest" else "")
                                else
                                    "Heart-rate ceiling: $ceiling bpm — above it, stop and rest",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (over) Washi.Persimmon else Washi.InkFaded,
                                modifier = Modifier.weight(1f)
                            )
                            InfoDot { info = PacingGuide.HEART_RATE }
                        }
                    }
                }
            }
        }

        if (load.spiking) {
            SectionCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🌫", modifier = Modifier.width(32.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Delayed-load caution",
                            style = MaterialTheme.typography.titleMedium,
                            color = Washi.Persimmon
                        )
                        Text(
                            "The last 3 days carried more than usual (${load.total}% spent). " +
                                "PEM arrives late — treat today as gentler than it feels.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Washi.InkFaded
                        )
                    }
                    InfoDot { info = PacingGuide.ROLLING_LOAD }
                }
            }
        }

        health?.let { h ->
            if (h.error == null && (h.steps != null || h.sleepMinutes != null || h.restingHr != null)) {
                SectionCard {
                    SectionLabel("From her wearable")
                    val parts = listOfNotNull(
                        h.steps?.let { "$it steps" },
                        h.sleepMinutes?.let { "${it / 60}h ${it % 60}m sleep" },
                        h.restingHr?.let { "resting HR $it" }
                    )
                    Text(parts.joinToString(" · "), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        SectionCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Log an activity", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "each one spends some of today's light",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Washi.InkFaded
                    )
                }
                InfoDot { info = PacingGuide.EFFORT_TYPES }
            }
            Spacer(Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MainViewModel.PRESETS.forEach { p ->
                    GentleButton(
                        text = "${p.name}  −${p.cost}%",
                        onClick = {
                            vm.add(Entry(type = "activity", name = p.name, cost = p.cost, kind = p.kind))
                        }
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            GentleButton(
                text = "Something else…",
                modifier = Modifier.fillMaxWidth(),
                onClick = { sheet = "activity" }
            )
        }

        SectionCard {
            SectionLabel("Quick log")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GentleButton(
                    "Rest", subText = "rest is an action",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        vm.add(Entry(type = "rest"))
                        Haptics.restPulse(hapticContext)
                    }
                )
                GentleButton(
                    "Symptom", subText = "how it feels",
                    modifier = Modifier.weight(1f),
                    onClick = { sheet = "symptom" }
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GentleButton(
                    "Food / meds", subText = "what you had",
                    modifier = Modifier.weight(1f),
                    onClick = { sheet = "food" }
                )
                GentleButton(
                    "Vitals", subText = "HR · BP · SpO2",
                    modifier = Modifier.weight(1f),
                    onClick = { sheet = "vitals" }
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                GentleButton(
                    "I'm crashing (PEM)",
                    subText = "one tap now helps find your triggers",
                    modifier = Modifier.weight(1f),
                    accent = Washi.Persimmon,
                    onClick = { vm.add(Entry(type = "pem")) }
                )
                InfoDot { info = PacingGuide.PEM }
            }
        }

        SectionCard {
            SectionLabel("Today's diary")
            if (today.entries.isEmpty()) {
                Text(
                    "Nothing logged yet. A quiet page is fine.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Washi.InkFaded
                )
            } else {
                today.entries.reversed().forEach { e ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(entryIcon(e.type), modifier = Modifier.width(32.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(describe(e), style = MaterialTheme.typography.bodyMedium)
                            Text(
                                Instant.ofEpochMilli(e.ts).atZone(ZoneId.systemDefault()).format(timeFmt),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Washi.InkFaded
                            )
                        }
                        TextButton(onClick = { vm.delete(e.id) }) {
                            Text("✕", color = Washi.InkFaded)
                        }
                    }
                    HorizontalDivider(color = Washi.Line)
                }
            }
        }

        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
            GentleButton(
                "Crash mode — big buttons, low light",
                modifier = Modifier.fillMaxWidth(),
                accent = Washi.Night,
                filled = true,
                onClick = onEnterCrashMode
            )
        }
        Text(
            "Everything stays on this phone. A diary, not medical advice.",
            style = MaterialTheme.typography.bodyMedium,
            color = Washi.InkFaded,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            textAlign = TextAlign.Center
        )
    }

    info?.let { InfoSheet(it) { info = null } }

    when (sheet) {
        "activity" -> ActivitySheet(onDismiss = { sheet = null }) { name, cost, kind ->
            vm.add(Entry(type = "activity", name = name, cost = cost, kind = kind)); sheet = null
        }
        "symptom" -> SymptomSheet(onDismiss = { sheet = null }) { name, sev ->
            vm.add(Entry(type = "symptom", name = name, sev = sev)); sheet = null
        }
        "food" -> FoodSheet(onDismiss = { sheet = null }) { text ->
            vm.add(Entry(type = "food", text = text)); sheet = null
        }
        "vitals" -> VitalsSheet(onDismiss = { sheet = null }) { hr, bp, spo2 ->
            vm.add(Entry(type = "vitals", hr = hr, bp = bp, spo2 = spo2)); sheet = null
        }
    }
}

@Composable
private fun CheckinCard(onInfo: () -> Unit, onSave: (Int, Int?) -> Unit) {
    var battery by remember { mutableIntStateOf(55) }
    var sleepQ by remember { mutableStateOf<Int?>(null) }

    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Morning check-in", style = MaterialTheme.typography.titleLarge)
                Text(
                    "How much energy does today hold? Go by how you feel — not by what's planned.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Washi.InkFaded,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            InfoDot(onClick = onInfo)
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GentleButton("Low", subText = "rough day", modifier = Modifier.weight(1f),
                accent = Washi.Persimmon, filled = battery == 30, onClick = { battery = 30 })
            GentleButton("So-so", subText = "middling", modifier = Modifier.weight(1f),
                accent = Washi.Amber, filled = battery == 55, onClick = { battery = 55 })
            GentleButton("Okay", subText = "gentler day", modifier = Modifier.weight(1f),
                accent = Washi.Moss, filled = battery == 80, onClick = { battery = 80 })
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Slider(
                value = battery.toFloat(),
                onValueChange = { battery = (it / 5).toInt() * 5 },
                valueRange = 5f..100f,
                modifier = Modifier.weight(1f)
            )
            Text(
                "$battery%",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.width(72.dp),
                textAlign = TextAlign.End
            )
        }
        SectionLabel("Sleep last night")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GentleButton("Poor", modifier = Modifier.weight(1f), filled = sleepQ == 1, onClick = { sleepQ = 1 })
            GentleButton("Okay", modifier = Modifier.weight(1f), filled = sleepQ == 2, onClick = { sleepQ = 2 })
            GentleButton("Good", modifier = Modifier.weight(1f), filled = sleepQ == 3, onClick = { sleepQ = 3 })
        }
        Spacer(Modifier.height(14.dp))
        GentleButton(
            "Set today's energy",
            modifier = Modifier.fillMaxWidth(),
            filled = true,
            onClick = { onSave(battery, sleepQ) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActivitySheet(onDismiss: () -> Unit, onSave: (String, Int, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var cost by remember { mutableIntStateOf(15) }
    var kind by remember { mutableStateOf("mixed") }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Washi.Card) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            Text("Log an activity", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("What was it?") },
                modifier = Modifier.fillMaxWidth()
            )
            SectionLabel("Mostly what kind of effort?")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GentleButton("Body", Modifier.weight(1f), filled = kind == "physical", onClick = { kind = "physical" })
                GentleButton("Brain", Modifier.weight(1f), filled = kind == "cognitive", onClick = { kind = "cognitive" })
                GentleButton("Heart", Modifier.weight(1f), filled = kind == "emotional", onClick = { kind = "emotional" })
            }
            SectionLabel("How much of today's energy did it take?")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Slider(
                    value = cost.toFloat(),
                    onValueChange = { cost = (it / 5).toInt() * 5 },
                    valueRange = 5f..60f,
                    modifier = Modifier.weight(1f)
                )
                Text("−$cost%", style = MaterialTheme.typography.titleLarge, modifier = Modifier.width(80.dp))
            }
            Spacer(Modifier.height(10.dp))
            GentleButton("Log it", Modifier.fillMaxWidth(), filled = true, onClick = {
                onSave(name.ifBlank { "Activity" }, cost, kind)
            })
            Spacer(Modifier.height(28.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun SymptomSheet(onDismiss: () -> Unit, onSave: (String, Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    var sev by remember { mutableIntStateOf(2) }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Washi.Card) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            Text("Log a symptom", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MainViewModel.SYMPTOMS.forEach { s ->
                    GentleButton(s, filled = name == s, onClick = { name = s })
                }
            }
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Or type another") },
                modifier = Modifier.fillMaxWidth()
            )
            SectionLabel("How strong?")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GentleButton("Mild", Modifier.weight(1f), filled = sev == 1, onClick = { sev = 1 })
                GentleButton("Moderate", Modifier.weight(1f), filled = sev == 2, onClick = { sev = 2 })
                GentleButton("Severe", Modifier.weight(1f), filled = sev == 3, onClick = { sev = 3 })
            }
            Spacer(Modifier.height(10.dp))
            GentleButton("Log it", Modifier.fillMaxWidth(), filled = true, onClick = {
                if (name.isNotBlank()) onSave(name.trim(), sev)
            })
            Spacer(Modifier.height(28.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FoodSheet(onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Washi.Card) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            Text("Food · meds · drink", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = text, onValueChange = { text = it },
                label = { Text("What did you have?") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
            Spacer(Modifier.height(12.dp))
            GentleButton("Log it", Modifier.fillMaxWidth(), filled = true, onClick = {
                if (text.isNotBlank()) onSave(text.trim())
            })
            Spacer(Modifier.height(28.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VitalsSheet(onDismiss: () -> Unit, onSave: (Int?, String?, Int?) -> Unit) {
    var hr by remember { mutableStateOf("") }
    var bp by remember { mutableStateOf("") }
    var spo2 by remember { mutableStateOf("") }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Washi.Card) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            Text("Vitals", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(hr, { hr = it }, label = { Text("Heart rate (bpm)") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(bp, { bp = it }, label = { Text("Blood pressure (e.g. 118/76)") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(spo2, { spo2 = it }, label = { Text("SpO2 (%)") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            GentleButton("Log it", Modifier.fillMaxWidth(), filled = true, onClick = {
                val h = hr.trim().toIntOrNull()
                val s = spo2.trim().toIntOrNull()
                val b = bp.trim().ifBlank { null }
                if (h != null || s != null || b != null) onSave(h, b, s)
            })
            Spacer(Modifier.height(28.dp))
        }
    }
}
