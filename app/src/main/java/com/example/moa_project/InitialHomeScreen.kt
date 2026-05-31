package com.example.moa_project

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import com.example.moa_project.ui.components.MeetingActionCard
import com.example.moa_project.ui.components.MoaBottomNavigationBar
import com.example.moa_project.ui.home.HomeDashboardState
import com.example.moa_project.ui.home.HomeDashboardViewModel
import com.example.moa_project.ui.home.HomeActivityItem
import com.example.moa_project.ui.home.HomeEventItem
import com.example.moa_project.ui.meetings.CreateGuestScheduleSheet
import com.example.moa_project.ui.meetings.CreateOrJoinMeetingSheet
import com.example.moa_project.ui.meetings.GuestScheduleListViewModel
import com.example.moa_project.ui.meetings.MyGuestSchedulesSection
import com.example.moa_project.ui.meetings.MeetingsViewModel
import com.example.moa_project.ui.my.UserState
import com.example.moa_project.ui.my.UserViewModel
import com.example.moa_project.ui.theme.SBAggroFontFamily
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InitialHomeScreen(
    currentRoute: String = "home",
    onNavigate: (String) -> Unit = {},
    onCreateMeetingClick: () -> Unit = {},
    onJoinMeetingClick: () -> Unit = {},
    onGuestScheduleResultClick: (String) -> Unit = {},
    userViewModel: UserViewModel = viewModel(),
    dashboardViewModel: HomeDashboardViewModel = viewModel(),
    meetingsViewModel: MeetingsViewModel = viewModel(),
    guestListViewModel: GuestScheduleListViewModel = viewModel(),
) {
    val uiState by userViewModel.uiState.collectAsState()
    val dashboardState by dashboardViewModel.state.collectAsState()
    val userName = if (uiState is UserState.Success) {
        (uiState as UserState.Success).user.nickname
    } else {
        "사용자"
    }

    LaunchedEffect(currentRoute) {
        if (currentRoute == "home") {
            dashboardViewModel.refresh()
        }
    }

    val hasGroups = when (val state = dashboardState) {
        is HomeDashboardState.Success -> state.groups.isNotEmpty()
        else -> false
    }
    val isDashboardLoading = dashboardState is HomeDashboardState.Loading

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val guestSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showSheet by remember { mutableStateOf(false) }
    var showGuestSheet by remember { mutableStateOf(false) }
    // 0 = 만들기 탭, 1 = 입장 탭
    var sheetInitialTab by remember { mutableStateOf(0) }

    if (showGuestSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showGuestSheet = false
                guestListViewModel.fetchMySchedules()
            },
            sheetState = guestSheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        ) {
            CreateGuestScheduleSheet(
                onDismiss = {
                    scope.launch { guestSheetState.hide() }.invokeOnCompletion {
                        showGuestSheet = false
                        guestListViewModel.fetchMySchedules()
                    }
                },
                onViewResult = { link ->
                    scope.launch { guestSheetState.hide() }.invokeOnCompletion {
                        showGuestSheet = false
                        guestListViewModel.fetchMySchedules()
                        onGuestScheduleResultClick(link)
                    }
                },
            )
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            CreateOrJoinMeetingSheet(
                initialTab = sheetInitialTab,
                onDismiss = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion { showSheet = false }
                },
                onSuccess = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showSheet = false
                        meetingsViewModel.fetchMyGroups()
                        dashboardViewModel.refresh()
                        onNavigate("meetings")
                    }
                }
            )
        }
    }

    Scaffold(
        bottomBar = {
            // 하단 네비게이션 바 부착 (추후 프로필 이미지 전달 가능)
            com.example.moa_project.ui.components.MoaBottomNavigationBar(
                currentRoute = currentRoute,
                profileImageResId = null, // 기본값 ic_character 사용
                onNavigate = onNavigate
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F8FC))
                .padding(innerPadding) // 하단 바 영역만큼의 여백을 확보
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 26.dp),
            verticalArrangement = Arrangement.Top
        ) {
            // 인사말 텍스트 (Medium 적용)
            Text(
                text = "안녕하세요, ${userName}님!",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = SBAggroFontFamily,
                color = Color(0xFF101B33)
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            // 슬로건 텍스트 (Light 적용, '모아' 부분만 포인트 컬러 유지)
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Light)) {
                        append("함께 시간을 ")
                    }
                    withStyle(style = SpanStyle(color = Color(0xFF2179FE), fontWeight = FontWeight.Light)) {
                        append("모아")
                    }
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Light)) {
                        append(",\n더 좋은 순간을 만들어요")
                    }
                },
                fontSize = 26.sp,
                fontFamily = SBAggroFontFamily,
                color = Color(0xFF101B33),
                lineHeight = 36.sp
            )

            Spacer(modifier = Modifier.height(if (hasGroups) 24.dp else 40.dp))

            if (hasGroups) {
                HomeDashboardSection(
                    state = dashboardState,
                    onCalendarClick = { onNavigate("calendar") },
                    onMeetingsClick = { onNavigate("meetings") },
                    onCreateGuestClick = { showGuestSheet = true },
                    onGuestResultClick = onGuestScheduleResultClick,
                    guestListViewModel = guestListViewModel,
                )
            } else if (!isDashboardLoading) {
                OnboardingActionSection(
                    onCreateClick = {
                        sheetInitialTab = 0
                        showSheet = true
                    },
                    onJoinClick = {
                        sheetInitialTab = 1
                        showSheet = true
                    },
                    onGuestScheduleClick = { showGuestSheet = true },
                )

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(18.dp), spotColor = Color(0xFFDDE4F2))
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White)
                        .padding(18.dp)
                ) {
                    MyGuestSchedulesSection(
                        onCreateClick = { showGuestSheet = true },
                        onViewResult = onGuestScheduleResultClick,
                        viewModel = guestListViewModel,
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingActionSection(
    onCreateClick: () -> Unit,
    onJoinClick: () -> Unit,
    onGuestScheduleClick: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 55.dp)
        ) {
            MeetingActionCard(
                titlePrefix = "모임",
                titleSuffix = " 생성하기",
                description = "새로운 모임을 만들고\n일정을 함께 조율해요",
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "생성하기",
                        modifier = Modifier.size(24.dp)
                    )
                },
                imageResId = R.drawable.ic_create,
                imageSize = 95.dp,
                onClick = onCreateClick
            )

            Spacer(modifier = Modifier.height(24.dp))

            MeetingActionCard(
                titlePrefix = "모임",
                titleSuffix = " 입장하기",
                description = "초대코드나 링크로\n모임에 참여해요",
                icon = {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "입장하기",
                        modifier = Modifier.size(24.dp)
                    )
                },
                imageResId = R.drawable.ic_position,
                imageSize = 120.dp,
                onClick = onJoinClick
            )

            Spacer(modifier = Modifier.height(24.dp))

            MeetingActionCard(
                titlePrefix = "단기 일정",
                titleSuffix = " 링크 만들기",
                description = "앱 없이 참여할 수 있는\n링크 일정을 만들어요",
                icon = {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "단기 일정",
                        modifier = Modifier.size(24.dp)
                    )
                },
                imageResId = R.drawable.ic_create,
                imageSize = 95.dp,
                onClick = onGuestScheduleClick
            )
        }

        Image(
            painter = painterResource(id = R.drawable.ic_character),
            contentDescription = "환영 캐릭터",
            modifier = Modifier
                .zIndex(1f)
                .size(85.dp)
                .align(Alignment.TopEnd)
                .offset(x = (-30).dp, y = -10.dp)
        )
    }
}

@Composable
private fun HomeDashboardSection(
    state: HomeDashboardState,
    onCalendarClick: () -> Unit,
    onMeetingsClick: () -> Unit,
    onCreateGuestClick: () -> Unit,
    onGuestResultClick: (String) -> Unit,
    guestListViewModel: GuestScheduleListViewModel,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val groupCount = (state as? HomeDashboardState.Success)?.groups?.size ?: 0
            val eventCount = (state as? HomeDashboardState.Success)?.upcomingEvents?.size ?: 0
            HomeSummaryCard(
                title = "참여 모임",
                value = "${groupCount}개",
                icon = Icons.Default.Person,
                iconColor = Color(0xFF35A96D),
                onClick = onMeetingsClick,
                modifier = Modifier.weight(1f)
            )
            HomeSummaryCard(
                title = "다가오는 일정",
                value = "${eventCount}개",
                icon = Icons.Default.DateRange,
                iconColor = Color(0xFF2179FE),
                onClick = onCalendarClick,
                modifier = Modifier.weight(1f)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(18.dp), spotColor = Color(0xFFDDE4F2))
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
                .padding(18.dp)
        ) {
            MyGuestSchedulesSection(
                onCreateClick = onCreateGuestClick,
                onViewResult = onGuestResultClick,
                viewModel = guestListViewModel,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
                .padding(18.dp)
        ) {
            Column {
                Text(
                    text = "모임 활동",
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF101B33)
                )
                Spacer(modifier = Modifier.height(12.dp))
                when (state) {
                    is HomeDashboardState.Loading -> DashboardMessage("모임 활동을 불러오는 중이에요")
                    is HomeDashboardState.Error -> DashboardMessage(state.message)
                    is HomeDashboardState.Success -> {
                        if (state.recentActivities.isEmpty()) {
                            DashboardMessage("진행 중인 일정 조율이 없어요")
                        } else {
                            state.recentActivities.forEachIndexed { index, activity ->
                                HomeActivityRow(activity)
                                if (index != state.recentActivities.lastIndex) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
                .padding(18.dp)
        ) {
            Column {
                Text(
                    text = "다가오는 일정",
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF101B33)
                )
                Spacer(modifier = Modifier.height(12.dp))
                when (state) {
                    is HomeDashboardState.Loading -> DashboardMessage("일정을 불러오는 중이에요")
                    is HomeDashboardState.Error -> DashboardMessage(state.message)
                    is HomeDashboardState.Success -> {
                        if (state.upcomingEvents.isEmpty()) {
                            DashboardMessage("이번 달 예정된 일정이 없어요")
                        } else {
                            state.upcomingEvents.forEachIndexed { index, event ->
                                UpcomingEventRow(event)
                                if (index != state.upcomingEvents.lastIndex) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeSummaryCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(104.dp)
            .shadow(8.dp, RoundedCornerShape(18.dp), spotColor = Color(0xFFDDE4F2))
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
            Icon(icon, contentDescription = title, tint = iconColor, modifier = Modifier.size(24.dp))
            Column {
                Text(
                    text = value,
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF101B33)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = title,
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = Color(0xFF737C99)
                )
            }
        }
    }
}

@Composable
private fun HomeActivityRow(activity: HomeActivityItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(parseColor(activity.groupColor))
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = activity.scheduleTitle,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFF101B33)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${activity.groupName} · ${activity.statusLabel}",
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = Color(0xFF737C99)
            )
        }
    }
}

@Composable
private fun UpcomingEventRow(event: HomeEventItem) {
    val formatter = DateTimeFormatter.ofPattern("M월 d일 HH:mm")
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(parseColor(event.color))
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.title,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFF101B33)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = event.start.format(formatter),
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = Color(0xFF737C99)
            )
        }
    }
}

@Composable
private fun DashboardMessage(text: String) {
    Text(
        text = text,
        fontFamily = SBAggroFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        color = Color(0xFF737C99)
    )
}

private fun parseColor(hex: String): Color {
    return runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(Color(0xFF2179FE))
}
