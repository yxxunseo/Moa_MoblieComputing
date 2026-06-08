package com.example.moa_project.ui.meetings

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.layout.ContentScale
import coil.compose.SubcomposeAsyncImage
import com.example.moa_project.util.ImageUrlHelper
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import android.content.ClipboardManager
import android.content.ClipData
import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import com.example.moa_project.ui.components.MoaDateRangePicker
import com.example.moa_project.ui.components.MoaMascot
import com.example.moa_project.ui.components.MoaMascotVariant
import com.example.moa_project.ui.components.MoaOutlinedTextField
import com.example.moa_project.ui.components.MoaShareBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moa_project.R
import com.example.moa_project.util.GroupInviteLinkHelper
import com.example.moa_project.util.GroupInviteShareHelper
import com.example.moa_project.util.KakaoShareHelper
import com.example.moa_project.util.launchImagePicker
import com.example.moa_project.util.rememberImagePickerLauncher
import com.example.moa_project.network.GroupResponse
import com.example.moa_project.network.TokenManager
import com.example.moa_project.network.GroupMemberResponse
import com.example.moa_project.network.ScheduleDetailResponse
import com.example.moa_project.ui.theme.MoaAccentOrange
import com.example.moa_project.ui.theme.MoaBlue
import com.example.moa_project.ui.theme.MoaBlueSoft
import com.example.moa_project.ui.theme.MoaError
import com.example.moa_project.ui.theme.MoaRadius
import com.example.moa_project.ui.theme.MoaScreenBackground
import com.example.moa_project.ui.theme.MoaStatusConfirmed
import com.example.moa_project.ui.theme.MoaTextPrimary
import com.example.moa_project.ui.theme.MoaTextSecondary
import com.example.moa_project.ui.theme.SBAggroFontFamily
import com.example.moa_project.ui.theme.moaCardSurface
import java.time.LocalDate

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
    var showMenu by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }
    val isCreateScheduleLoading = createState is CreateScheduleState.Loading

    fun dismissCreateScheduleDialog() {
        showCreateScheduleDialog = false
        createScheduleViewModel.resetState()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, groupId) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadGroupDetail()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(createState) {
        val success = createState as? CreateScheduleState.Success
        if (success != null) {
            showCreateScheduleDialog = false
            createScheduleViewModel.resetState()
            onCoordinateScheduleCreated(success.schedule.id)
        }
    }

    // 현재 로그인 유저가 관리자인지 확인 (state에서 읽기)
    val isCurrentUserAdmin = (state as? GroupDetailState.Success)?.let { s ->
        s.members.any { m ->
            m.userId == com.example.moa_project.network.TokenManager.getUserId() && m.role == "ADMIN"
        }
    } ?: false

    Box(modifier = Modifier.fillMaxSize()) {
    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = {
                Text("모임 나가기", fontFamily = SBAggroFontFamily, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    if (isCurrentUserAdmin)
                        "관리자가 나가면 모임이 완전히 삭제됩니다.\n멤버, 일정, 모든 데이터가 사라집니다.\n정말 나가시겠어요?"
                    else
                        "이 모임에서 나가시겠어요? 다시 입장하려면 초대 코드가 필요합니다.",
                    fontFamily = SBAggroFontFamily,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showLeaveDialog = false
                    viewModel.leaveGroup(
                        onSuccess = { groupDeleted ->
                            val msg = if (groupDeleted) "모임이 삭제되었습니다." else "모임에서 나갔습니다."
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            onBackClick()
                        },
                        onError = { msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() }
                    )
                }) {
                    Text("나가기", color = MoaError, fontFamily = SBAggroFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveDialog = false }) {
                    Text("취소", fontFamily = SBAggroFontFamily)
                }
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
                        color = MoaTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = MoaTextPrimary
                        )
                    }
                },
                actions = {
                    if (state is GroupDetailState.Success) {
                        IconButton(onClick = { showShareSheet = true }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "공유하기",
                                tint = MoaBlue,
                            )
                        }
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "더보기",
                                tint = MoaTextSecondary,
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "모임 나가기",
                                        color = MoaError,
                                        fontFamily = SBAggroFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp,
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    showLeaveDialog = true
                                },
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                modifier = Modifier.shadow(elevation = 2.dp)
            )
        },
        containerColor = MoaScreenBackground
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
                            color = MoaTextSecondary,
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
                val inviterName = TokenManager.getNickname()
                MoaShareBottomSheet(
                    visible = showShareSheet,
                    onDismiss = { showShareSheet = false },
                    kakaoEnabled = GroupInviteLinkHelper.isExternalShareReady(),
                    onKakaoClick = {
                        KakaoShareHelper.shareGroupInvite(
                            context = context,
                            groupName = s.group.name,
                            groupDescription = s.group.description,
                            inviteCode = s.group.inviteCode,
                            inviterName = inviterName,
                        )
                    },
                    onCopyLinkClick = {
                        GroupInviteShareHelper.copyInviteLink(
                            context = context,
                            inviteCode = s.group.inviteCode,
                            inviterName = inviterName,
                        )
                    },
                    onMoreClick = {
                        GroupInviteShareHelper.openShareChooser(
                            context = context,
                            groupName = s.group.name,
                            groupDescription = s.group.description,
                            inviteCode = s.group.inviteCode,
                            inviterName = inviterName,
                        )
                    },
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 20.dp, vertical = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        GroupInfoCard(
                            group = s.group,
                            isAdmin = isCurrentUserAdmin,
                            onUploadCover = { uri ->
                                viewModel.uploadCoverImage(
                                    context = context,
                                    uri = uri,
                                    onSuccess = {
                                        Toast.makeText(context, "모임 사진이 등록되었습니다.", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    },
                                )
                            },
                        )
                    }

                    if (s.members.isNotEmpty()) {
                        item {
                            GroupMembersCard(members = s.members)
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "일정 목록",
                                color = MoaTextPrimary,
                                fontFamily = SBAggroFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${s.schedules.size}개",
                                color = MoaTextSecondary,
                                fontFamily = SBAggroFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(MoaBlue.copy(alpha = 0.1f))
                                    .clickable { showCreateScheduleDialog = true }
                                    .padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = MoaBlue,
                                    modifier = Modifier.size(15.dp),
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "새 조율",
                                    color = MoaBlue,
                                    fontFamily = SBAggroFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                )
                            }
                        }
                    }

                    // 일정 없을 때
                    if (s.schedules.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .moaCardSurface(cornerRadius = MoaRadius.card)
                                    .padding(vertical = 28.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(MoaBlue.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DateRange,
                                            contentDescription = null,
                                            tint = MoaBlue,
                                            modifier = Modifier.size(24.dp),
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "아직 일정이 없어요",
                                        color = MoaTextPrimary,
                                        fontFamily = SBAggroFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "일정 목록 옆 + 새 조율로 만들어보세요",
                                        color = MoaTextSecondary,
                                        fontFamily = SBAggroFontFamily,
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
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
                                val openResult = schedule.status == "CONFIRMED" ||
                                    schedule.status == "DONE" ||
                                    schedule.respondedCount > 0
                                if (openResult) {
                                    onScheduleClick(schedule.id)
                                } else {
                                    onCoordinateClick(schedule.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showCreateScheduleDialog) {
        val dialogMaxHeight = LocalConfiguration.current.screenHeightDp.dp * 0.88f
        Dialog(
            onDismissRequest = {
                if (!isCreateScheduleLoading) dismissCreateScheduleDialog()
            },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = !isCreateScheduleLoading,
                dismissOnClickOutside = !isCreateScheduleLoading,
            ),
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .heightIn(max = dialogMaxHeight),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                tonalElevation = 0.dp,
            ) {
                CreateScheduleSheet(
                    state = createState,
                    onDismiss = { if (!isCreateScheduleLoading) dismissCreateScheduleDialog() },
                    onCreate = { title, description, startDate, endDate, isWeeklyRecurring ->
                        createScheduleViewModel.createSchedule(title, description, startDate, endDate, isWeeklyRecurring)
                    },
                )
            }
        }
    }
    }
}

@Composable
private fun GroupCoverPlaceholder(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.ic_character),
        contentDescription = "기본 모임 이미지",
        modifier = modifier.padding(10.dp),
        contentScale = ContentScale.Fit,
    )
}

@Composable
private fun GroupInfoCard(
    group: GroupResponse,
    isAdmin: Boolean = false,
    onUploadCover: (Uri) -> Unit = {},
) {
    val context = LocalContext.current
    val coverPicker = rememberImagePickerLauncher(onUploadCover)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .moaCardSurface()
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.size(64.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MoaBlueSoft)
                        .then(
                            if (isAdmin) Modifier.clickable { launchImagePicker(coverPicker) }
                            else Modifier,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (!group.coverImageUrl.isNullOrBlank()) {
                        SubcomposeAsyncImage(
                            model = ImageUrlHelper.resolve(group.coverImageUrl),
                            contentDescription = "모임 사진",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            loading = { GroupCoverPlaceholder(Modifier.fillMaxSize()) },
                            error = { GroupCoverPlaceholder(Modifier.fillMaxSize()) },
                        )
                    } else {
                        GroupCoverPlaceholder(Modifier.fillMaxSize())
                    }
                }
                if (isAdmin) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(MoaBlue)
                            .clickable { launchImagePicker(coverPicker) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "사진 등록",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = group.name,
                    color = MoaTextPrimary,
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!group.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = group.description,
                        color = MoaTextSecondary,
                        fontFamily = SBAggroFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MoaBlue,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${group.memberCount}명 참여 중",
                        color = MoaBlue,
                        fontFamily = SBAggroFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Moa Invite Code", group.inviteCode)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "초대 코드가 복사되었습니다!", Toast.LENGTH_SHORT).show()
                }
                .background(Color(0xFFF5F7FC))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "초대 코드",
                color = MoaTextSecondary,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = group.inviteCode,
                color = MoaTextPrimary,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "복사",
                tint = MoaBlue,
                modifier = Modifier.size(14.dp),
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "친구를 이 모임으로 초대해보세요!",
                    color = MoaTextPrimary,
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "링크를 공유하면 MOA 앱에서 코드로 바로 입장할 수 있어요.",
                    color = MoaTextSecondary,
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                )
            }
            MoaMascot(size = 56.dp, variant = MoaMascotVariant.Sparkle)
        }

    }
}

@Composable
private fun GroupMembersCard(members: List<GroupMemberResponse>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .moaCardSurface()
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(
            text = "멤버 ${members.size}명",
            color = MoaTextPrimary,
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
        )
        Spacer(modifier = Modifier.height(8.dp))
        members.forEach { member ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                com.example.moa_project.ui.components.ProfileAvatar(
                    imageUrl = member.profileImageUrl,
                    nickname = member.nickname,
                    size = 36.dp,
                    useLocalCache = false,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = member.nickname,
                        color = MoaTextPrimary,
                        fontFamily = SBAggroFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = if (member.role == "ADMIN") "관리자" else "멤버",
                        color = MoaTextSecondary,
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
        "CONFIRMED" -> MoaStatusConfirmed
        "WAITING" -> MoaBlue
        "ADJUSTING" -> MoaAccentOrange
        "DONE" -> MoaTextSecondary
        else -> MoaTextSecondary
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
            .moaCardSurface()
            .clickable(onClick = onClick)
            .padding(14.dp),
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
                color = MoaTextPrimary,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${schedule.startDate} ~ ${schedule.endDate}",
                color = MoaTextSecondary,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
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
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "상세 보기",
            tint = MoaTextSecondary,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun CreateScheduleSheet(
    state: CreateScheduleState,
    onDismiss: () -> Unit,
    onCreate: (String, String, LocalDate, LocalDate, Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf(LocalDate.now()) }
    var endDate by remember { mutableStateOf(LocalDate.now().plusDays(5)) }
    var isWeeklyRecurring by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }
    val isLoading = state is CreateScheduleState.Loading
    val serverError = (state as? CreateScheduleState.Error)?.message

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 20.dp, bottom = 32.dp),
    ) {
        Text(
            text = "새 일정 조율",
            color = MoaTextPrimary,
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
        )

        Spacer(modifier = Modifier.height(24.dp))

        MoaOutlinedTextField(
            value = title,
            onValueChange = { if (it.length <= 30) title = it },
            label = "일정 제목 (최대 30자)",
            placeholder = "예: OS Seminar OT",
            maxLength = 30,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(16.dp))
        MoaOutlinedTextField(
            value = description,
            onValueChange = { if (it.length <= 80) description = it },
            label = "설명 (선택, 최대 80자)",
            placeholder = "장소, 안건 등을 적어주세요",
            maxLength = 80,
            singleLine = false,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(20.dp))
        MoaDateRangePicker(
            startDate = startDate,
            endDate = endDate,
            onStartDateChange = { startDate = it },
            onEndDateChange = { endDate = it },
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(if (isWeeklyRecurring) Color(0xFFEEF5FF) else Color(0xFFF5F6FA))
                .clickable { isWeeklyRecurring = !isWeeklyRecurring }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "매주 반복 모임",
                    color = MoaTextPrimary,
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "매주 일요일까지 모든 팀원이 일정을 등록해요",
                    color = MoaTextSecondary,
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                )
            }
            androidx.compose.material3.Switch(
                checked = isWeeklyRecurring,
                onCheckedChange = { isWeeklyRecurring = it },
            )
        }

        val message = localError ?: serverError
        if (message != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                color = MoaError,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = { if (!isLoading) onDismiss() },
                enabled = !isLoading,
            ) {
                Text(
                    text = "취소",
                    color = MoaTextSecondary,
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    localError = if (title.isBlank()) {
                        "일정 제목을 입력해주세요."
                    } else if (endDate.isBefore(startDate)) {
                        "종료일은 시작일 이후여야 합니다."
                    } else {
                        onCreate(title, description, startDate, endDate, isWeeklyRecurring)
                        null
                    }
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MoaBlue),
                modifier = Modifier.height(44.dp),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text = "생성",
                        color = Color.White,
                        fontFamily = SBAggroFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                    )
                }
            }
        }
    }
}
