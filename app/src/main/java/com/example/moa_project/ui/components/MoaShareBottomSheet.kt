package com.example.moa_project.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moa_project.R
import com.example.moa_project.ui.theme.MoaBlue
import com.example.moa_project.ui.theme.MoaRadius
import com.example.moa_project.ui.theme.MoaTextPrimary
import com.example.moa_project.ui.theme.MoaTextSecondary
import com.example.moa_project.ui.theme.SBAggroFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoaShareBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onKakaoClick: () -> Unit,
    onCopyLinkClick: () -> Unit,
    onMoreClick: () -> Unit,
    kakaoEnabled: Boolean = true,
) {
    if (!visible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = MoaRadius.sheet, topEnd = MoaRadius.sheet),
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp, bottom = 36.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "공유하기",
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MoaTextPrimary,
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF0F2F7)),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "닫기",
                        tint = MoaTextSecondary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                MoaShareOption(
                    label = "카카오톡",
                    backgroundColor = Color(0xFFFEE500),
                    enabled = kakaoEnabled,
                    onClick = {
                        onDismiss()
                        onKakaoClick()
                    },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_kakao),
                            contentDescription = null,
                            tint = Color(0xFF3C1E1E),
                            modifier = Modifier.size(28.dp),
                        )
                    },
                )
                MoaShareOption(
                    label = "링크복사",
                    backgroundColor = MoaBlue,
                    onClick = {
                        onCopyLinkClick()
                        onDismiss()
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Link,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp),
                        )
                    },
                )
                MoaShareOption(
                    label = "더보기",
                    backgroundColor = Color(0xFFF0F2F7),
                    onClick = {
                        onMoreClick()
                        onDismiss()
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.MoreHoriz,
                            contentDescription = null,
                            tint = MoaTextSecondary,
                            modifier = Modifier.size(26.dp),
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun MoaShareOption(
    label: String,
    backgroundColor: Color,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    enabled: Boolean = true,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(
                    if (enabled) backgroundColor
                    else backgroundColor.copy(alpha = 0.45f),
                ),
            contentAlignment = Alignment.Center,
        ) {
            icon()
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = label,
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = if (enabled) MoaTextPrimary else MoaTextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}
