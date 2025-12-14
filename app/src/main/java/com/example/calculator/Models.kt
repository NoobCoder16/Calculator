package com.example.stockcalculator.com.example.calculator

import java.time.LocalDate

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

data class PortfolioPreset(
    val id: String,
    val name: String,
    val description: String,
    val stocks: List<Stock>,
    val lastModified: Long = System.currentTimeMillis()
)

// [Moved] 캘린더 이벤트를 여기로 옮김 (날짜 저장을 위해 LocalDate 사용)
data class CalendarEvent(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val date: LocalDate
)