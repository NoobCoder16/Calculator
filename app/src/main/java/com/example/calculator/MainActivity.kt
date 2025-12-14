package com.example.stockcalculator.com.example.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.stockcalculator.ui.theme.StockCalculatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val settings by settingsViewModel.settings.collectAsState()

            StockCalculatorTheme(
                darkTheme = settings.isDarkMode,
                dynamicColor = false,
                fontSizeScale = settings.fontSizeScale
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    // 뷰모델 생성 (앱 전체에서 공유)
    val calculatorViewModel: CalculatorViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()

    // 하단 네비게이션 아이템 목록
    val items = listOf(
        BottomNavItem("계산기", "calculator", androidx.compose.material.icons.Icons.Default.Home),
        BottomNavItem("포트폴리오", "portfolio", androidx.compose.material.icons.Icons.Default.Info),
        BottomNavItem("프리셋", "presets", androidx.compose.material.icons.Icons.Default.Bookmarks),
        BottomNavItem("캘린더", "calendar", androidx.compose.material.icons.Icons.Default.DateRange)
    )

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            // 설정 화면과 홈 화면에서는 하단바 숨김 (나머지 화면에선 보임)
            if (currentRoute != "settings" && currentRoute != "home") {
                NavigationBar {
                    items.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            // 1. 홈 화면 (인트로)
            composable("home") { HomeScreen(navController) }

            // 2. 메인 계산기 화면
            composable("calculator") {
                CalculatorScreen(
                    viewModel = calculatorViewModel,
                    onSettingsClick = { navController.navigate("settings") }
                )
            }

            // 3. 포트폴리오 분석 화면
            composable("portfolio") { PortfolioScreen(viewModel = calculatorViewModel) }

            // 4. 설정 화면
            composable("settings") {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // 5. 캘린더 화면 (수정됨: viewModel 전달!)
            composable("calendar") {
                CalendarScreen(
                    viewModel = calculatorViewModel, // 👈 뷰모델 전달 (데이터 저장용)
                    onBackClick = {
                        navController.navigate("calculator") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            // 6. 프리셋 화면
            composable("presets") {
                PresetScreen(
                    viewModel = calculatorViewModel,
                    onBackClick = {
                        navController.navigate("calculator") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}

data class BottomNavItem(val title: String, val route: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)