package com.example.stockcalculator.com.example.calculator

import android.content.Context
import android.content.SharedPreferences

class SettingsStorage(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

    var isDarkMode: Boolean
        get() = prefs.getBoolean("is_dark_mode", false) // Default to light mode or system? Let's say false for now, or handle system default logic in VM
        set(value) = prefs.edit().putBoolean("is_dark_mode", value).apply()

    // 0: Small, 1: Medium, 2: Large
    var fontSizeScale: Int
        get() = prefs.getInt("font_size_scale", 1)
        set(value) = prefs.edit().putInt("font_size_scale", value).apply()
}
