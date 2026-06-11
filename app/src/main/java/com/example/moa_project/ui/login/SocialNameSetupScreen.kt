package com.example.moa_project.ui.login

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moa_project.ui.my.UserState
import com.example.moa_project.ui.my.UserViewModel
import com.example.moa_project.ui.theme.SBAggroFontFamily

private val SetupCalmBlue = Color(0xFF6B86E8)

@Composable
fun SocialNameSetupScreen(
    onComplete: () -> Unit = {},
    userViewModel: UserViewModel = viewModel(),
) {
    val context = LocalContext.current
    val userState by userViewModel.uiState.collectAsState()
    var nickname by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    LaunchedEffect(userState) {
        if (userState is UserState.Error) {
            isSubmitting = false
            Toast.makeText(context, (userState as UserState.Error).message, Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        NicknameSetupContent(
            name = nickname,
            onNameChange = { nickname = it },
            modifier = Modifier.weight(1f),
        )

        Button(
            onClick = {
                val trimmed = nickname.trim()
                if (trimmed.isBlank()) {
                    Toast.makeText(context, "이름을 입력해 주세요.", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                isSubmitting = true
                userViewModel.updateProfile(trimmed, null) {
                    isSubmitting = false
                    onComplete()
                }
            },
            enabled = !isSubmitting && nickname.trim().isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SetupCalmBlue),
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = Color.White,
                    strokeWidth = 2.dp,
                )
            } else {
                Text(
                    text = "시작하기",
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
        }
    }
}
