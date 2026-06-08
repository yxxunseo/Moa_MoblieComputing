package com.example.moa_project.ui.my

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moa_project.ui.my.UserViewModel
import com.example.moa_project.ui.my.UserState
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import com.example.moa_project.ui.meetings.MeetingsViewModel
import com.example.moa_project.ui.meetings.MeetingsState
import com.example.moa_project.network.RetrofitClient
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import com.example.moa_project.util.GroupFavoriteManager
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moa_project.ui.components.MoaBottomNavigationBar
import com.example.moa_project.ui.components.MoaCaptionText
import com.example.moa_project.ui.components.MoaLabelText
import com.example.moa_project.ui.components.MoaTitleText
import com.example.moa_project.ui.components.ProfileAvatar
import com.example.moa_project.ui.theme.MoaBlue
import com.example.moa_project.ui.theme.MoaBlueSoft
import com.example.moa_project.ui.theme.MoaDivider
import com.example.moa_project.ui.theme.MoaError
import com.example.moa_project.ui.theme.MoaScreenBackground
import com.example.moa_project.ui.theme.MoaSpacing
import com.example.moa_project.ui.theme.MoaTextSecondary
import com.example.moa_project.util.userMessage
import com.example.moa_project.ui.components.MoaMascot
import android.content.Intent
import android.net.Uri
import com.example.moa_project.ui.theme.Moa_ProjectTheme
import com.example.moa_project.ui.theme.SBAggroFontFamily
import com.example.moa_project.ui.theme.moaCard

@Immutable
private data class MyMenuItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconColor: Color = MoaBlue,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPageScreen(
    currentRoute: String = "my",
    onNavigate: (String) -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onNavigateToFavoriteMeetings: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    userViewModel: UserViewModel = viewModel(),
    meetingsViewModel: MeetingsViewModel = viewModel()
) {
    val uiState by userViewModel.uiState.collectAsState()
    val meetingsState by meetingsViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var upcomingEventCount by remember { mutableStateOf("0") }
    var favoriteCount by remember { mutableStateOf("0") }

    LaunchedEffect(currentRoute) {
        if (currentRoute == "my") {
            favoriteCount = GroupFavoriteManager.favoriteCount(context).toString()
        }
    }

    LaunchedEffect(Unit) {
        meetingsViewModel.fetchMyGroups()
        scope.launch {
            runCatching {
                val month = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
                val response = RetrofitClient.instance.getMonthlyEvents(month)
                val events = (response["events"] as? List<*>) ?: (response["data"] as? List<*>) ?: emptyList<Any>()
                val count = events.count { raw ->
                    val item = raw as? Map<*, *> ?: return@count false
                    val startText = item["start"] as? String ?: return@count false
                    val start = runCatching { LocalDateTime.parse(startText) }.getOrNull() ?: return@count false
                    !start.isBefore(LocalDateTime.now())
                }
                upcomingEventCount = count.toString()
            }
        }
    }

    val groupCount = when (val state = meetingsState) {
        is MeetingsState.Success -> state.groups.size.toString()
        is MeetingsState.Loading -> "..."
        else -> "0"
    }

    var showFixedSheet by remember { mutableStateOf(false) }
    var dialogType by remember { mutableStateOf<MyPageDialog?>(null) }
    val fixedSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    dialogType?.let { type ->
        AlertDialog(
            onDismissRequest = { dialogType = null },
            title = {
                Text(type.title, fontFamily = SBAggroFontFamily, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            },
            text = {
                Text(type.body, fontFamily = SBAggroFontFamily, fontSize = 14.sp, color = MoaTextSecondary)
            },
            confirmButton = {
                TextButton(onClick = {
                    if (type == MyPageDialog.Support) {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:moa.support@example.com")
                            putExtra(Intent.EXTRA_SUBJECT, "MOA 문의")
                        }
                        runCatching { context.startActivity(intent) }
                    }
                    dialogType = null
                }) {
                    Text(
                        if (type == MyPageDialog.Support) "메일 보내기" else "확인",
                        fontFamily = SBAggroFontFamily,
                        color = MoaBlue,
                    )
                }
            },
            dismissButton = if (type == MyPageDialog.Support) {
                {
                    TextButton(onClick = { dialogType = null }) {
                        Text("닫기", fontFamily = SBAggroFontFamily)
                    }
                }
            } else null,
        )
    }

    if (showFixedSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFixedSheet = false },
            sheetState = fixedSheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        ) {
            FixedScheduleSheet(onDismiss = {
                scope.launch { fixedSheetState.hide() }.invokeOnCompletion { showFixedSheet = false }
            })
        }
    }

    Scaffold(
        bottomBar = {
            val profileUrl = (uiState as? UserState.Success)?.user?.profileImageUrl
            MoaBottomNavigationBar(
                currentRoute = currentRoute,
                profileImageUrl = profileUrl,
                onNavigate = onNavigate,
            )
        },
        containerColor = MoaScreenBackground,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MoaScreenBackground)
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = MoaSpacing.screen,
                end = MoaSpacing.screen,
                top = 28.dp,
                bottom = 32.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item { MyHeader() }
            item {
                ProfileSummaryCard(
                    uiState = uiState,
                    groupCount = groupCount,
                    favoriteCount = favoriteCount,
                    onEditProfileClick = onEditProfileClick
                )
            }
            item {
                SectionTitle("활동")
                Spacer(modifier = Modifier.height(10.dp))
                MenuGroup(
                    items = listOf(
                        MyMenuItem("확정 일정", "캘린더에서 ${upcomingEventCount}개 확인", Icons.Default.DateRange),
                        MyMenuItem("고정 일정", "시간표·알바 등록", Icons.Default.Star),
                        MyMenuItem("관심 모임", "하트한 모임 ${favoriteCount}개", Icons.Default.Favorite),
                    ),
                    onItemClick = { title ->
                        when (title) {
                            "확정 일정" -> onNavigate("calendar")
                            "고정 일정" -> showFixedSheet = true
                            "관심 모임" -> onNavigateToFavoriteMeetings()
                        }
                    }
                )
            }
            item {
                SectionTitle("연동 및 알림")
                Spacer(modifier = Modifier.height(10.dp))
                IntegrationSettingsCard()
            }
            item {
                SectionTitle("계정")
                Spacer(modifier = Modifier.height(10.dp))
                MenuGroup(
                    items = listOf(
                        MyMenuItem("계정 정보", "이메일, 비밀번호 관리", Icons.Default.Person),
                        MyMenuItem("보안 설정", "로그인, 2단계 인증", Icons.Default.Lock),
                        MyMenuItem("고객센터", "문의하기, 이용 가이드", Icons.Default.Info),
                        MyMenuItem("앱 정보", "버전 1.0.0", Icons.Default.Settings),
                    ),
                    onItemClick = { title ->
                        when (title) {
                            "계정 정보" -> onEditProfileClick()
                            "보안 설정" -> dialogType = MyPageDialog.Security
                            "고객센터" -> dialogType = MyPageDialog.Support
                            "앱 정보" -> dialogType = MyPageDialog.AppInfo
                        }
                    },
                )
            }
            item {
                LogoutButton(onClick = onLogoutClick)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IntegrationSettingsCard() {
    val context = LocalContext.current
    val sharedPreferences = remember {
        context.getSharedPreferences("moa_settings", Context.MODE_PRIVATE)
    }

    var scheduleConfirmedPush by remember {
        mutableStateOf(sharedPreferences.getBoolean("schedule_confirmed_push", true))
    }
    var calendarAddedPush by remember {
        mutableStateOf(sharedPreferences.getBoolean("calendar_added_push", true))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .moaCard(padding = 0.dp),
    ) {
        SettingsToggleRow(
            title = "일정 확정 알림",
            description = "최종 시간이 정해지면 알려드려요",
            icon = Icons.Default.Notifications,
            checked = scheduleConfirmedPush,
            onCheckedChange = { isChecked ->
                scheduleConfirmedPush = isChecked
                sharedPreferences.edit().putBoolean("schedule_confirmed_push", isChecked).apply()
            }
        )
        SettingsDivider()
        SettingsToggleRow(
            title = "캘린더 추가 알림",
            description = "새 일정이 추가되면 알려드려요",
            icon = Icons.Default.Notifications,
            checked = calendarAddedPush,
            onCheckedChange = { isChecked ->
                calendarAddedPush = isChecked
                sharedPreferences.edit().putBoolean("calendar_added_push", isChecked).apply()
            }
        )
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    description: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(MoaBlueSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MoaBlue, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            MoaLabelText(text = title, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(4.dp))
            MoaCaptionText(text = description)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MoaBlue,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFD8DEEA)
            )
        )
    }
}

@Composable
private fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 66.dp)
            .height(1.dp)
            .background(MoaDivider)
    )
}

@Composable
private fun MyHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            MoaTitleText(text = "마이페이지", fontSize = 22.sp)
            Spacer(modifier = Modifier.height(6.dp))
            MoaCaptionText(text = "내 정보와 활동을 확인하세요")
        }
        MoaMascot(size = 52.dp)
    }
}

@Composable
private fun ProfileSummaryCard(
    uiState: UserState,
    groupCount: String,
    favoriteCount: String,
    onEditProfileClick: () -> Unit
) {
    val nickname = if (uiState is UserState.Success) uiState.user.nickname else "사용자"
    val statusText = if (uiState is UserState.Success) {
        if (uiState.user.provider == "LOCAL") "모아와 함께하는 중"
        else "${uiState.user.provider} 계정 연동 완료"
    } else "로딩 중..."

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .moaCard(padding = 18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.BottomEnd) {
                val profileUrl = (uiState as? UserState.Success)?.user?.profileImageUrl
                val nick = (uiState as? UserState.Success)?.user?.nickname
                ProfileAvatar(
                    imageUrl = profileUrl,
                    nickname = nick,
                    size = 80.dp,
                )
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(Color.White)
                        .clickable(onClick = onEditProfileClick),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "프로필 편집",
                        tint = MoaTextSecondary,
                        modifier = Modifier.size(15.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                MoaTitleText(
                    text = nickname,
                    fontSize = 20.sp,
                    maxLines = 1,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = MoaBlue,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    MoaCaptionText(text = statusText)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MoaBlueSoft)
                        .clickable(onClick = onEditProfileClick)
                        .padding(horizontal = 13.dp, vertical = 7.dp),
                ) {
                    MoaLabelText(
                        text = "프로필 편집",
                        fontSize = 12.sp,
                        color = MoaBlue,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MoaDivider),
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            MyStatItem(
                title = "참여 중인 모임",
                value = groupCount,
                color = MoaBlue,
                icon = Icons.Default.Person,
                modifier = Modifier.weight(1f),
            )
            MyStatItem(
                title = "관심 모임",
                value = favoriteCount,
                color = MoaBlue,
                icon = Icons.Default.Favorite,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun MyStatItem(
    title: String,
    value: String,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(8.dp))
        MoaCaptionText(text = title)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            color = color,
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    MoaLabelText(
        text = text,
        fontSize = 13.sp,
        color = MoaTextSecondary,
    )
}

@Composable
private fun MenuGroup(
    items: List<MyMenuItem>,
    onItemClick: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .moaCard(padding = 0.dp),
    ) {
        items.forEachIndexed { index, item ->
            MenuRow(item, onClick = { onItemClick(item.title) })
            if (index != items.lastIndex) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .padding(horizontal = 18.dp)
                        .background(MoaDivider),
                )
            }
        }
    }
}

@Composable
private fun MenuRow(item: MyMenuItem, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(item.icon, contentDescription = null, tint = item.iconColor, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            MoaLabelText(text = item.title, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(3.dp))
            MoaCaptionText(text = item.description)
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "이동",
            tint = MoaTextSecondary,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun LogoutButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "로그아웃",
            color = MoaError,
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
        )
    }
}

private enum class MyPageDialog(val title: String, val body: String) {
    Security(
        title = "보안 설정",
        body = "Google·카카오 로그인은 해당 서비스 보안 정책을 따릅니다.\n이메일 가입 계정은 비밀번호 변경을 웹 고객센터로 요청해 주세요.",
    ),
    Support(
        title = "고객센터",
        body = "이용 중 문제가 있으면 메일로 문의해 주세요.\nFAQ: 모임 초대코드 복사 → 친구에게 공유 → 일정 조율",
    ),
    AppInfo(
        title = "앱 정보",
        body = "MOA v1.0.0\n한밭대 모바일컴퓨팅과응용 팀 프로젝트",
    ),
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun MyPageScreenPreview() {
    Moa_ProjectTheme {
        MyPageScreen()
    }
}
