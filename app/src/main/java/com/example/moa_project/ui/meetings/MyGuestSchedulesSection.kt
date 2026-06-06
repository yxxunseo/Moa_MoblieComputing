package com.example.moa_project.ui.meetings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moa_project.network.GuestScheduleResponse
import com.example.moa_project.ui.theme.MoaBlue
import com.example.moa_project.ui.theme.MoaBlueSoft
import com.example.moa_project.ui.theme.MoaStatusConfirmed
import com.example.moa_project.ui.theme.MoaTextPrimary
import com.example.moa_project.ui.theme.MoaTextSecondary
import com.example.moa_project.ui.theme.SBAggroFontFamily
import com.example.moa_project.util.GuestLinkHelper
import com.example.moa_project.util.GuestLinkShareHelper

@Composable
fun MyGuestSchedulesSection(
    onCreateClick: () -> Unit,
    onViewResult: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GuestScheduleListViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.fetchMySchedules()
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "내 단기 일정",
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MoaTextPrimary,
                )
                Text(
                    text = "링크로 참여하는 일정",
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = MoaTextSecondary,
                )
            }
            IconButton(
                onClick = onCreateClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MoaBlueSoft),
            ) {
                Icon(Icons.Default.Add, contentDescription = "단기 일정 만들기", tint = MoaBlue)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (val current = state) {
            is GuestScheduleListState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = MoaBlue, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                }
            }
            is GuestScheduleListState.Error -> {
                GuestEmptyCard(
                    message = current.message,
                    onClick = onCreateClick,
                )
            }
            is GuestScheduleListState.Success -> {
                if (current.schedules.isEmpty()) {
                    GuestEmptyCard(
                        message = "아직 만든 단기 일정이 없어요.\n+ 버튼으로 링크를 만들어보세요.",
                        onClick = onCreateClick,
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        current.schedules.forEach { schedule ->
                            GuestScheduleListItem(
                                schedule = schedule,
                                onViewResult = { onViewResult(schedule.uniqueLink) },
                                onShare = {
                                    val link = GuestLinkHelper.resolveWebLink(schedule.uniqueLink, schedule.webLink)
                                    GuestLinkShareHelper.share(
                                        context = context,
                                        scheduleTitle = schedule.title,
                                        scheduleDescription = schedule.description,
                                        startDate = schedule.startDate,
                                        endDate = schedule.endDate,
                                        uniqueLink = schedule.uniqueLink,
                                        webLink = link,
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GuestEmptyCard(
    message: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(20.dp),
    ) {
        Text(
            text = message,
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            lineHeight = 20.sp,
            color = MoaTextSecondary,
        )
    }
}

@Composable
private fun GuestScheduleListItem(
    schedule: GuestScheduleResponse,
    onViewResult: () -> Unit,
    onShare: () -> Unit,
) {
    val statusLabel = when (schedule.status) {
        "CONFIRMED" -> "확정됨"
        "DONE" -> "완료됨"
        else -> "조율 중"
    }
    val statusColor = when (schedule.status) {
        "CONFIRMED" -> MoaStatusConfirmed
        "DONE" -> MoaTextSecondary
        else -> MoaBlue
    }
    val actionLabel = if (schedule.status == "CONFIRMED") "결과 보기" else "조율/확정"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(16.dp),
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(2.dp))
                .background(statusColor),
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = schedule.title,
                        fontFamily = SBAggroFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MoaTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${formatDateRange(schedule.startDate, schedule.endDate)}",
                        fontFamily = SBAggroFontFamily,
                        fontSize = 12.sp,
                        color = MoaTextSecondary,
                    )
                }
                Text(
                    text = statusLabel,
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = statusColor,
                )
            }

            if (schedule.status == "CONFIRMED" && schedule.confirmedStart != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "확정 ${formatDateTime(schedule.confirmedStart)}",
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = MoaStatusConfirmed,
                )
            }

            if (schedule.status != "DONE") {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Button(
                        onClick = onViewResult,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MoaBlue),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(
                            text = actionLabel,
                            fontFamily = SBAggroFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White,
                        )
                    }
                    IconButton(
                        onClick = onShare,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MoaBlueSoft),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "공유하기",
                            tint = MoaBlue,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }
}

private fun formatDateRange(start: String, end: String): String {
    val s = start.replace("-", ".")
    val e = end.replace("-", ".")
    return if (s == e) s else "$s ~ $e"
}

private fun formatDateTime(raw: String): String {
    return runCatching {
        val parts = raw.split("T")
        val date = parts.getOrNull(0).orEmpty().replace("-", ".")
        val time = parts.getOrNull(1)?.substring(0, 5).orEmpty()
        if (time.isBlank()) date else "$date $time"
    }.getOrDefault(raw)
}
