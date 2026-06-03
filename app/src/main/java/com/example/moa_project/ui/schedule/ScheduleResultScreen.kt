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
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.moa_project.ui.components.Moa3DIconType
import com.example.moa_project.ui.components.MoaCaptionText
import com.example.moa_project.ui.components.MoaMascot
import com.example.moa_project.ui.components.MoaSectionTitle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.runtime.remember
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
import com.example.moa_project.ui.components.ScheduleHeatmapCard
import com.example.moa_project.ui.theme.Moa_ProjectTheme
import com.example.moa_project.util.MoaNotificationHelper
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import com.example.moa_project.network.ReactionDto
import com.example.moa_project.network.TokenManager
import com.example.moa_project.ui.theme.SBAggroFontFamily

private val MoaBlue = Color(0xFF2179FE)
private val ScreenBackground = Color(0xFFF7F8FC)
private val TextPrimary = Color(0xFF101B33)
private val TextSecondary = Color(0xFF737C99)

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
    onConfirmClick: () -> Unit = {},
    viewModel: GuestScheduleViewModel = viewModel(key = "guest_result_$uniqueLink")
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val canHostConfirm = TokenManager.isLoggedIn()

    LaunchedEffect(uniqueLink) {
        viewModel.fetchAnalysis(uniqueLink)
    }

    val analysis = (uiState as? GuestScheduleState.AnalysisSuccess)?.analysis
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
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "조율 결과",
                        fontFamily = SBAggroFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                ),
                modifier = Modifier.shadow(elevation = 2.dp, spotColor = Color(0x1A000000))
            )
        },
        containerColor = ScreenBackground
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
                    color = TextPrimary
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
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(20.dp))
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
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (!isConfirmed) {
                item {
                    MoaSectionTitle(title = "추천 시간대", iconType = Moa3DIconType.Trophy)
                    Spacer(modifier = Modifier.height(6.dp))
                    MoaCaptionText(
                        text = "가능 인원이 같으면 더 이른 시간을 1순위로 표시해요.",
                        modifier = Modifier.padding(start = 40.dp),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            if (isLoading) {
                item {
                    Text(
                        text = "분석 결과를 불러오는 중...",
                        color = TextSecondary,
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
                    RecommendationCard(
                        recommendation = recommendations[index],
                        isTopRank = index == 0,
                        showHostConfirm = canHostConfirm,
                        onConfirmClick = {
                            val raw = analysis?.recommendations?.getOrNull(index)
                            if (raw != null && canHostConfirm) {
                                viewModel.confirm(uniqueLink, raw.start, raw.end) {
                                    val timeText = raw.start.split("T").let {
                                        "${it.firstOrNull()?.substringAfterLast("-") ?: ""} ${it.lastOrNull()?.substringBeforeLast(":") ?: ""}"
                                    }
                                    MoaNotificationHelper.notifyScheduleConfirmed(
                                        context,
                                        analysis?.title ?: scheduleTitle,
                                        timeText
                                    )
                                    MoaNotificationHelper.notifyCalendarAdded(
                                        context,
                                        analysis?.title ?: scheduleTitle,
                                        timeText
                                    )
                                    viewModel.fetchAnalysis(uniqueLink)
                                    onConfirmClick()
                                }
                            }
                        }
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
    onConfirmComplete: () -> Unit = {},
    viewModel: ScheduleViewModel = viewModel(
        factory = ScheduleViewModel.Factory(scheduleId),
        key = "schedule_result_$scheduleId"
    )
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val reactions by viewModel.reactions.collectAsState()
    val syncGoogle = remember {
        context.getSharedPreferences("moa_settings", Context.MODE_PRIVATE)
            .getBoolean("google_calendar", false)
    }

    LaunchedEffect(scheduleId) {
        viewModel.fetchAnalysis()
        viewModel.fetchReactions()
    }

    val analysis = (uiState as? ScheduleState.AnalysisSuccess)?.analysis
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
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White),
                modifier = Modifier.shadow(elevation = 2.dp, spotColor = Color(0x1A000000))
            )
        },
        containerColor = ScreenBackground
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
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "멤버들의 가능 시간을 바탕으로 추천합니다.",
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // 히트맵 (상단)
            item {
                ScheduleHeatmapCard(
                    heatmap = analysis?.heatmap,
                    heatmapMembers = analysis?.heatmapMembers,
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                MoaSectionTitle(title = "추천 시간대", iconType = Moa3DIconType.Trophy)
                Spacer(modifier = Modifier.height(6.dp))
                MoaCaptionText(
                    text = "가능 인원이 같으면 더 이른 시간을 1순위로 표시해요.",
                    modifier = Modifier.padding(start = 40.dp),
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (isLoading) {
                item {
                    Text(
                        text = "분석 결과를 불러오는 중...",
                        color = TextSecondary,
                        fontFamily = SBAggroFontFamily,
                        fontSize = 14.sp
                    )
                }
            } else if (recommendations.isEmpty()) {
                item {
                    Text(
                        text = "아직 멤버 응답이 없거나 추천 시간을 계산할 수 없습니다.",
                        color = TextSecondary,
                        fontFamily = SBAggroFontFamily,
                        fontSize = 14.sp
                    )
                }
            } else {
                items(recommendations.size) { index ->
                    val recommendation = recommendations[index]
                    RecommendationCard(
                        recommendation = recommendation,
                        isTopRank = index == 0,
                        reactions = reactions,
                        myUserId = TokenManager.getUserId(),
                        onReactionClick = { viewModel.toggleReaction(it) },
                        onConfirmClick = {
                            val raw = analysis?.recommendations?.getOrNull(index)
                            if (raw != null) {
                                viewModel.confirm(
                                    start = raw.start,
                                    end = raw.end,
                                    title = analysis?.title ?: "일정",
                                    syncGoogle = syncGoogle
                                ) {
                                    val timeText = raw.start.split("T").let {
                                        "${it.firstOrNull()?.substringAfterLast("-") ?: ""} ${it.lastOrNull()?.substringBeforeLast(":") ?: ""}"
                                    }
                                    MoaNotificationHelper.notifyScheduleConfirmed(
                                        context,
                                        analysis?.title ?: "일정",
                                        timeText
                                    )
                                    MoaNotificationHelper.notifyCalendarAdded(
                                        context,
                                        analysis?.title ?: "일정",
                                        timeText
                                    )
                                    onConfirmComplete()
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun RecommendationCard(
    recommendation: RecommendedTime,
    isTopRank: Boolean,
    showHostConfirm: Boolean = true,
    reactions: List<ReactionDto> = emptyList(),
    myUserId: Long = -1L,
    onReactionClick: (String) -> Unit = {},
    onConfirmClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isTopRank) 12.dp else 4.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = if (isTopRank) MoaBlue else Color(0xFFDDE4F2)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(if (isTopRank) Color(0xFFF0F5FF) else Color.White)
            .border(
                width = if (isTopRank) 1.5.dp else 0.dp,
                color = if (isTopRank) MoaBlue.copy(alpha = 0.5f) else Color.Transparent,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(20.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isTopRank) MoaBlue else Color(0xFFE8EBF2))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isTopRank) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = "${recommendation.rank}순위",
                            fontFamily = SBAggroFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isTopRank) Color.White else TextSecondary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Available Count
                Text(
                    text = "${recommendation.availableCount}/${recommendation.totalCount}명 가능",
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (recommendation.availableCount == recommendation.totalCount) Color(0xFF35A96D) else TextPrimary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = recommendation.dateString,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = recommendation.timeString,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = TextPrimary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isTopRank) Color.White else Color(0xFFF7F8FC))
                    .padding(12.dp)
            ) {
                Text(
                    text = "참여 가능: ${recommendation.members}",
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            ReactionBar(
                reactions = reactions,
                myUserId = myUserId,
                onReactionClick = onReactionClick
            )
            
            if (showHostConfirm) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onConfirmClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isTopRank) MoaBlue else Color(0xFF4B556B),
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                ) {
                    Text(
                        text = "이 시간으로 확정하기",
                        fontFamily = SBAggroFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

private data class ReactionOption(val key: String, val icon: ImageVector)

private val reactionOptions = listOf(
    ReactionOption("👍", Icons.Default.ThumbUp),
    ReactionOption("❤️", Icons.Default.Favorite),
    ReactionOption("🔥", Icons.Default.LocalFireDepartment),
    ReactionOption("👏", Icons.Default.EmojiEvents),
)

@Composable
private fun ReactionIcon(
    emoji: String,
    tint: Color,
    modifier: Modifier = Modifier,
    iconSize: androidx.compose.ui.unit.Dp = 20.dp,
) {
    val icon = reactionOptions.firstOrNull { it.key == emoji }?.icon ?: Icons.Default.ThumbUp
    Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = modifier.size(iconSize))
}

@Composable
private fun ReactionBar(
    reactions: List<ReactionDto>,
    myUserId: Long,
    onReactionClick: (String) -> Unit,
) {
    val myReaction = reactions.firstOrNull { it.userId == myUserId }?.emoji

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            reactionOptions.forEach { option ->
                val isSelected = myReaction == option.key
                val count = reactions.count { it.emoji == option.key }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (isSelected) Color(0xFFEAF1FF) else Color(0xFFF7F8FC))
                        .border(
                            width = 1.dp,
                            color = if (isSelected) MoaBlue else Color(0xFFE8EBF2),
                            shape = RoundedCornerShape(999.dp),
                        )
                        .clickable { onReactionClick(option.key) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ReactionIcon(
                            emoji = option.key,
                            tint = if (isSelected) MoaBlue else TextSecondary,
                        )
                        if (count > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = count.toString(),
                                fontFamily = SBAggroFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isSelected) MoaBlue else TextPrimary,
                            )
                        }
                    }
                }
            }
        }
        if (reactions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                reactions.forEach { reaction ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ReactionIcon(emoji = reaction.emoji, tint = MoaBlue, iconSize = 16.dp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = reaction.nickname,
                            fontFamily = SBAggroFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                            color = TextSecondary,
                        )
                    }
                }
            }
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
            MoaMascot(size = 52.dp)
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = message,
                color = TextSecondary,
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
                color = TextPrimary,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        MoaBodyText(
            text = "누가 몇 시에 가능한지 확인하세요",
            fontSize = 12.sp,
            color = TextSecondary
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
                    color = TextPrimary
                )
                if (times.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    MoaBodyText(
                        text = times.joinToString("  "),
                        fontSize = 12.sp,
                        color = TextSecondary,
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
