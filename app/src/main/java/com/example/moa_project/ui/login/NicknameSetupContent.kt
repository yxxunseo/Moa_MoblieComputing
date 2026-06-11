package com.example.moa_project.ui.login

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moa_project.ui.theme.MoaPlaceholder
import com.example.moa_project.ui.theme.MoaTextPrimary
import com.example.moa_project.ui.theme.MoaTextTertiary
import com.example.moa_project.ui.theme.SBAggroFontFamily

private val NicknameInputBorder = Color(0xFFE0E4F0)
private val NicknameFieldHeight = 60.dp

@Composable
fun NicknameSetupContent(
    name: String,
    onNameChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    horizontalPadding: androidx.compose.ui.unit.Dp = 24.dp,
) {
    val focusManager = LocalFocusManager.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = horizontalPadding),
    ) {
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = "반가워요!\n제가 불러드릴 이름을\n알려주세요.",
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            lineHeight = 32.sp,
            color = MoaTextPrimary,
        )
        Spacer(modifier = Modifier.height(40.dp))
        NicknameTextInput(
            value = name,
            onValueChange = { if (it.length <= 20) onNameChange(it) },
            placeholder = "ex. 홍길동",
            imeAction = ImeAction.Done,
            onImeAction = { focusManager.clearFocus() },
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "특수문자를 제외한 한글, 영어만 입력해 주세요. (${name.length}/20)",
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = MoaTextTertiary,
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun NicknameTextInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    imeAction: ImeAction = ImeAction.Default,
    onImeAction: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(NicknameFieldHeight)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, NicknameInputBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (value.isEmpty()) {
            Text(
                placeholder,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = MoaPlaceholder,
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = MoaTextPrimary,
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = imeAction),
            keyboardActions = KeyboardActions(
                onNext = { onImeAction() },
                onDone = { onImeAction() },
            ),
        )
    }
}

fun isPlaceholderNickname(nickname: String?): Boolean =
    nickname.isNullOrBlank() || nickname == "카카오유저"
