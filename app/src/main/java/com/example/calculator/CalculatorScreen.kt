// CalculatorScreen.kt

package com.example.stockcalculator.com.example.calculator // 이 Kotlin 파일이 속한 패키지(네임스페이스).

import androidx.compose.foundation.layout.* // Column/Row/Spacer/padding/fillMaxSize 같은 레이아웃 관련 Compose 함수들을 한 번에 가져옴.
import androidx.compose.foundation.lazy.LazyColumn // 스크롤 가능한 리스트(지연 로딩 리스트) UI 컴포넌트.
import androidx.compose.foundation.lazy.items // LazyColumn에서 리스트 데이터를 반복 렌더링할 때 쓰는 items 확장 함수.
import androidx.compose.foundation.text.KeyboardOptions // TextField에 키보드 타입/옵션을 지정할 때 사용.
import androidx.compose.material.icons.Icons // Material 아이콘들의 모음(기본 아이콘 접근점).
import androidx.compose.material.icons.filled.Add // + (추가) 아이콘.
import androidx.compose.material.icons.filled.Edit // 연필(수정) 아이콘.
import androidx.compose.material.icons.filled.Delete // 휴지통(삭제) 아이콘.
import androidx.compose.material.icons.filled.Settings // 톱니바퀴(설정) 아이콘.
import androidx.compose.material3.* // Material3 UI 컴포넌트(Scaffold, TopAppBar, Card, TextField, Button 등) 전체.
import androidx.compose.runtime.* // remember, mutableStateOf, collectAsState 같은 Compose 상태 관리 기능들.
import androidx.compose.ui.Alignment // Row/Column에서 정렬(Alignment) 지정할 때 사용.
import androidx.compose.ui.Modifier // Compose에서 UI 요소의 크기/패딩/정렬 등을 꾸미는 “수식어” 객체.
import androidx.compose.ui.graphics.Color // 색상 지정(텍스트 색, 배경색 등)에 사용.
import androidx.compose.ui.text.font.FontWeight // 글씨 굵기(Bold 등) 지정.
import androidx.compose.ui.text.input.KeyboardType // 키보드 타입(숫자/텍스트 등) 지정.
import androidx.compose.ui.unit.dp // dp 단위(픽셀 독립 단위)로 크기/여백 지정할 때 사용.
import java.text.NumberFormat // (이 파일에는 직접 사용 안 보이지만) formatCurrency 구현에서 통화/숫자 포맷에 보통 사용.
import java.util.Locale // (이 파일에는 직접 사용 안 보이지만) formatCurrency 구현에서 로케일(한국 등) 지정에 보통 사용.

@OptIn(ExperimentalMaterial3Api::class) // Material3의 실험(Experimental) API를 쓰기 위해 “사용하겠다”라고 명시.
@Composable
fun CalculatorScreen( // 메인 계산기 화면을 그리는 Composable 함수.
    viewModel: CalculatorViewModel, // 화면이 참조할 ViewModel(데이터/로직 담당). UI는 직접 저장소를 만지지 않게 분리.
    onSettingsClick: () -> Unit // 설정 버튼을 눌렀을 때 실행할 콜백.
) {
    val stocks by viewModel.stocks.collectAsState() // ViewModel의 StateFlow<List<Stock>>를 Compose State로 구독. 값이 바뀌면 자동 리컴포지션.
    val totalAssets by viewModel.totalAssets.collectAsState() // ViewModel의 총자산 StateFlow를 구독. 변경 시 UI 자동 갱신.

    var showAddDialog by remember { mutableStateOf(false) } // “종목 추가 다이얼로그”를 띄울지 여부를 UI 로컬 상태로 저장(리컴포지션돼도 유지).
    var editingStock by remember { mutableStateOf<Stock?>(null) } // “수정 다이얼로그”가 열려있으면 수정 대상 Stock을 저장. null이면 수정창 닫힘.

    Scaffold( // Material3의 기본 화면 레이아웃(상단바/본문/FAB 등)을 제공하는 컨테이너.
        topBar = { // 상단바 영역을 정의.
            TopAppBar( // Material3 상단 앱바.
                title = { Text("메인 계산기") }, // 상단바의 제목 텍스트를 표시.
                actions = { // 상단바 오른쪽에 배치되는 액션(버튼) 영역.
                    IconButton(onClick = onSettingsClick) { // 설정 아이콘 버튼. 누르면 콜백 호출(네비게이션 등은 바깥에서 처리).
                        Icon(Icons.Default.Settings, contentDescription = "설정") // 설정(톱니바퀴) 아이콘. 접근성용 설명 포함.
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors( // TopAppBar 색상(테마) 설정.
                    containerColor = MaterialTheme.colorScheme.background // 상단바 배경을 앱의 background 색으로(테마에 따라 자동 변경).
                )
            )
        },
        floatingActionButton = { // 화면에 떠있는 플로팅 버튼(FAB) 영역.
            FloatingActionButton(onClick = { showAddDialog = true }) { // FAB 클릭 시 추가 다이얼로그 표시 상태를 true로 변경.
                Icon(Icons.Default.Add, contentDescription = "종목 추가") // + 아이콘 표시. 접근성용 설명 포함.
            }
        }
    ) { paddingValues -> // Scaffold의 본문(content). paddingValues는 상단바 등으로 가려지지 않도록 제공되는 안전 패딩.
        Column( // 본문을 세로로 쌓는 레이아웃.
            modifier = Modifier // Modifier로 레이아웃/스타일 지정.
                .padding(paddingValues) // Scaffold가 준 패딩(상단바/시스템UI 고려)을 먼저 적용.
                .padding(16.dp) // 화면 내부 기본 여백 16dp 추가.
                .fillMaxSize() // 화면에서 가능한 공간을 최대한 채움.
        ) {
            Card( // 총자산을 강조해서 보여주기 위한 카드 UI.
                modifier = Modifier // 카드의 Modifier 지정.
                    .fillMaxWidth() // 카드 너비를 화면 가로 전체로.
                    .padding(bottom = 16.dp), // 아래 요소와 간격을 주기 위해 아래쪽에 16dp 여백.
                colors = CardDefaults.cardColors( // 카드의 배경/글자 색 테마 설정.
                    containerColor = MaterialTheme.colorScheme.primaryContainer, // 강조용 컨테이너 색(테마에 따라 자동).
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer // 컨테이너 위에 올라갈 텍스트/아이콘 색(가독성 보장).
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) { // 카드 내부는 세로 정렬, 내부 여백 16dp.
                    Text(text = "총 자산", style = MaterialTheme.typography.labelMedium) // 라벨(작은 텍스트)로 “총 자산” 표시.
                    Text( // 실제 총자산 금액 표시(강조).
                        text = formatCurrency(totalAssets), // totalAssets(숫자)를 통화 문자열로 변환해 표시. (formatCurrency 함수는 다른 곳에 정의되어 있어야 함)
                        style = MaterialTheme.typography.headlineMedium, // 큰 글씨 스타일 적용.
                        fontWeight = FontWeight.Bold // 굵게 표시해서 강조.
                    )
                }
            }

            LazyColumn( // 종목 리스트(스크롤) 표시.
                verticalArrangement = Arrangement.spacedBy(8.dp) // 아이템들 사이 간격을 8dp로 일정하게 유지.
            ) {
                items(stocks) { stock -> // stocks 리스트를 순회하며 각 stock에 대해 UI 아이템을 생성.
                    StockItem( // 한 종목(Stock) 카드 아이템을 그리는 Composable 호출.
                        stock = stock, // 현재 렌더링할 Stock 데이터 전달.
                        totalAssets = totalAssets, // 목표금액/증감액 계산을 위해 총자산 전달.
                        onDelete = { viewModel.deleteStock(stock.id) }, // 삭제 버튼 클릭 시 ViewModel의 삭제 로직 호출(데이터 변경은 ViewModel에서).
                        onEdit = { editingStock = stock } // 수정 버튼 클릭 시 editingStock에 현재 Stock을 넣어 수정 다이얼로그를 열게 함.
                    )
                }
            }
        }

        if (showAddDialog) { // “추가 다이얼로그 표시 상태”가 true일 때만 다이얼로그를 실제로 화면에 띄움.
            AddStockDialog( // 종목 추가 다이얼로그 Composable 호출.
                onDismiss = { showAddDialog = false }, // 취소/바깥 클릭 등으로 닫을 때 showAddDialog를 false로 돌려 닫음.
                onAdd = { name, ratio, value -> // 저장(추가) 버튼 누르면 입력값(name/ratio/value)을 콜백으로 받음.
                    viewModel.addStock(name, ratio, value) // ViewModel을 통해 실제 종목을 추가하고 저장소에 반영.
                    showAddDialog = false // 추가가 끝났으니 다이얼로그 닫기.
                }
            )
        }

        editingStock?.let { s -> // editingStock이 null이 아니면(=수정할 종목이 선택되어 있으면) 수정 다이얼로그 표시.
            EditStockDialog( // 종목 수정 다이얼로그 Composable 호출.
                stock = s, // 수정할 대상 Stock(현재 선택된 종목) 전달.
                onDismiss = { editingStock = null }, // 취소/바깥 클릭으로 닫을 때 null로 만들어 닫음.
                onSave = { name, ratio, value -> // 저장 버튼 누르면 수정된 입력값을 콜백으로 받음.
                    viewModel.updateStock(s.id, name, ratio, value) // ViewModel에 “이 id 종목을 이 값으로 갱신” 요청.
                    editingStock = null // 저장 후 다이얼로그 닫기.
                }
            )
        }
    }
}

@Composable
fun StockItem(stock: Stock, totalAssets: Double, onDelete: () -> Unit, onEdit: () -> Unit) { // 종목 1개를 카드 형태로 표시하고 수정/삭제 콜백을 받음.
    val targetValue = totalAssets * (stock.targetRatio / 100) // 목표금액 = 총자산 * (목표비중/100). “이 종목이 목표로 가져야 할 금액”.
    val diff = targetValue - stock.currentValue // 증감액 = 목표금액 - 현재평가액. 양수면 더 사야 하고, 음수면 줄여야 함.
    val diffColor = if (diff >= 0) Color(0xFF4CAF50) else Color(0xFFE57373) // 증감액이 양수면 초록, 음수면 빨강으로 직관적 표시.

    Card(elevation = CardDefaults.cardElevation(2.dp)) { // 각 종목을 카드로 감싸 시각적으로 구분
        Column(modifier = Modifier.padding(16.dp)) { // 카드 내부는 세로 정렬
            Row( // 상단 행: 왼쪽(이름/비중) + 오른쪽(수정/삭제 버튼).
                modifier = Modifier.fillMaxWidth(), // Row가 가로 전체를 차지하게 해서 양쪽 정렬이 자연스럽게 되게 함.
                horizontalArrangement = Arrangement.SpaceBetween, // 좌/우 끝으로 벌려 배치(왼쪽 정보, 오른쪽 버튼).
                verticalAlignment = Alignment.CenterVertically // 세로 중앙 정렬로 보기 좋게 정렬.
            ) {
                Column {
                    Text( // 종목명 표시.
                        text = stock.name, // Stock의 name(종목명) 표시.
                        style = MaterialTheme.typography.titleMedium, // 중간 크기 제목 스타일.
                        fontWeight = FontWeight.Bold // 굵게 해서 강조.
                    )
                    Text( // 목표 비중 표시(보조 텍스트).
                        text = "목표 비중 ${stock.targetRatio}%", // 목표비중 퍼센트 표시.
                        style = MaterialTheme.typography.bodySmall, // 작은 본문 스타일.
                        color = Color.Gray // 회색으로 덜 강조(보조 정보).
                    )
                }
                Row { // 오른쪽 영역: 수정/삭제 버튼을 가로로 배치.
                    IconButton(onClick = onEdit) { // 수정 버튼 클릭 시 onEdit 콜백 호출(부모가 다이얼로그 열도록 상태 변경).
                        Icon(Icons.Default.Edit, contentDescription = "수정", tint = Color.Gray) // 수정 아이콘(회색) 표시.
                    }
                    IconButton(onClick = onDelete) { // 삭제 버튼 클릭 시 onDelete 콜백 호출(부모가 ViewModel delete 호출).
                        Icon(Icons.Default.Delete, contentDescription = "삭제", tint = Color.Gray) // 삭제 아이콘(회색) 표시.
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp)) // 상단 행과 아래 정보 사이 간격 8dp.

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { // “현재 평가액” 라벨과 값을 좌우로 배치.
                Text(text = "현재 평가액", style = MaterialTheme.typography.bodyMedium) // 왼쪽 라벨 텍스트.
                Text( // 오른쪽 값 텍스트.
                    text = formatCurrency(stock.currentValue), // 현재 평가액을 통화 포맷으로 표시(예: ₩1,000,000).
                    style = MaterialTheme.typography.bodyMedium, // 본문 스타일.
                    fontWeight = FontWeight.Medium // 약간 굵게 해서 값이 눈에 띄게 함.
                )
            }

            Spacer(modifier = Modifier.height(8.dp)) // 현재평가액 행과 증감액 행 사이 간격 8dp.

            Row( // 증감액 라벨과 값을 좌우로 배치.
                modifier = Modifier.fillMaxWidth(), // 가로 전체 사용.
                horizontalArrangement = Arrangement.SpaceBetween, // 좌/우 끝 배치.
                verticalAlignment = Alignment.CenterVertically // 세로 중앙 정렬.
            ) {
                Text(text = "증감액", style = MaterialTheme.typography.bodyMedium) // 왼쪽 라벨.
                Text( // 오른쪽 값(증감액) 표시.
                    text = "${if (diff > 0) "+" else ""}${formatCurrency(diff)}", // diff가 양수면 +를 붙이고 통화 포맷으로 표시.
                    style = MaterialTheme.typography.titleMedium, // 값이 중요하니 조금 더 큰 스타일.
                    color = diffColor, // 양/음에 따라 색상 적용(직관성).
                    fontWeight = FontWeight.Bold // 굵게 표시해 강조.
                )
            }
        }
    }
}

@Composable
fun AddStockDialog(onDismiss: () -> Unit, onAdd: (String, Double, Double) -> Unit) { // 닫기 콜백과 “추가 저장” 콜백을 받음.
    var name by remember { mutableStateOf("") } // 입력 중인 종목명(UI 상태). remember로 리컴포지션에도 값 유지.
    var ratio by remember { mutableStateOf("") } // 입력 중인 목표비중(UI 상태).
    var value by remember { mutableStateOf("") } // 입력 중인 현재평가액(UI 상태).

    AlertDialog( // Material3 제공 다이얼로그 컴포넌트.
        onDismissRequest = onDismiss, // 바깥 클릭/뒤로가기 등으로 닫힐 때 실행할 처리(부모 상태를 false로 바꾸는 등).
        title = { Text("종목 추가") }, // 다이얼로그 제목.
        text = { // 다이얼로그 본문(입력 폼).
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { // 입력칸들을 세로로 쌓고, 각 칸 간격 8dp.
                OutlinedTextField( // 종목명 입력 필드(테두리 있는 텍스트필드).
                    value = name, // 현재 입력값을 name 상태에서 읽음.
                    onValueChange = { name = it }, // 입력이 바뀔 때마다 name 상태를 갱신(Compose 단방향 데이터 흐름).
                    label = { Text("주식 이름") }, // 필드 라벨.
                    singleLine = true, // 한 줄 입력만 허용(엔터로 줄바꿈 방지).
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text) // 텍스트 키보드(한글 입력 포함) 유도.
                )

                OutlinedTextField( // 목표 비중 입력 필드.
                    value = ratio, // 현재 입력값.
                    onValueChange = { ratio = it }, // 입력 변화 시 ratio 상태 갱신.
                    label = { Text("목표 비중 (%)") }, // 라벨.
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), // 숫자 키보드 유도.
                    singleLine = true // 한 줄 입력.
                )

                OutlinedTextField( // 현재 평가액 입력 필드.
                    value = value, // 현재 입력값.
                    onValueChange = { value = it }, // 입력 변화 시 value 상태 갱신.
                    label = { Text("현재 평가액 (₩)") }, // 라벨.
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), // 숫자 키보드 유도.
                    singleLine = true // 한 줄 입력.
                )
            }
        },
        confirmButton = { // 확인(저장) 버튼 영역.
            Button( // Material3 버튼.
                onClick = { // 버튼 클릭 시 실행될 로직.
                    val r = ratio.replace(",", "").toDoubleOrNull() // ratio 문자열을 숫자로 변환. 쉼표가 있어도 변환되게 제거 후 안전 변환.
                    val v = value.replace(",", "").toDoubleOrNull() // value 문자열을 숫자로 변환. 실패하면 null.
                    if (name.isNotBlank() && r != null && v != null) onAdd(name, r, v) // 값이 정상일 때만 onAdd 호출(부모가 addStock 실행).
                },
                enabled = name.isNotBlank() && ratio.isNotBlank() && value.isNotBlank() // 입력이 비어있으면 버튼 비활성화(기본적인 UX).
            ) { Text("저장") } // 버튼 안 텍스트.
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } } // 취소 버튼: 눌렀을 때 onDismiss 호출로 닫기.
    )
}

@Composable
fun EditStockDialog( // 기존 종목을 수정하기 위한 다이얼로그 함수.
    stock: Stock, // 수정 대상 Stock(기존 값이 초기값으로 들어가야 함).
    onDismiss: () -> Unit, // 닫기(취소/바깥 클릭) 콜백.
    onSave: (String, Double, Double) -> Unit // 저장 콜백(수정한 name/ratio/value를 부모에게 전달).
) { // EditStockDialog 본문.
    var name by remember { mutableStateOf(stock.name) } // 종목명 입력 상태를 기존 stock.name으로 초기화.
    var ratio by remember { mutableStateOf(stock.targetRatio.toString()) } // 목표비중 입력 상태를 기존 값 문자열로 초기화.
    var value by remember { mutableStateOf(stock.currentValue.toString()) } // 평가액 입력 상태를 기존 값 문자열로 초기화.


    AlertDialog( // 수정 다이얼로그 UI.
        onDismissRequest = onDismiss, // 바깥 클릭/뒤로가기 등으로 닫을 때 처리.
        title = { Text("종목 수정") }, // 다이얼로그 제목.
        text = { // 다이얼로그 본문(입력 폼).
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { // 입력칸들을 세로로 쌓고 간격 8dp.
                OutlinedTextField( // 종목명 수정 입력 필드.
                    value = name, // 현재 입력값.
                    onValueChange = { name = it }, // 입력 변화 시 name 상태 갱신.
                    label = { Text("주식 이름") }, // 라벨.
                    singleLine = true, // 한 줄 입력.
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text) // 텍스트 키보드(한글 가능).
                )

                OutlinedTextField( // 목표 비중 수정 입력 필드.
                    value = ratio, // 현재 입력값.
                    onValueChange = { ratio = it }, // 입력 변화 시 ratio 상태 갱신.
                    label = { Text("목표 비중 (%)") }, // 라벨.
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), // 숫자 키보드.
                    singleLine = true // 한 줄 입력.
                )

                OutlinedTextField( // 평가액 수정 입력 필드.
                    value = value, // 현재 입력값.
                    onValueChange = { value = it }, // 입력 변화 시 value 상태 갱신.
                    label = { Text("현재 평가액 (₩)") }, // 라벨.
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), // 숫자 키보드.
                    singleLine = true // 한 줄 입력.
                )
            }
        },
        confirmButton = { // 저장 버튼 영역.
            Button( // 저장 버튼.
                onClick = { // 클릭 시 실행.
                    val r = ratio.replace(",", "").toDoubleOrNull() // ratio를 숫자로 안전 변환(쉼표 제거 후).
                    val v = value.replace(",", "").toDoubleOrNull() // value를 숫자로 안전 변환.
                    if (name.isNotBlank() && r != null && v != null) onSave(name, r, v) // 정상 값일 때만 저장 콜백 호출(부모가 updateStock 실행).
                }
            ) { Text("저장") } // 버튼 텍스트.
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } } // 취소 버튼: 눌러서 닫기.
    )
}
