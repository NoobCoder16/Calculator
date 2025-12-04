package com.example.stockcalculator.com.example.calculator

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class `StockStorage.kt`(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("stock_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

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
}
