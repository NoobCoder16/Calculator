package com.example.stockcalculator.com.example.calculator

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioScreen(viewModel: CalculatorViewModel) {
    val stocks by viewModel.stocks.collectAsState()
    val totalAssets by viewModel.totalAssets.collectAsState()
    val assetHistory by viewModel.assetHistory.collectAsState()

    // Generate colors for stocks
    val stockColors = remember(stocks) {
        stocks.mapIndexed { index, stock ->
            stock.id to generateColor(index)
        }.toMap()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("포트폴리오 분석") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Summary Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("총 자산", style = MaterialTheme.typography.labelMedium)
                        Text(
                            formatCurrency(totalAssets),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("보유 종목", style = MaterialTheme.typography.labelMedium)
                        Text(
                            "${stocks.size}개",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Pie Chart Section
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("자산 구성", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))

                        if (stocks.isNotEmpty() && totalAssets > 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(250.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                PieChart(stocks = stocks, totalAssets = totalAssets, colors = stockColors)
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("데이터가 없습니다", color = Color.Gray)
                            }
                        }
                    }
                }
            }

            // Stock List (Legend)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    stocks.forEach { stock ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(stockColors[stock.id] ?: Color.Gray, shape = MaterialTheme.shapes.small)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stock.name, style = MaterialTheme.typography.bodyMedium)
                            }
                            Text(
                                formatCurrency(stock.currentValue),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Asset History Chart Section
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("자산 변동 추이", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))

                        if (assetHistory.size >= 2) {
                            AssetHistoryChart(history = assetHistory)
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("데이터가 부족합니다 (최소 2개)", color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PieChart(stocks: List<Stock>, totalAssets: Double, colors: Map<String, Color>) {
    Canvas(modifier = Modifier.size(200.dp)) {
        var startAngle = -90f
        val radius = size.minDimension / 2
        val center = Offset(size.width / 2, size.height / 2)

        stocks.forEach { stock ->
            val sweepAngle = (stock.currentValue / totalAssets * 360).toFloat()
            val color = colors[stock.id] ?: Color.Gray

            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                size = Size(radius * 2, radius * 2),
                topLeft = Offset(center.x - radius, center.y - radius)
            )
            startAngle += sweepAngle
        }

        // Inner circle for donut effect (optional, but looks better)
        drawCircle(
            color = Color.White,
            radius = radius * 0.6f,
            center = center
        )
    }
}

@Composable
fun AssetHistoryChart(history: List<AssetHistory>) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        if (history.isEmpty()) return@Canvas

        val padding = 40.dp.toPx()
        val graphWidth = size.width - padding * 2
        val graphHeight = size.height - padding * 2

        val maxAsset = history.maxOf { it.totalAssets }
        val minAsset = history.minOf { it.totalAssets } * 0.9 // Start slightly below min
        val assetRange = maxAsset - minAsset

        // Draw Axes
        drawLine(
            color = Color.Gray,
            start = Offset(padding, size.height - padding),
            end = Offset(size.width - padding, size.height - padding),
            strokeWidth = 2f
        )
        drawLine(
            color = Color.Gray,
            start = Offset(padding, padding),
            end = Offset(padding, size.height - padding),
            strokeWidth = 2f
        )

        // Draw Line
        val path = Path()
        history.forEachIndexed { index, item ->
            val x = padding + (index.toFloat() / (history.size - 1)) * graphWidth
            val y = size.height - padding - ((item.totalAssets - minAsset) / assetRange * graphHeight).toFloat()

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }

            // Draw points
            drawCircle(
                color = Color(0xFF6200EE),
                radius = 4.dp.toPx(),
                center = Offset(x, y)
            )
        }

        drawPath(
            path = path,
            color = Color(0xFF6200EE),
            style = Stroke(width = 3.dp.toPx())
        )
    }
}

fun generateColor(index: Int): Color {
    val colors = listOf(
        Color(0xFFE57373), Color(0xFF81C784), Color(0xFF64B5F6),
        Color(0xFFFFD54F), Color(0xFFBA68C8), Color(0xFF4DB6AC),
        Color(0xFFFF8A65), Color(0xFFAED581), Color(0xFF7986CB),
        Color(0xFFA1887F)
    )
    return colors[index % colors.size]
}
