package com.example.moa_project.ui.login

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moa_project.BuildConfig
import com.example.moa_project.R
import com.example.moa_project.ui.components.MoaMascot
import com.example.moa_project.ui.components.MoaMascotVariant
import com.example.moa_project.ui.theme.MoaBlue
import com.example.moa_project.ui.theme.MoaCardShadow
import com.example.moa_project.ui.theme.MoaPlaceholder
import com.example.moa_project.ui.theme.MoaScreenBackground
import com.example.moa_project.ui.theme.MoaTextPrimary
import com.example.moa_project.ui.theme.MoaTextSecondary
import com.example.moa_project.ui.theme.Moa_ProjectTheme
import com.example.moa_project.ui.theme.SBAggroFontFamily
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

@Composable
fun LoginScreen(
    onLoginClick: () -> Unit = {},
    onNavigateToSignUp: () -> Unit = {},
    authViewModel: AuthViewModel = viewModel(),
) {
    val context = LocalContext.current
    val loginState by authViewModel.loginState.collectAsState()

    LaunchedEffect(loginState) {
        when (val state = loginState) {
            is LoginState.Success -> {
                android.widget.Toast.makeText(context, "${state.provider} 로그인 성공!", android.widget.Toast.LENGTH_SHORT).show()
                onLoginClick()
            }
            is LoginState.Error -> {
                android.widget.Toast.makeText(context, state.message, android.widget.Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        authViewModel.handleGoogleSignInResult(result.data, onSuccess = onLoginClick)
    }

    val onGoogleLoginClick = {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.GOOGLE_CLIENT_ID)
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        googleSignInLauncher.launch(googleSignInClient.signInIntent)
    }

    val onKakaoLoginClick = { authViewModel.loginWithKakao(context, onSuccess = onLoginClick) }
    var id by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MoaScreenBackground),
    ) {
        LoginHeader(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 56.dp, bottom = 8.dp),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(18.dp), spotColor = MoaCardShadow)
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
                .padding(horizontal = 24.dp, vertical = 36.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "MOA",
                    style = TextStyle(
                        fontFamily = SBAggroFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 44.sp,
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF749BFF), Color(0xFFA594FF)),
                        ),
                    ),
                )

                Spacer(modifier = Modifier.height(28.dp))

                InputField(
                    label = "ID",
                    value = id,
                    onValueChange = { id = it },
                    placeholder = "ID를 입력하세요",
                )

                Spacer(modifier = Modifier.height(18.dp))

                InputField(
                    label = "PW",
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "비밀번호를 입력하세요",
                    isPassword = true,
                )

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = {
                        if (id.isBlank() || password.isBlank()) {
                            android.widget.Toast.makeText(context, "아이디와 비밀번호를 입력해 주세요.", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            authViewModel.loginWithEmail(id, password, onSuccess = onLoginClick)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MoaBlue),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(
                        text = "로그인하기",
                        fontFamily = SBAggroFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "처음이신가요?",
                        fontFamily = SBAggroFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = MoaTextSecondary,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "회원가입",
                        fontFamily = SBAggroFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MoaBlue,
                        modifier = Modifier.clickable { onNavigateToSignUp() },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Canvas(modifier = Modifier.weight(1f).height(1.dp)) {
                    drawLine(
                        color = Color(0xFFDCE0EA),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f),
                    )
                }
                Text(
                    text = "sns로 로그인",
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = Color(0xFFAAB0C6),
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Canvas(modifier = Modifier.weight(1f).height(1.dp)) {
                    drawLine(
                        color = Color(0xFFDCE0EA),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f),
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SnsButton(
                    backgroundColor = Color(0xFFFEE500),
                    onClick = onKakaoLoginClick,
                    content = {
                        androidx.compose.foundation.Image(
                            painter = painterResource(id = R.drawable.ic_kakao),
                            contentDescription = "Kakao Login",
                            modifier = Modifier.size(24.dp),
                        )
                    },
                )
                Spacer(modifier = Modifier.width(32.dp))
                SnsButton(
                    backgroundColor = Color.White,
                    onClick = onGoogleLoginClick,
                    content = {
                        androidx.compose.foundation.Image(
                            painter = painterResource(id = R.drawable.ic_google),
                            contentDescription = "Google Login",
                            modifier = Modifier.size(24.dp),
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun LoginHeader(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MoaMascot(
            size = 108.dp,
            variant = MoaMascotVariant.Heart,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "친구들을 모아봐요",
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = MoaTextPrimary,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "함께 하는 시간 MOA",
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = MoaTextSecondary,
        )
    }
}

@Composable
private fun InputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MoaBlue,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MoaScreenBackground)
                .border(1.dp, MoaBlue.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = MoaPlaceholder,
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = MoaTextPrimary,
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (isPassword) PasswordVisualTransformation(mask = '*') else VisualTransformation.None,
                keyboardOptions = if (isPassword) KeyboardOptions(keyboardType = KeyboardType.Password) else KeyboardOptions.Default,
            )
        }
    }
}

@Composable
private fun SnsButton(
    backgroundColor: Color,
    onClick: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .shadow(elevation = 6.dp, shape = CircleShape, spotColor = MoaCardShadow)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
fun LoginScreenPreview() {
    Moa_ProjectTheme {
        LoginScreen()
    }
}
