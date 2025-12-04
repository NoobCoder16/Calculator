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

class `CalculatorViewModel.kt`(application: Application) : AndroidViewModel(application) {
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
