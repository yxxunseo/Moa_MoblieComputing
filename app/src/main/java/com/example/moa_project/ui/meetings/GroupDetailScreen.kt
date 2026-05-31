package com.example.moa_project.ui.meetings

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import android.content.ClipboardManager
import android.content.ClipData
import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import com.example.moa_project.ui.components.MoaDialogButtonText
import com.example.moa_project.ui.components.MoaOutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moa_project.network.GroupResponse
import com.example.moa_project.network.GroupMemberResponse
import com.example.moa_project.network.ScheduleDetailResponse
import com.example.moa_project.ui.theme.SBAggroFontFamily
import java.time.LocalDate

private val MoaBlue = Color(0xFF2179FE)
private val ScreenBackground = Color(0xFFF7F8FC)
private val TextPrimary = Color(0xFF101B33)
private val TextSecondary = Color(0xFF737C99)

/**
 * 그룹 상세 화면 - 해당 그룹의 일정 목록과 그룹 정보 표시
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    groupId: Long,
    onBackClick: () -> Unit = {},
    onScheduleClick: (Long) -> Unit = {},       // CONFIRMED/DONE → 결과 화면
    onCoordinateClick: (Long) -> Unit = {},     // WAITING/ADJUSTING → 조율 화면
    onCoordinateScheduleCreated: (Long) -> Unit = {}
) {
    val viewModel: GroupDetailViewModel = viewModel(
        factory = GroupDetailViewModel.Factory(groupId),
        key = "group_$groupId"
    )
    val context = LocalContext.current
    val createScheduleViewModel: CreateScheduleViewModel = viewModel(
        factory = CreateScheduleViewModel.Factory(groupId),
        key = "create_schedule_$groupId"
    )
    val state by viewModel.state.collectAsState()
    val createState by createScheduleViewModel.state.collectAsState()
    var showCreateScheduleDialog by remember { mutableStateOf(false) }
    var showLeaveDialog by remember { mutableStateOf(false) }

    LaunchedEffect(createState) {
        val success = createState as? CreateScheduleState.Success
        if (success != null) {
            showCreateScheduleDialog = false
            createScheduleViewModel.resetState()
            onCoordinateScheduleCreated(success.schedule.id)
        }
    }

    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = {
                Text("모임 나가기", fontFamily = SBAggroFontFamily, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "이 모임에서 나가시겠어요? 다시 입장하려면 초대 코드가 필요합니다.",
                    fontFamily = SBAggroFontFamily,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showLeaveDialog = false
                    viewModel.leaveGroup(
                        onSuccess = onBackClick,
                        onError = { msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() }
                    )
                }) {
                    Text("나가기", color = Color(0xFFFF6262), fontFamily = SBAggroFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveDialog = false }) {
                    Text("취소", fontFamily = SBAggroFontFamily)
                }
            }
        )
    }

    if (showCreateScheduleDialog) {
        CreateScheduleDialog(
            state = createState,
            onDismiss = {
                showCreateScheduleDialog = false
                createScheduleViewModel.resetState()
            },
            onCreate = { title, description, startDate, endDate ->
                createScheduleViewModel.createSchedule(title, description, startDate, endDate)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val groupName = when (val s = state) {
                        is GroupDetailState.Success -> s.group.name
                        else -> "모임 상세"
                    }
                    Text(
                        text = groupName,
                        fontFamily = SBAggroFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                modifier = Modifier.shadow(elevation = 2.dp)
            )
        },
        containerColor = ScreenBackground
    ) { innerPadding ->
        when (val s = state) {
            is GroupDetailState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MoaBlue)
                }
            }

            is GroupDetailState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = s.message,
                            color = TextSecondary,
                            fontFamily = SBAggroFontFamily,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadGroupDetail() },
                            colors = ButtonDefaults.buttonColors(containerColor = MoaBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("다시 시도", color = Color.White, fontFamily = SBAggroFontFamily)
                        }
                    }
                }
            }

            is GroupDetailState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 20.dp, vertical = 24.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 그룹 정보 카드
                    item {
                        GroupInfoCard(group = s.group)
                    }

                    if (s.members.isNotEmpty()) {
                        item {
                            GroupMembersCard(members = s.members)
                        }
                    }

                    // 일정 조율 시작 버튼
                    item {
                        Button(
                            onClick = { showCreateScheduleDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MoaBlue)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "새 일정 조율 시작",
                                color = Color.White,
                                fontFamily = SBAggroFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    // 일정 목록 헤더
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "일정 목록",
                                color = TextPrimary,
                                fontFamily = SBAggroFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "${s.schedules.size}개",
                                color = MoaBlue,
                                fontFamily = SBAggroFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    // 일정 없을 때
                    if (s.schedules.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = null,
                                        tint = Color(0xFFDDE4F2),
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "아직 일정이 없어요\n새 일정 조율을 시작해보세요!",
                                        color = TextSecondary,
                                        fontFamily = SBAggroFontFamily,
                                        fontSize = 13.sp,
                                        lineHeight = 20.sp,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    // 일정 카드 목록
                    items(s.schedules) { schedule ->
                        ScheduleCard(
                            schedule = schedule,
                            onClick = {
                                if (schedule.status == "CONFIRMED" || schedule.status == "DONE") {
                                    onScheduleClick(schedule.id)
                                } else {
                                    onCoordinateClick(schedule.id)
                                }
                            }
                        )
                    }

                    item {
                        TextButton(
                            onClick = { showLeaveDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "모임 나가기",
                                color = Color(0xFFFF6262),
                                fontFamily = SBAggroFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupInfoCard(group: GroupResponse) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = Color(0xFFDDE4F2))
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // 그룹 색상 아이콘
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        try { Color(android.graphics.Color.parseColor(group.color)) }
                        catch (e: Exception) { MoaBlue }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = group.name.take(1),
                    color = Color.White,
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = group.name,
                    color = TextPrimary,
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                if (!group.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = group.description,
                        color = TextSecondary,
                        fontFamily = SBAggroFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF0F5FF))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MoaBlue,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${group.memberCount}명 참여 중",
                color = MoaBlue,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Moa Invite Code", group.inviteCode)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "초대 코드가 복사되었습니다!", Toast.LENGTH_SHORT).show()
                    }
                    .background(Color(0xFFE2ECFF))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "초대 코드: ${group.inviteCode}",
                    color = MoaBlue,
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "복사",
                    tint = MoaBlue,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
private fun GroupMembersCard(members: List<GroupMemberResponse>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color(0xFFDDE4F2))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text(
            text = "멤버 ${members.size}명",
            color = TextPrimary,
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        members.forEach { member ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MoaBlue.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = member.nickname.take(1),
                        color = MoaBlue,
                        fontFamily = SBAggroFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = member.nickname,
                        color = TextPrimary,
                        fontFamily = SBAggroFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = if (member.role == "ADMIN") "관리자" else "멤버",
                        color = TextSecondary,
                        fontFamily = SBAggroFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ScheduleCard(
    schedule: ScheduleDetailResponse,
    onClick: () -> Unit
) {
    val statusColor = when (schedule.status) {
        "CONFIRMED" -> Color(0xFF35A96D)
        "WAITING" -> MoaBlue
        "ADJUSTING" -> Color(0xFFFF9C1A)
        "DONE" -> TextSecondary
        else -> TextSecondary
    }
    val statusLabel = when (schedule.status) {
        "WAITING" -> "응답 대기"
        "ADJUSTING" -> "조율 중"
        "CONFIRMED" -> "확정됨"
        "DONE" -> "완료됨"
        else -> schedule.status
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color(0xFFDDE4F2))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(statusColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = schedule.title,
                color = TextPrimary,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${schedule.startDate} ~ ${schedule.endDate}",
                color = TextSecondary,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(statusColor.copy(alpha = 0.1f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = statusLabel,
                    color = statusColor,
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }

        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = "상세 보기",
            tint = TextSecondary,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun CreateScheduleDialog(
    state: CreateScheduleState,
    onDismiss: () -> Unit,
    onCreate: (String, String, LocalDate, LocalDate) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var startDateText by remember { mutableStateOf(LocalDate.now().toString()) }
    var endDateText by remember { mutableStateOf(LocalDate.now().plusDays(5).toString()) }
    var localError by remember { mutableStateOf<String?>(null) }
    val isLoading = state is CreateScheduleState.Loading
    val serverError = (state as? CreateScheduleState.Error)?.message

    AlertDialog(
        onDismissRequest = {
            if (!isLoading) onDismiss()
        },
        title = {
            Text(
                text = "새 일정 조율",
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                MoaOutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = "일정 제목",
                    modifier = Modifier.fillMaxWidth(),
                )
                MoaOutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "설명",
                    modifier = Modifier.fillMaxWidth(),
                )
                MoaOutlinedTextField(
                    value = startDateText,
                    onValueChange = { startDateText = it },
                    label = "시작일",
                    placeholder = "YYYY-MM-DD",
                    modifier = Modifier.fillMaxWidth(),
                )
                MoaOutlinedTextField(
                    value = endDateText,
                    onValueChange = { endDateText = it },
                    label = "종료일",
                    placeholder = "YYYY-MM-DD",
                    modifier = Modifier.fillMaxWidth(),
                )
                val message = localError ?: serverError
                if (message != null) {
                    Text(
                        text = message,
                        color = Color(0xFFFF6262),
                        fontFamily = SBAggroFontFamily,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = MoaBlue),
                onClick = {
                    localError = try {
                        val startDate = LocalDate.parse(startDateText)
                        val endDate = LocalDate.parse(endDateText)
                        onCreate(title, description, startDate, endDate)
                        null
                    } catch (e: Exception) {
                        "날짜는 YYYY-MM-DD 형식으로 입력해주세요."
                    }
                }
            ) {
                Text(
                    if (isLoading) "생성 중" else "생성",
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            TextButton(
                enabled = !isLoading,
                onClick = onDismiss
            ) {
                MoaDialogButtonText("취소", TextSecondary)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(18.dp),
    )
}
