package com.example.moa_project.ui.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import com.example.moa_project.ui.components.MoaCaptionText
import com.example.moa_project.ui.components.MoaMascot
import com.example.moa_project.ui.components.MoaMascotVariant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moa_project.network.GuestParticipantDto
import com.example.moa_project.network.GuestScheduleAnalysisResponse
import com.example.moa_project.network.ScheduleAnalysisResponse
import com.example.moa_project.ui.components.MoaBodyText
import com.example.moa_project.ui.components.MoaShareBottomSheet
import com.example.moa_project.ui.components.ScheduleHeatmapCard
import com.example.moa_project.util.GuestLinkShareHelper
import com.example.moa_project.ui.theme.Moa_ProjectTheme
import com.example.moa_project.util.GuestLinkHelper
import com.example.moa_project.util.KakaoShareHelper
import com.example.moa_project.util.MoaNotificationHelper
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import com.example.moa_project.network.ReactionDto
import com.example.moa_project.network.TokenManager
import com.example.moa_project.ui.theme.MoaAccentGreen
import com.example.moa_project.ui.theme.MoaAccentOrange
import com.example.moa_project.ui.theme.MoaBlue
import com.example.moa_project.ui.theme.MoaRadius
import com.example.moa_project.ui.theme.MoaScreenBackground
import com.example.moa_project.ui.theme.MoaStatusConfirmed
import com.example.moa_project.ui.theme.MoaTextPrimary
import com.example.moa_project.ui.theme.MoaTextSecondary
import com.example.moa_project.ui.theme.SBAggroFontFamily
import com.example.moa_project.ui.theme.moaCardSurface
import com.example.moa_project.ui.theme.MoaBlueSoft
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

data class RecommendedTime(
    val rank: Int,
    val dateString: String,
    val timeString: String,
    val availableCount: Int,
    val totalCount: Int,
    val members: String
)

private fun ScheduleAnalysisResponse.toRecommendedTimes(): List<RecommendedTime> {
    return recommendations.map { dto ->
        val startDate = dto.start.split("T").firstOrNull() ?: ""
        val startTime = dto.start.split("T").lastOrNull()?.substringBeforeLast(":") ?: ""
        val endTime = dto.end.split("T").lastOrNull()?.substringBeforeLast(":") ?: ""

        RecommendedTime(
            rank = dto.rank,
            dateString = startDate,
            timeString = "$startTime - $endTime",
            availableCount = dto.availableCount,
            totalCount = totalMembers,
            members = dto.availableMembers.joinToString(", ")
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleResultScreen(
    uniqueLink: String = "DEMO",
    onBackClick: () -> Unit = {},
    onEditMyVoteClick: () -> Unit = {},
    onConfirmClick: () -> Unit = {},
    viewModel: GuestScheduleViewModel = viewModel(key = "guest_result_$uniqueLink")
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val canHostConfirm = TokenManager.isLoggedIn()

    LaunchedEffect(uniqueLink) {
        viewModel.fetchSchedule(uniqueLink)
        viewModel.fetchAnalysis(uniqueLink)
    }

    val analysis = (uiState as? GuestScheduleState.AnalysisSuccess)?.analysis
    val guestStartDate by viewModel.scheduleStartDate.collectAsState()
    val guestEndDate by viewModel.scheduleEndDate.collectAsState()
    val guestHeatmapStartDate = analysis?.startDate?.takeIf { it.isNotBlank() } ?: guestStartDate
    val guestHeatmapEndDate = analysis?.endDate?.takeIf { it.isNotBlank() } ?: guestEndDate
    val isConfirmed = analysis?.status == "CONFIRMED" || analysis?.status == "DONE"

    LaunchedEffect(uniqueLink, isConfirmed) {
        if (!isConfirmed) {
            while (true) {
                delay(15_000)
                viewModel.refreshAnalysis(uniqueLink)
            }
        }
    }

    val scheduleTitle = analysis?.title ?: "로딩 중..."
    val totalCount = analysis?.totalParticipants ?: 0
    val recommendations = analysis?.recommendations?.map { dto ->
        val startDate = dto.start.split("T").firstOrNull() ?: ""
        val startTime = dto.start.split("T").lastOrNull()?.substringBeforeLast(":") ?: ""
        val endTime = dto.end.split("T").lastOrNull()?.substringBeforeLast(":") ?: ""

        RecommendedTime(
            rank = dto.rank,
            dateString = startDate,
            timeString = "$startTime - $endTime",
            availableCount = dto.availableCount,
            totalCount = totalCount,
            members = dto.availableMembers.joinToString(", ")
        )
    } ?: emptyList()
    val isLoading = uiState is GuestScheduleState.Loading
    val webLink = remember(uniqueLink, analysis?.webLink) {
        GuestLinkHelper.resolveWebLink(uniqueLink, analysis?.webLink)
    }
    val linkReachable = remember(webLink) { GuestLinkHelper.isExternalReachable(webLink) }
    var showShareSheet by remember { mutableStateOf(false) }

    if (!isLoading && analysis != null) {
        MoaShareBottomSheet(
            visible = showShareSheet,
            onDismiss = { showShareSheet = false },
            kakaoEnabled = linkReachable,
            onKakaoClick = {
                KakaoShareHelper.shareGuestSchedule(
                    context = context,
                    scheduleTitle = analysis.title,
                    scheduleDescription = analysis.description?.takeIf { it.isNotBlank() },
                    startDate = analysis.startDate ?: "-",
                    endDate = analysis.endDate ?: "-",
                    uniqueLink = uniqueLink,
                    webLink = webLink,
                )
            },
            onCopyLinkClick = {
                GuestLinkShareHelper.copyWebLink(context, webLink)
            },
            onMoreClick = {
                GuestLinkShareHelper.openShareChooser(
                    context = context,
                    scheduleTitle = analysis.title,
                    scheduleDescription = analysis.description?.takeIf { it.isNotBlank() },
                    startDate = analysis.startDate ?: "-",
                    endDate = analysis.endDate ?: "-",
                    webLink = webLink,
                )
            },
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "조율 결과",
                        fontFamily = SBAggroFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MoaTextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = MoaTextPrimary
                        )
                    }
                },
                actions = {
                    if (!isLoading && analysis != null) {
                        IconButton(onClick = { showShareSheet = true }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "공유하기",
                                tint = MoaBlue,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                ),
                modifier = Modifier.shadow(elevation = 2.dp, spotColor = Color(0x1A000000))
            )
        },
        containerColor = MoaScreenBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 24.dp,
                bottom = 24.dp
            )
        ) {
            // 제목 + 설명
            item {
                Text(
                    text = scheduleTitle,
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = MoaTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                MoaBodyText(
                    text = when {
                        isLoading -> "참여자 응답을 불러오는 중..."
                        isConfirmed -> "확정된 일정입니다. 참여자에게 공유된 링크에서도 확인할 수 있어요."
                        totalCount == 0 -> "링크를 공유하고 참여자들의 가능 시간을 기다려주세요."
                        else -> "참여자 ${totalCount}명 · 날짜·시간별 가능 인원과 추천 시간을 확인하세요."
                    },
                    fontSize = 14.sp,
                    color = MoaTextSecondary
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            if (!isLoading && analysis != null && !isConfirmed) {
                item {
                    EditMyVoteCard(onClick = onEditMyVoteClick)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                item {
                    GuestSharePromptCard(
                        linkReachable = linkReachable,
                        onShareClick = { showShareSheet = true },
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            if (isConfirmed && analysis != null) {
                item {
                    GuestConfirmedCard(
                        title = scheduleTitle,
                        confirmedStart = analysis.confirmedStart,
                        confirmedEnd = analysis.confirmedEnd,
                        isDone = analysis.status == "DONE"
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            if (!isLoading && totalCount > 0) {
                item {
                    GuestParticipantsCard(analysis?.participants.orEmpty())
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // 히트맵 (상단)
            item {
                ScheduleHeatmapCard(
                    heatmap = analysis?.heatmap,
                    heatmapMembers = analysis?.heatmapMembers,
                    participants = analysis?.participants,
                    totalMembers = analysis?.totalParticipants ?: 0,
                    allMemberNames = analysis?.participants?.map { it.name },
                    startDate = guestHeatmapStartDate,
                    endDate = guestHeatmapEndDate,
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (!isConfirmed) {
                item {
                    Text(
                        text = "추천 시간대",
                        fontFamily = SBAggroFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = MoaTextPrimary,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    MoaCaptionText(
                        text = "추천 시간을 고른 뒤, 확정할 시간(1시간 이상)을 선택할 수 있어요.",
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            if (isLoading) {
                item {
                    Text(
                        text = "분석 결과를 불러오는 중...",
                        color = MoaTextSecondary,
                        fontFamily = SBAggroFontFamily,
                        fontSize = 14.sp
                    )
                }
            } else if (isConfirmed) {
                // 확정 후에는 추천·확정 버튼 숨김
            } else if (recommendations.isEmpty()) {
                item {
                    EmptyGuestResultCard(
                        message = if (totalCount == 0) {
                            "아직 참여자가 없어요.\n링크를 공유하면 브라우저에서 이름과 가능 시간을 입력할 수 있어요."
                        } else {
                            "참여자는 있지만 겹치는 시간을 찾지 못했어요."
                        }
                    )
                }
            } else {
                items(recommendations.size) { index ->
                    val raw = analysis?.recommendations?.getOrNull(index)
                    RecommendationCard(
                        recommendation = recommendations[index],
                        startIso = raw?.start.orEmpty(),
                        heatmap = analysis?.heatmap,
                        isTopRank = index == 0,
                        showHostConfirm = canHostConfirm,
                        onConfirmClick = { start, end ->
                            if (raw != null && canHostConfirm) {
                                viewModel.confirm(uniqueLink, start, end) {
                                    val timeText = ScheduleConfirmHelper.formatTimeRange(
                                        start,
                                        ScheduleConfirmHelper.durationHoursBetween(start, end),
                                    )
                                    val notificationKey = "guest-${analysis?.scheduleId ?: uniqueLink}"
                                    MoaNotificationHelper.notifyScheduleConfirmed(
                                        context,
                                        analysis?.title ?: scheduleTitle,
                                        timeText,
                                        notificationKey = notificationKey,
                                        targetRoute = "schedule_result/$uniqueLink",
                                    )
                                    viewModel.fetchAnalysis(uniqueLink)
                                    onConfirmClick()
                                }
                            }
                        },
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupScheduleResultScreen(
    scheduleId: Long,
    onBackClick: () -> Unit = {},
    onSaveClick: () -> Unit = {},
    onEditMyVoteClick: () -> Unit = {},
    onConfirmComplete: () -> Unit = {},
    viewModel: ScheduleViewModel = viewModel(
        factory = ScheduleViewModel.Factory(scheduleId),
        key = "schedule_result_$scheduleId"
    )
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val reactions by viewModel.reactions.collectAsState()
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(scheduleId) {
        viewModel.fetchDetail()
        viewModel.fetchMyTimeSlots()
        viewModel.fetchAnalysis()
        viewModel.fetchReactions()
    }

    val analysis = (uiState as? ScheduleState.AnalysisSuccess)?.analysis
    val scheduleStatus by viewModel.scheduleStatus.collectAsState()
    val canEditVote = scheduleStatus in listOf("WAITING", "ADJUSTING")
    val scheduleStartDate by viewModel.scheduleStartDate.collectAsState()
    val scheduleEndDate by viewModel.scheduleEndDate.collectAsState()
    val heatmapStartDate = analysis?.startDate?.takeIf { it.isNotBlank() } ?: scheduleStartDate
    val heatmapEndDate = analysis?.endDate?.takeIf { it.isNotBlank() } ?: scheduleEndDate
    val recommendations = analysis?.toRecommendedTimes() ?: emptyList()
    val isLoading = uiState is ScheduleState.Loading

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "조율 결과",
                        fontFamily = SBAggroFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MoaTextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = MoaTextPrimary
                        )
                    }
                },
                actions = {
                    TextButton(
                        enabled = !isSaving && !isLoading,
                        onClick = {
                            isSaving = true
                            viewModel.saveProgress {
                                isSaving = false
                                android.widget.Toast.makeText(
                                    context,
                                    "선택한 시간이 저장됐어요.",
                                    android.widget.Toast.LENGTH_SHORT,
                                ).show()
                                viewModel.fetchAnalysis()
                                onSaveClick()
                            }
                        },
                    ) {
                        Text(
                            text = if (isSaving) "저장 중..." else "저장하기",
                            fontFamily = SBAggroFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MoaBlue,
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White),
                modifier = Modifier.shadow(elevation = 2.dp, spotColor = Color(0x1A000000))
            )
        },
        containerColor = MoaScreenBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 24.dp,
                bottom = 24.dp
            )
        ) {
            // 제목 + 설명
            item {
                Text(
                    text = analysis?.title ?: "조율 결과 불러오는 중",
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = MoaTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "멤버들의 가능 시간을 바탕으로 추천합니다.",
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = MoaTextSecondary
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            if (canEditVote) {
                item {
                    EditMyVoteCard(onClick = onEditMyVoteClick)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // 히트맵 (상단)
            item {
                ScheduleHeatmapCard(
                    heatmap = analysis?.heatmap,
                    heatmapMembers = analysis?.heatmapMembers,
                    totalMembers = analysis?.totalMembers ?: 0,
                    allMemberNames = analysis?.allMembers,
                    startDate = heatmapStartDate,
                    endDate = heatmapEndDate,
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Text(
                    text = "추천 시간대",
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = MoaTextPrimary,
                )
                Spacer(modifier = Modifier.height(6.dp))
                MoaCaptionText(
                    text = "추천 시간을 고른 뒤, 확정할 시간(1시간 이상)을 선택할 수 있어요.",
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (isLoading) {
                item {
                    Text(
                        text = "분석 결과를 불러오는 중...",
                        color = MoaTextSecondary,
                        fontFamily = SBAggroFontFamily,
                        fontSize = 14.sp
                    )
                }
            } else if (recommendations.isEmpty()) {
                item {
                    Text(
                        text = "아직 멤버 응답이 없거나 추천 시간을 계산할 수 없습니다.",
                        color = MoaTextSecondary,
                        fontFamily = SBAggroFontFamily,
                        fontSize = 14.sp
                    )
                }
            } else {
                items(recommendations.size) { index ->
                    val recommendation = recommendations[index]
                    val raw = analysis?.recommendations?.getOrNull(index)
                    RecommendationCard(
                        recommendation = recommendation,
                        startIso = raw?.start.orEmpty(),
                        heatmap = analysis?.heatmap,
                        isTopRank = index == 0,
                        reactions = reactions,
                        myUserId = TokenManager.getUserId(),
                        onReactionClick = { viewModel.toggleReaction(it) },
                        onConfirmClick = { start, end ->
                            if (raw != null) {
                                viewModel.confirm(
                                    start = start,
                                    end = end,
                                ) {
                                    val timeText = ScheduleConfirmHelper.formatTimeRange(
                                        start,
                                        ScheduleConfirmHelper.durationHoursBetween(start, end),
                                    )
                                    val notificationKey = "sch-$scheduleId"
                                    MoaNotificationHelper.notifyScheduleConfirmed(
                                        context,
                                        analysis?.title ?: "일정",
                                        timeText,
                                        notificationKey = notificationKey,
                                    )
                                    onConfirmComplete()
                                }
                            }
                        },
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun EditMyVoteCard(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "내 가능 시간 수정",
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MoaTextPrimary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "이미 투표했어도 다시 선택해서 수정할 수 있어요",
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = MoaTextSecondary,
            )
        }
        Text(
            text = "수정하기",
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = MoaBlue,
        )
    }
}

@Composable
private fun RecommendationCard(
    recommendation: RecommendedTime,
    startIso: String,
    heatmap: Map<String, Map<String, Int>>? = null,
    isTopRank: Boolean,
    showHostConfirm: Boolean = true,
    reactions: List<ReactionDto> = emptyList(),
    myUserId: Long = -1L,
    onReactionClick: (String) -> Unit = {},
    onConfirmClick: (start: String, end: String) -> Unit,
) {
    var durationHours by remember(startIso) { mutableIntStateOf(1) }
    val maxHours = remember(startIso, heatmap, recommendation.availableCount) {
        ScheduleConfirmHelper.maxConsecutiveHours(
            startIso = startIso,
            heatmap = heatmap,
            minAvailableCount = recommendation.availableCount,
        )
    }
    LaunchedEffect(maxHours) {
        if (durationHours > maxHours) durationHours = maxHours
    }
    val timeRangeLabel = remember(startIso, durationHours) {
        if (startIso.isBlank()) recommendation.timeString
        else ScheduleConfirmHelper.formatTimeRange(startIso, durationHours)
    }
    val availabilityRatio = if (recommendation.totalCount > 0) {
        recommendation.availableCount.toFloat() / recommendation.totalCount
    } else 0f
    val memberNames = remember(recommendation.members) {
        recommendation.members.split(",").map { it.trim() }.filter { it.isNotBlank() }
    }
    val dateLabel = formatRecommendationDate(recommendation.dateString)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (isTopRank) Color(0xFFF4F8FF) else Color.White)
            .border(
                width = if (isTopRank) 1.5.dp else 1.dp,
                color = if (isTopRank) MoaBlue.copy(alpha = 0.35f) else Color(0xFFE8ECF4),
                shape = RoundedCornerShape(18.dp),
            )
            .padding(18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RankBadge(rank = recommendation.rank, isTopRank = isTopRank)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dateLabel,
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = MoaTextSecondary,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = timeRangeLabel,
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isTopRank) 22.sp else 18.sp,
                    color = MoaTextPrimary,
                )
            }
            AvailabilityRing(
                available = recommendation.availableCount,
                total = recommendation.totalCount,
                ratio = availabilityRatio,
                highlight = isTopRank,
            )
        }

        if (memberNames.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                memberNames.take(5).forEach { name ->
                    Text(
                        text = name,
                        fontFamily = SBAggroFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = MoaBlue,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(MoaBlueSoft)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
                if (memberNames.size > 5) {
                    Text(
                        text = "+${memberNames.size - 5}",
                        fontFamily = SBAggroFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MoaTextSecondary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFF0F2F8))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
            }
        }

        if (showHostConfirm && maxHours > 1) {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "확정 길이",
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = MoaTextSecondary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                (1..maxHours.coerceAtMost(4)).forEach { hours ->
                    val selected = durationHours == hours
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) MoaBlue else Color(0xFFF0F2F8))
                            .clickable { durationHours = hours }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "${hours}시간",
                            fontFamily = SBAggroFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (selected) Color.White else MoaTextPrimary,
                        )
                    }
                }
            }
        }

        if (showHostConfirm) {
            Spacer(modifier = Modifier.height(16.dp))
            if (isTopRank) {
                Button(
                    onClick = {
                        if (startIso.isNotBlank()) {
                            onConfirmClick(
                                startIso,
                                ScheduleConfirmHelper.buildEndTime(startIso, durationHours),
                            )
                        }
                    },
                    enabled = startIso.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = MoaBlue),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                ) {
                    Text(
                        text = "이 시간으로 확정하기",
                        fontFamily = SBAggroFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White,
                    )
                }
            } else {
                OutlinedButton(
                    onClick = {
                        if (startIso.isNotBlank()) {
                            onConfirmClick(
                                startIso,
                                ScheduleConfirmHelper.buildEndTime(startIso, durationHours),
                            )
                        }
                    },
                    enabled = startIso.isNotBlank(),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                ) {
                    Text(
                        text = "이 시간으로 확정",
                        fontFamily = SBAggroFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MoaBlue,
                    )
                }
            }
        }
    }
}

@Composable
private fun RankBadge(rank: Int, isTopRank: Boolean) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(if (isTopRank) MoaBlue else Color(0xFFE8ECF4)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (isTopRank) "1st" else "${rank}위",
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = if (isTopRank) 12.sp else 11.sp,
            color = if (isTopRank) Color.White else MoaTextSecondary,
        )
    }
}

@Composable
private fun AvailabilityRing(
    available: Int,
    total: Int,
    ratio: Float,
    highlight: Boolean,
) {
    Box(contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = { ratio.coerceIn(0f, 1f) },
            modifier = Modifier.size(52.dp),
            color = if (highlight) MoaBlue else MoaBlue.copy(alpha = 0.55f),
            trackColor = Color(0xFFE8ECF4),
            strokeWidth = 5.dp,
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$available",
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MoaTextPrimary,
            )
            Text(
                text = "/$total",
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp,
                color = MoaTextSecondary,
            )
        }
    }
}

private fun formatRecommendationDate(isoDate: String): String =
    runCatching {
        val date = LocalDate.parse(isoDate)
        val dow = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN)
        "${date.monthValue}월 ${date.dayOfMonth}일 ($dow)"
    }.getOrDefault(isoDate)

@Composable
private fun GuestSharePromptCard(
    linkReachable: Boolean,
    onShareClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "팀원들에게 링크를 공유해보세요!",
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MoaTextPrimary,
                    lineHeight = 22.sp,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (linkReachable) {
                        "카카오톡·링크복사로 가능한 시간을 받아보세요."
                    } else {
                        "WEB_SHARE_URL 설정 후 외부 공유가 가능해요."
                    },
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = if (linkReachable) MoaTextSecondary else MoaAccentOrange,
                    lineHeight = 18.sp,
                )
            }
            MoaMascot(size = 56.dp, variant = MoaMascotVariant.Sparkle)
        }
        Spacer(modifier = Modifier.height(14.dp))
        Button(
            onClick = onShareClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MoaBlue),
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "공유하기",
                color = Color.White,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
            )
        }
    }
}

@Composable
private fun EmptyGuestResultCard(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MoaMascot(size = 52.dp, variant = MoaMascotVariant.Sparkle)
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = message,
                color = MoaTextSecondary,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 22.sp,
            )
        }
    }
}

@Composable
private fun GuestConfirmedCard(
    title: String,
    confirmedStart: String?,
    confirmedEnd: String?,
    isDone: Boolean,
) {
    val dateStr = confirmedStart?.split("T")?.firstOrNull()?.let { d ->
        val parts = d.split("-")
        if (parts.size == 3) "${parts[1]}월 ${parts[2]}일" else d
    } ?: "-"
    val startTime = confirmedStart?.split("T")?.getOrNull(1)?.take(5) ?: ""
    val endTime = confirmedEnd?.split("T")?.getOrNull(1)?.take(5) ?: ""

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF2179FE))
            .padding(20.dp)
    ) {
        Text(
            text = if (isDone) "완료된 일정" else "일정 확정",
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.9f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = dateStr,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = Color.White,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Icon(Icons.Default.Schedule, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "$startTime ~ $endTime",
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = Color.White,
            )
        }
    }
}

@Composable
private fun GuestParticipantsCard(participants: List<GuestParticipantDto>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = Color(0xFFDDE4F2))
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Groups, contentDescription = null, tint = MoaBlue, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "참여 현황",
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MoaTextPrimary,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        MoaBodyText(
            text = "누가 몇 시에 가능한지 확인하세요",
            fontSize = 12.sp,
            color = MoaTextSecondary
        )
        Spacer(modifier = Modifier.height(12.dp))
        participants.forEach { p ->
            val times = p.slots.orEmpty().mapNotNull { slot ->
                val start = slot.start.split("T")
                if (start.size == 2) "${start[0].substring(5)} ${start[1].take(5)}" else null
            }.distinct().sorted().take(6)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                MoaBodyText(
                    text = "${p.name} · ${p.slotCount}개 시간 선택",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MoaTextPrimary
                )
                if (times.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    MoaBodyText(
                        text = times.joinToString("  "),
                        fontSize = 12.sp,
                        color = MoaTextSecondary,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
fun ScheduleResultScreenPreview() {
    Moa_ProjectTheme {
        ScheduleResultScreen()
    }
}
