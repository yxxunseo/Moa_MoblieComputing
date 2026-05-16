package com.example.moa_project.ui.my

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moa_project.R
import com.example.moa_project.ui.components.MoaBottomNavigationBar
import com.example.moa_project.ui.theme.Moa_ProjectTheme
import com.example.moa_project.ui.theme.SBAggroFontFamily

private val MoaBlue = Color(0xFF2179FE)
private val ScreenBackground = Color(0xFFF7F8FC)
private val TextPrimary = Color(0xFF101B33)
private val TextSecondary = Color(0xFF737C99)
private val Divider = Color(0xFFE8ECF4)

@Immutable
private data class MyMenuItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconColor: Color = MoaBlue,
)

@Composable
fun MyPageScreen(
    currentRoute: String = "my",
    onNavigate: (String) -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
) {
    Scaffold(
        bottomBar = {
            MoaBottomNavigationBar(
                currentRoute = currentRoute,
                onNavigate = onNavigate,
            )
        },
        containerColor = ScreenBackground,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(ScreenBackground)
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 36.dp,
                bottom = 32.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item { MyHeader() }
            item {
                ProfileSummaryCard(onEditProfileClick = onEditProfileClick)
            }
            item {
                SectionTitle("활동")
                Spacer(modifier = Modifier.height(10.dp))
                MenuGroup(
                    items = listOf(
                        MyMenuItem("내 일정", "예정된 일정 3개", Icons.Default.DateRange),
                        MyMenuItem("관심 모임", "12개", Icons.Default.Favorite, Color(0xFFFF6B9A)),
                    ),
                )
            }
            item {
                SectionTitle("계정")
                Spacer(modifier = Modifier.height(10.dp))
                MenuGroup(
                    items = listOf(
                        MyMenuItem("계정 정보", "이메일, 비밀번호 관리", Icons.Default.Person),
                        MyMenuItem("보안 설정", "로그인, 2단계 인증", Icons.Default.Lock),
                        MyMenuItem("알림 설정", "모임 초대, 일정 알림", Icons.Default.Notifications),
                        MyMenuItem("고객센터", "문의하기, 이용 가이드", Icons.Default.Info),
                        MyMenuItem("앱 정보", "버전 1.0.0", Icons.Default.Settings),
                    ),
                )
            }
            item {
                ReviewBanner()
            }
            item {
                LogoutButton(onClick = onLogoutClick)
            }
        }
    }
}

@Composable
private fun MyHeader() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "마이페이지",
            color = TextPrimary,
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = buildAnnotatedString {
                append("내 정보와 활동을 확인하세요")
                withStyle(SpanStyle(color = MoaBlue)) {
                    append(" ·")
                }
            },
            color = TextSecondary,
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
        )
    }
}

@Composable
private fun ProfileSummaryCard(onEditProfileClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(22.dp), spotColor = Color(0xFFDDE4F2))
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
            .padding(18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(94.dp), contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = R.drawable.ic_character),
                    contentDescription = "프로필 캐릭터",
                    modifier = Modifier.size(88.dp),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(26.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(Color.White)
                        .clickable(onClick = onEditProfileClick),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "프로필 편집",
                        tint = TextSecondary,
                        modifier = Modifier.size(15.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "김윤서",
                    color = TextPrimary,
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "함께하는 시간을 좋아해요! ✨",
                    color = TextSecondary,
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFEAF1FF))
                        .clickable(onClick = onEditProfileClick)
                        .padding(horizontal = 13.dp, vertical = 7.dp),
                ) {
                    Text(
                        text = "프로필 편집",
                        color = TextSecondary,
                        fontFamily = SBAggroFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        MannerScoreCard()
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Divider),
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            MyStatItem(
                title = "참여 중인 모임",
                value = "8",
                color = Color(0xFF43C879),
                icon = Icons.Default.Person,
                modifier = Modifier.weight(1f),
            )
            MyStatItem(
                title = "관심 모임",
                value = "12",
                color = Color(0xFFFF6B9A),
                icon = Icons.Default.Favorite,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun MannerScoreCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(82.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF0F4FF))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Star, contentDescription = null, tint = MoaBlue, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "모임 지수",
                    color = TextSecondary,
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "다음 레벨까지 270",
                    color = TextSecondary,
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp,
                )
            }
        }
        Column(
            modifier = Modifier.width(96.dp),
            horizontalAlignment = Alignment.End,
        ) {
            Text(
                text = "730",
                color = MoaBlue,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFDDE7FF)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.70f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MoaBlue),
                )
            }
        }
    }
}

@Composable
private fun MyStatItem(
    title: String,
    value: String,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            color = TextSecondary,
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            color = color,
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = TextSecondary,
        fontFamily = SBAggroFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
    )
}

@Composable
private fun MenuGroup(items: List<MyMenuItem>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(18.dp), spotColor = Color(0xFFDDE4F2))
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White),
    ) {
        items.forEachIndexed { index, item ->
            MenuRow(item)
            if (index != items.lastIndex) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .padding(horizontal = 18.dp)
                        .background(Divider),
                )
            }
        }
    }
}

@Composable
private fun MenuRow(item: MyMenuItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clickable { }
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(item.icon, contentDescription = null, tint = item.iconColor, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = item.title,
            color = TextPrimary,
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = item.description,
            color = TextSecondary,
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(Icons.Default.KeyboardArrowRight, contentDescription = "이동", tint = TextSecondary, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun ReviewBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(82.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFEAF2FF))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Canvas(modifier = Modifier.size(58.dp)) {
            val center = Offset(size.width * 0.42f, size.height * 0.55f)
            val r = size.minDimension * 0.28f
            drawRoundRect(
                brush = Brush.verticalGradient(listOf(Color(0xFF68A2FF), Color(0xFF4D7DFF))),
                topLeft = Offset(center.x - r, center.y - r),
                size = Size(r * 2f, r * 2.15f),
                cornerRadius = CornerRadius(r, r),
            )
            drawCircle(Color.Black, r * 0.06f, Offset(center.x - r * 0.28f, center.y - r * 0.10f))
            drawCircle(Color.Black, r * 0.06f, Offset(center.x + r * 0.28f, center.y - r * 0.10f))
            drawArc(Color.Black, 20f, 140f, false, Offset(center.x - r * 0.13f, center.y - r * 0.05f), Size(r * 0.26f, r * 0.20f), style = Stroke(1.2.dp.toPx(), cap = StrokeCap.Round))
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(size.width * 0.62f, size.height * 0.12f),
                size = Size(size.width * 0.30f, size.height * 0.28f),
                cornerRadius = CornerRadius(14.dp.toPx()),
            )
            drawCircle(MoaBlue, 3.dp.toPx(), Offset(size.width * 0.76f, size.height * 0.26f))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "더 좋은 모임 경험을 만들어주세요!",
                color = TextPrimary,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "리뷰를 남겨주시면 큰 힘이 됩니다 💙",
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
                .clickable { }
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Text(
                text = "리뷰 남기기",
                color = MoaBlue,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
            )
        }
    }
}

@Composable
private fun LogoutButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .shadow(8.dp, RoundedCornerShape(18.dp), spotColor = Color(0xFFDDE4F2))
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "로그아웃",
            color = Color(0xFFFF5E70),
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
        )
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun MyPageScreenPreview() {
    Moa_ProjectTheme {
        MyPageScreen()
    }
}
