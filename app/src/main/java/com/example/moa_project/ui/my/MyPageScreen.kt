package com.example.moa_project.ui.my

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import com.example.moa_project.network.GoogleConnectRequest
import com.example.moa_project.util.GoogleCalendarHelper
import com.example.moa_project.util.GroupFavoriteManager
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moa_project.R
import com.example.moa_project.ui.components.MoaBottomNavigationBar
import com.example.moa_project.ui.theme.Moa_ProjectTheme
import com.example.moa_project.ui.theme.SBAggroFontFamily

private val MoaBlue = Color(0xFF2179FE)
private val ScreenBackground = Color(0xFFF7F8FC)
private val TextPrimary = Color(0xFF101B33)
private val TextSecondary = Color(0xFF737C99)
private val Divider = Color(0xFFE8ECF4)

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
    val fixedSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
            MoaBottomNavigationBar(
                currentRoute = currentRoute,
                onNavigate = onNavigate,
            )
        },
        containerColor = ScreenBackground,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(ScreenBackground)
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 36.dp,
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
                        MyMenuItem("내 일정", "예정된 일정 ${upcomingEventCount}개", Icons.Default.DateRange),
                        MyMenuItem("고정 일정", "시간표·알바 등록", Icons.Default.Star),
                        MyMenuItem("관심 모임", "하트한 모임 ${favoriteCount}개", Icons.Default.Favorite, Color(0xFFFF6B9A)),
                    ),
                    onItemClick = { title ->
                        if (title == "고정 일정") showFixedSheet = true
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
                )
            }
            item {
                ReviewBanner()
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
    val scope = rememberCoroutineScope()
    val sharedPreferences = remember {
        context.getSharedPreferences("moa_settings", Context.MODE_PRIVATE)
    }

    var googleCalendarEnabled by remember {
        mutableStateOf(sharedPreferences.getBoolean("google_calendar", false))
    }
    var scheduleConfirmedPush by remember {
        mutableStateOf(sharedPreferences.getBoolean("schedule_confirmed_push", true))
    }
    var calendarAddedPush by remember {
        mutableStateOf(sharedPreferences.getBoolean("calendar_added_push", true))
    }

    LaunchedEffect(Unit) {
        runCatching {
            val status = RetrofitClient.instance.getGoogleCalendarStatus()
            val connected = status["connected"] as? Boolean ?: false
            googleCalendarEnabled = connected
            sharedPreferences.edit().putBoolean("google_calendar", connected).apply()
        }
    }

    val googleConnectLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val authCode = GoogleCalendarHelper.extractServerAuthCode(result.data)
        if (authCode.isNullOrBlank()) {
            googleCalendarEnabled = false
            sharedPreferences.edit().putBoolean("google_calendar", false).apply()
            android.widget.Toast.makeText(context, "구글 캘린더 연동에 실패했습니다.", android.widget.Toast.LENGTH_SHORT).show()
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            runCatching {
                RetrofitClient.instance.connectGoogleCalendar(GoogleConnectRequest(authCode))
                googleCalendarEnabled = true
                sharedPreferences.edit().putBoolean("google_calendar", true).apply()
                android.widget.Toast.makeText(context, "구글 캘린더가 연동되었습니다.", android.widget.Toast.LENGTH_SHORT).show()
            }.onFailure {
                googleCalendarEnabled = false
                sharedPreferences.edit().putBoolean("google_calendar", false).apply()
                android.widget.Toast.makeText(context, "구글 캘린더 연동에 실패했습니다.", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(18.dp), spotColor = Color(0xFFDDE4F2))
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .padding(vertical = 4.dp)
    ) {
        SettingsToggleRow(
            title = "Google Calendar",
            description = "외부 일정으로 불가능한 시간을 막아요",
            icon = Icons.Default.DateRange,
            checked = googleCalendarEnabled,
            onCheckedChange = { isChecked ->
                if (isChecked) {
                    googleConnectLauncher.launch(
                        GoogleCalendarHelper.createConnectClient(context).signInIntent
                    )
                } else {
                    scope.launch {
                        runCatching { RetrofitClient.instance.disconnectGoogleCalendar() }
                        googleCalendarEnabled = false
                        sharedPreferences.edit().putBoolean("google_calendar", false).apply()
                        android.widget.Toast.makeText(context, "구글 캘린더 연동이 해제되었습니다.", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
        SettingsDivider()
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
                .background(Color(0xFFEAF1FF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MoaBlue, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextPrimary,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = description,
                color = TextSecondary,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp
            )
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
            .background(Divider)
    )
}

@Composable
private fun MyHeader() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "마이페이지",
            color = TextPrimary,
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = buildAnnotatedString {
                append("내 정보와 활동을 확인하세요")
                withStyle(SpanStyle(color = MoaBlue)) {
                    append(" ·")
                }
            },
            color = TextSecondary,
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
        )
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
    val description = if (uiState is UserState.Success) {
        if (uiState.user.provider == "LOCAL") "모아와 함께하는 중! ✨"
        else "${uiState.user.provider} 계정 연동 완료! ✨"
    } else "로딩 중..."

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(22.dp), spotColor = Color(0xFFDDE4F2))
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
            .padding(18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(94.dp), contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = R.drawable.ic_character),
                    contentDescription = "프로필 캐릭터",
                    modifier = Modifier.size(88.dp),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(26.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(Color.White)
                        .clickable(onClick = onEditProfileClick),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "프로필 편집",
                        tint = TextSecondary,
                        modifier = Modifier.size(15.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nickname,
                    color = TextPrimary,
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = description,
                    color = TextSecondary,
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFEAF1FF))
                        .clickable(onClick = onEditProfileClick)
                        .padding(horizontal = 13.dp, vertical = 7.dp),
                ) {
                    Text(
                        text = "프로필 편집",
                        color = TextSecondary,
                        fontFamily = SBAggroFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Divider),
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            MyStatItem(
                title = "참여 중인 모임",
                value = groupCount,
                color = Color(0xFF43C879),
                icon = Icons.Default.Person,
                modifier = Modifier.weight(1f),
            )
            MyStatItem(
                title = "관심 모임",
                value = favoriteCount,
                color = Color(0xFFFF6B9A),
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
        Text(
            text = title,
            color = TextSecondary,
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            color = color,
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = TextSecondary,
        fontFamily = SBAggroFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
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
            .shadow(8.dp, RoundedCornerShape(18.dp), spotColor = Color(0xFFDDE4F2))
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White),
    ) {
        items.forEachIndexed { index, item ->
            MenuRow(item, onClick = { onItemClick(item.title) })
            if (index != items.lastIndex) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .padding(horizontal = 18.dp)
                        .background(Divider),
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
            .height(58.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(item.icon, contentDescription = null, tint = item.iconColor, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = item.title,
            color = TextPrimary,
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = item.description,
            color = TextSecondary,
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(Icons.Default.KeyboardArrowRight, contentDescription = "이동", tint = TextSecondary, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun ReviewBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(82.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFEAF2FF))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Canvas(modifier = Modifier.size(58.dp)) {
            val center = Offset(size.width * 0.42f, size.height * 0.55f)
            val r = size.minDimension * 0.28f
            drawRoundRect(
                brush = Brush.verticalGradient(listOf(Color(0xFF68A2FF), Color(0xFF4D7DFF))),
                topLeft = Offset(center.x - r, center.y - r),
                size = Size(r * 2f, r * 2.15f),
                cornerRadius = CornerRadius(r, r),
            )
            drawCircle(Color.Black, r * 0.06f, Offset(center.x - r * 0.28f, center.y - r * 0.10f))
            drawCircle(Color.Black, r * 0.06f, Offset(center.x + r * 0.28f, center.y - r * 0.10f))
            drawArc(Color.Black, 20f, 140f, false, Offset(center.x - r * 0.13f, center.y - r * 0.05f), Size(r * 0.26f, r * 0.20f), style = Stroke(1.2.dp.toPx(), cap = StrokeCap.Round))
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(size.width * 0.62f, size.height * 0.12f),
                size = Size(size.width * 0.30f, size.height * 0.28f),
                cornerRadius = CornerRadius(14.dp.toPx()),
            )
            drawCircle(MoaBlue, 3.dp.toPx(), Offset(size.width * 0.76f, size.height * 0.26f))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "더 좋은 모임 경험을 만들어주세요!",
                color = TextPrimary,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "리뷰를 남겨주시면 큰 힘이 됩니다 💙",
                color = TextSecondary,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp,
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .clickable { }
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Text(
                text = "리뷰 남기기",
                color = MoaBlue,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
            )
        }
    }
}

@Composable
private fun LogoutButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .shadow(8.dp, RoundedCornerShape(18.dp), spotColor = Color(0xFFDDE4F2))
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "로그아웃",
            color = Color(0xFFFF5E70),
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
        )
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun MyPageScreenPreview() {
    Moa_ProjectTheme {
        MyPageScreen()
    }
}
