package com.example.stockcalculator.com.example.calculator

// Utils.kt (새로 생성)

import java.text.NumberFormat
import java.util.Locale

// 모든 파일에서 공통으로 사용할 수 있는 함수
fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale.KOREA)
    return format.format(amount)
}