package com.example.stockcalculator.com.example.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.calculator.CalendarScreen
import com.example.stockcalculator.ui.theme.StockCalculatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val settings by settingsViewModel.settings.collectAsState()

            StockCalculatorTheme(
                darkTheme = settings.isDarkMode,
                dynamicColor = false, // Disable dynamic color to force our theme
                fontSizeScale = settings.fontSizeScale
            ) {
                // A surface container using the 'background' color from the theme
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
    val calculatorViewModel: CalculatorViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()
    val settings by settingsViewModel.settings.collectAsState()

    // Bottom Navigation Items
    val items = listOf(
        BottomNavItem("계산기", "calculator", androidx.compose.material.icons.Icons.Default.Home),
        BottomNavItem("포트폴리오", "portfolio", androidx.compose.material.icons.Icons.Default.Info),
        BottomNavItem("캘린더", "calendar", androidx.compose.material.icons.Icons.Default.DateRange)
    )

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            // Hide bottom bar on settings screen and homescreen
            if (currentRoute != "settings" && currentRoute != "home") {
                NavigationBar {
                    items.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
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
            composable(route = "home") {
                HomeScreen(navController = navController)
            }
            composable("calculator") {
                CalculatorScreen(
                    viewModel = calculatorViewModel,
                    onSettingsClick = { navController.navigate("settings") }
                )
            }
            composable("portfolio") {
                PortfolioScreen(viewModel = calculatorViewModel)
            }
            composable("settings") {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(route = "calendar") {
                CalendarScreen(
                    onBackClick = {
                        navController.navigate("calculator") {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
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
