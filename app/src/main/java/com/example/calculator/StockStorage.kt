package com.example.stockcalculator.com.example.calculator

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class StockStorage(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("stock_prefs", Context.MODE_PRIVATE)

    // [중요] LocalDate를 저장하기 위한 Gson 설정 추가
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, LocalDateSerializer())
        .registerTypeAdapter(LocalDate::class.java, LocalDateDeserializer())
        .create()

    // --- 1. 주식 목록 ---
    fun getStocks(): List<Stock> {
        val json = prefs.getString("stocks", null)
        return if (json != null) {
            val type = object : TypeToken<List<Stock>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    fun saveStocks(stocks: List<Stock>) {
        val json = gson.toJson(stocks)
        prefs.edit().putString("stocks", json).apply()
    }

    // --- 2. 자산 기록 ---
    fun getAssetHistory(): List<AssetHistory> {
        val json = prefs.getString("asset_history", null)
        return if (json != null) {
            val type = object : TypeToken<List<AssetHistory>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    fun saveAssetHistory(history: List<AssetHistory>) {
        val json = gson.toJson(history)
        prefs.edit().putString("asset_history", json).apply()
    }

    // --- 3. 프리셋 ---
    fun getPresets(): List<PortfolioPreset> {
        val json = prefs.getString("presets", null)
        return if (json != null) {
            val type = object : TypeToken<List<PortfolioPreset>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    fun savePresets(presets: List<PortfolioPreset>) {
        val json = gson.toJson(presets)
        prefs.edit().putString("presets", json).apply()
    }

    // --- 4. [NEW] 캘린더 이벤트 ---
    fun getEvents(): List<CalendarEvent> {
        val json = prefs.getString("calendar_events", null)
        return if (json != null) {
            val type = object : TypeToken<List<CalendarEvent>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    fun saveEvents(events: List<CalendarEvent>) {
        val json = gson.toJson(events)
        prefs.edit().putString("calendar_events", json).apply()
    }

    // --- 날짜 변환기 (Gson용) ---
    class LocalDateSerializer : JsonSerializer<LocalDate> {
        override fun serialize(src: LocalDate, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            return JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE))
        }
    }

    class LocalDateDeserializer : JsonDeserializer<LocalDate> {
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): LocalDate {
            return LocalDate.parse(json.asString, DateTimeFormatter.ISO_LOCAL_DATE)
        }
    }
}