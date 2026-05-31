package com.example.moa_project.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.moa_project.R

val SBAggroFontFamily = FontFamily(
    Font(R.font.sb_aggro_l, FontWeight.Light),
    Font(R.font.sb_aggro_m, FontWeight.Medium),
    Font(R.font.sb_aggro_b, FontWeight.Bold)
)

private val moaTextStyle = TextStyle(fontFamily = SBAggroFontFamily)

val Typography = Typography(
    displayLarge = moaTextStyle.copy(fontWeight = FontWeight.Bold, fontSize = 57.sp),
    displayMedium = moaTextStyle.copy(fontWeight = FontWeight.Bold, fontSize = 45.sp),
    displaySmall = moaTextStyle.copy(fontWeight = FontWeight.Bold, fontSize = 36.sp),
    headlineLarge = moaTextStyle.copy(fontWeight = FontWeight.Bold, fontSize = 32.sp),
    headlineMedium = moaTextStyle.copy(fontWeight = FontWeight.Bold, fontSize = 28.sp),
    headlineSmall = moaTextStyle.copy(fontWeight = FontWeight.Bold, fontSize = 24.sp),
    titleLarge = moaTextStyle.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp),
    titleMedium = moaTextStyle.copy(fontWeight = FontWeight.Medium, fontSize = 16.sp),
    titleSmall = moaTextStyle.copy(fontWeight = FontWeight.Medium, fontSize = 14.sp),
    bodyLarge = moaTextStyle.copy(fontWeight = FontWeight.Medium, fontSize = 16.sp),
    bodyMedium = moaTextStyle.copy(fontWeight = FontWeight.Medium, fontSize = 14.sp),
    bodySmall = moaTextStyle.copy(fontWeight = FontWeight.Medium, fontSize = 12.sp),
    labelLarge = moaTextStyle.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
    labelMedium = moaTextStyle.copy(fontWeight = FontWeight.Medium, fontSize = 12.sp),
    labelSmall = moaTextStyle.copy(fontWeight = FontWeight.Medium, fontSize = 11.sp),
)
