package com.example.moa_project.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moa_project.ui.components.MoaBodyText
import com.example.moa_project.ui.components.MoaCaptionText
import com.example.moa_project.ui.components.MoaMascot
import com.example.moa_project.ui.components.MoaMascotVariant
import com.example.moa_project.ui.components.MoaTitleText
import com.example.moa_project.ui.theme.MoaBlue
import com.example.moa_project.ui.theme.MoaBlueSoft
import com.example.moa_project.ui.theme.MoaCardShadow
import com.example.moa_project.ui.theme.MoaDivider
import com.example.moa_project.ui.theme.MoaRadius
import com.example.moa_project.ui.theme.MoaScreenBackground
import com.example.moa_project.ui.theme.MoaStatusConfirmed
import com.example.moa_project.ui.theme.MoaTextPrimary
import com.example.moa_project.ui.theme.MoaTextSecondary
import com.example.moa_project.ui.theme.SBAggroFontFamily
import com.example.moa_project.ui.theme.moaCardSurface

private val DesignWidth = 360.dp
private val DesignHeight = 780.dp

/** 실제 앱 화면(360dp)을 그대로 그린 뒤 프레임 크기에 맞게 축소 */
@Composable
fun OnboardingDevicePreview(
    activeRoute: String,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(420.dp)
            .shadow(10.dp, RoundedCornerShape(24.dp), spotColor = MoaCardShadow.copy(alpha = 0.35f))
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, Color(0xFFE8ECF4), RoundedCornerShape(24.dp))
            .background(Color.White),
    ) {
        val scale = maxWidth / DesignWidth

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .width(DesignWidth)
                .height(DesignHeight)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    transformOrigin = TransformOrigin(0.5f, 0f)
                },
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(MoaScreenBackground),
                    content = content,
                )
                PreviewBottomNavigationBar(currentRoute = activeRoute)
            }
        }
    }
}

@Composable
fun GroupsOnboardingPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(top = 36.dp, bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        PreviewMeetingsHeader(showAddButton = true, selectedTab = "groups")
        PreviewMeetingCard(
            category = "정기 모임",
            title = "MOA 스터디",
            memberCount = 5,
            accent = Color(0xFF2179FE),
        )
        PreviewMeetingCard(
            category = "졸업 프로젝트",
            title = "캡스톤 A팀",
            memberCount = 4,
            accent = Color(0xFF9B62FF),
        )
    }
}

@Composable
fun GuestScheduleOnboardingPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(top = 36.dp, bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        PreviewMeetingsHeader(showAddButton = false, selectedTab = "guest")
        PreviewGuestSectionHeader()
        PreviewGuestCard(
            title = "팀 회의",
            dateRange = "3/12 ~ 3/14",
            status = "조율 중",
            statusColor = MoaBlue,
            actionLabel = "조율/확정",
        )
        PreviewGuestCard(
            title = "저녁 약속",
            dateRange = "3/15",
            status = "확정됨",
            statusColor = MoaStatusConfirmed,
            actionLabel = "결과 보기",
        )
    }
}

@Composable
fun ScheduleCalendarOnboardingPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp, bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = "일정 조율",
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MoaTextPrimary,
        )
        Text(
            text = "MOA 스터디 · 3월 2주차",
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            color = MoaTextSecondary,
        )
        PreviewHeatmapCard()
        Text(
            text = "캘린더",
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MoaTextPrimary,
        )
        PreviewCalendarEventCard()
    }
}

@Composable
fun DashboardOnboardingPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PreviewHomeHeader()
        PreviewHomeHeroCard()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PreviewHomeQuickCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.CalendarMonth,
                title = "이번 주 일정",
                subtitle = "확정 3건\n다음 3/12 스터디",
            )
            PreviewHomeQuickCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Groups,
                title = "MOA 스터디",
                subtitle = "모임 2개\n조율 필요 1건",
            )
        }
        Text(
            text = "내 일정",
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MoaTextPrimary,
        )
        PreviewHomeProgressCard(
            title = "캡스톤 미팅",
            subtitle = "MOA 스터디",
            progress = 0.75f,
            progressLabel = "3/4명 응답",
        )
    }
}

@Composable
private fun PreviewMeetingsHeader(showAddButton: Boolean, selectedTab: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "모임",
                color = MoaTextPrimary,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                modifier = Modifier.weight(1f),
            )
            if (showAddButton) {
                IconButton(
                    onClick = {},
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(6.dp, RoundedCornerShape(12.dp), spotColor = Color(0xFFDDE6FA))
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = MoaBlue, modifier = Modifier.size(24.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        PreviewMeetingsTabSelector(selectedTab = selectedTab)
    }
}

@Composable
private fun PreviewMeetingsTabSelector(selectedTab: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFECEFF5))
            .padding(4.dp),
    ) {
        PreviewTabChip("내 모임", selected = selectedTab == "groups", modifier = Modifier.weight(1f))
        PreviewTabChip("단기 일정", selected = selectedTab == "guest", modifier = Modifier.weight(1f))
    }
}

@Composable
private fun PreviewTabChip(label: String, selected: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) Color.White else Color.Transparent)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontFamily = SBAggroFontFamily,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 14.sp,
            color = if (selected) MoaBlue else MoaTextSecondary,
        )
    }
}

@Composable
private fun PreviewMeetingCard(
    category: String,
    title: String,
    memberCount: Int,
    accent: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .moaCardSurface()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(accent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Groups, contentDescription = null, tint = accent, modifier = Modifier.size(32.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            MoaCaptionText(text = category)
            Spacer(modifier = Modifier.height(6.dp))
            MoaTitleText(text = title, fontSize = 17.sp, maxLines = 2)
            Spacer(modifier = Modifier.height(6.dp))
            MoaCaptionText(text = "멤버 ${memberCount}명")
            Spacer(modifier = Modifier.height(10.dp))
            MoaBodyText(text = "${memberCount}명 참여 중", fontSize = 11.sp, color = MoaTextSecondary)
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.height(120.dp),
        ) {
            Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = MoaTextSecondary, modifier = Modifier.size(24.dp))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MoaTextSecondary, modifier = Modifier.size(30.dp))
        }
    }
}

@Composable
private fun PreviewGuestSectionHeader() {
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
            onClick = {},
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MoaBlueSoft),
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = MoaBlue)
        }
    }
}

@Composable
private fun PreviewGuestCard(
    title: String,
    dateRange: String,
    status: String,
    statusColor: Color,
    actionLabel: String,
) {
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
                        text = title,
                        fontFamily = SBAggroFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MoaTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = dateRange,
                        fontFamily = SBAggroFontFamily,
                        fontSize = 12.sp,
                        color = MoaTextSecondary,
                    )
                }
                Text(
                    text = status,
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = statusColor,
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = {},
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MoaBlue),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(actionLabel, fontFamily = SBAggroFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                }
                IconButton(
                    onClick = {},
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MoaBlueSoft),
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = MoaBlue, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun PreviewHeatmapCard() {
    val levels = listOf(
        listOf(0, 1, 2, 3, 2, 1, 0),
        listOf(1, 2, 3, 4, 3, 2, 1),
        listOf(0, 1, 3, 4, 4, 2, 0),
        listOf(0, 0, 2, 3, 3, 1, 0),
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .moaCardSurface(cornerRadius = MoaRadius.card)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("월", "화", "수", "목", "금", "토", "일").forEach { day ->
                Text(
                    day,
                    modifier = Modifier.weight(1f),
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = MoaTextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
        levels.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { level ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(22.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(heatmapColor(level)),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "진한 색일수록 많은 사람이 가능해요",
            fontFamily = SBAggroFontFamily,
            fontSize = 12.sp,
            color = MoaTextSecondary,
        )
    }
}

@Composable
private fun PreviewCalendarEventCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .moaCardSurface(cornerRadius = MoaRadius.homeCard)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .width(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MoaBlue.copy(alpha = 0.1f))
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("12", fontFamily = SBAggroFontFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MoaBlue)
            Text("수", fontFamily = SBAggroFontFamily, fontWeight = FontWeight.Medium, fontSize = 11.sp, color = MoaBlue)
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("MOA 스터디", fontFamily = SBAggroFontFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MoaTextPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text("14:00 ~ 16:00", fontFamily = SBAggroFontFamily, fontSize = 12.sp, color = MoaTextSecondary)
            Spacer(modifier = Modifier.height(2.dp))
            Text("MOA 스터디", fontFamily = SBAggroFontFamily, fontSize = 11.sp, color = MoaBlue)
        }
    }
}

@Composable
private fun PreviewHomeHeader() {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(MoaBlueSoft),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("안녕하세요,", fontFamily = SBAggroFontFamily, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = MoaTextSecondary)
            Text("모아님 반가워요", fontFamily = SBAggroFontFamily, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = MoaTextPrimary)
        }
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Notifications, contentDescription = null, tint = MoaTextPrimary, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
private fun PreviewHomeHeroCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 200.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFFDCE8FF), Color(0xFFE8E0FF), Color(0xFFDDEFE6)),
                ),
            ),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth(0.62f)
                .padding(start = 22.dp, top = 24.dp, bottom = 24.dp),
        ) {
            Text(
                text = "이번 주\n일정 확인",
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                lineHeight = 34.sp,
                color = MoaTextPrimary,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "조율 2건 · 확정 3건",
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = MoaTextSecondary,
            )
            Spacer(modifier = Modifier.height(18.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(22.dp))
                    .border(1.5.dp, MoaBlue, RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 11.dp),
            ) {
                Text("조율하러 가기", fontFamily = SBAggroFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MoaBlue)
            }
        }
        MoaMascot(
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 8.dp, bottom = 4.dp),
            size = 118.dp,
            variant = MoaMascotVariant.Default,
        )
    }
}

@Composable
private fun PreviewHomeQuickCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .heightIn(min = 132.dp)
            .moaCardSurface(cornerRadius = MoaRadius.homeCard)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MoaBlue.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = MoaBlue, modifier = Modifier.size(22.dp))
        }
        Column {
            Text(title, fontFamily = SBAggroFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MoaTextPrimary, maxLines = 1)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, fontFamily = SBAggroFontFamily, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp, color = MoaTextSecondary)
        }
    }
}

@Composable
private fun PreviewHomeProgressCard(
    title: String,
    subtitle: String,
    progress: Float,
    progressLabel: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .moaCardSurface(cornerRadius = MoaRadius.homeCard)
            .padding(horizontal = 18.dp, vertical = 16.dp),
    ) {
        Text(title, fontFamily = SBAggroFontFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MoaTextPrimary, maxLines = 1)
        Spacer(modifier = Modifier.height(4.dp))
        Text(subtitle, fontFamily = SBAggroFontFamily, fontSize = 12.sp, color = MoaTextSecondary)
        Spacer(modifier = Modifier.height(12.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = MoaBlue,
            trackColor = MoaBlueSoft,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(progressLabel, fontFamily = SBAggroFontFamily, fontSize = 11.sp, color = MoaTextSecondary)
    }
}

@Composable
private fun PreviewBottomNavigationBar(currentRoute: String) {
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, spotColor = MoaCardShadow),
        containerColor = Color.White,
        tonalElevation = 0.dp,
    ) {
        PreviewNavItem("홈", Icons.Rounded.Home, currentRoute == "home")
        PreviewNavItem("캘린더", Icons.Default.DateRange, currentRoute == "calendar")
        PreviewNavItem("모임", Icons.Default.Groups, currentRoute == "meetings")
        NavigationBarItem(
            selected = currentRoute == "my",
            onClick = {},
            icon = {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(MoaBlueSoft),
                )
            },
            label = {
                Text("마이", fontFamily = SBAggroFontFamily, fontWeight = if (currentRoute == "my") FontWeight.Bold else FontWeight.Medium, fontSize = 11.sp)
            },
            colors = previewNavColors(currentRoute == "my"),
        )
    }
}

@Composable
private fun RowScope.PreviewNavItem(title: String, icon: ImageVector, selected: Boolean) {
    NavigationBarItem(
        selected = selected,
        onClick = {},
        icon = { Icon(icon, contentDescription = title, modifier = Modifier.size(28.dp)) },
        label = {
            Text(title, fontFamily = SBAggroFontFamily, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, fontSize = 11.sp)
        },
        colors = previewNavColors(selected),
    )
}

@Composable
private fun previewNavColors(selected: Boolean) = NavigationBarItemDefaults.colors(
    selectedIconColor = MoaBlue,
    unselectedIconColor = MoaTextSecondary,
    selectedTextColor = MoaBlue,
    unselectedTextColor = MoaTextSecondary,
    indicatorColor = Color.Transparent,
)

private fun heatmapColor(level: Int): Color = when (level) {
    0 -> MoaDivider
    1 -> MoaBlueSoft
    2 -> MoaBlue.copy(alpha = 0.45f)
    3 -> MoaBlue.copy(alpha = 0.72f)
    else -> MoaBlue
}
