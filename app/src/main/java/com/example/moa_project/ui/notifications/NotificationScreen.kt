package com.example.moa_project.ui.notifications

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moa_project.ui.components.MoaBodyText
import com.example.moa_project.ui.components.MoaMascot
import com.example.moa_project.ui.components.MoaTitleText
import com.example.moa_project.ui.theme.MoaAccentBlue
import com.example.moa_project.ui.theme.MoaAccentBlueBg
import com.example.moa_project.ui.theme.MoaAccentGreen
import com.example.moa_project.ui.theme.MoaAccentGreenBg
import com.example.moa_project.ui.theme.MoaAccentOrange
import com.example.moa_project.ui.theme.MoaAccentOrangeBg
import com.example.moa_project.ui.theme.MoaAccentPurple
import com.example.moa_project.ui.theme.MoaAccentPurpleBg
import com.example.moa_project.ui.theme.MoaScreenBackground
import com.example.moa_project.ui.theme.MoaTextPrimary
import com.example.moa_project.ui.theme.MoaTextSecondary
import com.example.moa_project.ui.theme.SBAggroFontFamily
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onBackClick: () -> Unit = {},
    viewModel: NotificationsViewModel = viewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.attachContext(context)
        viewModel.refresh()
        viewModel.markAllRead()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "알림",
                        fontFamily = SBAggroFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MoaTextPrimary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = MoaTextPrimary,
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White),
                modifier = Modifier.shadow(elevation = 2.dp, spotColor = Color(0x1A000000)),
            )
        },
        containerColor = MoaScreenBackground,
    ) { innerPadding ->
        when (val s = state) {
            is NotificationsState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = MoaAccentBlue)
                }
            }
            is NotificationsState.Error -> {
                NotificationEmpty(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    message = s.message,
                )
            }
            is NotificationsState.Success -> {
                if (s.items.isEmpty()) {
                    NotificationEmpty(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        message = "아직 새로운 알림이 없어요.\n일정이 확정되면 여기로 알려드릴게요!",
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(s.items.size) { idx ->
                            NotificationCard(s.items[idx])
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(item: MoaNotification) {
    val (fg, bg, icon) = notificationStyle(item.type)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(18.dp), spotColor = Color(0x14101B33))
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .padding(16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(bg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            MoaTitleText(text = item.title, fontSize = 15.sp, maxLines = 2)
            Spacer(modifier = Modifier.height(4.dp))
            MoaBodyText(
                text = item.body,
                fontSize = 13.sp,
                color = MoaTextSecondary,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = relativeLabel(item.timestamp.toLocalDate()),
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = fg,
            )
        }
    }
}

@Composable
private fun NotificationEmpty(modifier: Modifier, message: String) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        MoaMascot(size = 88.dp)
        Spacer(modifier = Modifier.height(16.dp))
        MoaBodyText(
            text = message,
            fontSize = 14.sp,
            color = MoaTextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

private data class NotificationStyle(val fg: Color, val bg: Color, val icon: ImageVector)

private fun notificationStyle(type: MoaNotificationType): NotificationStyle = when (type) {
    MoaNotificationType.CONFIRMED -> NotificationStyle(MoaAccentGreen, MoaAccentGreenBg, Icons.Default.CheckCircle)
    MoaNotificationType.WAITING -> NotificationStyle(MoaAccentOrange, MoaAccentOrangeBg, Icons.Default.Schedule)
    MoaNotificationType.ADJUSTING -> NotificationStyle(MoaAccentPurple, MoaAccentPurpleBg, Icons.Default.Tune)
    MoaNotificationType.UPCOMING -> NotificationStyle(MoaAccentBlue, MoaAccentBlueBg, Icons.Default.Event)
    MoaNotificationType.INFO -> NotificationStyle(MoaAccentBlue, MoaAccentBlueBg, Icons.Default.Info)
}

private fun relativeLabel(date: LocalDate): String {
    val today = LocalDate.now()
    return when {
        date.isEqual(today) -> "오늘"
        date.isEqual(today.minusDays(1)) -> "어제"
        date.isAfter(today) -> date.format(DateTimeFormatter.ofPattern("M월 d일"))
        else -> date.format(DateTimeFormatter.ofPattern("M월 d일"))
    }
}
