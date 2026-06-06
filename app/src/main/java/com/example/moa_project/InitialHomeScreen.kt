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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.StrokeCap
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
import com.example.moa_project.ui.home.HomeActivityItem
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
    onCoordinationScheduleClick: (Long) -> Unit = {},
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
    val coordinationSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showSheet by remember { mutableStateOf(false) }
    var showCoordinationSheet by remember { mutableStateOf(false) }
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

    if (showCoordinationSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCoordinationSheet = false },
            sheetState = coordinationSheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        ) {
            CoordinationListSheet(
                items = success?.pendingCoordinationItems.orEmpty(),
                onDismiss = {
                    scope.launch { coordinationSheetState.hide() }.invokeOnCompletion {
                        showCoordinationSheet = false
                    }
                },
                onItemClick = { scheduleId ->
                    scope.launch { coordinationSheetState.hide() }.invokeOnCompletion {
                        showCoordinationSheet = false
                        onCoordinationScheduleClick(scheduleId)
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
                .padding(innerPadding),
        ) {
            HomeProfileHeader(
                nickname = userNickname,
                profileImageUrl = profileImageUrl,
                hasUnread = unreadCount > 0,
                onNotificationsClick = onNotificationsClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MoaScreenBackground)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 16.dp),
            ) {
                Spacer(modifier = Modifier.height(4.dp))

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
                    onCoordinationListClick = { showCoordinationSheet = true },
                    onCoordinationScheduleClick = onCoordinationScheduleClick,
                    onCalendarClick = { onNavigate("calendar") },
                    onCreateClick = { openCreateSheet() },
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/** 상단: 프로필 + 인사 | 알림 (스크롤과 분리되어 고정) */
@Composable
private fun HomeProfileHeader(
    nickname: String?,
    profileImageUrl: String?,
    hasUnread: Boolean,
    onNotificationsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
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
                text = "안녕하세요,",
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

/** 내 일정: 조율 현황(진행률) + 다가오는 확정 일정 */
@Composable
private fun HomeTaskSection(
    state: HomeDashboardState,
    onCoordinationListClick: () -> Unit,
    onCoordinationScheduleClick: (Long) -> Unit,
    onCalendarClick: () -> Unit,
    onCreateClick: () -> Unit,
) {
    Text(
        text = "내 일정",
        fontFamily = SBAggroFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        color = MoaTextPrimary,
    )

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
            HomeProgressCard(
                title = "일정을 불러오지 못했어요",
                subtitle = state.message.lines().firstOrNull() ?: "연결 확인",
                progress = 0f,
                progressLabel = "다시 시도",
                dueLabel = "—",
                accent = Color(0xFFFF9500),
                onClick = onCoordinationListClick,
            )
        }
        is HomeDashboardState.Success -> {
            val activities = buildCoordinationItems(state)
            val events = buildUpcomingEvents(state)

            if (activities.isNotEmpty()) {
                HomeSectionHeader(
                    title = "조율 현황",
                    actionLabel = "전체 보기",
                    onActionClick = onCoordinationListClick,
                )
                Spacer(modifier = Modifier.height(10.dp))
                activities.take(2).forEachIndexed { index, item ->
                    HomeProgressCard(
                        title = item.title,
                        subtitle = item.subtitle,
                        progress = item.progress,
                        progressLabel = item.progressLabel,
                        dueLabel = item.dueLabel,
                        accent = item.accent,
                        onClick = { onCoordinationScheduleClick(item.scheduleId) },
                    )
                    if (index < activities.take(2).lastIndex) {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            HomeSectionHeader(
                title = "다가오는 확정 일정",
                actionLabel = "캘린더 보기",
                onActionClick = onCalendarClick,
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (events.isEmpty()) {
                HomeScheduleCard(
                    title = "확정된 일정이 없어요",
                    dayNumber = "—",
                    dayOfWeek = "",
                    timeLabel = "모임·단기 일정에서 확정하면 여기에 표시돼요",
                    sourceLabel = "일정 만들기",
                    onClick = onCreateClick,
                )
            } else {
                events.take(2).forEachIndexed { index, event ->
                    HomeScheduleCard(
                        title = event.title,
                        dayNumber = event.dayNumber,
                        dayOfWeek = event.dayOfWeek,
                        timeLabel = event.timeLabel,
                        sourceLabel = event.sourceLabel,
                        onClick = onCalendarClick,
                    )
                    if (index < events.take(2).lastIndex) {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeSectionHeader(
    title: String,
    actionLabel: String,
    onActionClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = MoaTextSecondary,
        )
        Text(
            text = actionLabel,
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = MoaBlue,
            modifier = Modifier.clickable(onClick = onActionClick),
        )
    }
}

private data class HomeScheduleItem(
    val title: String,
    val dayNumber: String,
    val dayOfWeek: String,
    val timeLabel: String,
    val sourceLabel: String,
)

private data class HomeProgressItem(
    val scheduleId: Long,
    val title: String,
    val subtitle: String,
    val progress: Float,
    val progressLabel: String,
    val dueLabel: String,
    val accent: Color,
)

private fun buildCoordinationItems(state: HomeDashboardState.Success): List<HomeProgressItem> {
    return state.pendingCoordinationItems.map { activity ->
        val (progress, label) = progressForResponse(activity.respondedCount, activity.totalMembers)
        HomeProgressItem(
            scheduleId = activity.scheduleId,
            title = activity.scheduleTitle,
            subtitle = activity.groupName,
            progress = progress,
            progressLabel = label,
            dueLabel = activity.statusLabel,
            accent = parseColor(activity.groupColor),
        )
    }
}

private fun buildUpcomingEvents(state: HomeDashboardState.Success): List<HomeScheduleItem> {
    val locale = java.util.Locale.KOREAN
    return state.upcomingEvents.map { event ->
        HomeScheduleItem(
            title = event.title,
            dayNumber = event.start.dayOfMonth.toString(),
            dayOfWeek = event.start.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, locale),
            timeLabel = formatKoreanTime(event.start.hour, event.start.minute),
            sourceLabel = event.subtitle ?: "확정 일정",
        )
    }
}

private fun progressForResponse(respondedCount: Int, totalMembers: Int): Pair<Float, String> {
    if (totalMembers <= 0) return 0f to "응답 없음"
    val progress = respondedCount.toFloat() / totalMembers
    return progress to "${respondedCount}/${totalMembers}명 응답"
}

@Composable
private fun HomeProgressCard(
    title: String,
    subtitle: String,
    progress: Float,
    progressLabel: String,
    dueLabel: String,
    accent: Color,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
    ) {
        Text(
            text = title,
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MoaTextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(12.dp))
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = accent,
            trackColor = accent.copy(alpha = 0.15f),
            strokeCap = StrokeCap.Round,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = progressLabel,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = accent,
            )
            Text(
                text = dueLabel,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = MoaTextSecondary,
            )
        }
        if (subtitle.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
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
private fun CoordinationListSheet(
    items: List<HomeActivityItem>,
    onDismiss: () -> Unit,
    onItemClick: (Long) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "조율 중인 일정",
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MoaTextPrimary,
            )
            Text(
                text = "닫기",
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = MoaTextSecondary,
                modifier = Modifier.clickable(onClick = onDismiss),
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "${items.size}건 · 응답 대기·조율 중",
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = MoaTextSecondary,
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "조율 중인 일정이 없어요",
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = MoaTextSecondary,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(items, key = { it.scheduleId }) { item ->
                    CoordinationListItem(
                        item = item,
                        onClick = { onItemClick(item.scheduleId) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CoordinationListItem(
    item: HomeActivityItem,
    onClick: () -> Unit,
) {
    val accent = parseColor(item.groupColor)
    val (progress, responseLabel) = progressForResponse(item.respondedCount, item.totalMembers)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = item.scheduleTitle,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MoaTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = item.statusLabel,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = MoaTextSecondary,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = item.groupName,
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = MoaTextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(10.dp))
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = accent,
            trackColor = accent.copy(alpha = 0.15f),
            strokeCap = StrokeCap.Round,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = responseLabel,
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            color = accent,
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
                .background(MoaBlue.copy(alpha = 0.1f))
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = dayNumber,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MoaBlue,
                maxLines = 1,
            )
            if (dayOfWeek.isNotBlank()) {
                Text(
                    text = dayOfWeek,
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp,
                    color = MoaBlue.copy(alpha = 0.75f),
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
                color = MoaBlue,
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
