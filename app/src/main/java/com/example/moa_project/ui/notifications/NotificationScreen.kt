package com.example.moa_project.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.moa_project.ui.theme.MoaBlue
import com.example.moa_project.ui.theme.MoaScreenBackground
import com.example.moa_project.ui.theme.moaCardSurface
import com.example.moa_project.ui.theme.MoaTextPrimary
import com.example.moa_project.ui.theme.MoaTextSecondary
import com.example.moa_project.ui.theme.SBAggroFontFamily
import java.time.LocalDate
import java.time.LocalDateTime

@Composable
fun NotificationScreen(
    onBackClick: () -> Unit = {},
    onNavigate: (String) -> Unit = {},
    viewModel: NotificationsViewModel = viewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    LaunchedEffect(context) {
        viewModel.refresh(context, markReadAfterLoad = true)
    }

    Scaffold(
        containerColor = MoaScreenBackground,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            NotificationTopBar(onBackClick = onBackClick)

            when (val s = state) {
                is NotificationsState.Loading -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = MoaBlue, strokeWidth = 2.dp)
                    }
                }
                is NotificationsState.Error -> {
                    NotificationEmpty(
                        message = s.message,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    )
                }
                is NotificationsState.Success -> {
                    if (s.items.isEmpty()) {
                        NotificationEmpty(
                            message = "새 알림이 없어요",
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                        )
                    } else {
                        val sections = remember(s.items) { groupByDate(s.items) }
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            sections.forEach { section ->
                                item(key = "header-${section.label}") {
                                    Text(
                                        text = section.label,
                                        fontFamily = SBAggroFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = MoaTextSecondary,
                                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                                    )
                                }
                                items(section.items, key = { it.id }) { item ->
                                    NotificationCard(
                                        item = item,
                                        onClick = {
                                            if (item.isNavigable()) {
                                                viewModel.onNotificationClicked(item, onNavigate)
                                            }
                                        },
                                    )
                                }
                            }
                            item { Spacer(modifier = Modifier.height(16.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationTopBar(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .clickable(onClick = onBackClick)
                .padding(10.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "뒤로가기",
                tint = MoaTextPrimary,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "알림",
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MoaTextPrimary,
        )
    }
}

@Composable
private fun NotificationCard(
    item: MoaNotification,
    onClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .moaCardSurface(cornerRadius = 18.dp)
            .clickable(enabled = item.isNavigable(), onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = notificationTypeLabel(item.type),
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = notificationTypeColor(item.type),
            )
            Text(
                text = formatTime(item.timestamp),
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = MoaTextSecondary,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = item.title,
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MoaTextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (item.body.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.body,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = MoaTextSecondary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun NotificationEmpty(
    message: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .moaCardSurface(cornerRadius = 20.dp)
                .padding(horizontal = 24.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = message,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MoaTextPrimary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "일정이 확정되면\n여기에 표시돼요",
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                color = MoaTextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

private data class NotificationSection(
    val label: String,
    val items: List<MoaNotification>,
)

private fun groupByDate(items: List<MoaNotification>): List<NotificationSection> {
    val today = LocalDate.now()
    val order = listOf("오늘", "어제", "이번 주", "이전")
    val grouped = items.groupBy { dateSectionLabel(it.timestamp.toLocalDate(), today) }
    return order.mapNotNull { label ->
        grouped[label]?.takeIf { it.isNotEmpty() }?.let { NotificationSection(label, it) }
    }
}

private fun dateSectionLabel(date: LocalDate, today: LocalDate): String = when {
    date.isEqual(today) -> "오늘"
    date.isEqual(today.minusDays(1)) -> "어제"
    date.isAfter(today.minusDays(7)) -> "이번 주"
    else -> "이전"
}

private fun formatTime(timestamp: LocalDateTime): String {
    val today = LocalDate.now()
    val date = timestamp.toLocalDate()
    val time = formatKoreanTime(timestamp.hour, timestamp.minute)
    return when {
        date.isEqual(today) -> time
        date.isEqual(today.minusDays(1)) -> "어제 $time"
        else -> "${date.monthValue}.${date.dayOfMonth} $time"
    }
}

private fun notificationTypeLabel(type: MoaNotificationType): String = when (type) {
    MoaNotificationType.CONFIRMED -> "확정"
    MoaNotificationType.WEEKLY_REMINDER -> "일정 등록"
    MoaNotificationType.UPCOMING -> "예정"
    MoaNotificationType.WAITING -> "대기"
    MoaNotificationType.ADJUSTING -> "조율 중"
    MoaNotificationType.INFO -> "안내"
}

private fun notificationTypeColor(type: MoaNotificationType): Color = when (type) {
    MoaNotificationType.CONFIRMED -> MoaBlue
    MoaNotificationType.WEEKLY_REMINDER -> Color(0xFFF2994A)
    else -> MoaTextSecondary
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
