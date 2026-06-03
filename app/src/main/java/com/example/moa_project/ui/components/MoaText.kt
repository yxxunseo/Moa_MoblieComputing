package com.example.moa_project.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.moa_project.ui.theme.MoaTextPrimary
import com.example.moa_project.ui.theme.MoaTextSecondary
import com.example.moa_project.ui.theme.SBAggroFontFamily

@Composable
fun MoaBodyText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 14.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    color: Color = MoaTextPrimary,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = (fontSize.value * 1.45f).sp,
) {
    Text(
        text = text,
        modifier = modifier,
        fontFamily = SBAggroFontFamily,
        fontWeight = fontWeight,
        fontSize = fontSize,
        lineHeight = lineHeight,
        color = color,
        maxLines = maxLines,
        overflow = overflow,
        textAlign = textAlign,
    )
}

/** 화면·섹션 제목 */
@Composable
fun MoaTitleText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 18.sp,
    fontWeight: FontWeight = FontWeight.Bold,
    color: Color = MoaTextPrimary,
    maxLines: Int = 2,
) {
    Text(
        text = text,
        modifier = modifier,
        fontFamily = SBAggroFontFamily,
        fontWeight = fontWeight,
        fontSize = fontSize,
        lineHeight = (fontSize.value * 1.35f).sp,
        color = color,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
    )
}

/** 카드/리스트 강조 한 줄 */
@Composable
fun MoaLabelText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 15.sp,
    color: Color = MoaTextPrimary,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    MoaBodyText(
        text = text,
        modifier = modifier,
        fontSize = fontSize,
        fontWeight = FontWeight.Medium,
        color = color,
        maxLines = maxLines,
        overflow = overflow,
    )
}

/** 보조 설명 */
@Composable
fun MoaCaptionText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MoaTextSecondary,
) {
    MoaBodyText(
        text = text,
        modifier = modifier,
        fontSize = 13.sp,
        fontWeight = FontWeight.Normal,
        color = color,
        lineHeight = 19.sp,
    )
}
