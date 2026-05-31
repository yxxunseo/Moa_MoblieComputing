package com.example.moa_project.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moa_project.ui.theme.MoaBlue
import com.example.moa_project.ui.theme.MoaCardShadow
import com.example.moa_project.ui.theme.MoaPlaceholder
import com.example.moa_project.ui.theme.MoaScreenBackground
import com.example.moa_project.ui.theme.MoaTextPrimary
import com.example.moa_project.ui.theme.MoaTextSecondary
import com.example.moa_project.ui.theme.SBAggroFontFamily

@Composable
fun SignUpScreen(
    onBackClick: () -> Unit = {},
    onSignUpSuccess: () -> Unit = {},
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val loginState by authViewModel.loginState.collectAsState()
    var email by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    LaunchedEffect(loginState) {
        when (val state = loginState) {
            is LoginState.Success -> {
                android.widget.Toast.makeText(context, "회원가입 완료!", android.widget.Toast.LENGTH_SHORT).show()
                onSignUpSuccess()
            }
            is LoginState.Error -> {
                android.widget.Toast.makeText(context, state.message, android.widget.Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MoaScreenBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 48.dp)
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기", tint = MoaTextPrimary)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(18.dp), spotColor = MoaCardShadow)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .padding(24.dp)
            ) {
            Text(
                text = "회원가입",
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                color = MoaTextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "모아와 함께 일정을 조율해 보세요",
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = MoaTextSecondary
            )

            Spacer(modifier = Modifier.height(32.dp))

            SignUpField("닉네임", nickname, { nickname = it }, "닉네임을 입력하세요")
            Spacer(modifier = Modifier.height(16.dp))
            SignUpField("이메일", email, { email = it }, "이메일을 입력하세요")
            Spacer(modifier = Modifier.height(16.dp))
            SignUpField("비밀번호", password, { password = it }, "비밀번호를 입력하세요", isPassword = true)
            Spacer(modifier = Modifier.height(16.dp))
            SignUpField("비밀번호 확인", confirmPassword, { confirmPassword = it }, "비밀번호를 다시 입력하세요", isPassword = true)

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    when {
                        nickname.isBlank() || email.isBlank() || password.isBlank() -> {
                            android.widget.Toast.makeText(context, "모든 항목을 입력해주세요.", android.widget.Toast.LENGTH_SHORT).show()
                        }
                        password != confirmPassword -> {
                            android.widget.Toast.makeText(context, "비밀번호가 일치하지 않습니다.", android.widget.Toast.LENGTH_SHORT).show()
                        }
                        password.length < 6 -> {
                            android.widget.Toast.makeText(context, "비밀번호는 6자 이상이어야 합니다.", android.widget.Toast.LENGTH_SHORT).show()
                        }
                        else -> authViewModel.signup(email, password, nickname, onSuccess = onSignUpSuccess)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MoaBlue),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "가입하기",
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "이미 계정이 있으신가요? 로그인하기",
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = MoaBlue,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable(onClick = onBackClick)
            )
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
    isPassword: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MoaBlue
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MoaScreenBackground)
                .border(1.dp, MoaBlue.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = MoaPlaceholder
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = MoaTextPrimary
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}
