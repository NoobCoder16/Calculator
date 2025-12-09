package com.example.stockcalculator.com.example.calculator
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {
    private val storage = StockStorage(application)

    private val _stocks = MutableStateFlow<List<Stock>>(emptyList())
    val stocks: StateFlow<List<Stock>> = _stocks.asStateFlow()

    private val _totalAssets = MutableStateFlow(0.0)
    val totalAssets: StateFlow<Double> = _totalAssets.asStateFlow()

    private val _assetHistory = MutableStateFlow<List<AssetHistory>>(emptyList())
    val assetHistory: StateFlow<List<AssetHistory>> = _assetHistory.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val savedStocks = storage.getStocks()
        _stocks.value = savedStocks

        val savedHistory = storage.getAssetHistory()
        _assetHistory.value = savedHistory

        calculateTotalAssets()
    }

    private fun calculateTotalAssets() {
        val total = _stocks.value.sumOf { it.currentValue }
        _totalAssets.value = total
    }

    private fun recordAssetHistory() {
        val currentHistory = _assetHistory.value.toMutableList()
        currentHistory.add(AssetHistory(System.currentTimeMillis(), _totalAssets.value))
        _assetHistory.value = currentHistory
        storage.saveAssetHistory(currentHistory)
    }

    fun addStock(name: String, targetRatio: Double, currentValue: Double) {
        val newStock = Stock(
            id = UUID.randomUUID().toString(),
            name = name,
            targetRatio = targetRatio,
            currentValue = currentValue
        )
        val updatedList = _stocks.value + newStock
        _stocks.value = updatedList
        storage.saveStocks(updatedList)
        calculateTotalAssets()
        recordAssetHistory()
    }

    fun deleteStock(stockId: String) {
        val updatedList = _stocks.value.filter { it.id != stockId }
        _stocks.value = updatedList
        storage.saveStocks(updatedList)
        calculateTotalAssets()
        recordAssetHistory()
    }
}

data class Stock(
    val id: String,          // 고유 ID (UUID 등)
    val name: String,        // 종목 이름
    val targetRatio: Double, // 목표 비중 (%)
    val currentValue: Double // 현재 평가액 (원)
)

data class AssetHistory(
    val timestamp: Long,     // 기록 시각 (epoch millis)
    val totalAssets: Double  // 그 시점의 총 자산 값
)

class StockStorage(app: Application) {

    private val prefs = app.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun getStocks(): List<Stock> {
        val json = prefs.getString(KEY_STOCKS, null) ?: return emptyList()
        return try {
            val array = JSONArray(json)
            val result = mutableListOf<Stock>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.optString("id", "")
                val name = obj.optString("name", "")
                val targetRatio = obj.optDouble("targetRatio", 0.0)
                val currentValue = obj.optDouble("currentValue", 0.0)

                if (id.isNotBlank() && name.isNotBlank()) {
                    result.add(
                        Stock(
                            id = id,
                            name = name,
                            targetRatio = targetRatio,
                            currentValue = currentValue
                        )
                    )
                }
            }
            result
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun saveStocks(stocks: List<Stock>) {
        try {
            val array = JSONArray()
            stocks.forEach { stock ->
                val obj = JSONObject().apply {
                    put("id", stock.id)
                    put("name", stock.name)
                    put("targetRatio", stock.targetRatio)
                    put("currentValue", stock.currentValue)
                }
                array.put(obj)
            }
            prefs.edit().putString(KEY_STOCKS, array.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getAssetHistory(): List<AssetHistory> {
        val json = prefs.getString(KEY_ASSET_HISTORY, null) ?: return emptyList()
        return try {
            val array = JSONArray(json)
            val result = mutableListOf<AssetHistory>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val timestamp = obj.optLong("timestamp", 0L)
                val totalAssets = obj.optDouble("totalAssets", 0.0)

                if (timestamp > 0L) {
                    result.add(
                        AssetHistory(
                            timestamp = timestamp,
                            totalAssets = totalAssets
                        )
                    )
                }
            }
            result
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun saveAssetHistory(history: List<AssetHistory>) {
        try {
            val array = JSONArray()
            history.forEach { item ->
                val obj = JSONObject().apply {
                    put("timestamp", item.timestamp)
                    put("totalAssets", item.totalAssets)
                }
                array.put(obj)
            }
            prefs.edit().putString(KEY_ASSET_HISTORY, array.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val PREF_NAME = "stock_calculator_prefs"
        private const val KEY_STOCKS = "stocks"
        private const val KEY_ASSET_HISTORY = "asset_history"
    }
}
