package com.example.moa_project.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moa_project.ui.theme.MoaTextPrimary
import com.example.moa_project.ui.theme.MoaTextSecondary
import com.example.moa_project.ui.theme.SBAggroFontFamily

private const val TOTAL_STEPS = 4
private val OnboardingBlue = Color(0xFF6B86E8)
private val OnboardingProgressInactive = Color(0xFFE8ECF4)
private val OnboardingHorizontalPadding = 24.dp

private data class OnboardingPage(
    val title: String,
    val subtitle: String,
    val tip: String,
    val activeRoute: String,
    val preview: @Composable () -> Unit,
)

private val onboardingPages = listOf(
    OnboardingPage(
        title = "내 모임",
        subtitle = "스터디·동아리처럼 반복되는 장기 모임을 관리해요",
        tip = "모임 탭 → + 버튼으로 만들고 초대 링크를 공유하세요",
        activeRoute = "meetings",
        preview = { GroupsOnboardingPreview() },
    ),
    OnboardingPage(
        title = "단기 일정",
        subtitle = "1회성 회의·약속을 링크로 빠르게 조율해요",
        tip = "단기 일정 탭 → + 로 만들고 링크를 공유하세요",
        activeRoute = "meetings",
        preview = { GuestScheduleOnboardingPreview() },
    ),
    OnboardingPage(
        title = "일정 & 캘린더",
        subtitle = "히트맵으로 조율하고, 확정 일정은 캘린더에 반영돼요",
        tip = "모임에서 일정을 만들고 가능 시간을 투표해요",
        activeRoute = "calendar",
        preview = { ScheduleCalendarOnboardingPreview() },
    ),
    OnboardingPage(
        title = "홈 대시보드",
        subtitle = "조율·확정 일정과 바로가기를 한곳에서 확인해요",
        tip = "홈에서 이번 주 일정과 조율 현황을 확인하세요",
        activeRoute = "home",
        preview = { DashboardOnboardingPreview() },
    ),
)

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
) {
    var step by remember { mutableIntStateOf(0) }
    val page = onboardingPages[step]
    val isLastStep = step == TOTAL_STEPS - 1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        OnboardingTopBar(
            step = step,
            onBack = { if (step > 0) step -= 1 },
            onSkip = onComplete,
            showBack = step > 0,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = OnboardingHorizontalPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = page.title,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = MoaTextPrimary,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = page.subtitle,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = MoaTextSecondary,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = page.tip,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = OnboardingBlue,
            )

            Spacer(modifier = Modifier.height(16.dp))

            OnboardingDevicePreview(activeRoute = page.activeRoute) {
                page.preview()
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        OnboardingBottomButton(
            label = if (isLastStep) "시작하기" else "다음",
            onClick = {
                if (isLastStep) onComplete() else step += 1
            },
        )
    }
}

@Composable
private fun OnboardingTopBar(
    step: Int,
    onBack: () -> Unit,
    onSkip: () -> Unit,
    showBack: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 12.dp, top = 12.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showBack) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "뒤로가기",
                    tint = MoaTextPrimary,
                )
            }
        } else {
            Spacer(modifier = Modifier.size(48.dp))
        }
        OnboardingProgressIndicator(currentStep = step + 1)
        TextButton(onClick = onSkip) {
            Text(
                text = "건너뛰기",
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = MoaTextSecondary,
            )
        }
    }
}

@Composable
private fun RowScope.OnboardingProgressIndicator(currentStep: Int) {
    Row(
        modifier = Modifier
            .weight(1f)
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        repeat(TOTAL_STEPS) { index ->
            val isFilled = index + 1 <= currentStep
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(if (isFilled) OnboardingBlue else OnboardingProgressInactive),
            )
        }
    }
}

@Composable
private fun OnboardingBottomButton(
    label: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = OnboardingHorizontalPadding, vertical = 16.dp),
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = OnboardingBlue),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(
                text = label,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White,
            )
        }
    }
}
