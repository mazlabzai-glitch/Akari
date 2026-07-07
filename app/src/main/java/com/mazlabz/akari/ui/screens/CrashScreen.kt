package com.mazlabz.akari.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mazlabz.akari.data.Entry
import com.mazlabz.akari.ui.components.Lantern
import com.mazlabz.akari.ui.theme.Washi
import kotlinx.coroutines.delay

/**
 * The lantern turned down to embers. Four large soft buttons, one tap each,
 * silent confirmation. No navigation, no numbers, no demands.
 */
@Composable
fun CrashScreen(
    remainingFraction: Float,
    onLog: (Entry) -> Unit,
    onExit: () -> Unit
) {
    var flash by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(flash) {
        if (flash != null) {
            delay(1600)
            flash = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Washi.Night)
            .padding(horizontal = 22.dp, vertical = 26.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Lantern(level = remainingFraction * 0.3f, size = 110.dp, night = true)
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
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Column(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CrashButton("Crashing / PEM") { onLog(Entry(type = "pem")); flash = "noted — be gentle" }
            CrashButton("Resting now") { onLog(Entry(type = "rest")); flash = "rest logged" }
            CrashButton("Took meds") { onLog(Entry(type = "food", text = "Meds (crash mode)")); flash = "noted" }
            CrashButton("Drank water") { onLog(Entry(type = "food", text = "Water (crash mode)")); flash = "noted" }
        }
        Spacer(Modifier.height(10.dp))
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
            .heightIn(min = 78.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Washi.NightCard,
            contentColor = Washi.NightInk
        )
    ) {
        Text(text, fontSize = 21.sp)
    }
}
