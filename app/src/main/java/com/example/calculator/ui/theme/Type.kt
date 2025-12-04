package com.example.stockcalculator.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

fun getTypography(scale: Float): Typography {
    return Typography(
        bodyLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp * scale,
            lineHeight = 24.sp * scale,
            letterSpacing = 0.5.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp * scale,
            lineHeight = 20.sp * scale,
            letterSpacing = 0.25.sp
        ),
        titleMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp * scale,
            lineHeight = 24.sp * scale,
            letterSpacing = 0.15.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 28.sp * scale,
            lineHeight = 36.sp * scale,
            letterSpacing = 0.sp
        )
        // Add other styles as needed
    )
}
