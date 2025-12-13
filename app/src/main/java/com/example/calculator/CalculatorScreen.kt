package com.example.stockcalculator.com.example.calculator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    onSettingsClick: () -> Unit
) {
    val stocks by viewModel.stocks.collectAsState()
    val totalAssets by viewModel.totalAssets.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("메인 계산기") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(androidx.compose.material.icons.Icons.Default.Settings, contentDescription = "설정")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "종목 추가")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // Total Assets Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "총 자산", style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = formatCurrency(totalAssets),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Stock List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(stocks) { stock ->
                    StockItem(
                        stock = stock,
                        totalAssets = totalAssets,
                        onDelete = { viewModel.deleteStock(stock.id) }
                    )
                }
            }
        }

        if (showAddDialog) {
            AddStockDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { name, ratio, value ->
                    viewModel.addStock(name, ratio, value)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun StockItem(stock: Stock, totalAssets: Double, onDelete: () -> Unit) {
    // Calculate difference based on target ratio
    val targetValue = totalAssets * (stock.targetRatio / 100)
    val diff = targetValue - stock.currentValue
    val diffColor = if (diff >= 0) Color(0xFF4CAF50) else Color(0xFFE57373) // Green or Red

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = stock.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = "목표 비중 ${stock.targetRatio}%", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "삭제", tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "현재 평가액", style = MaterialTheme.typography.bodyMedium)
                Text(text = formatCurrency(stock.currentValue), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "증감액", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "${if (diff > 0) "+" else ""}${formatCurrency(diff)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = diffColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AddStockDialog(onDismiss: () -> Unit, onAdd: (String, Double, Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var ratio by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("종목 추가") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("주식 이름") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = ratio,
                    onValueChange = { ratio = it },
                    label = { Text("목표 비중 (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text("현재 평가액 (₩)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val r = ratio.toDoubleOrNull()
                    val v = value.toDoubleOrNull()
                    if (name.isNotBlank() && r != null && v != null) {
                        onAdd(name, r, v)
                    }
                },
                enabled = name.isNotBlank() && ratio.isNotBlank() && value.isNotBlank()
            ) {
                Text("저장")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

private fun formatCurrency(value: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.KOREA)
    return formatter.format(value) + "원"
}
