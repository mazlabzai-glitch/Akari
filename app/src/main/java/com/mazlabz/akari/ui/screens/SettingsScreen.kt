package com.mazlabz.akari.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mazlabz.akari.PacingGuide
import com.mazlabz.akari.data.Settings
import com.mazlabz.akari.health.HealthConnectManager
import com.mazlabz.akari.ui.components.GentleButton
import com.mazlabz.akari.ui.components.KeyValueRow
import com.mazlabz.akari.ui.components.SectionCard
import com.mazlabz.akari.ui.components.SectionLabel
import com.mazlabz.akari.ui.theme.Washi

@Composable
fun SettingsScreen(
    settings: Settings,
    healthManager: HealthConnectManager,
    healthPermissionsGranted: Boolean,
    onSave: (Settings) -> Unit,
    onRequestHealthPermissions: () -> Unit,
    onReplayIntro: () -> Unit,
    onExportCsv: () -> Unit,
    onExportJson: () -> Unit,
    onImport: () -> Unit,
    onErase: () -> Unit
) {
    var name by remember { mutableStateOf(settings.name) }
    var rhr by remember { mutableStateOf(settings.restingHr?.toString() ?: "") }
    var confirmErase by remember { mutableStateOf(false) }

    LaunchedEffect(settings) {
        name = settings.name
        rhr = settings.restingHr?.toString() ?: ""
    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        SectionCard {
            Text("About her", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Name (for the greeting)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = rhr, onValueChange = { rhr = it },
                label = { Text("Resting heart rate (bpm)") },
                supportingText = { Text("Measure on waking before getting up, 7 mornings, average them. Ceiling = resting + 15 (Workwell Foundation)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            GentleButton("Save", Modifier.fillMaxWidth(), filled = true, onClick = {
                onSave(settings.copy(name = name.trim(), restingHr = rhr.trim().toIntOrNull()))
            })
        }

        SectionCard {
            Text("Wearable · Health Connect", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            if (!healthManager.isAvailable) {
                Text(
                    "Health Connect isn't available on this phone yet. Install \"Health Connect\" from the Play Store, connect her watch's app to it, then return here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Washi.InkFaded
                )
            } else if (!healthPermissionsGranted) {
                Text(
                    "Connect a wearable's data (heart rate, sleep, steps) so vitals flow in on their own — no manual logging.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Washi.InkFaded
                )
                Spacer(Modifier.height(12.dp))
                GentleButton("Allow reading wearable data", Modifier.fillMaxWidth(), onClick = onRequestHealthPermissions)
            } else {
                KeyValueRow("Status", "Connected")
                Text(
                    "Heart rate, resting HR, sleep and steps are read automatically each time the app opens.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Washi.InkFaded
                )
            }
        }

        SectionCard {
            Text("Backup & export", style = MaterialTheme.typography.titleLarge)
            Text(
                "Data lives only on this phone. Back up now and then.",
                style = MaterialTheme.typography.bodyMedium,
                color = Washi.InkFaded,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GentleButton("Backup (JSON)", Modifier.weight(1f), onClick = onExportJson)
                GentleButton("Doctor CSV", Modifier.weight(1f), onClick = onExportCsv)
            }
            Spacer(Modifier.height(8.dp))
            GentleButton("Restore from backup", Modifier.fillMaxWidth(), onClick = onImport)
        }


        SectionCard {
            Text("About pacing — the science", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            PacingGuide.ALL.forEach { t ->
                Text(t.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    t.body,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 2.dp, bottom = 2.dp)
                )
                Text(
                    "Source: " + t.source,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Washi.InkFaded,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            GentleButton("Replay the introduction", Modifier.fillMaxWidth(), onClick = onReplayIntro)
        }

        SectionCard {
            SectionLabel("Danger zone")
            GentleButton(
                "Erase all data",
                Modifier.fillMaxWidth(),
                accent = Washi.Persimmon,
                onClick = { confirmErase = true }
            )
        }

        Text(
            "Akari (明かり) — light.\nSymptom-contingent pacing: on a bad day, the right amount is less.\nA personal diary, not medical advice.",
            style = MaterialTheme.typography.bodyMedium,
            color = Washi.InkFaded,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        )
    }

    if (confirmErase) {
        AlertDialog(
            onDismissRequest = { confirmErase = false },
            title = { Text("Erase all diary data?") },
            text = { Text("This can't be undone. Consider a JSON backup first.") },
            confirmButton = {
                TextButton(onClick = { confirmErase = false; onErase() }) {
                    Text("Erase", color = Washi.Persimmon)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmErase = false }) { Text("Keep it") }
            },
            containerColor = Washi.Card
        )
    }
}
