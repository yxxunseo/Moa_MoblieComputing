package com.example.moa_project

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import com.example.moa_project.ui.components.MeetingActionCard
import com.example.moa_project.ui.components.MoaBottomNavigationBar
import com.example.moa_project.ui.theme.SBAggroFontFamily

@Composable
fun InitialHomeScreen(
    userName: String = "윤서",
    currentRoute: String = "home",
    onNavigate: (String) -> Unit = {},
    onCreateMeetingClick: () -> Unit = {},
    onJoinMeetingClick: () -> Unit = {}
) {
    Scaffold(
        bottomBar = {
            // 하단 네비게이션 바 부착 (추후 프로필 이미지 전달 가능)
            com.example.moa_project.ui.components.MoaBottomNavigationBar(
                currentRoute = currentRoute,
                profileImageResId = null, // 기본값 ic_character 사용
                onNavigate = onNavigate
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F8FC))
                .padding(innerPadding) // 하단 바 영역만큼의 여백을 확보
                .padding(horizontal = 20.dp), // 24dp에서 20dp로 줄여서 화면을 더 넓게 씀
            verticalArrangement = Arrangement.Center
        ) {
            // 인사말 텍스트 (Medium 적용)
            Text(
                text = "안녕하세요, ${userName}님!",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = SBAggroFontFamily,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            // 슬로건 텍스트 (Light 적용, '모아' 부분만 포인트 컬러 유지)
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Light)) {
                        append("함께 시간을 ")
                    }
                    withStyle(style = SpanStyle(color = Color(0xFF2179FE), fontWeight = FontWeight.Light)) {
                        append("모아")
                    }
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Light)) {
                        append(",\n더 좋은 순간을 만들어요")
                    }
                },
                fontSize = 26.sp,
                fontFamily = SBAggroFontFamily,
                color = Color.Black,
                lineHeight = 36.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            // 캐릭터 이미지와 카드들이 겹치도록 Box 레이아웃 사용
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                // 카드 목록
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 55.dp) // 캐릭터가 걸터앉을 수 있도록 공간 세밀 조정
                ) {
                    // 모임 생성하기 카드
                    MeetingActionCard(
                        titlePrefix = "모임",
                        titleSuffix = " 생성하기",
                        description = "새로운 모임을 만들고\n일정을 함께 조율해요",
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "생성하기",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        imageResId = R.drawable.ic_create,
                        imageSize = 95.dp, // 크기 적절히 조절
                        onClick = onCreateMeetingClick
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // 모임 입장하기 카드
                    MeetingActionCard(
                        titlePrefix = "모임",
                        titleSuffix = " 입장하기",
                        description = "초대코드나 링크로\n모임에 참여해요",
                        icon = {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "입장하기",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        imageResId = R.drawable.ic_position,
                        imageSize = 120.dp, // 너무 컸던 이미지를 줄임
                        onClick = onJoinMeetingClick
                    )
                }

                // 캐릭터를 살짝 작게 하고 엉덩이가 걸쳐지도록 offset 수정 (zIndex로 앞쪽 배치)
                Image(
                    painter = painterResource(id = R.drawable.ic_character),
                    contentDescription = "환영 캐릭터",
                    modifier = Modifier
                        .zIndex(1f)
                        .size(85.dp) // 캐릭터 크기 축소
                        .align(Alignment.TopEnd)
                        .offset(x = (-30).dp, y = -10.dp) // 엉덩이가 박스 위에 얹어지도록 Y축 밀어줌
                )
            }
        }
    }
}
