package com.example.moa_project.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.moa_project.R

/** 로그인 화면 전용 — 핑크퐁 아기상어 서체 */
val LoginPinkfongFontFamily = FontFamily(
    Font(R.font.pinkfong_light, FontWeight.Light),
    Font(R.font.pinkfong_regular, FontWeight.Normal),
    Font(R.font.pinkfong_regular, FontWeight.Medium),
    Font(R.font.pinkfong_bold, FontWeight.SemiBold),
    Font(R.font.pinkfong_bold, FontWeight.Bold),
)

/** 전역 폰트(기존 이름 유지로 참조 호환) */
val SBAggroFontFamily = FontFamily(
    Font(R.font.pretendard_regular, FontWeight.Normal),
    Font(R.font.pretendard_medium, FontWeight.Medium),
    Font(R.font.pretendard_semibold, FontWeight.SemiBold),
    Font(R.font.pretendard_bold, FontWeight.Bold),
)

private val body = TextStyle(
    fontFamily = SBAggroFontFamily,
    color = MoaTextPrimary,
)

val Typography = Typography(
    displayLarge = body.copy(fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 40.sp),
    displayMedium = body.copy(fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 36.sp),
    displaySmall = body.copy(fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 32.sp),
    headlineLarge = body.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 30.sp),
    headlineMedium = body.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 28.sp),
    headlineSmall = body.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 26.sp),
    titleLarge = body.copy(fontWeight = FontWeight.Bold, fontSize = 17.sp, lineHeight = 24.sp),
    titleMedium = body.copy(fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 22.sp),
    titleSmall = body.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.sp, lineHeight = 18.sp),
    bodyLarge = body.copy(fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = body.copy(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall = body.copy(fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 18.sp),
    labelLarge = body.copy(fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = body.copy(fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 17.sp),
    labelSmall = body.copy(fontWeight = FontWeight.Normal, fontSize = 11.sp, lineHeight = 15.sp),
)
