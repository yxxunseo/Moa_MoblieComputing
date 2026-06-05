package com.example.moa_project.ui.meetings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moa_project.network.GuestScheduleResponse
import com.example.moa_project.ui.theme.MoaBlue
import com.example.moa_project.ui.theme.MoaTextPrimary
import com.example.moa_project.ui.theme.MoaTextSecondary
import com.example.moa_project.ui.theme.SBAggroFontFamily
import com.example.moa_project.ui.components.Moa3DIcon
import com.example.moa_project.ui.components.Moa3DIconType
import com.example.moa_project.util.GuestLinkHelper
import com.example.moa_project.util.KakaoShareHelper

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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Moa3DIcon(type = Moa3DIconType.Link, size = 32.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "내 단기 일정",
                        fontFamily = SBAggroFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MoaTextPrimary,
                    )
                    Text(
                        text = "앱 없이 참여하는 링크 일정",
                        fontFamily = SBAggroFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        color = MoaTextSecondary,
                    )
                }
            }
            IconButton(onClick = onCreateClick) {
                Icon(Icons.Default.Add, contentDescription = "단기 일정 만들기", tint = MoaBlue)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (val current = state) {
            is GuestScheduleListState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = MoaBlue, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                }
            }
            is GuestScheduleListState.Error -> {
                Text(
                    text = current.message,
                    fontFamily = SBAggroFontFamily,
                    fontSize = 12.sp,
                    color = MoaTextSecondary,
                )
            }
            is GuestScheduleListState.Success -> {
                if (current.schedules.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .clickable(onClick = onCreateClick)
                            .padding(16.dp),
                    ) {
                        Text(
                            text = "아직 만든 단기 일정이 없어요. + 버튼으로 링크를 만들어보세요.",
                            fontFamily = SBAggroFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = MoaTextSecondary,
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        current.schedules.forEach { schedule ->
                            GuestScheduleListItem(
                                schedule = schedule,
                                onViewResult = { onViewResult(schedule.uniqueLink) },
                                onComplete = {
                                    viewModel.completeSchedule(schedule.uniqueLink) {
                                        Toast.makeText(context, "일정을 완료 처리했어요.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onCopyLink = {
                                    val link = GuestLinkHelper.resolveWebLink(schedule.uniqueLink, schedule.webLink)
                                    copyLink(context, link)
                                    Toast.makeText(context, "링크를 복사했어요.", Toast.LENGTH_SHORT).show()
                                },
                                onKakaoShare = {
                                    val link = GuestLinkHelper.resolveWebLink(schedule.uniqueLink, schedule.webLink)
                                    KakaoShareHelper.shareGuestSchedule(
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
private fun GuestScheduleListItem(
    schedule: GuestScheduleResponse,
    onViewResult: () -> Unit,
    onComplete: () -> Unit,
    onCopyLink: () -> Unit,
    onKakaoShare: () -> Unit,
) {
    val statusLabel = when (schedule.status) {
        "CONFIRMED" -> "확정됨"
        "DONE" -> "완료됨"
        else -> "조율 중"
    }
    val statusColor = when (schedule.status) {
        "CONFIRMED" -> com.example.moa_project.ui.theme.MoaStatusConfirmed
        "DONE" -> MoaTextSecondary
        else -> MoaBlue
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(14.dp), spotColor = Color(0xFFDDE4F2))
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .padding(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = schedule.title,
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MoaTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${schedule.startDate} ~ ${schedule.endDate}",
                    fontFamily = SBAggroFontFamily,
                    fontSize = 11.sp,
                    color = MoaTextSecondary,
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(statusColor.copy(alpha = 0.12f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    text = statusLabel,
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = statusColor,
                )
            }
        }

        if (schedule.status == "CONFIRMED" && schedule.confirmedStart != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "확정: ${formatDateTime(schedule.confirmedStart)}",
                fontFamily = SBAggroFontFamily,
                fontSize = 11.sp,
                color = Color(0xFF35A96D),
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (schedule.status != "DONE") {
                Button(
                    onClick = onViewResult,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MoaBlue),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text(
                        text = if (schedule.status == "CONFIRMED") "결과 보기" else "조율/확정",
                        fontFamily = SBAggroFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                    )
                }
            }
            IconButton(onClick = onKakaoShare) {
                Icon(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.moa_project.R.drawable.ic_kakao),
                    contentDescription = "카카오톡 공유",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(22.dp),
                )
            }
            IconButton(onClick = onCopyLink) {
                Icon(Icons.Default.Share, contentDescription = "링크 복사", tint = MoaTextSecondary, modifier = Modifier.size(20.dp))
            }
            if (schedule.status == "CONFIRMED") {
                TextButton(onClick = onComplete) {
                    Text("완료", fontFamily = SBAggroFontFamily, fontWeight = FontWeight.Bold, color = Color(0xFF35A96D), fontSize = 12.sp)
                }
            }
        }
    }
}

private fun formatDateTime(raw: String): String {
    return runCatching {
        val parts = raw.split("T")
        val date = parts.getOrNull(0).orEmpty()
        val time = parts.getOrNull(1)?.substring(0, 5).orEmpty()
        "$date $time"
    }.getOrDefault(raw)
}

private fun copyLink(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("Moa schedule link", text))
}
