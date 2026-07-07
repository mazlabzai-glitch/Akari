package com.mazlabz.akari.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class Settings(
    val name: String = "",
    val restingHr: Int? = null,
    val onboarded: Boolean = false,
    val lowLight: Boolean = false,
    val reduceMotion: Boolean = false,
    val presetCosts: Map<String, Int> = emptyMap()
) {
    val pacingCeiling: Int? get() = restingHr?.plus(15)
}

class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("akari_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(readSettings())
    val settings: StateFlow<Settings> = _settings

    private fun readSettings() = Settings(
        name = prefs.getString("name", "") ?: "",
        restingHr = prefs.getInt("rhr", -1).takeIf { it > 0 },
        onboarded = prefs.getBoolean("onboarded", false),
        lowLight = prefs.getBoolean("lowLight", false),
        reduceMotion = prefs.getBoolean("reduceMotion", false),
        presetCosts = readCosts()
    )

    private fun readCosts(): Map<String, Int> = prefs.all.entries
        .filter { it.key.startsWith("cost_") }
        .mapNotNull { (k, v) -> (v as? Int)?.let { k.removePrefix("cost_") to it } }
        .toMap()

    /** Her shower is not my shower: preset costs are personal and adjustable. */
    fun setPresetCost(name: String, cost: Int) {
        prefs.edit().putInt("cost_" + name, cost).apply()
        _settings.value = _settings.value.copy(presetCosts = readCosts())
    }

    fun save(settings: Settings) {
        prefs.edit()
            .putString("name", settings.name)
            .putInt("rhr", settings.restingHr ?: -1)
            .putBoolean("onboarded", settings.onboarded)
            .putBoolean("lowLight", settings.lowLight)
            .putBoolean("reduceMotion", settings.reduceMotion)
            .apply()
        _settings.value = settings
    }
}
