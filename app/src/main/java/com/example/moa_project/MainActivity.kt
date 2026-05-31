package com.example.moa_project

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import com.example.moa_project.ui.calendar.CalendarScreen
import com.example.moa_project.ui.meetings.MeetingsScreen
import com.example.moa_project.ui.my.MyPageScreen
import com.example.moa_project.ui.splash.MoaSplashScreen
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.moa_project.ui.login.LoginScreen
import com.example.moa_project.ui.login.SignUpScreen
import com.example.moa_project.ui.my.EditProfileScreen
import com.example.moa_project.ui.schedule.GroupScheduleResultScreen
import com.example.moa_project.ui.schedule.ScheduleCoordinationScreen
import com.example.moa_project.ui.schedule.ScheduleResultScreen
import com.example.moa_project.ui.schedule.ScheduleState
import com.example.moa_project.ui.schedule.ScheduleViewModel
import com.example.moa_project.ui.schedule.TimeSlot
import com.example.moa_project.ui.meetings.GroupDetailScreen
import com.example.moa_project.ui.theme.Moa_ProjectTheme
import java.time.LocalDate
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.graphics.Color
import androidx.navigation.navDeepLink
import androidx.compose.ui.platform.LocalContext
import com.example.moa_project.util.BusyTimeHelper
import com.example.moa_project.network.TokenManager

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()
        val scheduleLink = intent?.data?.takeIf {
            it.scheme == "moa" && it.host == "schedule"
        }?.lastPathSegment
        setContent {
            Moa_ProjectTheme {
                MainScreen(initialScheduleLink = scheduleLink)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@Composable
fun MainScreen(initialScheduleLink: String? = null) {
    val navController = rememberNavController()
    // 현재 네비게이션 상태를 가져옴
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"
    val navigateBottomBar: (String) -> Unit = { route ->
        if (route != currentRoute) {
            if (route == "home") {
                val poppedToHome = navController.popBackStack("home", inclusive = false)
                if (!poppedToHome) {
                    navController.navigate("home") {
                        launchSingleTop = true
                    }
                }
            } else {
                navController.navigate(route) {
                    popUpTo("home") { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    }

    // 실제 화면 이동 및 구조 정의
    NavHost(
        navController = navController,
        startDestination = "splash",
        modifier = Modifier.fillMaxSize()
    ) {
        composable("splash") {
            MoaSplashScreen(
                onSplashFinished = {
                    val destination = if (TokenManager.isLoggedIn()) "home" else "login"
                    if (initialScheduleLink != null) {
                        navController.navigate("schedule_coordination/$initialScheduleLink") {
                            popUpTo("splash") { inclusive = true }
                            launchSingleTop = true
                        }
                    } else {
                        navController.navigate(destination) {
                            popUpTo("splash") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        // 로그인 화면
        composable("login") {
            LoginScreen(
                onLoginClick = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate("signup")
                }
            )
        }

        composable("signup") {
            SignUpScreen(
                onBackClick = { navController.popBackStack() },
                onSignUpSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // 홈 화면 (초기 화면)
        composable("home") {
            InitialHomeScreen(
                currentRoute = currentRoute,
                onNavigate = navigateBottomBar,
                onCreateMeetingClick = { navigateBottomBar("meetings") },
                onJoinMeetingClick = { navigateBottomBar("meetings") },
                onGuestScheduleResultClick = { link ->
                    navController.navigate("schedule_result/$link")
                },
            )
        }
        
        // 캘린더 화면 (구현됨)
        composable("calendar") {
            CalendarScreen(currentRoute = currentRoute, onNavigate = navigateBottomBar)
        }
        
        // 모임 화면
        composable("meetings") {
            MeetingsScreen(
                currentRoute = currentRoute,
                onNavigate = navigateBottomBar,
                onMeetingClick = { groupId ->
                    navController.navigate("group_detail/$groupId")
                },
                onGuestScheduleResultClick = { link ->
                    navController.navigate("schedule_result/$link")
                },
                onNeedsReLogin = {
                    TokenManager.clear()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // 그룹 상세 화면
        composable("group_detail/{groupId}") { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")?.toLongOrNull() ?: 0L
            GroupDetailScreen(
                groupId = groupId,
                onBackClick = { navController.popBackStack() },
                onScheduleClick = { scheduleId ->
                    // CONFIRMED/DONE → 결과 화면
                    navController.navigate("schedule_result_group/$scheduleId")
                },
                onCoordinateClick = { scheduleId ->
                    // WAITING/ADJUSTING → 조율(시간 선택) 화면
                    navController.navigate("schedule_coordination_group/$scheduleId")
                },
                onCoordinateScheduleCreated = { scheduleId ->
                    navController.navigate("schedule_coordination_group/$scheduleId")
                }
            )
        }
        
        // 마이 화면
        composable("my") {
            MyPageScreen(
                currentRoute = currentRoute,
                onNavigate = navigateBottomBar,
                onEditProfileClick = {
                    navController.navigate("edit_profile")
                },
                onLogoutClick = {
                    TokenManager.clear()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable("edit_profile") {
            EditProfileScreen(
                onBackClick = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }

        // 일정 조율 시간 선택 화면 (link 파라미터로 연동)
        composable(
            route = "schedule_coordination/{link}",
            deepLinks = listOf(
                navDeepLink { uriPattern = "moa://schedule/{link}" }
            )
        ) { backStackEntry ->
            val link = backStackEntry.arguments?.getString("link") ?: "DEMO"
            GuestScheduleCoordinationRoute(
                link = link,
                onBackClick = { navController.popBackStack() },
                onSubmitSuccess = {
                    navController.navigate("schedule_result/$link") {
                        launchSingleTop = true
                    }
                }
            )
        }

        // 기본 route (파라미터 없이 접근 시 DEMO 링크로)
        composable("schedule_coordination") {
            ScheduleCoordinationScreen(
                onBackClick = { navController.popBackStack() },
                onSubmitClick = { guestName, slots ->
                    navController.navigate("schedule_result/DEMO")
                }
            )
        }

        composable("schedule_coordination_group/{scheduleId}") { backStackEntry ->
            val scheduleId = backStackEntry.arguments?.getString("scheduleId")?.toLongOrNull() ?: 0L
            GroupScheduleCoordinationRoute(
                scheduleId = scheduleId,
                onBackClick = { navController.popBackStack() },
                onSubmitSuccess = {
                    navController.navigate("schedule_result_group/$scheduleId") {
                        launchSingleTop = true
                    }
                }
            )
        }

        // 조율 결과 (히트맵/추천) 화면
        composable("schedule_result/{link}") { backStackEntry ->
            val link = backStackEntry.arguments?.getString("link") ?: "DEMO"
            ScheduleResultScreen(
                uniqueLink = link,
                onBackClick = { navController.popBackStack() },
                onConfirmClick = {
                    navController.navigate("calendar") {
                        popUpTo("home") { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }

        // 기본 route (파라미터 없이)
        composable("schedule_result") {
            ScheduleResultScreen(
                onBackClick = { navController.popBackStack() },
                onConfirmClick = {
                    navController.navigate("calendar") {
                        popUpTo("home") { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable("schedule_result_group/{scheduleId}") { backStackEntry ->
            val scheduleId = backStackEntry.arguments?.getString("scheduleId")?.toLongOrNull() ?: 0L
            GroupScheduleResultScreen(
                scheduleId = scheduleId,
                onBackClick = { navController.popBackStack() },
                onConfirmComplete = {
                    navController.navigate("calendar") {
                        popUpTo("home") { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

@Composable
private fun GroupScheduleCoordinationRoute(
    scheduleId: Long,
    onBackClick: () -> Unit,
    onSubmitSuccess: () -> Unit,
    viewModel: ScheduleViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = ScheduleViewModel.Factory(scheduleId),
        key = "schedule_coordination_$scheduleId"
    )
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var blockedSlots by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<Set<TimeSlot>>(emptySet()) }

    androidx.compose.runtime.LaunchedEffect(scheduleId) {
        viewModel.fetchDetail()
    }

    val schedule = (uiState as? ScheduleState.DetailSuccess)?.schedule
    val startDate = schedule?.startDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        ?: LocalDate.now()
    val endDate = schedule?.endDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        ?: startDate.plusDays(5)

    androidx.compose.runtime.LaunchedEffect(startDate, endDate) {
        blockedSlots = runCatching {
            BusyTimeHelper.loadBlockedSlots(context, startDate, endDate)
        }.getOrDefault(emptySet())
    }

    ScheduleCoordinationScreen(
        scheduleTitle = schedule?.title ?: "일정 조율",
        startDate = startDate,
        endDate = endDate,
        isGuest = false,
        blockedSlots = blockedSlots,
        onBackClick = onBackClick,
        onSubmitClick = { _, slots ->
            viewModel.submitTimeSlots(slots, onSubmitSuccess)
        }
    )
}

@Composable
private fun GuestScheduleCoordinationRoute(
    link: String,
    onBackClick: () -> Unit,
    onSubmitSuccess: () -> Unit,
    viewModel: com.example.moa_project.ui.schedule.GuestScheduleViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    androidx.compose.runtime.LaunchedEffect(link) {
        viewModel.fetchSchedule(link)
    }

    when (val state = uiState) {
        is com.example.moa_project.ui.schedule.GuestScheduleState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF2179FE)
                )
            }
        }
        is com.example.moa_project.ui.schedule.GuestScheduleState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = state.message)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.fetchSchedule(link) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2179FE)
                        )
                    ) {
                        Text(text = "재시도")
                    }
                }
            }
        }
        is com.example.moa_project.ui.schedule.GuestScheduleState.Success -> {
            val schedule = state.schedule
            val startDate = schedule.startDate.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: LocalDate.now()
            val endDate = schedule.endDate.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: startDate.plusDays(5)

            ScheduleCoordinationScreen(
                scheduleTitle = schedule.title,
                startDate = startDate,
                endDate = endDate,
                isGuest = true,
                onBackClick = onBackClick,
                onSubmitClick = { guestName, slots ->
                    viewModel.submitTimeSlots(link, guestName, slots)
                }
            )
        }
        is com.example.moa_project.ui.schedule.GuestScheduleState.SubmitSuccess -> {
            androidx.compose.runtime.LaunchedEffect(state) {
                onSubmitSuccess()
            }
        }
        else -> {
            // Idle or other states
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF2179FE)
                )
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
