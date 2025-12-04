package com.example.stockcalculator.com.example.calculator

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppSettings(
    val isDarkMode: Boolean,
    val language: String,
    val fontSizeScale: Int
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val storage = SettingsStorage(application)

    private val _settings = MutableStateFlow(
        AppSettings(
            isDarkMode = storage.isDarkMode,
            language = storage.language,
            fontSizeScale = storage.fontSizeScale
        )
    )
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    fun setDarkMode(isDark: Boolean) {
        storage.isDarkMode = isDark
        updateState()
    }

    fun setLanguage(lang: String) {
        storage.language = lang
        updateState()
    }

    fun setFontSizeScale(scale: Int) {
        storage.fontSizeScale = scale
        updateState()
    }

    private fun updateState() {
        _settings.value = AppSettings(
            isDarkMode = storage.isDarkMode,
            language = storage.language,
            fontSizeScale = storage.fontSizeScale
        )
    }
}
