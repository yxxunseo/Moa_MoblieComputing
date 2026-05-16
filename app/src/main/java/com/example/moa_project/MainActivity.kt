package com.example.moa_project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import com.example.moa_project.ui.calendar.CalendarScreen
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.moa_project.ui.theme.Moa_ProjectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Moa_ProjectTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    // 현재 네비게이션 상태를 가져옴
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"

    // 실제 화면 이동 및 구조 정의
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = Modifier.fillMaxSize()
    ) {
        // 홈 화면 (초기 화면)
        composable("home") {
            InitialHomeScreen(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo("home") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        
        // 캘린더 화면 (구현됨)
        composable("calendar") {
            CalendarScreen(currentRoute = currentRoute) { route ->
                navController.navigate(route) {
                    popUpTo("home") { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
        
        // 그룹 화면 (임시)
        composable("group") {
            PlaceholderScreen("그룹 화면 준비중...", currentRoute) { route ->
                navController.navigate(route) {
                    popUpTo("home") { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
        
        // 마이 화면 (임시)
        composable("my") {
            PlaceholderScreen("마이(내 정보) 화면 준비중...", currentRoute) { route ->
                navController.navigate(route) {
                    popUpTo("home") { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    }
}

// 아직 구현되지 않은 빈 화면들을 위한 임시 컴포저블
@Composable
fun PlaceholderScreen(title: String, currentRoute: String, onNavigate: (String) -> Unit) {
    Scaffold(
        bottomBar = {
            com.example.moa_project.ui.components.MoaBottomNavigationBar(
                currentRoute = currentRoute,
                onNavigate = onNavigate
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(text = title)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InitialHomeScreenPreview() {
    Moa_ProjectTheme {
        InitialHomeScreen()
    }
}