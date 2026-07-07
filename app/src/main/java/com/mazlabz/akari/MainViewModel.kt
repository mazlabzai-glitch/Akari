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
import com.mazlabz.akari.widget.WidgetUpdater
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


data class LoadState(
    val physical: Int,
    val cognitive: Int,
    val emotional: Int,
    val total: Int,
    val baseline: Float,
    val spiking: Boolean
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


    /**
     * Rolling 3-day energy budget across the Bateman Horne pillars.
     * PEM is delayed, so today's risk lives in the last 72 hours, not the last hour:
     * we sum activity costs over 72 h (mixed effort split across pillars) and compare
     * with the average 72-hour load over the preceding 11 days. Well above the norm
     * (>1.35x, and meaningfully large) => a gentle "delayed load" caution.
     */
    fun rollingLoad(all: List<Entry>): LoadState {
        val now = System.currentTimeMillis()
        val h72 = 72L * 3600 * 1000
        var phys = 0f; var cog = 0f; var emo = 0f
        all.filter { it.type == "activity" && now - it.ts <= h72 }.forEach { a ->
            val c = (a.cost ?: 0).toFloat()
            when (a.kind) {
                "physical" -> phys += c
                "cognitive" -> cog += c
                "emotional" -> emo += c
                else -> { phys += c / 3f; cog += c / 3f; emo += c / 3f }
            }
        }
        val total = phys + cog + emo
        val start = now - 14L * 24 * 3600 * 1000
        val end = now - h72
        val past = all.filter { it.type == "activity" && it.ts in start..end }
            .sumOf { it.cost ?: 0 }
        val baseline = if (past > 0) past / 11f * 3f else 0f
        val spiking = if (baseline > 0f) total > baseline * 1.35f && total >= 60f
        else total >= 90f
        return LoadState(phys.toInt(), cog.toInt(), emo.toInt(), total.toInt(), baseline, spiking)
    }

    /** Presets with her personal cost overrides applied. */
    fun presets(settings: Settings): List<Preset> = PRESETS.map { p ->
        settings.presetCosts[p.name]?.let { p.copy(cost = it) } ?: p
    }

    fun setPresetCost(name: String, cost: Int) = settingsStore.setPresetCost(name, cost)

    /** Average morning battery after poor sleep vs okay/good sleep (needs 3+ of each). */
    fun sleepInsight(all: List<Entry>): Pair<Int, Int>? {
        val checkins = all.filter { it.type == "checkin" && it.battery != null && it.sleepQ != null }
        val poor = checkins.filter { it.sleepQ == 1 }.mapNotNull { it.battery }
        val decent = checkins.filter { (it.sleepQ ?: 0) >= 2 }.mapNotNull { it.battery }
        if (poor.size < 3 || decent.size < 3) return null
        return poor.average().toInt() to decent.average().toInt()
    }

    fun update(entry: Entry) = viewModelScope.launch {
        dao.update(entry)
        WidgetUpdater.refresh(getApplication())
    }

    fun add(entry: Entry) = viewModelScope.launch {
        dao.insert(entry)
        WidgetUpdater.refresh(getApplication())
    }

    fun delete(id: Long) = viewModelScope.launch {
        dao.delete(id)
        WidgetUpdater.refresh(getApplication())
    }

    fun saveSettings(s: Settings) = settingsStore.save(s)

    fun eraseAll() = viewModelScope.launch {
        dao.clear()
        WidgetUpdater.refresh(getApplication())
    }

    fun refreshHealth() = viewModelScope.launch {
        healthSnapshot.value = healthManager.todaySnapshot()
    }

    suspend fun exportSnapshot(): List<Entry> = dao.snapshot()
}
