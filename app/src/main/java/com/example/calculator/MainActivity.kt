package com.example.stockcalculator // <-- 본인 프로젝트 패키지명으로 유지하세요

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.stockcalculator.com.example.calculator.HomeScreen
import com.example.stockcalculator.com.example.calculator.Routes

// HomeScreen.kt 파일에 Routes 객체가 정의되어 있어야 합니다.
// 만약 빨간줄이 뜬다면 HomeScreen.kt 파일이 같은 패키지 안에 있는지 확인해주세요.

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 안드로이드 기본 테마 적용
            MaterialTheme {
                // 앱의 배경색을 설정하고 꽉 채움
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 네비게이션 컨트롤러 생성
                    val navController = rememberNavController()

                    // 네비게이션 호스트 설정 (시작 화면: Routes.HOME)
                    NavHost(navController = navController, startDestination = Routes.HOME) {

                        // 1. 시작 화면 (HomeScreen.kt에 있는 함수 호출)
                        composable(Routes.HOME) {
                            HomeScreen(navController = navController)
                        }

                        // 2. 로그인 화면 (아직 안 만들었으므로 임시 화면 표시)
                        composable(Routes.LOGIN) {
                            TempScreen(text = "로그인 화면\n(개발 예정)")
                        }

                        // 3. 메인 계산기 화면
                        composable(Routes.CALCULATOR) {
                            TempScreen(text = "메인 계산기 화면\n(개발 예정)")
                        }

                        // 4. 프리셋 화면
                        composable(Routes.PRESETS) {
                            TempScreen(text = "프리셋 목록 화면\n(개발 예정)")
                        }

                        // 5. 캘린더 화면
                        composable(Routes.CALENDAR) {
                            TempScreen(text = "캘린더 화면\n(개발 예정)")
                        }

                        // 6. 포트폴리오 화면
                        composable(Routes.PORTFOLIO) {
                            TempScreen(text = "보유 자산 화면\n(개발 예정)")
                        }
                    }
                }
            }
        }
    }
}

// 아직 개발되지 않은 화면을 위한 임시 컴포넌트 (텍스트만 보여줌)
@Composable
fun TempScreen(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = MaterialTheme.typography.headlineSmall)
    }
}