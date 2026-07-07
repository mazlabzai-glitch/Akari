package com.mazlabz.akari.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mazlabz.akari.PacingGuide
import com.mazlabz.akari.ui.components.GentleButton
import com.mazlabz.akari.ui.components.Lantern
import com.mazlabz.akari.ui.theme.Washi

private data class OnboardPage(
    val lanternLevel: Float,
    val title: String,
    val body: String,
    val source: String
)

private val PAGES = listOf(
    OnboardPage(
        lanternLevel = 0.85f,
        title = "The lantern is your energy for today",
        body = "Each morning you set how much light the day starts with — going by how " +
            "you feel, not by what's planned. That's the heart of pacing for ME/CFS: " +
            "staying inside your \"energy envelope\" prevents crashes, and on a bad " +
            "day the right amount is simply less. Nothing here ever asks you to do " +
            "more; exercise quotas were removed from ME/CFS care by the 2021 NICE " +
            "guideline.",
        source = "Envelope theory (Jason et al.) · NICE NG206 (2021)"
    ),
    OnboardPage(
        lanternLevel = 0.45f,
        title = "Activities spend light — all three kinds",
        body = "Logging an activity dims the lantern by a rough cost. Physical effort " +
            "(a shower), cognitive effort (screens, decisions) and emotional effort " +
            "(a hard phone call) all draw on the same reserve. The ring shows three " +
            "zones: green — steady; amber — getting low, one small thing at most; " +
            "warm red — rest zone, stop and rest now, before you feel forced to.",
        source = "Bateman Horne Center · Emerge Australia"
    ),
    OnboardPage(
        lanternLevel = 0.12f,
        title = "Crashes teach the app",
        body = "Post-exertional malaise arrives late — typically 12–48 hours after the " +
            "exertion that caused it — so cause and effect can't be felt in the " +
            "moment. When a crash comes, one tap on \"I'm crashing\" is enough: Akari " +
            "looks back 48 hours and gradually reveals which activities most often " +
            "precede your crashes. On crash days, Crash mode gives you four big " +
            "buttons and a dark, quiet screen.",
        source = "NICE NG206 (2021) · ME Association"
    )
)

/** Three gentle cards shown on first launch (replayable from Settings). */
@Composable
fun Onboarding(onDone: () -> Unit) {
    var page by remember { mutableIntStateOf(0) }
    val p = PAGES[page]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Washi.Paper)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 26.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))
        Lantern(level = p.lanternLevel, size = 190.dp)
        Spacer(Modifier.height(6.dp))
        Text(
            p.title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(p.body, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(10.dp))
        Text(
            p.source,
            style = MaterialTheme.typography.bodyMedium,
            color = Washi.InkFaded,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(22.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PAGES.indices.forEach { i ->
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .size(if (i == page) 11.dp else 8.dp)
                        .background(
                            if (i == page) Washi.Ink else Washi.Line,
                            shape = CircleShape
                        )
                )
            }
        }
        Spacer(Modifier.height(18.dp))
        GentleButton(
            text = if (page < PAGES.lastIndex) "Next" else "Begin",
            modifier = Modifier.fillMaxWidth(),
            filled = true,
            onClick = { if (page < PAGES.lastIndex) page++ else onDone() }
        )
        TextButton(onClick = onDone) {
            Text("skip for now", color = Washi.InkFaded)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "A personal diary, not medical advice.",
            style = MaterialTheme.typography.bodyMedium,
            color = Washi.InkFaded
        )
    }
}
