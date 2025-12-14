package com.example.stockcalculator.com.example.calculator

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.util.UUID

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {
    private val storage = StockStorage(application)

    private val _stocks = MutableStateFlow<List<Stock>>(emptyList())
    val stocks: StateFlow<List<Stock>> = _stocks.asStateFlow()

    private val _totalAssets = MutableStateFlow(0.0)
    val totalAssets: StateFlow<Double> = _totalAssets.asStateFlow()

    private val _assetHistory = MutableStateFlow<List<AssetHistory>>(emptyList())
    val assetHistory: StateFlow<List<AssetHistory>> = _assetHistory.asStateFlow()

    private val _presets = MutableStateFlow<List<PortfolioPreset>>(emptyList())
    val presets: StateFlow<List<PortfolioPreset>> = _presets.asStateFlow()

    // [NEW] 캘린더 이벤트 리스트
    private val _events = MutableStateFlow<List<CalendarEvent>>(emptyList())
    val events: StateFlow<List<CalendarEvent>> = _events.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        _stocks.value = storage.getStocks()
        _assetHistory.value = storage.getAssetHistory()
        _presets.value = storage.getPresets()
        _events.value = storage.getEvents() // 이벤트 불러오기
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

    // --- 주식 ---
    fun addStock(name: String, targetRatio: Double, currentValue: Double) {
        val newStock = Stock(UUID.randomUUID().toString(), name, targetRatio, currentValue)
        val updated = _stocks.value + newStock
        _stocks.value = updated
        storage.saveStocks(updated)
        calculateTotalAssets()
        recordAssetHistory()
    }

    fun deleteStock(stockId: String) {
        val updated = _stocks.value.filter { it.id != stockId }
        _stocks.value = updated
        storage.saveStocks(updated)
        calculateTotalAssets()
        recordAssetHistory()
    }

    // --- 프리셋 ---
    fun loadPreset(preset: PortfolioPreset) {
        val newStocks = preset.stocks.map { it.copy(id = UUID.randomUUID().toString()) }
        _stocks.value = newStocks
        storage.saveStocks(newStocks)
        calculateTotalAssets()
        recordAssetHistory()
    }

    fun addPreset(name: String, description: String, selectedStocks: List<Stock>) {
        val newPreset = PortfolioPreset(UUID.randomUUID().toString(), name, description, selectedStocks)
        val updated = listOf(newPreset) + _presets.value
        _presets.value = updated
        storage.savePresets(updated)
    }

    fun updatePreset(presetId: String, newName: String, newStocks: List<Stock>) {
        val currentList = _presets.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == presetId }
        if (index != -1) {
            val updated = currentList[index].copy(name = newName, stocks = newStocks, lastModified = System.currentTimeMillis())
            currentList[index] = updated
            _presets.value = currentList
            storage.savePresets(currentList)
        }
    }

    fun deletePreset(presetId: String) {
        val updated = _presets.value.filter { it.id != presetId }
        _presets.value = updated
        storage.savePresets(updated)
    }

    // --- [NEW] 캘린더 이벤트 관리 ---
    fun addEvent(title: String, date: LocalDate) {
        val newEvent = CalendarEvent(title = title, date = date)
        val updated = _events.value + newEvent
        _events.value = updated
        storage.saveEvents(updated)
    }

    fun deleteEvent(event: CalendarEvent) {
        val updated = _events.value.filter { it.id != event.id }
        _events.value = updated
        storage.saveEvents(updated)
    }
}