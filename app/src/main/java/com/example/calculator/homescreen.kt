package com.example.stockcalculator.com.example.calculator

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

// 화면 이동을 위한 경로 정의 (Next.js의 page 파일명과 비슷)
object Routes {
    const val HOME = "home"

    const val CALCULATOR = "calculator"
    const val PRESETS = "presets"
    const val CALENDAR = "calendar"
    const val PORTFOLIO = "portfolio"
}

@Composable
fun HomeScreen(navController: NavController) {
    // min-h-screen bg-background 대응
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        // mx-auto max-w-md p-6 대응 (스크롤 가능하게 설정)
        Column(
            modifier = Modifier
                .padding(24.dp)
                .verticalScroll(rememberScrollState()), // 내용이 길어지면 스크롤
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // --- 헤더 섹션 ---
            Spacer(modifier = Modifier.height(24.dp)) // mb-12 (상단 여백)

            // 로고 아이콘 박스
            Box(
                modifier = Modifier
                    .size(64.dp) // h-16 w-161111111111
                    .clip(RoundedCornerShape(16.dp)) // rounded-2xl
                    .background(MaterialTheme.colorScheme.primary), // bg-primary
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Dashboard, // LayoutDashboard 아이콘 대체
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary, // text-primary-foreground
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 타이틀 (h1)
            Text(
                text = "주식 포트폴리오",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            // 서브타이틀 (p)
            Text(
                text = "스마트한 리밸런싱 계산기",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray, // text-muted-foreground
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(48.dp)) // mb-12 간격

            // --- 시작하기 버튼 ---
            Button(
                onClick = { navController.navigate(Routes.CALCULATOR) }, // Link href="/login"
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp), // h-14
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "시작하기", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- 화면 목록 카드 ---
            // rounded-xl border bg-card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = SAMPLING_BORDER // 아래에 정의된 변수 사용 혹은 BorderStroke(1.dp, Color.LightGray)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "화면 목록",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // 메뉴 리스트 아이템들 (Link 컴포넌트 반복을 함수로 분리)

                    NavigationItem(
                        title = "메인 계산기",
                        subtitle = "포트폴리오 리밸런싱",
                        icon = Icons.Default.Calculate, // Dashboard 대체
                        iconColor = Color(0xFF6200EE), // accent color 예시
                        onClick = { navController.navigate(Routes.CALCULATOR) }
                    )

                    NavigationItem(
                        title = "프리셋 목록",
                        subtitle = "저장된 포트폴리오",
                        icon = Icons.Default.BookmarkAdded, // BookmarkCheck 대체
                        iconColor = Color(0xFF03DAC5), // chart-3 예시
                        onClick = { navController.navigate(Routes.PRESETS) }
                    )

                    NavigationItem(
                        title = "캘린더",
                        subtitle = "알람 설정",
                        icon = Icons.Default.CalendarToday, // Calendar 대체
                        iconColor = Color(0xFFFF5722), // chart-5 예시
                        onClick = { navController.navigate(Routes.CALENDAR) }
                    )

                    NavigationItem(
                        title = "보유 자산",
                        subtitle = "포트폴리오 분석",
                        icon = Icons.Default.PieChart, // PieChart 대체
                        iconColor = Color(0xFF4CAF50), // chart-1 예시
                        onClick = { navController.navigate(Routes.PORTFOLIO) }
                    )
                }
            }
        }
    }
}

@Composable
fun NavigationItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick) // Link 기능
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 아이콘 배경 (bg-primary/10)
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 텍스트 영역
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

// 테두리 스타일 정의
val SAMPLING_BORDER = BorderStroke(1.dp, Color(0xFFE0E0E0))