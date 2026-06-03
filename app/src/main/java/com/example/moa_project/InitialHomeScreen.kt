package com.example.moa_project

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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import com.example.moa_project.ui.components.MeetingActionCard
import com.example.moa_project.ui.components.MoaBodyText
import com.example.moa_project.ui.components.MoaBottomNavigationBar
import com.example.moa_project.ui.components.MoaMascot
import com.example.moa_project.ui.components.MoaCaptionText
import com.example.moa_project.ui.components.MoaLabelText
import com.example.moa_project.ui.components.MoaTitleText
import com.example.moa_project.ui.theme.MoaBlue
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
import androidx.core.graphics.toColorInt
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InitialHomeScreen(
    currentRoute: String = "home",
    onNavigate: (String) -> Unit = {},
    userViewModel: UserViewModel = viewModel(),
    dashboardViewModel: HomeDashboardViewModel = viewModel(),
    meetingsViewModel: MeetingsViewModel = viewModel(),
) {
    val uiState by userViewModel.uiState.collectAsState()
    val dashboardState by dashboardViewModel.state.collectAsState()
    val user = (uiState as? UserState.Success)?.user
    val profileImageUrl = user?.profileImageUrl

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
            HomeHeaderRow()
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
private fun HomeHeaderRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MoaMascot(size = 40.dp)
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "MOA",
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            color = MoaBlue,
        )
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
            icon = {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "생성하기",
                    modifier = Modifier.size(24.dp),
                )
            },
            mascotSize = 76.dp,
            onClick = onCreateClick,
        )

        Spacer(modifier = Modifier.height(20.dp))

        MeetingActionCard(
            titlePrefix = "모임",
            titleSuffix = " 입장하기",
            description = "초대코드나 링크로\n모임에 참여해요",
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "입장하기",
                    modifier = Modifier.size(24.dp),
                )
            },
            mascotSize = 76.dp,
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
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    HomeSummaryCard(
                        title = "조율 중",
                        value = if (state.pendingCoordinationCount > 0) {
                            "${state.pendingCoordinationCount}건"
                        } else {
                            "없음"
                        },
                        icon = Icons.Default.Schedule,
                        iconColor = com.example.moa_project.ui.theme.MoaStatusAdjusting,
                        onClick = onMeetingsClick,
                        modifier = Modifier.weight(1f),
                    )
                    HomeSummaryCard(
                        title = "이번 주 확정",
                        value = "${state.confirmedThisWeekCount}건",
                        icon = Icons.Default.Event,
                        iconColor = com.example.moa_project.ui.theme.MoaStatusConfirmed,
                        onClick = onCalendarClick,
                        modifier = Modifier.weight(1f),
                    )
                }
                DashboardSectionCard(title = "다가오는 일정") {
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
    content: @Composable () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().moaCard(padding = 16.dp)) {
        MoaTitleText(text = title, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(14.dp))
        content()
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
            .height(100.dp)
            .shadow(4.dp, RoundedCornerShape(MoaRadius.card), spotColor = Color(0x14000000))
            .clip(RoundedCornerShape(MoaRadius.card))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
            Icon(icon, contentDescription = title, tint = iconColor, modifier = Modifier.size(22.dp))
            Column {
                MoaTitleText(text = value, fontSize = 22.sp)
                Spacer(modifier = Modifier.height(2.dp))
                MoaCaptionText(text = title)
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
            MoaBodyText(
                text = event.title,
                fontWeight = FontWeight.Medium,
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
                    color = Color(0xFF737C99),
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
