package com.example.moa_project

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.VideoCall
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moa_project.ui.components.MoaBottomNavigationBar
import com.example.moa_project.ui.components.MoaMascot
import com.example.moa_project.ui.components.ProfileAvatar
import com.example.moa_project.ui.home.HomeDashboardState
import com.example.moa_project.ui.home.HomeDashboardViewModel
import com.example.moa_project.ui.meetings.CreateOrJoinMeetingSheet
import com.example.moa_project.ui.meetings.MeetingsViewModel
import com.example.moa_project.ui.my.UserState
import com.example.moa_project.ui.my.UserViewModel
import com.example.moa_project.ui.theme.MoaBlue
import com.example.moa_project.ui.theme.MoaScreenBackground
import com.example.moa_project.ui.theme.MoaTextPrimary
import com.example.moa_project.ui.theme.MoaTextSecondary
import com.example.moa_project.ui.theme.SBAggroFontFamily
import com.example.moa_project.util.MoaInAppNotificationStore
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
            userViewModel.fetchMyProfile()
            dashboardViewModel.refresh()
            unreadCount = MoaInAppNotificationStore.unreadCount(context)
        }
    }

    val success = dashboardState as? HomeDashboardState.Success
    val hasGroups = success?.groups?.isNotEmpty() == true
    val isDashboardLoading = dashboardState is HomeDashboardState.Loading

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showSheet by remember { mutableStateOf(false) }
    var sheetInitialTab by remember { mutableIntStateOf(0) }

    fun openCreateSheet() {
        sheetInitialTab = 0
        showSheet = true
    }

    fun openJoinSheet() {
        sheetInitialTab = 1
        showSheet = true
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
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
                },
            )
        }
    }

    Scaffold(
        bottomBar = {
            MoaBottomNavigationBar(
                currentRoute = currentRoute,
                profileImageUrl = profileImageUrl,
                onNavigate = onNavigate,
            )
        },
        containerColor = MoaScreenBackground,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            HomeProfileHeader(
                nickname = userNickname,
                profileImageUrl = profileImageUrl,
                hasUnread = unreadCount > 0,
                onNotificationsClick = onNotificationsClick,
            )

            Spacer(modifier = Modifier.height(20.dp))

            HomeHeroCard(
                nickname = userNickname,
                hasGroups = hasGroups,
                pendingCount = success?.pendingCoordinationCount ?: 0,
                confirmedCount = success?.confirmedThisWeekCount ?: 0,
                isLoading = isDashboardLoading,
                onPrimaryClick = {
                    if (hasGroups) onNavigate("meetings") else openCreateSheet()
                },
            )

            Spacer(modifier = Modifier.height(20.dp))

            HomeQuickAccessGrid(
                state = dashboardState,
                onCreateClick = { openCreateSheet() },
                onJoinClick = { openJoinSheet() },
                onCalendarClick = { onNavigate("calendar") },
                onMeetingsClick = { onNavigate("meetings") },
            )

            Spacer(modifier = Modifier.height(28.dp))

            HomeTaskSection(
                state = dashboardState,
                onSeeAllClick = { onNavigate("calendar") },
                onCalendarClick = { onNavigate("calendar") },
                onCreateClick = { openCreateSheet() },
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/** 상단: 프로필 + 인사 | 알림 */
@Composable
private fun HomeProfileHeader(
    nickname: String?,
    profileImageUrl: String?,
    hasUnread: Boolean,
    onNotificationsClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProfileAvatar(
            imageUrl = profileImageUrl,
            nickname = nickname,
            size = 52.dp,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Hello,",
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = MoaTextSecondary,
            )
            Text(
                text = if (!nickname.isNullOrBlank()) "${nickname}님 반가워요" else "MOA에 오신 것을 환영해요",
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = MoaTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        NotificationBell(hasUnread = hasUnread, onClick = onNotificationsClick)
    }
}

@Composable
private fun HeaderIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MoaTextPrimary,
            modifier = Modifier.size(22.dp),
        )
    }
}

/** 히어로 배너: 좌 텍스트·CTA / 우 마스코트(겹침) */
@Composable
private fun HomeHeroCard(
    nickname: String?,
    hasGroups: Boolean,
    pendingCount: Int,
    confirmedCount: Int,
    isLoading: Boolean,
    onPrimaryClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 200.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFDCE8FF), Color(0xFFE8E0FF), Color(0xFFDDEFE6)),
                ),
            ),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth(0.62f)
                .padding(start = 22.dp, top = 24.dp, bottom = 24.dp),
        ) {
            Text(
                text = if (hasGroups) {
                    "이번 주\n일정 확인"
                } else {
                    "새로운\n일정 경험"
                },
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                lineHeight = 34.sp,
                color = MoaTextPrimary,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = when {
                    isLoading -> "불러오는 중..."
                    hasGroups && pendingCount > 0 -> "조율 ${pendingCount}건 · 확정 ${confirmedCount}건"
                    hasGroups -> "확정 ${confirmedCount}건"
                    else -> "모임·단기 일정을 한곳에서"
                },
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = MoaTextSecondary,
                lineHeight = 18.sp,
            )
            Spacer(modifier = Modifier.height(18.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(22.dp))
                    .border(1.5.dp, MoaBlue, RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .clickable(onClick = onPrimaryClick)
                    .padding(horizontal = 20.dp, vertical = 11.dp),
            ) {
                Text(
                    text = if (hasGroups) "조율하러 가기" else "시작하기",
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MoaBlue,
                )
            }
        }
        MoaMascot(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 6.dp, y = 10.dp),
            size = 118.dp,
        )
    }
}

/** 2열 요약 카드: 실제 데이터 기반 */
@Composable
private fun HomeQuickAccessGrid(
    state: HomeDashboardState,
    onCreateClick: () -> Unit,
    onJoinClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onMeetingsClick: () -> Unit,
) {
    val success = state as? HomeDashboardState.Success
    val hasGroups = success?.groups?.isNotEmpty() == true
    val locale = java.util.Locale.KOREAN

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (hasGroups && success != null) {
            val nextEvent = success.upcomingEvents.firstOrNull()
            val calendarSubtitle = buildString {
                append("이번 주 확정 ${success.confirmedThisWeekCount}건")
                if (nextEvent != null) {
                    append("\n다음 ")
                    append(nextEvent.start.format(DateTimeFormatter.ofPattern("M/d", locale)))
                    append(" ")
                    append(nextEvent.title.take(8))
                    if (nextEvent.title.length > 8) append("…")
                } else {
                    append("\n다가오는 일정 없음")
                }
            }
            val pending = success.pendingCoordinationCount
            val topGroup = success.groups.firstOrNull()?.name ?: "내 모임"
            val meetingsSubtitle = buildString {
                append("모임 ${success.groups.size}개")
                append(if (pending > 0) "\n조율 필요 ${pending}건" else "\n조율 필요 없음")
            }
            HomeQuickAccessCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.CalendarMonth,
                title = "이번 주 일정",
                subtitle = calendarSubtitle,
                onClick = onCalendarClick,
            )
            HomeQuickAccessCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Groups,
                title = topGroup,
                subtitle = meetingsSubtitle,
                onClick = onMeetingsClick,
            )
        } else if (state is HomeDashboardState.Loading) {
            HomeQuickAccessCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.CalendarMonth,
                title = "불러오는 중",
                subtitle = "정보를\n가져오고 있어요",
                onClick = {},
                enabled = false,
            )
            HomeQuickAccessCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Groups,
                title = "불러오는 중",
                subtitle = "잠시만\n기다려주세요",
                onClick = {},
                enabled = false,
            )
        } else {
            HomeQuickAccessCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.VideoCall,
                title = "모임 만들기",
                subtitle = "새 모임을\n시작해요",
                onClick = onCreateClick,
            )
            HomeQuickAccessCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Link,
                title = "코드로 입장",
                subtitle = "초대코드로\n참여해요",
                onClick = onJoinClick,
            )
        }
    }
}

@Composable
private fun HomeQuickAccessCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Column(
        modifier = modifier
            .heightIn(min = 132.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MoaBlue.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MoaBlue,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = title,
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MoaTextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            color = MoaTextSecondary,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * 내 일정: 모임·단기 일정에서 확정된, 아직 지나지 않은 일정을 날짜순으로 보여준다.
 */
@Composable
private fun HomeTaskSection(
    state: HomeDashboardState,
    onSeeAllClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onCreateClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "내 일정",
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MoaTextPrimary,
                )
                Text(
                    text = "확정된 다가오는 일정",
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = MoaTextSecondary,
                )
            }
            Text(
                text = "캘린더 보기",
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = MoaBlue,
                modifier = Modifier.clickable(onClick = onSeeAllClick),
            )
        }
    }

    Spacer(modifier = Modifier.height(14.dp))

    when (state) {
        is HomeDashboardState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MoaBlue, strokeWidth = 2.dp)
            }
        }
        is HomeDashboardState.Error -> {
            HomeScheduleCard(
                title = "일정을 불러오지 못했어요",
                dayNumber = "—",
                dayOfWeek = "",
                timeLabel = state.message.lines().firstOrNull() ?: "연결 확인",
                sourceLabel = "다시 시도",
                accent = Color(0xFFFF9500),
                onClick = onSeeAllClick,
            )
        }
        is HomeDashboardState.Success -> {
            val events = buildUpcomingEvents(state)
            if (events.isEmpty()) {
                HomeScheduleCard(
                    title = "확정된 일정이 없어요",
                    dayNumber = "—",
                    dayOfWeek = "",
                    timeLabel = "모임·단기 일정에서 확정하면 여기에 표시돼요",
                    sourceLabel = "일정 만들기",
                    accent = MoaBlue,
                    onClick = onCreateClick,
                )
            } else {
                events.take(3).forEachIndexed { index, event ->
                    HomeScheduleCard(
                        title = event.title,
                        dayNumber = event.dayNumber,
                        dayOfWeek = event.dayOfWeek,
                        timeLabel = event.timeLabel,
                        sourceLabel = event.sourceLabel,
                        accent = event.accent,
                        onClick = onCalendarClick,
                    )
                    if (index != events.lastIndex && index < 2) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

private data class HomeScheduleItem(
    val title: String,
    val dayNumber: String,
    val dayOfWeek: String,
    val timeLabel: String,
    val sourceLabel: String,
    val accent: Color,
)

private fun buildUpcomingEvents(state: HomeDashboardState.Success): List<HomeScheduleItem> {
    val locale = java.util.Locale.KOREAN
    return state.upcomingEvents.map { event ->
        HomeScheduleItem(
            title = event.title,
            dayNumber = event.start.dayOfMonth.toString(),
            dayOfWeek = event.start.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, locale),
            timeLabel = formatKoreanTime(event.start.hour, event.start.minute),
            sourceLabel = event.subtitle ?: "확정 일정",
            accent = parseColor(event.color),
        )
    }
}

@Composable
private fun HomeScheduleCard(
    title: String,
    dayNumber: String,
    dayOfWeek: String,
    timeLabel: String,
    sourceLabel: String,
    accent: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .width(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(accent.copy(alpha = 0.12f))
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = dayNumber,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = accent,
                maxLines = 1,
            )
            if (dayOfWeek.isNotBlank()) {
                Text(
                    text = dayOfWeek,
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp,
                    color = accent.copy(alpha = 0.8f),
                    maxLines = 1,
                )
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MoaTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = timeLabel,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = accent,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = sourceLabel,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = MoaTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun NotificationBell(hasUnread: Boolean, onClick: () -> Unit) {
    Box {
        HeaderIconButton(
            icon = Icons.Outlined.Notifications,
            contentDescription = "알림",
            onClick = onClick,
        )
        if (hasUnread) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(com.example.moa_project.ui.theme.MoaAccentRed),
            )
        }
    }
}

private fun parseColor(hex: String): Color {
    return runCatching { Color(hex.toColorInt()) }.getOrDefault(MoaBlue)
}

private fun formatKoreanTime(hour: Int, minute: Int): String {
    val period = if (hour < 12) "오전" else "오후"
    val h = when {
        hour == 0 || hour == 24 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return if (minute == 0) "$period ${h}시" else "$period ${h}:${"%02d".format(minute)}"
}
