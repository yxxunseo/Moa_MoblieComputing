package com.example.moa_project.ui.meetings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moa_project.ui.components.MoaOutlinedTextField
import com.example.moa_project.ui.theme.MoaAccentBlueBg
import com.example.moa_project.ui.theme.MoaBlue
import com.example.moa_project.ui.theme.MoaError
import com.example.moa_project.ui.theme.MoaTextPrimary
import com.example.moa_project.ui.theme.MoaTextSecondary
import com.example.moa_project.ui.theme.SBAggroFontFamily

// 그룹 색상 팔레트 (hex 문자열 → Compose Color 쌍)
private val groupColorOptions = listOf(
    "#2179FE" to Color(0xFF2179FE),
    "#9B62FF" to Color(0xFF9B62FF),
    "#FF9C1A" to Color(0xFFFF9C1A),
    "#35A96D" to Color(0xFF35A96D),
    "#FF6262" to Color(0xFFFF6262),
    "#5E8CFF" to Color(0xFF5E8CFF),
    "#FFB800" to Color(0xFFFFB800),
    "#00B8D9" to Color(0xFF00B8D9),
)

/**
 * 모임 생성 / 입장 바텀시트 내부 컨텐츠
 * ModalBottomSheet 안에서 호출됨
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOrJoinMeetingSheet(
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    initialTab: Int = 0,
    initialInviteCode: String? = null,
    viewModel: GroupActionViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    // 탭 상태: 0 = 생성하기, 1 = 입장하기
    var selectedTab by remember(initialTab, initialInviteCode) {
        mutableIntStateOf(if (!initialInviteCode.isNullOrBlank()) 1 else initialTab)
    }

    // 생성 폼 상태
    var groupName by remember { mutableStateOf("") }
    var groupDescription by remember { mutableStateOf("") }
    var selectedColorHex by remember { mutableStateOf("#2179FE") }

    // 입장 폼 상태
    var inviteCode by remember(initialInviteCode) {
        mutableStateOf(initialInviteCode?.uppercase().orEmpty())
    }

    // 성공/에러 처리
    LaunchedEffect(state) {
        when (state) {
            is GroupActionState.CreateSuccess -> {
                onSuccess()
                viewModel.resetState()
            }
            is GroupActionState.JoinSuccess -> {
                onSuccess()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // ── 헤더 ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "모임 시작하기",
                color = MoaTextPrimary,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "닫기",
                    tint = MoaTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── 탭 선택 ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFF0F3FA))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val tabs = listOf("모임 만들기", "코드로 입장")
            tabs.forEachIndexed { index, label ->
                val isSelected = selectedTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) Color.White else Color.Transparent)
                        .clickable { selectedTab = index }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) MoaBlue else MoaTextSecondary,
                        fontFamily = SBAggroFontFamily,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── 탭 내용 ──
        if (selectedTab == 0) {
            // 모임 만들기 탭
            MoaOutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it },
                label = "모임 이름 *",
                placeholder = "예: 멋쟁이사자처럼 12기",
                maxLength = 30,
            )

            Spacer(modifier = Modifier.height(16.dp))

            MoaOutlinedTextField(
                value = groupDescription,
                onValueChange = { groupDescription = it },
                label = "모임 설명 (선택)",
                placeholder = "모임에 대해 간단히 소개해주세요",
                maxLength = 80,
                singleLine = false,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 색상 선택
            Text(
                text = "모임 색상",
                color = MoaTextSecondary,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                groupColorOptions.forEach { (hex, color) ->
                    ColorDot(
                        color = color,
                        isSelected = selectedColorHex == hex,
                        onClick = { selectedColorHex = hex }
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 에러 메시지
            if (state is GroupActionState.Error) {
                Text(
                    text = (state as GroupActionState.Error).message,
                    color = MoaError,
                    fontFamily = SBAggroFontFamily,
                    fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 생성 버튼
            Button(
                onClick = { viewModel.createGroup(groupName, groupDescription, selectedColorHex) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MoaBlue),
                enabled = state !is GroupActionState.Loading
            ) {
                if (state is GroupActionState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "모임 만들기",
                        color = Color.White,
                        fontFamily = SBAggroFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

        } else {
            // 코드로 입장 탭
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MoaAccentBlueBg)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MoaBlue,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = "초대 코드로 입장하기",
                        color = MoaBlue,
                        fontFamily = SBAggroFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "모임장에게 받은 코드를 입력하세요",
                        color = MoaTextSecondary,
                        fontFamily = SBAggroFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            MoaOutlinedTextField(
                value = inviteCode,
                onValueChange = { inviteCode = it.uppercase() },
                label = "초대 코드 *",
                placeholder = "예: MOA-A3X9K2",
                maxLength = 12,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Done,
                ),
            )

            Spacer(modifier = Modifier.height(28.dp))

            // 에러 메시지
            if (state is GroupActionState.Error) {
                Text(
                    text = (state as GroupActionState.Error).message,
                    color = MoaError,
                    fontFamily = SBAggroFontFamily,
                    fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 입장 버튼
            Button(
                onClick = { viewModel.joinGroup(inviteCode) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MoaBlue),
                enabled = state !is GroupActionState.Loading
            ) {
                if (state is GroupActionState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "모임 입장하기",
                        color = Color.White,
                        fontFamily = SBAggroFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun ColorDot(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = Color.White,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "선택됨",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
