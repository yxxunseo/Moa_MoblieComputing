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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import com.example.moa_project.ui.components.MoaCaptionText
import com.example.moa_project.ui.components.MoaMascot
import com.example.moa_project.ui.components.MoaMascotVariant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.moa_project.network.TokenManager
import com.example.moa_project.ui.theme.MoaAccentGreen
import com.example.moa_project.ui.theme.MoaBlue
import com.example.moa_project.ui.theme.MoaError
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
                if (isConfirmed) {
                    KakaoShareHelper.shareGuestConfirmedSchedule(
                        context = context,
                        scheduleTitle = analysis.title,
                        confirmedStart = analysis.confirmedStart,
                        confirmedEnd = analysis.confirmedEnd,
                        webLink = webLink,
                    )
                } else {
                    KakaoShareHelper.shareGuestSchedule(
                        context = context,
                        scheduleTitle = analysis.title,
                        scheduleDescription = analysis.description?.takeIf { it.isNotBlank() },
                        startDate = analysis.startDate ?: "-",
                        endDate = analysis.endDate ?: "-",
                        uniqueLink = uniqueLink,
                        webLink = webLink,
                    )
                }
            },
            onCopyLinkClick = {
                if (isConfirmed) {
                    GuestLinkShareHelper.copyConfirmedShareText(
                        context = context,
                        scheduleTitle = analysis.title,
                        confirmedStart = analysis.confirmedStart,
                        confirmedEnd = analysis.confirmedEnd,
                        webLink = webLink,
                    )
                } else {
                    GuestLinkShareHelper.copyWebLink(context, webLink)
                }
            },
            onMoreClick = {
                if (isConfirmed) {
                    GuestLinkShareHelper.shareConfirmedSchedule(
                        context = context,
                        scheduleTitle = analysis.title,
                        confirmedStart = analysis.confirmedStart,
                        confirmedEnd = analysis.confirmedEnd,
                        webLink = webLink,
                    )
                } else {
                    GuestLinkShareHelper.openShareChooser(
                        context = context,
                        scheduleTitle = analysis.title,
                        scheduleDescription = analysis.description?.takeIf { it.isNotBlank() },
                        startDate = analysis.startDate ?: "-",
                        endDate = analysis.endDate ?: "-",
                        webLink = webLink,
                    )
                }
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
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            if (isConfirmed && analysis != null) {
                item {
                    GuestConfirmedCard(
                        title = scheduleTitle,
                        confirmedStart = analysis.confirmedStart,
                        confirmedEnd = analysis.confirmedEnd,
                        isDone = analysis.status == "DONE",
                        onShareClick = { showShareSheet = true },
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
                item {
                    val slots = recommendations.mapIndexed { index, rec ->
                        RecommendationSlot(
                            recommendation = rec,
                            startIso = analysis?.recommendations?.getOrNull(index)?.start.orEmpty(),
                        )
                    }
                    val participantNames = analysis?.participants?.map { it.name }.orEmpty()
                    RecommendationsSection(
                        slots = slots,
                        participatedCount = totalCount,
                        totalCount = totalCount.coerceAtLeast(1),
                        participantSummary = participantNames.joinToString(", ").ifBlank { null },
                        heatmap = analysis?.heatmap,
                        showHostConfirm = canHostConfirm,
                        onConfirmClick = { start, end ->
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
                        },
                        onSuggestOtherTime = onEditMyVoteClick.takeIf { !isConfirmed },
                    )
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
    onScheduleDeleted: () -> Unit = {},
    viewModel: ScheduleViewModel = viewModel(
        factory = ScheduleViewModel.Factory(scheduleId),
        key = "schedule_result_$scheduleId"
    )
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var isSaving by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }

    LaunchedEffect(scheduleId) {
        viewModel.fetchDetail()
        viewModel.fetchMyTimeSlots()
        viewModel.fetchAnalysis()
    }

    val errorMessage by viewModel.errorMessage.collectAsState()
    LaunchedEffect(errorMessage) {
        val message = errorMessage ?: return@LaunchedEffect
        android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
        viewModel.clearErrorMessage()
    }

    val analysis by viewModel.displayAnalysis.collectAsState()
    val scheduleStatus by viewModel.scheduleStatus.collectAsState()
    val scheduleTitle by viewModel.scheduleTitle.collectAsState()
    val confirmedStart by viewModel.confirmedStart.collectAsState()
    val confirmedEnd by viewModel.confirmedEnd.collectAsState()
    val canConfirm by viewModel.canConfirm.collectAsState()
    val isConfirming by viewModel.isConfirming.collectAsState()
    val isDone = scheduleStatus == "DONE"
    val isConfirmed = isDone ||
        scheduleStatus == "CONFIRMED" ||
        uiState is ScheduleState.ConfirmSuccess
    val canEditVote = scheduleStatus in listOf("WAITING", "ADJUSTING")
    val scheduleStartDate by viewModel.scheduleStartDate.collectAsState()
    val scheduleEndDate by viewModel.scheduleEndDate.collectAsState()
    val heatmapStartDate = analysis?.startDate?.takeIf { it.isNotBlank() } ?: scheduleStartDate
    val heatmapEndDate = analysis?.endDate?.takeIf { it.isNotBlank() } ?: scheduleEndDate
    val recommendations = analysis?.toRecommendedTimes() ?: emptyList()
    val respondedCount by viewModel.respondedCount.collectAsState()
    val totalMembers by viewModel.totalMembers.collectAsState()
    val isLoading = uiState is ScheduleState.Loading && analysis == null

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { if (!isDeleting) showDeleteDialog = false },
            title = {
                Text("일정 삭제", fontFamily = SBAggroFontFamily, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "완료된 일정 \"${scheduleTitle ?: ""}\"을(를) 삭제할까요?\n삭제하면 복구할 수 없습니다.",
                    fontFamily = SBAggroFontFamily,
                    fontSize = 14.sp,
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !isDeleting,
                    onClick = {
                        isDeleting = true
                        viewModel.deleteSchedule(
                            onSuccess = {
                                isDeleting = false
                                showDeleteDialog = false
                                android.widget.Toast.makeText(
                                    context,
                                    "일정이 삭제되었습니다.",
                                    android.widget.Toast.LENGTH_SHORT,
                                ).show()
                                onScheduleDeleted()
                            },
                            onError = { msg ->
                                isDeleting = false
                                android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                            },
                        )
                    },
                ) {
                    Text("삭제", color = MoaError, fontFamily = SBAggroFontFamily)
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !isDeleting,
                    onClick = { showDeleteDialog = false },
                ) {
                    Text("취소", fontFamily = SBAggroFontFamily)
                }
            },
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (isDone) "완료된 일정" else "조율 결과",
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
                    if (isDone) {
                        IconButton(
                            enabled = !isDeleting,
                            onClick = { showDeleteDialog = true },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "일정 삭제",
                                tint = MoaError,
                            )
                        }
                    } else {
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
                    text = scheduleTitle ?: analysis?.title ?: "조율 결과 불러오는 중",
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = MoaTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = when {
                        isDone -> "종료된 일정입니다. 필요 없으면 삭제할 수 있어요."
                        isConfirmed -> "확정된 일정입니다."
                        else -> "멤버들의 가능 시간을 바탕으로 추천합니다."
                    },
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = MoaTextSecondary
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            if (isConfirmed && !confirmedStart.isNullOrBlank()) {
                item {
                    GuestConfirmedCard(
                        title = scheduleTitle ?: analysis?.title.orEmpty(),
                        confirmedStart = confirmedStart,
                        confirmedEnd = confirmedEnd,
                        isDone = isDone,
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }
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

            if (isLoading) {
                item {
                    Text(
                        text = "분석 결과를 불러오는 중...",
                        color = MoaTextSecondary,
                        fontFamily = SBAggroFontFamily,
                        fontSize = 14.sp
                    )
                }
            } else if (!isConfirmed) {
                if (recommendations.isEmpty()) {
                    item {
                        Text(
                            text = "아직 멤버 응답이 없거나 추천 시간을 계산할 수 없습니다.",
                            color = MoaTextSecondary,
                            fontFamily = SBAggroFontFamily,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    item {
                        val slots = recommendations.mapIndexed { index, rec ->
                            RecommendationSlot(
                                recommendation = rec,
                                startIso = analysis?.recommendations?.getOrNull(index)?.start.orEmpty(),
                            )
                        }
                        val memberSummary = analysis?.allMembers?.joinToString(", ")
                            ?: slots.flatMap { slot ->
                                slot.recommendation.members.split(",").map { it.trim() }
                            }.filter { it.isNotBlank() }.distinct().joinToString(", ")
                        RecommendationsSection(
                            slots = slots,
                            participatedCount = respondedCount.coerceAtLeast(0),
                            totalCount = totalMembers.coerceAtLeast(analysis?.totalMembers ?: 1),
                            participantSummary = memberSummary.ifBlank { null },
                            heatmap = analysis?.heatmap,
                            showHostConfirm = canConfirm,
                            isConfirming = isConfirming,
                            onConfirmClick = { start, end ->
                                viewModel.confirm(start = start, end = end) {
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
                            },
                            onSuggestOtherTime = onEditMyVoteClick.takeIf { canEditVote },
                        )
                        if (!canConfirm) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "일정을 만든 사람만 확정할 수 있어요.",
                                color = MoaTextSecondary,
                                fontFamily = SBAggroFontFamily,
                                fontSize = 13.sp,
                            )
                        }
                    }
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

private data class RecommendationSlot(
    val recommendation: RecommendedTime,
    val startIso: String,
)

@Composable
private fun RecommendationsSection(
    slots: List<RecommendationSlot>,
    participatedCount: Int,
    totalCount: Int,
    participantSummary: String?,
    heatmap: Map<String, Map<String, Int>>?,
    showHostConfirm: Boolean,
    isConfirming: Boolean = false,
    onConfirmClick: (start: String, end: String) -> Unit,
    onSuggestOtherTime: (() -> Unit)? = null,
) {
    var selectedIndex by remember(slots.size) { mutableIntStateOf(0) }
    val selected = slots.getOrNull(selectedIndex) ?: return
    var durationHours by remember(selectedIndex) { mutableIntStateOf(1) }

    val maxHours = remember(selected.startIso, heatmap, selected.recommendation.availableCount) {
        ScheduleConfirmHelper.maxConsecutiveHours(
            startIso = selected.startIso,
            heatmap = heatmap,
            minAvailableCount = selected.recommendation.availableCount,
        )
    }
    LaunchedEffect(selectedIndex, maxHours) {
        durationHours = 1.coerceIn(1, maxHours)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "추천 시간대",
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = MoaTextPrimary,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "투표 결과를 바탕으로 모두가 가능한 시간을 추천해드려요.",
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = MoaTextSecondary,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Groups,
                contentDescription = null,
                tint = MoaTextSecondary,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = buildString {
                    append("참여 $participatedCount/$totalCount")
                    if (!participantSummary.isNullOrBlank()) {
                        append(" · 참여자 $participantSummary")
                    }
                },
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = MoaTextSecondary,
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        slots.forEachIndexed { index, slot ->
            RecommendationSelectableCard(
                slot = slot,
                selected = index == selectedIndex,
                onSelect = { selectedIndex = index },
            )
            if (index < slots.lastIndex) {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        if (showHostConfirm) {
            Spacer(modifier = Modifier.height(20.dp))
            if (maxHours > 1) {
                Text(
                    text = "확정 길이",
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MoaTextPrimary,
                )
                Spacer(modifier = Modifier.height(10.dp))
                DurationSegmentRow(
                    maxHours = maxHours.coerceAtMost(4),
                    selectedHours = durationHours,
                    onSelect = { durationHours = it },
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
            Button(
                onClick = {
                    if (selected.startIso.isNotBlank() && !isConfirming) {
                        onConfirmClick(
                            selected.startIso,
                            ScheduleConfirmHelper.buildEndTime(selected.startIso, durationHours),
                        )
                    }
                },
                enabled = selected.startIso.isNotBlank() && !isConfirming,
                colors = ButtonDefaults.buttonColors(containerColor = MoaBlue),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                Text(
                    text = if (isConfirming) "확정 중..." else "이 시간으로 확정하기",
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White,
                )
            }
            if (onSuggestOtherTime != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "다른 시간 제안하기",
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = MoaTextSecondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onSuggestOtherTime)
                        .padding(vertical = 8.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun RecommendationSelectableCard(
    slot: RecommendationSlot,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    val recommendation = slot.recommendation
    val memberNames = remember(recommendation.members) {
        recommendation.members.split(",").map { it.trim() }.filter { it.isNotBlank() }
    }
    val dateLabel = formatRecommendationDate(recommendation.dateString)
    val timeLabel = if (slot.startIso.isNotBlank()) {
        ScheduleConfirmHelper.formatTimeRange(slot.startIso, 1)
    } else {
        recommendation.timeString
    }
    val cardBg = if (selected) Color(0xFFEEF4FF) else Color.White
    val cardBorder = if (selected) MoaBlue.copy(alpha = 0.45f) else Color(0xFFE3E8F0)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(width = if (selected) 1.5.dp else 1.dp, color = cardBorder, shape = RoundedCornerShape(16.dp))
            .clickable(onClick = onSelect)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                RecommendationRankBadge(rank = recommendation.rank, selected = selected)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = dateLabel,
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = MoaTextSecondary,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = timeLabel,
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = MoaTextPrimary,
                )
            }
            RecommendationRadio(selected = selected)
        }
        Spacer(modifier = Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFFE8ECF4)),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Groups,
                    contentDescription = null,
                    tint = MoaTextSecondary,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${recommendation.availableCount}/${recommendation.totalCount} 참여",
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = MoaTextSecondary,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                memberNames.take(2).forEach { name ->
                    ParticipantNameChip(name = name)
                }
                if (memberNames.size > 2) {
                    ParticipantNameChip(name = "+${memberNames.size - 2}")
                }
            }
        }
    }
}

@Composable
private fun RecommendationRankBadge(rank: Int, selected: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) MoaBlueSoft else Color(0xFFF0F2F8))
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(
            text = "추천 $rank",
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = if (selected) MoaBlue else MoaTextSecondary,
        )
    }
}

@Composable
private fun RecommendationRadio(selected: Boolean) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .border(
                width = 2.dp,
                color = if (selected) MoaBlue else Color(0xFFD0D5DF),
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(MoaBlue),
            )
        }
    }
}

@Composable
private fun ParticipantNameChip(name: String) {
    Text(
        text = name,
        fontFamily = SBAggroFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        color = MoaTextSecondary,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFF3F5FA))
            .padding(horizontal = 10.dp, vertical = 5.dp),
    )
}

@Composable
private fun DurationSegmentRow(
    maxHours: Int,
    selectedHours: Int,
    onSelect: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF0F2F8))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        (1..maxHours).forEach { hours ->
            val selected = selectedHours == hours
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selected) Color.White else Color.Transparent)
                    .clickable { onSelect(hours) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "${hours}시간",
                    fontFamily = SBAggroFontFamily,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 14.sp,
                    color = if (selected) MoaBlue else MoaTextSecondary,
                )
            }
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
    onShareClick: () -> Unit = {},
) {
    val dateStr = confirmedStart?.split("T")?.firstOrNull()?.let { d ->
        val parts = d.split("-")
        if (parts.size == 3) "${parts[1].toInt()}월 ${parts[2].toInt()}일" else d
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
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onShareClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = MoaBlue,
            ),
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = null,
                tint = MoaBlue,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "공유하기",
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MoaBlue,
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
