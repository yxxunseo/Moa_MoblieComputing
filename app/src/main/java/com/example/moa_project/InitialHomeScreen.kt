package com.example.moa_project

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import com.example.moa_project.ui.components.MeetingActionCard
import com.example.moa_project.ui.components.MoaBodyText
import com.example.moa_project.ui.components.MoaBottomNavigationBar
import com.example.moa_project.ui.components.MoaMascot
import com.example.moa_project.ui.components.MoaLabelText
import com.example.moa_project.ui.components.MoaTitleText
import com.example.moa_project.ui.theme.MoaBlue
import com.example.moa_project.ui.theme.MoaAccentBlue
import com.example.moa_project.ui.theme.MoaAccentBlueBg
import com.example.moa_project.ui.theme.MoaAccentPurple
import com.example.moa_project.ui.theme.MoaAccentPurpleBg
import com.example.moa_project.ui.theme.MoaAccentGreen
import com.example.moa_project.ui.theme.MoaAccentGreenBg
import com.example.moa_project.ui.theme.MoaAccentOrange
import com.example.moa_project.ui.theme.MoaAccentOrangeBg
import com.example.moa_project.ui.theme.SBAggroFontFamily
import com.example.moa_project.ui.theme.MoaRadius
import com.example.moa_project.ui.theme.MoaScreenBackground
import com.example.moa_project.ui.theme.MoaSpacing
import com.example.moa_project.ui.theme.MoaTextPrimary
import com.example.moa_project.ui.theme.MoaTextSecondary
import com.example.moa_project.ui.theme.moaCard
import com.example.moa_project.ui.home.HomeDashboardState
import com.example.moa_project.ui.home.HomeDashboardViewModel
import com.example.moa_project.ui.home.WeeklyTimetableDashboardCard
import com.example.moa_project.ui.home.HomeActivityItem
import com.example.moa_project.ui.home.HomeEventItem
import com.example.moa_project.ui.meetings.CreateOrJoinMeetingSheet
import com.example.moa_project.ui.meetings.MeetingsViewModel
import com.example.moa_project.ui.my.UserState
import com.example.moa_project.ui.my.UserViewModel
import com.example.moa_project.util.MoaInAppNotificationStore
import androidx.core.graphics.toColorInt
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InitialHomeScreen(
    currentRoute: String = "home",
    onNavigate: (String) -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    userViewModel: UserViewModel = viewModel(),
    dashboardViewModel: HomeDashboardViewModel = viewModel(),
    meetingsViewModel: MeetingsViewModel = viewModel(),
) {
    val context = LocalContext.current
    val uiState by userViewModel.uiState.collectAsState()
    val dashboardState by dashboardViewModel.state.collectAsState()
    val user = (uiState as? UserState.Success)?.user
    val profileImageUrl = user?.profileImageUrl
    val userNickname = user?.nickname
    var unreadCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(currentRoute) {
        if (currentRoute == "home") {
            dashboardViewModel.refresh()
            unreadCount = MoaInAppNotificationStore.unreadCount(context)
        }
    }

    val hasGroups = when (val state = dashboardState) {
        is HomeDashboardState.Success -> state.groups.isNotEmpty()
        else -> false
    }
    val isDashboardLoading = dashboardState is HomeDashboardState.Loading

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showSheet by remember { mutableStateOf(false) }
    var sheetInitialTab by remember { mutableIntStateOf(0) }

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
            MoaBottomNavigationBar(
                currentRoute = currentRoute,
                profileImageUrl = profileImageUrl,
                onNavigate = onNavigate,
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MoaScreenBackground)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = MoaSpacing.screen, vertical = 24.dp),
            verticalArrangement = Arrangement.Top,
        ) {
            HomeHeaderRow(
                hasUnread = unreadCount > 0,
                onNotificationsClick = onNotificationsClick,
            )
            Spacer(modifier = Modifier.height(16.dp))
            HomeHeroBanner(nickname = userNickname)
            Spacer(modifier = Modifier.height(20.dp))

            if (isDashboardLoading && !hasGroups) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator(color = MoaBlue)
                }
            } else if (hasGroups) {
                HomeDashboardSection(
                    state = dashboardState,
                    onCalendarClick = { onNavigate("calendar") },
                    onMeetingsClick = { onNavigate("meetings") },
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
                )

                val weeklyData = (dashboardState as? HomeDashboardState.Success)?.weeklyTimetable
                if (weeklyData != null) {
                    Spacer(modifier = Modifier.height(20.dp))
                    WeeklyTimetableDashboardCard(
                        data = weeklyData,
                        isLoading = false,
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeHeroBanner(nickname: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFEAF1FF), Color(0xFFEFEBFF), Color(0xFFE3F7EE)),
                )
            )
            .padding(horizontal = 22.dp, vertical = 22.dp),
    ) {
        Column {
            Text(
                text = if (!nickname.isNullOrBlank()) {
                    "$nickname 님,\n일정을 함께 맞춰볼까요?"
                } else {
                    "함께 맞추는 일정,\nMOA"
                },
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                lineHeight = 30.sp,
                color = MoaTextPrimary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            MoaBodyText(
                text = "모임·단기 일정을 한곳에서 조율하고 확정해요",
                fontSize = 13.sp,
                color = MoaTextSecondary,
            )
        }
    }
}

@Composable
private fun HomeHeaderRow(
    hasUnread: Boolean,
    onNotificationsClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MoaMascot(size = 40.dp)
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            modifier = Modifier.weight(1f),
            text = "MOA",
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = MoaBlue,
        )
        NotificationBell(hasUnread = hasUnread, onClick = onNotificationsClick)
    }
}

@Composable
private fun NotificationBell(hasUnread: Boolean, onClick: () -> Unit) {
    Box {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = "알림",
                tint = MoaTextPrimary,
                modifier = Modifier.size(24.dp),
            )
        }
        if (hasUnread) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(com.example.moa_project.ui.theme.MoaAccentRed),
            )
        }
    }
}

@Composable
private fun OnboardingActionSection(
    onCreateClick: () -> Unit,
    onJoinClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        MeetingActionCard(
            titlePrefix = "모임",
            titleSuffix = " 생성하기",
            description = "새로운 모임을 만들고\n일정을 함께 조율해요",
            mascotSize = 76.dp,
            containerColor = MoaAccentBlueBg,
            titleColor = MoaAccentBlue,
            onClick = onCreateClick,
        )

        Spacer(modifier = Modifier.height(20.dp))

        MeetingActionCard(
            titlePrefix = "모임",
            titleSuffix = " 입장하기",
            description = "초대코드나 링크로\n모임에 참여해요",
            mascotSize = 76.dp,
            containerColor = MoaAccentPurpleBg,
            titleColor = MoaAccentPurple,
            onClick = onJoinClick,
        )
    }
}

@Composable
private fun HomeDashboardSection(
    state: HomeDashboardState,
    onCalendarClick: () -> Unit,
    onMeetingsClick: () -> Unit,
) {
    val weeklyData = (state as? HomeDashboardState.Success)?.weeklyTimetable
    val weeklyLoading = state is HomeDashboardState.Loading
    val success = state as? HomeDashboardState.Success

    Column(verticalArrangement = Arrangement.spacedBy(MoaSpacing.item)) {
        MoaTitleText(text = "한눈에 보기", fontSize = 17.sp)

        when (state) {
            is HomeDashboardState.Loading -> DashboardMessage("일정 정보를 불러오는 중이에요")
            is HomeDashboardState.Error -> DashboardMessage(state.message)
            is HomeDashboardState.Success -> {
                HomeSummaryTwoLine(
                    pendingCount = state.pendingCoordinationCount,
                    confirmedCount = state.confirmedThisWeekCount,
                    onConfirmedClick = onCalendarClick,
                    onPendingClick = onMeetingsClick,
                )
                DashboardSectionCard(title = "다가오는 일정", accent = MoaAccentPurple) {
                    if (state.upcomingEvents.isEmpty()) {
                        DashboardMessage("확정된 일정이 없어요.\n모임·캘린더에서 일정을 확정하면 여기에 표시돼요.")
                    } else {
                        state.upcomingEvents.forEachIndexed { index, event ->
                            UpcomingEventRow(event)
                            if (index != state.upcomingEvents.lastIndex) {
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }
            }
        }

        WeeklyTimetableDashboardCard(
            data = weeklyData,
            isLoading = weeklyLoading && success == null,
        )
    }
}

@Composable
private fun DashboardSectionCard(
    title: String,
    accent: Color = MoaAccentBlue,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(20.dp), spotColor = Color(0x14101B33))
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(accent),
            )
            Spacer(modifier = Modifier.width(8.dp))
            MoaTitleText(text = title, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(14.dp))
        content()
    }
}

@Composable
private fun HomeSummaryTwoLine(
    pendingCount: Int,
    confirmedCount: Int,
    onConfirmedClick: () -> Unit,
    onPendingClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SummaryStatCard(
            modifier = Modifier.weight(1f),
            label = "이번 주 확정",
            count = confirmedCount,
            accent = MoaAccentGreen,
            accentBg = MoaAccentGreenBg,
            onClick = onConfirmedClick,
        )
        SummaryStatCard(
            modifier = Modifier.weight(1f),
            label = "조율 중",
            count = pendingCount,
            accent = MoaAccentOrange,
            accentBg = MoaAccentOrangeBg,
            onClick = onPendingClick,
        )
    }
}

@Composable
private fun SummaryStatCard(
    modifier: Modifier = Modifier,
    label: String,
    count: Int,
    accent: Color,
    accentBg: Color,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(accentBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 18.dp),
    ) {
        MoaBodyText(text = label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = accent)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "${count}건",
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            color = accent,
        )
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
            MoaLabelText(
                text = activity.scheduleTitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            MoaBodyText(
                text = "${activity.groupName} · ${activity.statusLabel}",
                fontSize = 13.sp,
                color = MoaTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun UpcomingEventRow(event: HomeEventItem) {
    val formatter = DateTimeFormatter.ofPattern("M월 d일 HH:mm")
    val accent = parseColor(event.color)
    val accentBg = accent.copy(alpha = 0.12f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(accentBg)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(accent.copy(alpha = 0.22f)),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(accent),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            MoaBodyText(
                text = event.title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF101B33),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            MoaBodyText(
                text = event.start.format(formatter),
                fontSize = 12.sp,
                color = Color(0xFF737C99),
            )
            event.subtitle?.let { sub ->
                Spacer(modifier = Modifier.height(2.dp))
                MoaBodyText(
                    text = sub,
                    fontSize = 11.sp,
                    color = accent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun DashboardMessage(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MoaMascot(size = 48.dp)
        Spacer(modifier = Modifier.width(12.dp))
        MoaBodyText(text = text, fontSize = 13.sp, color = MoaTextSecondary)
    }
}

private fun parseColor(hex: String): Color {
    return runCatching { Color(hex.toColorInt()) }.getOrDefault(Color(0xFF2179FE))
}
