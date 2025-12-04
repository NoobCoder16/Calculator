package com.example.stockcalculator.com.example.calculator

data class Stock(
    val id: String,
    val name: String,
    val targetRatio: Double,
    val currentValue: Double
) {
    val diff: Double
        get() = 0.0
}

data class AssetHistory(
    val timestamp: Long,
    val totalAssets: Double
)
