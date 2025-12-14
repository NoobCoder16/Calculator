package com.example.stockcalculator.com.example.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetScreen(
    viewModel: CalculatorViewModel,
    onBackClick: () -> Unit
) {
    val presets by viewModel.presets.collectAsState()
    val currentStocks by viewModel.stocks.collectAsState()

    // 다이얼로그 상태 관리
    var showDialog by remember { mutableStateOf(false) }
    var editingPreset by remember { mutableStateOf<PortfolioPreset?>(null) } // 수정 중인 프리셋

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("프리셋 목록", fontWeight = FontWeight.Bold)
                        Text("저장된 포트폴리오", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(16.dp)
        ) {
            // [새 프리셋 만들기 버튼]
            Button(
                onClick = {
                    editingPreset = null // 새 생성 모드
                    showDialog = true
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("새 프리셋 만들기", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // [프리셋 리스트]
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(presets) { preset ->
                    PresetItem(
                        preset = preset,
                        onLoad = {
                            viewModel.loadPreset(preset)
                            onBackClick()
                        },
                        onEdit = {
                            editingPreset = preset // 수정 모드 진입
                            showDialog = true
                        },
                        onDelete = { viewModel.deletePreset(preset.id) }
                    )
                }
            }
        }
    }

    // [통합 다이얼로그] 생성 및 수정 겸용
    if (showDialog) {
        PresetDialog(
            title = if (editingPreset == null) "새 프리셋 만들기" else "프리셋 수정",
            initialName = editingPreset?.name ?: "",
            // 수정 시 기존 프리셋에 있던 종목들은 미리 선택됨
            initialSelectedIds = editingPreset?.stocks?.map { it.id } ?: emptyList(),
            currentStocks = currentStocks,
            onDismiss = { showDialog = false },
            onSave = { name, selectedStocks ->
                if (editingPreset == null) {
                    // 생성
                    viewModel.addPreset(name, "사용자 정의 포트폴리오", selectedStocks)
                } else {
                    // 수정
                    viewModel.updatePreset(editingPreset!!.id, name, selectedStocks)
                }
                showDialog = false
            }
        )
    }
}

@Composable
fun PresetItem(
    preset: PortfolioPreset,
    onLoad: () -> Unit,
    onEdit: () -> Unit, // 수정 클릭 시 실행할 함수 받기
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Bookmark, null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(preset.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(preset.description, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, "삭제", tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("보유 종목", fontSize = 12.sp, color = Color.Gray)
                Text("${preset.stocks.size}개", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("마지막 수정", fontSize = 12.sp, color = Color.Gray)
                Text(dateFormat.format(Date(preset.lastModified)), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onLoad,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("불러오기", color = Color.Black)
                }
                // [수정 버튼] 기능 연결
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("수정", color = Color.Black)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetDialog(
    title: String,
    initialName: String,
    initialSelectedIds: List<String>,
    currentStocks: List<Stock>,
    onDismiss: () -> Unit,
    onSave: (String, List<Stock>) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    // 초기 선택된 종목 ID 리스트를 상태로 관리
    val selectedStockIds = remember { mutableStateListOf<String>().apply { addAll(initialSelectedIds) } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("포트폴리오 이름") },
                    placeholder = { Text("예: 공격형 포트폴리오") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text("종목 선택 (메인 계산기 목록)", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    if (currentStocks.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("메인 화면에 종목이 없습니다", color = Color.Gray, fontSize = 12.sp)
                        }
                    } else {
                        LazyColumn(contentPadding = PaddingValues(8.dp)) {
                            items(currentStocks) { stock ->
                                // ID 비교 시 안전하게 처리
                                val isSelected = selectedStockIds.contains(stock.id)

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (isSelected) selectedStockIds.remove(stock.id)
                                            else selectedStockIds.add(stock.id)
                                        }
                                        .padding(4.dp)
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = {
                                            if (it) selectedStockIds.add(stock.id)
                                            else selectedStockIds.remove(stock.id)
                                        }
                                    )
                                    Text(stock.name, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("(목표 ${stock.targetRatio}%)", color = Color.Gray, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // ID가 일치하는 종목들을 찾아서 저장
                    val selected = currentStocks.filter { stock -> selectedStockIds.contains(stock.id) }
                    onSave(name, selected)
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("저장")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소", color = Color.Gray) }
        }
    )
}