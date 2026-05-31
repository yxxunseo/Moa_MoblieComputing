package com.example.moa_project.ui.my

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moa_project.ui.theme.SBAggroFontFamily

private val MoaBlue = Color(0xFF2179FE)
private val ScreenBackground = Color(0xFFF7F8FC)
private val TextPrimary = Color(0xFF101B33)
private val TextSecondary = Color(0xFF737C99)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBackClick: () -> Unit = {},
    onSaveSuccess: () -> Unit = {},
    userViewModel: UserViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by userViewModel.uiState.collectAsState()
    val currentNickname = (uiState as? UserState.Success)?.user?.nickname.orEmpty()
    var nickname by remember(currentNickname) { mutableStateOf(currentNickname) }
    val isLoading = uiState is UserState.Loading

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "프로필 편집",
                        fontFamily = SBAggroFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = ScreenBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Text(
                text = "닉네임",
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
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE8EBF2), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    textStyle = TextStyle(
                        fontFamily = SBAggroFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            if (uiState is UserState.Error) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = (uiState as UserState.Error).message,
                    color = Color(0xFFFF6262),
                    fontFamily = SBAggroFontFamily,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                enabled = !isLoading && nickname.isNotBlank(),
                onClick = {
                    userViewModel.updateProfile(nickname.trim()) {
                        android.widget.Toast.makeText(context, "프로필이 저장되었습니다.", android.widget.Toast.LENGTH_SHORT).show()
                        onSaveSuccess()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MoaBlue),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.height(22.dp))
                } else {
                    Text(
                        text = "저장하기",
                        fontFamily = SBAggroFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "이메일: ${(uiState as? UserState.Success)?.user?.email ?: "-"}",
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = TextSecondary
            )
        }
    }
}
