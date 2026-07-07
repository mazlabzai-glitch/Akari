package com.mazlabz.akari

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mazlabz.akari.data.AkariDatabase
import com.mazlabz.akari.data.Entry
import com.mazlabz.akari.data.Settings
import com.mazlabz.akari.data.SettingsStore
import com.mazlabz.akari.health.HealthConnectManager
import com.mazlabz.akari.health.HealthSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.Instant

data class Preset(val name: String, val cost: Int, val kind: String)

data class DaySummary(
    val date: LocalDate,
    val battery: Int?,
    val spent: Int,
    val pem: Boolean
)

data class TodayState(
    val checkin: Entry? = null,
    val spent: Int = 0,
    val entries: List<Entry> = emptyList()
) {
    val remaining: Int get() = ((checkin?.battery ?: 0) - spent).coerceAtLeast(0)
    val fraction: Float get() {
        val b = checkin?.battery ?: return 0f
        if (b <= 0) return 0f
        return (remaining.toFloat() / 100f).coerceIn(0f, 1f)
    }
}

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = AkariDatabase.get(app).entryDao()
    private val settingsStore = SettingsStore(app)
    val healthManager = HealthConnectManager(app)

    val settings: StateFlow<Settings> = settingsStore.settings

    val entries: StateFlow<List<Entry>> = dao.all()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val healthSnapshot = MutableStateFlow<HealthSnapshot?>(null)
    val crashMode = MutableStateFlow(false)

    companion object {
        val PRESETS = listOf(
            Preset("Shower", 15, "physical"),
            Preset("Cooking", 15, "physical"),
            Preset("Light meal prep", 5, "physical"),
            Preset("Short walk", 20, "physical"),
            Preset("Chores", 15, "physical"),
            Preset("Screen time", 10, "cognitive"),
            Preset("Reading", 5, "cognitive"),
            Preset("Phone call", 10, "emotional"),
            Preset("Socialising", 20, "emotional"),
            Preset("Appointment", 30, "mixed"),
            Preset("Errands", 25, "physical"),
            Preset("Driving", 15, "cognitive")
        )
        val SYMPTOMS = listOf(
            "Fatigue", "Brain fog", "Headache", "Muscle pain", "Sore throat",
            "Dizziness", "Nausea", "Unrefreshing sleep", "Sound/light sensitivity", "Racing heart"
        )
    }

    private fun dateOf(ts: Long): LocalDate =
        Instant.ofEpochMilli(ts).atZone(ZoneId.systemDefault()).toLocalDate()

    fun todayState(all: List<Entry>): TodayState {
        val today = LocalDate.now()
        val todays = all.filter { dateOf(it.ts) == today }
        return TodayState(
            checkin = todays.lastOrNull { it.type == "checkin" },
            spent = todays.filter { it.type == "activity" }.sumOf { it.cost ?: 0 },
            entries = todays
        )
    }

    fun trend(all: List<Entry>, days: Int = 14): List<DaySummary> {
        val today = LocalDate.now()
        return (days - 1 downTo 0).map { back ->
            val d = today.minusDays(back.toLong())
            val es = all.filter { dateOf(it.ts) == d }
            DaySummary(
                date = d,
                battery = es.lastOrNull { it.type == "checkin" }?.battery,
                spent = es.filter { it.type == "activity" }.sumOf { it.cost ?: 0 },
                pem = es.any { it.type == "pem" }
            )
        }
    }

    /** Activities within 48h before each PEM flag, most frequent first. */
    fun pemTriggers(all: List<Entry>): List<Pair<String, Int>> {
        val window = 48L * 3600 * 1000
        val counts = mutableMapOf<String, Int>()
        all.filter { it.type == "pem" }.forEach { pem ->
            all.filter { it.type == "activity" && it.ts < pem.ts && pem.ts - it.ts <= window }
                .forEach { a ->
                    val n = a.name ?: return@forEach
                    counts[n] = (counts[n] ?: 0) + 1
                }
        }
        return counts.entries.sortedByDescending { it.value }.take(6).map { it.key to it.value }
    }

    fun symptomFrequency(all: List<Entry>, days: Int = 30): List<Pair<String, Int>> {
        val cutoff = System.currentTimeMillis() - days.toLong() * 24 * 3600 * 1000
        val counts = mutableMapOf<String, Int>()
        all.filter { it.type == "symptom" && it.ts > cutoff }.forEach { s ->
            val n = s.name ?: return@forEach
            counts[n] = (counts[n] ?: 0) + 1
        }
        return counts.entries.sortedByDescending { it.value }.take(8).map { it.key to it.value }
    }

    fun add(entry: Entry) = viewModelScope.launch { dao.insert(entry) }
    fun delete(id: Long) = viewModelScope.launch { dao.delete(id) }

    fun saveSettings(s: Settings) = settingsStore.save(s)

    fun eraseAll() = viewModelScope.launch { dao.clear() }

    fun refreshHealth() = viewModelScope.launch {
        healthSnapshot.value = healthManager.todaySnapshot()
    }

    suspend fun exportSnapshot(): List<Entry> = dao.snapshot()
}
