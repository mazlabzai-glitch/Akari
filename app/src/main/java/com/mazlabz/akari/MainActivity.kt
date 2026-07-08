package com.mazlabz.akari

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.lifecycleScope
import com.mazlabz.akari.data.Entry
import com.mazlabz.akari.export.Exporter
import com.mazlabz.akari.ui.components.AkariIcons
import com.mazlabz.akari.ui.screens.CrashScreen
import com.mazlabz.akari.ui.screens.Onboarding
import com.mazlabz.akari.ui.screens.SettingsScreen
import com.mazlabz.akari.ui.screens.TodayScreen
import com.mazlabz.akari.ui.screens.TrendsScreen
import com.mazlabz.akari.ui.theme.AkariTheme
import com.mazlabz.akari.ui.theme.Washi
import kotlinx.coroutines.launch
import java.time.LocalDate

class MainActivity : ComponentActivity() {

    private val vm: MainViewModel by viewModels()

    private var pendingExport: String? = null

    private val createCsv = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri -> uri?.let { writePending(it) } }

    private val createJson = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let { writePending(it) } }

    private val openJson = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri ?: return@registerForActivityResult
        lifecycleScope.launch {
            try {
                val text = contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    ?: return@launch
                Exporter.fromJson(text).forEach { vm.add(it) }
            } catch (_: Throwable) { /* ignore malformed files */ }
        }
    }

    private val requestHealthPermissions = registerForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) { granted ->
        healthGranted = granted.containsAll(vm.healthManager.permissions)
        if (healthGranted) vm.refreshHealth()
    }

    private var healthGranted = false

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.getBooleanExtra("crash_mode", false)) vm.crashMode.value = true
    }

    private fun writePending(uri: android.net.Uri) {
        val data = pendingExport ?: return
        try {
            contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(data) }
        } catch (_: Throwable) { }
        pendingExport = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent?.getBooleanExtra("crash_mode", false) == true) {
            vm.crashMode.value = true
        }

        lifecycleScope.launch {
            healthGranted = vm.healthManager.hasAllPermissions()
            if (healthGranted) vm.refreshHealth()
        }

        setContent {
            AkariTheme {
                AkariApp(
                    vm = vm,
                    onExportCsv = {
                        lifecycleScope.launch {
                            pendingExport = Exporter.toCsv(vm.exportSnapshot())
                            createCsv.launch("akari-diary-${LocalDate.now()}.csv")
                        }
                    },
                    onExportJson = {
                        lifecycleScope.launch {
                            pendingExport = Exporter.toJson(vm.exportSnapshot())
                            createJson.launch("akari-backup-${LocalDate.now()}.json")
                        }
                    },
                    onImport = { openJson.launch(arrayOf("application/json")) },
                    onRequestHealthPermissions = {
                        requestHealthPermissions.launch(vm.healthManager.permissions)
                    },
                    healthGranted = { healthGranted }
                )
            }
        }
    }
}

@Composable
fun AkariApp(
    vm: MainViewModel,
    onExportCsv: () -> Unit,
    onExportJson: () -> Unit,
    onImport: () -> Unit,
    onRequestHealthPermissions: () -> Unit,
    healthGranted: () -> Boolean
) {
    val entries by vm.entries.collectAsState()
    val settings by vm.settings.collectAsState()
    val health by vm.healthSnapshot.collectAsState()
    val crashMode by vm.crashMode.collectAsState()

    var tab by remember { mutableIntStateOf(0) }

    SideEffect { Washi.night = settings.lowLight }

    val today = vm.todayState(entries)
    val load = vm.rollingLoad(entries)

    if (!settings.onboarded) {
        Onboarding(onDone = { vm.saveSettings(settings.copy(onboarded = true)) })
        return
    }

    if (crashMode) {
        CrashScreen(
            remainingFraction = today.fraction,
            onLog = { vm.add(it) },
            onExit = { vm.crashMode.value = false }
        )
        return
    }

    Scaffold(
        containerColor = Washi.Paper,
        bottomBar = {
            NavigationBar(containerColor = Washi.Card) {
                val items = listOf(
                    Triple("Today", AkariIcons.Today, 0),
                    Triple("Trends", AkariIcons.Trends, 1),
                    Triple("Settings", AkariIcons.Settings, 2)
                )
                items.forEach { (label, icon, i) ->
                    NavigationBarItem(
                        selected = tab == i,
                        onClick = { tab = i },
                        icon = {
                            Icon(
                                icon,
                                contentDescription = label,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = { Text(label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Washi.Ink,
                            unselectedIconColor = Washi.InkFaded,
                            selectedTextColor = Washi.Ink,
                            unselectedTextColor = Washi.InkFaded,
                            indicatorColor = Washi.Paper
                        )
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Header(settings.name)
            when (tab) {
                0 -> TodayScreen(
                    vm = vm,
                    today = today,
                    settings = settings,
                    health = health,
                    load = load,
                    onEnterCrashMode = { vm.crashMode.value = true }
                )
                1 -> TrendsScreen(
                    trend = vm.trend(entries),
                    load = load,
                    sleep = vm.sleepInsight(entries),
                    triggers = vm.pemTriggers(entries),
                    symptomFreq = vm.symptomFrequency(entries)
                )
                2 -> SettingsScreen(
                    settings = settings,
                    healthManager = vm.healthManager,
                    healthPermissionsGranted = healthGranted(),
                    onSave = { vm.saveSettings(it) },
                    onRequestHealthPermissions = onRequestHealthPermissions,
                    onReplayIntro = { vm.saveSettings(settings.copy(onboarded = false)) },
                    onExportCsv = onExportCsv,
                    onExportJson = onExportJson,
                    onImport = onImport,
                    onErase = { vm.eraseAll() }
                )
            }
        }
    }
}

@Composable
private fun Header(name: String) {
    val date = java.time.LocalDate.now()
        .format(java.time.format.DateTimeFormatter.ofPattern("EEEE d MMMM"))
    Column(modifier = Modifier.padding(start = 20.dp, top = 14.dp, end = 20.dp, bottom = 4.dp)) {
        Text(
            if (name.isBlank()) "Akari" else "Akari · $name",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(date, style = MaterialTheme.typography.bodyMedium, color = Washi.InkFaded)
    }
}
