package com.example.moa_project.ui.meetings

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.moa_project.ui.components.MoaBottomNavigationBar
import com.example.moa_project.util.GroupFavoriteManager
import com.example.moa_project.ui.theme.Moa_ProjectTheme
import com.example.moa_project.ui.theme.SBAggroFontFamily

private val MoaBlue = Color(0xFF2179FE)
private val ScreenBackground = Color(0xFFF7F8FC)
private val TextPrimary = Color(0xFF101B33)
private val TextSecondary = Color(0xFF737C99)

@Immutable
private data class MeetingItem(
    val groupId: Long = 0L,
    val title: String,
    val category: String,
    val categoryColor: Color,
    val categoryBackground: Color,
    val memberCount: Int,
    val illustration: MeetingIllustration,
    val avatarColors: List<Color>,
    val extraMemberCount: Int = 0,
    val isFavorite: Boolean = false,
)

private enum class MeetingIllustration {
    Team,
    Study,
    Travel,
    Sports,
    Cooking,
    Language,
}

private val avatarPalette = listOf(
    Color(0xFFFFC7A6),
    Color(0xFFFFB3B3),
    Color(0xFFE3C0FF),
    Color(0xFFFFD18C),
    Color(0xFFFFC2D8),
    Color(0xFF90EE90),
    Color(0xFF5E8CFF),
)

private fun avatarColorsForCount(count: Int): List<Color> =
    avatarPalette.take(count.coerceIn(0, 4))

private fun participantSummary(memberCount: Int): String {
    return when (memberCount) {
        0 -> "멤버 없음"
        1 -> com.example.moa_project.network.TokenManager.getNickname()?.let { "$it (나)" } ?: "멤버 1명"
        else -> "멤버 ${memberCount}명 참여 중"
    }
}

private fun parseGroupColor(hex: String): Color {
    return runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(MoaBlue)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingsScreen(
    currentRoute: String = "meetings",
    onNavigate: (String) -> Unit = {},
    onMeetingClick: (Long) -> Unit = {},
    onGuestScheduleResultClick: (String) -> Unit = {},
    onNeedsReLogin: () -> Unit = {},
    viewModel: MeetingsViewModel = viewModel(),
    guestListViewModel: GuestScheduleListViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val guestSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showSheet by remember { mutableStateOf(false) }
    var showGuestSheet by remember { mutableStateOf(false) }
    var favoriteRevision by remember { mutableStateOf(0) }
    val favoriteIds = remember(favoriteRevision) { GroupFavoriteManager.favoriteIds(context) }

    // 토큰 만료 시 즉시 로그인 화면으로 이동
    androidx.compose.runtime.LaunchedEffect(uiState) {
        if (uiState is MeetingsState.NeedsReLogin) {
            onNeedsReLogin()
        }
    }

    // 바텀 시트
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            CreateOrJoinMeetingSheet(
                onDismiss = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion { showSheet = false }
                },
                onSuccess = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showSheet = false
                        viewModel.fetchMyGroups() // 목록 새로고침
                    }
                }
            )
        }
    }

    if (showGuestSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showGuestSheet = false
                guestListViewModel.fetchMySchedules()
            },
            sheetState = guestSheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
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
                }
            )
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
        when (uiState) {
            is MeetingsState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator(color = MoaBlue)
                }
            }

            is MeetingsState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = (uiState as MeetingsState.Error).message,
                            color = TextSecondary,
                            fontFamily = SBAggroFontFamily,
                            fontSize = 14.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        androidx.compose.material3.Button(
                            onClick = { viewModel.fetchMyGroups() },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MoaBlue),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                        ) {
                            Text("다시 시도", color = Color.White, fontFamily = SBAggroFontFamily)
                        }
                    }
                }
            }

            is MeetingsState.Success -> {
                val groups = (uiState as MeetingsState.Success).groups
                    .sortedWith(
                        compareByDescending<com.example.moa_project.network.GroupResponse> { favoriteIds.contains(it.id) }
                            .thenBy { it.name }
                    )
                val displayMeetings = groups.map { group ->
                    val memberCount = group.memberCount.toInt()
                    val groupColor = parseGroupColor(group.color)
                    MeetingItem(
                        groupId = group.id,
                        title = group.name,
                        category = group.description?.takeIf { it.isNotBlank() } ?: "그룹 모임",
                        categoryColor = groupColor,
                        categoryBackground = groupColor.copy(alpha = 0.12f),
                        memberCount = memberCount,
                        illustration = MeetingIllustration.Team,
                        avatarColors = avatarColorsForCount(memberCount),
                        extraMemberCount = if (memberCount > 4) memberCount - 4 else 0,
                        isFavorite = favoriteIds.contains(group.id),
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ScreenBackground)
                        .padding(innerPadding),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        top = 36.dp,
                        bottom = 18.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    item {
                        MeetingsHeader(
                            onCreateMeetingClick = { showSheet = true },
                            onCreateGuestScheduleClick = { showGuestSheet = true }
                        )
                    }

                    item {
                        MyGuestSchedulesSection(
                            onCreateClick = { showGuestSheet = true },
                            onViewResult = onGuestScheduleResultClick,
                            viewModel = guestListViewModel,
                        )
                    }

                    if (displayMeetings.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "🫂",
                                        fontSize = 40.sp
                                    )
                                    Text(
                                        text = "아직 참여 중인 모임이 없어요",
                                        color = TextPrimary,
                                        fontFamily = SBAggroFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "오른쪽 + 버튼으로 모임을 만들어보세요!",
                                        color = TextSecondary,
                                        fontFamily = SBAggroFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    } else {
                        items(displayMeetings) { meeting ->
                            MeetingListCard(
                                meeting = meeting,
                                onClick = { onMeetingClick(meeting.groupId) },
                                onFavoriteClick = {
                                    GroupFavoriteManager.toggleFavorite(context, meeting.groupId)
                                    favoriteRevision++
                                },
                            )
                        }
                    }
                }
            }

            is MeetingsState.NeedsReLogin -> {
                // LaunchedEffect에서 onNeedsReLogin()이 호출됨 – 여기선 빈 로딩 표시
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator(color = MoaBlue)
                }
            }

            else -> {
                // Idle: 초기 로딩 트리거
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ScreenBackground)
                        .padding(innerPadding),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 20.dp, end = 20.dp, top = 36.dp, bottom = 18.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    item {
                        MeetingsHeader(
                            onCreateMeetingClick = { showSheet = true },
                            onCreateGuestScheduleClick = { showGuestSheet = true }
                        )
                    }
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.material3.CircularProgressIndicator(color = MoaBlue)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MeetingsHeader(
    onCreateMeetingClick: () -> Unit,
    onCreateGuestScheduleClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = buildAnnotatedString {
                    append("모임")
                    withStyle(SpanStyle(color = MoaBlue)) {
                        append(" ·")
                    }
                },
                color = TextPrimary,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 34.sp,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "내가 참여 중인 모든 모임이에요",
                color = TextSecondary,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            IconButton(
                onClick = onCreateGuestScheduleClick,
                modifier = Modifier
                    .size(48.dp)
                    .shadow(8.dp, RoundedCornerShape(14.dp), spotColor = Color(0xFFDDE6FA))
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White),
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "단기 일정 링크 만들기",
                    tint = Color(0xFF35A96D),
                    modifier = Modifier.size(24.dp),
                )
            }
            IconButton(
                onClick = onCreateMeetingClick,
                modifier = Modifier
                    .size(48.dp)
                    .shadow(8.dp, RoundedCornerShape(14.dp), spotColor = Color(0xFFDDE6FA))
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "모임 만들기",
                    tint = MoaBlue,
                    modifier = Modifier.size(26.dp),
                )
            }
        }
    }
}

@Composable
private fun MeetingListCard(
    meeting: MeetingItem,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(156.dp)
            .shadow(9.dp, RoundedCornerShape(22.dp), spotColor = Color(0xFFDDE4F2))
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(start = 18.dp, end = 10.dp, top = 18.dp, bottom = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MeetingThumbnail(type = meeting.illustration)

        Spacer(modifier = Modifier.width(18.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            CategoryChip(meeting)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = meeting.title,
                color = TextPrimary,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
            )
            Spacer(modifier = Modifier.height(9.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(15.dp),
                )
                Text(
                    text = " 멤버 ${meeting.memberCount}명",
                    color = TextSecondary,
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                )
            }
            Spacer(modifier = Modifier.height(13.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (meeting.memberCount > 0) {
                    AvatarStack(
                        colors = meeting.avatarColors,
                        extraMemberCount = meeting.extraMemberCount,
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Text(
                    text = participantSummary(meeting.memberCount),
                    color = TextSecondary,
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.height(120.dp),
        ) {
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    imageVector = if (meeting.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "관심 모임",
                    tint = if (meeting.isFavorite) Color(0xFFFF6B9A) else TextSecondary,
                    modifier = Modifier.size(24.dp),
                )
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "상세 보기",
                tint = TextSecondary,
                modifier = Modifier.size(30.dp),
            )
        }
    }
}

@Composable
private fun MeetingThumbnail(type: MeetingIllustration) {
    Box(
        modifier = Modifier
            .size(104.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFF1F5FF)),
        contentAlignment = Alignment.Center,
    ) {
        MeetingIllustrationCanvas(
            type = type,
            modifier = Modifier.size(width = 90.dp, height = 98.dp),
        )
    }
}

@Composable
private fun CategoryChip(meeting: MeetingItem) {
    Text(
        text = meeting.category,
        color = Color(0xFF8A92A8),
        fontFamily = SBAggroFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
    )
}

@Composable
private fun AvatarStack(
    colors: List<Color>,
    extraMemberCount: Int,
) {
    val avatarSize = 34.dp
    val visibleColors = colors.take(4)
    val stackWidth = avatarSize + ((visibleColors.size - 1).coerceAtLeast(0) * 22).dp +
        if (extraMemberCount > 0) 28.dp else 0.dp

    Box(
        modifier = Modifier
            .width(stackWidth)
            .height(avatarSize),
        contentAlignment = Alignment.CenterStart,
    ) {
        visibleColors.forEachIndexed { index, color ->
            MiniAvatar(
                color = color,
                modifier = Modifier.offset(x = (index * 22).dp),
            )
        }
        if (extraMemberCount > 0) {
            Box(
                modifier = Modifier
                    .offset(x = (visibleColors.size * 22).dp)
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8EBF2)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "+$extraMemberCount",
                    color = TextSecondary,
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                )
            }
        }
    }
}

@Composable
private fun MiniAvatar(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(
        modifier = modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(Color.White)
            .padding(2.dp),
    ) {
        val r = size.minDimension * 0.48f
        val center = Offset(size.width / 2f, size.height / 2f)
        drawCircle(color = Color(0xFFF0F3FA), radius = r, center = center)
        drawCircle(color = Color(0xFF2A2530), radius = r * 0.72f, center = Offset(center.x, center.y - r * 0.12f))
        drawCircle(color = color, radius = r * 0.50f, center = Offset(center.x, center.y - r * 0.02f))
        drawArc(
            color = Color(0xFF202436),
            startAngle = 200f,
            sweepAngle = 140f,
            useCenter = true,
            topLeft = Offset(center.x - r * 0.72f, center.y + r * 0.16f),
            size = Size(r * 1.44f, r * 1.10f),
        )
    }
}

@Composable
private fun CreateMeetingBanner(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(82.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFEAF2FF))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MeetingIllustrationCanvas(
            type = MeetingIllustration.Language,
            modifier = Modifier.size(58.dp),
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "새로운 모임을 만들어보세요!",
                color = TextPrimary,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "사람들과 시간을 모아 더 좋은 순간을 만들어요.",
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
                .padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            Text(
                text = "모임 만들기",
                color = MoaBlue,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
            )
        }
    }
}

@Composable
private fun MeetingIllustrationCanvas(
    type: MeetingIllustration,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val baseY = h * 0.88f
        drawOval(
            color = Color(0xFFDCE5F5),
            topLeft = Offset(w * 0.10f, baseY - h * 0.06f),
            size = Size(w * 0.78f, h * 0.10f),
        )

        when (type) {
            MeetingIllustration.Team -> {
                drawBlob(Offset(w * 0.50f, h * 0.38f), w * 0.20f, Color(0xFF6EA4FF))
                drawBlob(Offset(w * 0.30f, h * 0.60f), w * 0.18f, Color(0xFF72D98A))
                drawBlob(Offset(w * 0.68f, h * 0.62f), w * 0.18f, Color(0xFFFFD94A))
                drawLaptop(Offset(w * 0.50f, h * 0.72f), w * 0.44f, h * 0.24f)
                drawSparkleSmall(Offset(w * 0.18f, h * 0.28f), Color(0xFF9CC9FF))
                drawSparkleSmall(Offset(w * 0.88f, h * 0.22f), Color(0xFFFFD44C))
            }
            MeetingIllustration.Study -> {
                drawBlob(Offset(w * 0.50f, h * 0.56f), w * 0.28f, Color(0xFF9B75F2))
                drawBook(Offset(w * 0.50f, h * 0.62f), w * 0.58f, h * 0.38f)
                drawSparkleSmall(Offset(w * 0.12f, h * 0.30f), Color(0xFFFFD44C))
                drawSparkleSmall(Offset(w * 0.88f, h * 0.22f), Color(0xFFFFD44C))
            }
            MeetingIllustration.Travel -> {
                drawBlob(Offset(w * 0.42f, h * 0.63f), w * 0.25f, Color(0xFFFFD43D))
                drawPalmTree(Offset(w * 0.56f, h * 0.34f), w * 0.42f, h * 0.48f)
                drawSuitcase(Offset(w * 0.74f, h * 0.72f), w * 0.18f, h * 0.28f)
            }
            MeetingIllustration.Sports -> {
                drawBlob(Offset(w * 0.56f, h * 0.58f), w * 0.26f, Color(0xFF8BE18E))
                drawSoccerBall(Offset(w * 0.22f, h * 0.76f), w * 0.16f)
            }
            MeetingIllustration.Cooking -> {
                drawBlob(Offset(w * 0.48f, h * 0.64f), w * 0.24f, Color(0xFFFF7D97))
                drawChefHat(Offset(w * 0.48f, h * 0.28f), w * 0.40f)
                drawWhisk(Offset(w * 0.17f, h * 0.52f), w * 0.18f, h * 0.38f)
                drawBowl(Offset(w * 0.66f, h * 0.74f), w * 0.28f, h * 0.16f)
            }
            MeetingIllustration.Language -> {
                drawBlob(Offset(w * 0.50f, h * 0.64f), w * 0.25f, Color(0xFF5E98FF))
                drawHeadphone(Offset(w * 0.50f, h * 0.46f), w * 0.62f, h * 0.50f)
                drawSpeechBubble(Offset(w * 0.78f, h * 0.18f), w * 0.30f, h * 0.22f)
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBlob(center: Offset, radius: Float, color: Color) {
    drawRoundRect(
        brush = Brush.verticalGradient(listOf(color.copy(alpha = 0.92f), color)),
        topLeft = Offset(center.x - radius, center.y - radius * 1.15f),
        size = Size(radius * 2f, radius * 2.35f),
        cornerRadius = CornerRadius(radius, radius),
    )
    drawCircle(Color.Black, radius * 0.055f, Offset(center.x - radius * 0.28f, center.y - radius * 0.18f))
    drawCircle(Color.Black, radius * 0.055f, Offset(center.x + radius * 0.28f, center.y - radius * 0.18f))
    drawArc(
        color = Color.Black,
        startAngle = 20f,
        sweepAngle = 140f,
        useCenter = false,
        topLeft = Offset(center.x - radius * 0.14f, center.y - radius * 0.14f),
        size = Size(radius * 0.28f, radius * 0.22f),
        style = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round),
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLaptop(center: Offset, width: Float, height: Float) {
    drawRoundRect(Color(0xFFE1E4EC), Offset(center.x - width / 2f, center.y - height / 2f), Size(width, height), CornerRadius(5.dp.toPx()))
    drawCircle(Color.White, width * 0.04f, center)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBook(center: Offset, width: Float, height: Float) {
    drawRoundRect(Color(0xFF4B78E6), Offset(center.x - width / 2f, center.y - height / 2f), Size(width, height), CornerRadius(7.dp.toPx()))
    drawLine(Color(0xFFDDE9FF), Offset(center.x, center.y - height * 0.38f), Offset(center.x, center.y + height * 0.38f), 1.5.dp.toPx())
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPalmTree(center: Offset, width: Float, height: Float) {
    drawLine(Color(0xFF9A6B2F), Offset(center.x, center.y), Offset(center.x - width * 0.08f, center.y + height), 8.dp.toPx(), cap = StrokeCap.Round)
    listOf(-0.45f, -0.18f, 0.15f, 0.42f).forEach {
        drawLine(Color(0xFF48BD62), Offset(center.x, center.y), Offset(center.x + width * it, center.y - height * 0.18f), 8.dp.toPx(), cap = StrokeCap.Round)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSuitcase(center: Offset, width: Float, height: Float) {
    drawRoundRect(Color(0xFF4E9DFF), Offset(center.x - width / 2f, center.y - height / 2f), Size(width, height), CornerRadius(5.dp.toPx()))
    drawLine(Color(0xFF2D6FE0), Offset(center.x, center.y - height * 0.40f), Offset(center.x, center.y + height * 0.40f), 1.5.dp.toPx())
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSoccerBall(center: Offset, radius: Float) {
    drawCircle(Color.White, radius, center)
    drawCircle(Color.Black, radius * 0.30f, center)
    drawCircle(Color.Black, radius * 0.16f, Offset(center.x - radius * 0.55f, center.y - radius * 0.25f))
    drawCircle(Color.Black, radius * 0.16f, Offset(center.x + radius * 0.55f, center.y + radius * 0.15f))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawChefHat(center: Offset, width: Float) {
    drawCircle(Color.White, width * 0.18f, Offset(center.x - width * 0.18f, center.y))
    drawCircle(Color.White, width * 0.22f, center)
    drawCircle(Color.White, width * 0.18f, Offset(center.x + width * 0.18f, center.y))
    drawRoundRect(Color.White, Offset(center.x - width * 0.32f, center.y), Size(width * 0.64f, width * 0.20f), CornerRadius(6.dp.toPx()))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawWhisk(center: Offset, width: Float, height: Float) {
    drawLine(Color(0xFF838CA3), Offset(center.x, center.y), Offset(center.x, center.y + height), 2.dp.toPx(), cap = StrokeCap.Round)
    drawOval(Color.Transparent, Offset(center.x - width / 2f, center.y - height * 0.10f), Size(width, height * 0.48f), style = Stroke(1.5.dp.toPx()))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBowl(center: Offset, width: Float, height: Float) {
    drawArc(Color(0xFFD5E2EB), 0f, 180f, true, Offset(center.x - width / 2f, center.y - height / 2f), Size(width, height))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHeadphone(center: Offset, width: Float, height: Float) {
    drawArc(Color(0xFFE7F0FF), 190f, 160f, false, Offset(center.x - width / 2f, center.y - height / 2f), Size(width, height), style = Stroke(6.dp.toPx(), cap = StrokeCap.Round))
    drawRoundRect(Color.White, Offset(center.x - width * 0.42f, center.y - height * 0.02f), Size(width * 0.16f, height * 0.28f), CornerRadius(10.dp.toPx()))
    drawRoundRect(Color.White, Offset(center.x + width * 0.26f, center.y - height * 0.02f), Size(width * 0.16f, height * 0.28f), CornerRadius(10.dp.toPx()))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSpeechBubble(center: Offset, width: Float, height: Float) {
    drawRoundRect(Color(0xFF85A9FF), Offset(center.x - width / 2f, center.y - height / 2f), Size(width, height), CornerRadius(16.dp.toPx()))
    drawCircle(Color.White, width * 0.045f, Offset(center.x - width * 0.18f, center.y))
    drawCircle(Color.White, width * 0.045f, Offset(center.x, center.y))
    drawCircle(Color.White, width * 0.045f, Offset(center.x + width * 0.18f, center.y))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSparkleSmall(center: Offset, color: Color) {
    drawLine(color, Offset(center.x, center.y - 6.dp.toPx()), Offset(center.x, center.y + 6.dp.toPx()), 1.4.dp.toPx(), cap = StrokeCap.Round)
    drawLine(color, Offset(center.x - 6.dp.toPx(), center.y), Offset(center.x + 6.dp.toPx(), center.y), 1.4.dp.toPx(), cap = StrokeCap.Round)
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun MeetingsScreenPreview() {
    Moa_ProjectTheme {
        MeetingsScreen()
    }
}
