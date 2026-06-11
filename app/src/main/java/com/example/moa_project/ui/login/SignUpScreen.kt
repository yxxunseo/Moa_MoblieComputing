package com.example.moa_project.ui.login

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moa_project.ui.components.MoaMascot
import com.example.moa_project.ui.components.MoaMascotVariant
import com.example.moa_project.ui.theme.MoaPlaceholder
import com.example.moa_project.ui.theme.MoaTextPrimary
import com.example.moa_project.ui.theme.MoaTextSecondary
import com.example.moa_project.ui.theme.MoaTextTertiary
import com.example.moa_project.ui.theme.SBAggroFontFamily
private const val TOTAL_STEPS = 3

/** 회원가입 온보딩 전용 — 차분한 블루 톤 */
private val SignUpCalmBlue = Color(0xFF6B86E8)
private val SignUpCalmBlueSoft = Color(0xFFEEF1F8)
private val SignUpCalmGreen = Color(0xFF6B9E85)
private val SignUpCalmRed = Color(0xFFBF7A7A)
private val SignUpProgressInactive = Color(0xFFE8ECF4)
private val SignUpInputBorder = Color(0xFFE0E4F0)
private val SignUpFieldHeight = 60.dp
private val SignUpHorizontalPadding = 24.dp

@Composable
fun SignUpScreen(
    onBackClick: () -> Unit = {},
    onSignUpSuccess: () -> Unit = {},
    viewModel: SignUpViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.consumeErrorMessage()
        }
    }

    LaunchedEffect(state.step, state.loginIdAvailability, state.emailAvailability) {
        if (
            state.step == 0 &&
            (state.loginIdAvailability == FieldAvailability.Failed || state.emailAvailability == FieldAvailability.Failed)
        ) {
            delay(1500)
            viewModel.retryAvailabilityChecksIfNeeded()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        if (state.step < TOTAL_STEPS) {
            SignUpTopBar(
                step = state.step,
                onBack = { viewModel.goBack(onBackClick) },
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            when (state.step) {
                0 -> AccountStep(
                    loginId = state.loginId,
                    email = state.email,
                    password = state.password,
                    confirmPassword = state.confirmPassword,
                    hasLower = state.hasLower,
                    hasUpper = state.hasUpper,
                    hasSpecial = state.hasSpecial,
                    loginIdAvailability = state.loginIdAvailability,
                    emailAvailability = state.emailAvailability,
                    onLoginIdChange = viewModel::updateLoginId,
                    onEmailChange = viewModel::updateEmail,
                    onPasswordChange = viewModel::updatePassword,
                    onConfirmPasswordChange = viewModel::updateConfirmPassword,
                )
                1 -> NameStep(
                    name = state.nickname,
                    onNameChange = viewModel::updateNickname,
                )
                2 -> PurposeStep(
                    selected = state.selectedPurpose,
                    customText = state.customPurpose,
                    onSelect = viewModel::selectPurpose,
                    onCustomChange = viewModel::updateCustomPurpose,
                )
                else -> CompleteStep()
            }
        }

        SignUpBottomButton(
            label = when (state.step) {
                TOTAL_STEPS -> "시작하기"
                else -> "다음"
            },
            enabled = !state.isSubmitting,
            loading = state.isSubmitting,
            verticalPadding = when (state.step) {
                0 -> 12.dp
                else -> 24.dp
            },
            onClick = {
                viewModel.goNext {
                    Toast.makeText(context, "회원가입 완료!", Toast.LENGTH_SHORT).show()
                    onSignUpSuccess()
                }
            },
        )
    }
}

@Composable
private fun SignUpTopBar(step: Int, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 20.dp, top = 20.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기", tint = MoaTextPrimary)
        }
        SignUpProgressIndicator(currentStep = step + 1)
    }
}

@Composable
private fun RowScope.SignUpProgressIndicator(currentStep: Int) {
    Row(
        modifier = Modifier
            .weight(1f)
            .padding(start = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        repeat(TOTAL_STEPS) { index ->
            val isFilled = index + 1 <= currentStep
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(if (isFilled) SignUpCalmBlue else SignUpProgressInactive),
            )
        }
    }
}

@Composable
private fun AccountStep(
    loginId: String,
    email: String,
    password: String,
    confirmPassword: String,
    hasLower: Boolean,
    hasUpper: Boolean,
    hasSpecial: Boolean,
    loginIdAvailability: FieldAvailability,
    emailAvailability: FieldAvailability,
    onLoginIdChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
) {
    val emailFocus = remember { FocusRequester() }
    val passwordFocus = remember { FocusRequester() }
    val confirmFocus = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = SignUpHorizontalPadding),
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "MOA에 오신 걸 환영해요!",
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            lineHeight = 28.sp,
            color = MoaTextPrimary,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "먼저 계정 정보를 입력해 주세요.",
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            color = MoaTextSecondary,
        )
        Spacer(modifier = Modifier.height(12.dp))
        SignUpField(
            label = "아이디",
            value = loginId,
            onValueChange = onLoginIdChange,
            placeholder = "아이디",
            compact = true,
            imeAction = ImeAction.Next,
            onImeAction = { emailFocus.requestFocus() },
        )
        if (loginIdAvailability != FieldAvailability.Idle) {
            AvailabilityRequirement(
                status = loginIdAvailability,
                text = availabilityMessage(
                    status = loginIdAvailability,
                    availableText = "사용 가능한 아이디입니다",
                    duplicateText = "중복된 아이디입니다",
                ),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        SignUpField(
            label = "이메일",
            value = email,
            onValueChange = onEmailChange,
            placeholder = "이메일 주소",
            compact = true,
            focusRequester = emailFocus,
            imeAction = ImeAction.Next,
            onImeAction = { passwordFocus.requestFocus() },
        )
        if (emailAvailability != FieldAvailability.Idle) {
            AvailabilityRequirement(
                status = emailAvailability,
                text = availabilityMessage(
                    status = emailAvailability,
                    availableText = "사용 가능한 이메일입니다",
                    duplicateText = "중복된 이메일입니다",
                ),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        SignUpField(
            label = "비밀번호",
            value = password,
            onValueChange = onPasswordChange,
            placeholder = "비밀번호",
            isPassword = true,
            compact = true,
            focusRequester = passwordFocus,
            imeAction = ImeAction.Next,
            onImeAction = { confirmFocus.requestFocus() },
        )
        Spacer(modifier = Modifier.height(4.dp))
        PasswordRequirement("영문 소문자 포함", hasLower)
        PasswordRequirement("영문 대문자 포함", hasUpper)
        PasswordRequirement("특수문자 포함 (!@#$ 등)", hasSpecial)
        Spacer(modifier = Modifier.height(8.dp))
        SignUpField(
            label = "비밀번호 확인",
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            placeholder = "비밀번호 다시 입력",
            isPassword = true,
            compact = true,
            focusRequester = confirmFocus,
            imeAction = ImeAction.Done,
            onImeAction = { focusManager.clearFocus() },
        )
    }
}

private fun availabilityMessage(
    status: FieldAvailability,
    availableText: String,
    duplicateText: String,
): String = when (status) {
    FieldAvailability.Available -> availableText
    FieldAvailability.Duplicate -> duplicateText
    FieldAvailability.Checking -> "확인 중..."
    FieldAvailability.Failed -> "서버 연결 확인 중… 잠시만 기다려 주세요."
    FieldAvailability.Idle -> ""
}

@Composable
private fun AvailabilityRequirement(status: FieldAvailability, text: String) {
    val color = when (status) {
        FieldAvailability.Available -> SignUpCalmGreen
        FieldAvailability.Duplicate, FieldAvailability.Failed -> SignUpCalmRed
        else -> MoaTextSecondary
    }
    val icon = when (status) {
        FieldAvailability.Available -> "✓"
        FieldAvailability.Duplicate, FieldAvailability.Failed -> "•"
        else -> "•"
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 1.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = icon,
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = color,
        )
        Text(
            text = "  $text",
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = color,
        )
    }
}

@Composable
private fun NameStep(name: String, onNameChange: (String) -> Unit) {
    NicknameSetupContent(
        name = name,
        onNameChange = onNameChange,
        horizontalPadding = SignUpHorizontalPadding,
    )
}

@Composable
private fun PurposeStep(
    selected: SignUpPurposeOption?,
    customText: String,
    onSelect: (SignUpPurposeOption) -> Unit,
    onCustomChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = SignUpHorizontalPadding),
    ) {
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = "MOA를 주로\n어떻게 쓸 건가요?",
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            lineHeight = 32.sp,
            color = MoaTextPrimary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "하나만 선택해 주세요.",
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            color = MoaTextSecondary,
        )
        Spacer(modifier = Modifier.height(20.dp))
        SignUpPurposeOption.entries.forEach { option ->
            SelectableOptionRow(label = option.label, selected = selected == option, onClick = { onSelect(option) })
            if (option == SignUpPurposeOption.CUSTOM && selected == SignUpPurposeOption.CUSTOM) {
                Spacer(modifier = Modifier.height(8.dp))
                SignUpTextInput(
                    value = customText,
                    onValueChange = onCustomChange,
                    placeholder = "사용 목적을 입력해 주세요",
                    trailingCounter = "${customText.length}/20",
                )
                Spacer(modifier = Modifier.height(12.dp))
            } else {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun CompleteStep() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(SignUpCalmBlue).padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text("가입 완료", fontFamily = SBAggroFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "함께하는 시간,\nMOA와 시작해요.",
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            lineHeight = 34.sp,
            color = MoaTextPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(32.dp))
        MoaMascot(size = 140.dp, variant = MoaMascotVariant.Heart)
    }
}

@Composable
private fun SelectableOptionRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(if (selected) 2.dp else 1.dp, if (selected) SignUpCalmBlue else SignUpInputBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            fontFamily = SBAggroFontFamily,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 15.sp,
            color = if (selected) SignUpCalmBlue else MoaTextPrimary,
        )
        if (selected) {
            Box(Modifier.size(24.dp).clip(CircleShape).background(SignUpCalmBlue), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun SignUpTextInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    trailingCounter: String? = null,
    imeAction: ImeAction = ImeAction.Default,
    onImeAction: () -> Unit = {},
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(SignUpFieldHeight)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(1.dp, SignUpInputBorder, RoundedCornerShape(16.dp))
                .padding(horizontal = 18.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (value.isEmpty()) {
                Text(placeholder, fontFamily = SBAggroFontFamily, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = MoaPlaceholder)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(fontFamily = SBAggroFontFamily, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = MoaTextPrimary),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = imeAction),
                keyboardActions = KeyboardActions(
                    onNext = { onImeAction() },
                    onDone = { onImeAction() },
                ),
            )
        }
        trailingCounter?.let {
            Text(it, modifier = Modifier.fillMaxWidth().padding(top = 6.dp), fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Medium, fontSize = 12.sp, color = MoaTextTertiary, textAlign = TextAlign.End)
        }
    }
}

@Composable
private fun SignUpBottomButton(
    label: String,
    enabled: Boolean,
    loading: Boolean,
    onClick: () -> Unit,
    verticalPadding: Dp = 20.dp,
) {
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = SignUpHorizontalPadding, vertical = verticalPadding)) {
        Button(
            onClick = onClick,
            enabled = enabled && !loading,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SignUpCalmBlue,
                disabledContainerColor = SignUpCalmBlue.copy(alpha = 0.45f),
            ),
            shape = RoundedCornerShape(16.dp),
        ) {
            if (loading) {
                CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text(label, fontFamily = SBAggroFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            }
        }
    }
}

@Composable
private fun SignUpField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false,
    compact: Boolean = false,
    focusRequester: FocusRequester? = null,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {},
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            label,
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = if (compact) 13.sp else 14.sp,
            color = SignUpCalmBlue,
        )
        Spacer(modifier = Modifier.height(if (compact) 4.dp else 8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (compact) 48.dp else SignUpFieldHeight)
                .clip(RoundedCornerShape(if (compact) 12.dp else 16.dp))
                .background(Color.White)
                .border(1.dp, SignUpInputBorder, RoundedCornerShape(if (compact) 12.dp else 16.dp))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (value.isEmpty()) {
                Text(placeholder, fontFamily = SBAggroFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = MoaPlaceholder)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(fontFamily = SBAggroFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = MoaTextPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier),
                singleLine = true,
                visualTransformation = if (isPassword) PasswordVisualTransformation(mask = '*') else VisualTransformation.None,
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Text,
                    imeAction = imeAction,
                ),
                keyboardActions = KeyboardActions(
                    onNext = { onImeAction() },
                    onDone = { onImeAction() },
                ),
            )
        }
    }
}

@Composable
private fun PasswordRequirement(text: String, satisfied: Boolean) {
    val color = if (satisfied) SignUpCalmBlue else MoaTextSecondary
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(if (satisfied) "✓" else "•", fontFamily = SBAggroFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = color)
        Text("  $text", fontFamily = SBAggroFontFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = color)
    }
}
