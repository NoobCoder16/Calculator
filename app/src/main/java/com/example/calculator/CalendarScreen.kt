package com.example.calculator

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

// 데이터 모델
data class CalendarEvent(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val date: LocalDate
)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onBackClick: () -> Unit // 👈 뒤로가기 기능 받기
) {
    // --- 상태 관리 ---
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var showAddDialog by remember { mutableStateOf(false) }

    // [수정] 빈 목록으로 시작
    val events = remember { mutableStateListOf<CalendarEvent>() }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, "추가")
                Spacer(modifier = Modifier.width(8.dp))
                Text("새 알림 추가")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // [수정] 헤더: 뒤로가기 버튼 + 제목
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "뒤로가기"
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "캘린더",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // 달력 부분 (월 이동 및 그리드)
            CalendarView(
                currentMonth = currentMonth,
                onMonthChange = { currentMonth = it },
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it },
                events = events
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "예정된 알람",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // 일정 목록
            val selectedEvents = events.filter { it.date == selectedDate }

            if (selectedEvents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "등록된 일정이 없습니다.",
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(selectedEvents) { event ->
                        EventItem(
                            event = event,
                            onDelete = { events.remove(event) }
                        )
                    }
                }
            }
        }
    }

    // 추가 다이얼로그
    if (showAddDialog) {
        AddEventDialog(
            initialDate = selectedDate,
            onDismiss = { showAddDialog = false },
            onConfirm = { title, date ->
                events.add(CalendarEvent(title = title, date = date))
                showAddDialog = false
                selectedDate = date // 추가한 날짜로 이동
            }
        )
    }
}

// --- 하위 컴포넌트들 (CalendarView, EventItem 등) ---

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarView(
    currentMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    events: List<CalendarEvent>
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onMonthChange(currentMonth.minusMonths(1)) }) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Prev")
            }
            Text(
                text = "${currentMonth.year}년 ${currentMonth.monthValue}월",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { onMonthChange(currentMonth.plusMonths(1)) }) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("일", "월", "화", "수", "목", "금", "토").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = if (day == "일") Color.Red else if (day == "토") Color.Blue else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val daysInMonth = currentMonth.lengthOfMonth()
        val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value % 7

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(300.dp)
        ) {
            items(firstDayOfWeek) { Box(modifier = Modifier.size(40.dp)) }

            items(daysInMonth) { day ->
                val date = currentMonth.atDay(day + 1)
                val isSelected = date == selectedDate
                val hasEvent = events.any { it.date == date }

                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                        .clickable { onDateSelected(date) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${day + 1}",
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                        if (hasEvent) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EventItem(event: CalendarEvent, onDelete: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = event.date.format(DateTimeFormatter.ofPattern("yyyy. MM. dd.")),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "삭제",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddEventDialog(
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (String, LocalDate) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(initialDate) }
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate.toEpochDay() * 86400000)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        selectedDate = LocalDate.ofEpochDay(it / 86400000)
                    }
                    showDatePicker = false
                }) { Text("확인") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("취소") } }
        ) { DatePicker(state = datePickerState) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("새 알람 추가") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("알람 제목") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedCard(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = selectedDate.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")),
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { if (title.isNotBlank()) onConfirm(title, selectedDate) }) { Text("추가") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } }
    )
}

@Preview(showBackground = true)
@Composable
fun CalendarScreenPreview() {
    CalendarScreen(onBackClick = {})
}