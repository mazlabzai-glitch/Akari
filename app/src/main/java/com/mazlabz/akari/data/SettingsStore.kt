package com.mazlabz.akari.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class Settings(
    val name: String = "",
    val restingHr: Int? = null,
    val onboarded: Boolean = false
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
        onboarded = prefs.getBoolean("onboarded", false)
    )

    fun save(settings: Settings) {
        prefs.edit()
            .putString("name", settings.name)
            .putInt("rhr", settings.restingHr ?: -1)
            .putBoolean("onboarded", settings.onboarded)
            .apply()
        _settings.value = settings
    }
}
