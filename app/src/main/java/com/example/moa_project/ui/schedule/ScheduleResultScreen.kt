package com.example.moa_project.ui.schedule

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.moa_project.network.ScheduleAnalysisResponse
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
    viewModel: GuestScheduleViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uniqueLink) {
        viewModel.fetchAnalysis(uniqueLink)
    }

    val analysis = (uiState as? GuestScheduleState.AnalysisSuccess)?.analysis
    
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
                            imageVector = Icons.Default.ArrowBack,
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
            item {
                Column {
                    Text(
                        text = scheduleTitle,
                        fontFamily = SBAggroFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "멤버들의 일정을 분석한 결과입니다.",
                        fontFamily = SBAggroFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(28.dp))
                
                Text(
                    text = "🏆 추천 시간대",
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(14.dp))
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
                        text = "아직 참여자가 없거나 추천 시간을 계산할 수 없습니다.",
                        color = TextSecondary,
                        fontFamily = SBAggroFontFamily,
                        fontSize = 14.sp
                    )
                }
            } else {
                items(recommendations.size) { index ->
                    RecommendationCard(
                        recommendation = recommendations[index],
                        isTopRank = index == 0,
                        onConfirmClick = {
                            val raw = analysis?.recommendations?.getOrNull(index)
                            if (raw != null) {
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
                                    onConfirmClick()
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
                HeatmapPreview(analysis?.heatmap)
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
                            imageVector = Icons.Default.ArrowBack,
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
            item {
                Column {
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
                }
                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "추천 시간대",
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(14.dp))
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

            item {
                Spacer(modifier = Modifier.height(20.dp))
                HeatmapPreview(analysis?.heatmap)
            }
        }
    }
}

@Composable
private fun RecommendationCard(
    recommendation: RecommendedTime,
    isTopRank: Boolean,
    reactions: List<ReactionDto> = emptyList(),
    myUserId: Long = -1L,
    onReactionClick: (String) -> Unit = {},
    onConfirmClick: () -> Unit
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
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onConfirmClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isTopRank) MoaBlue else Color(0xFF4B556B)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "이 시간으로 확정하기",
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun ReactionBar(
    reactions: List<ReactionDto>,
    myUserId: Long,
    onReactionClick: (String) -> Unit
) {
    val emojiOptions = listOf("👍", "❤️", "🔥", "👏")
    val myReaction = reactions.firstOrNull { it.userId == myUserId }?.emoji

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            emojiOptions.forEach { reaction ->
                val isSelected = myReaction == reaction
                val count = reactions.count { it.emoji == reaction }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (isSelected) Color(0xFFEAF1FF) else Color(0xFFF7F8FC))
                        .border(
                            width = 1.dp,
                            color = if (isSelected) MoaBlue else Color(0xFFE8EBF2),
                            shape = RoundedCornerShape(999.dp)
                        )
                        .clickable { onReactionClick(reaction) }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = if (count > 0) "$reaction $count" else reaction,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                }
            }
        }
        if (reactions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = reactions.joinToString(" · ") { "${it.nickname} ${it.emoji}" },
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun HeatmapPreview(heatmap: Map<String, Map<String, Int>>? = null) {
    val dates = heatmap?.keys?.sorted()?.take(4).orEmpty()
    val hours = heatmap
        ?.values
        ?.flatMap { it.keys }
        ?.distinct()
        ?.sorted()
        ?.take(4)
        .orEmpty()
    val maxCount = heatmap
        ?.values
        ?.flatMap { it.values }
        ?.maxOrNull()
        ?.coerceAtLeast(1) ?: 5
    val hasRealHeatmap = dates.isNotEmpty() && hours.isNotEmpty()
    val displayDates = if (hasRealHeatmap) dates else listOf("20(수)", "21(목)", "22(금)", "23(토)")
    val displayHours = if (hasRealHeatmap) hours else listOf("10:00", "12:00", "14:00", "16:00")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = Color(0xFFDDE4F2))
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(20.dp)
    ) {
        Text(
            text = "📊 시간대별 혼잡도",
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "색상이 진할수록 가능한 멤버가 많습니다.",
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = TextSecondary
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Mock Heatmap Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Hours Column
            Column(modifier = Modifier.width(40.dp)) {
                Spacer(modifier = Modifier.height(30.dp))
                displayHours.forEach {
                    Text(
                        text = it.replace(":00", "시"),
                        fontFamily = SBAggroFontFamily,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        modifier = Modifier.height(24.dp),
                        textAlign = TextAlign.End
                    )
                }
            }
            
            // Days Columns
            displayDates.forEachIndexed { dayIdx, day ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (hasRealHeatmap) day.substringAfterLast("-") else day,
                        fontFamily = SBAggroFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Blocks
                    displayHours.forEachIndexed { hourIdx, hour ->
                        val count = if (hasRealHeatmap) heatmap?.get(day)?.get(hour) ?: 0 else 0

                        val intensity = if (hasRealHeatmap) {
                            (count.toFloat() / maxCount.toFloat()).coerceIn(0f, 1f)
                        } else {
                            when {
                                dayIdx == 1 && hourIdx == 0 -> 1.0f
                                dayIdx == 3 && hourIdx == 3 -> 0.8f
                                dayIdx == 0 && hourIdx == 2 -> 0.6f
                                else -> Math.random().toFloat() * 0.4f
                            }
                        }
                        
                        Box(
                            modifier = Modifier
                                .padding(vertical = 2.dp)
                                .size(48.dp, 20.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MoaBlue.copy(alpha = 0.1f + intensity * 0.9f))
                        )
                    }
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
