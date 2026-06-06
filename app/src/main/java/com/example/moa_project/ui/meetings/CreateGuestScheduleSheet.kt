package com.example.moa_project.ui.meetings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moa_project.util.GuestLinkHelper
import com.example.moa_project.ui.components.Moa3DIcon
import com.example.moa_project.ui.components.Moa3DIconType
import com.example.moa_project.ui.components.MoaDateRangePicker
import com.example.moa_project.ui.components.MoaOutlinedTextField
import com.example.moa_project.ui.components.MoaPrimaryButton
import com.example.moa_project.ui.theme.MoaAccentBlueBg
import com.example.moa_project.ui.theme.MoaAccentGreen
import com.example.moa_project.ui.theme.MoaBlue
import com.example.moa_project.ui.theme.MoaError
import com.example.moa_project.ui.theme.MoaRadius
import com.example.moa_project.ui.theme.MoaScreenBackground
import com.example.moa_project.ui.theme.MoaInputBorder
import com.example.moa_project.ui.theme.MoaTextPrimary
import com.example.moa_project.ui.theme.MoaTextSecondary
import com.example.moa_project.ui.theme.SBAggroFontFamily
import java.time.LocalDate

@Composable
fun CreateGuestScheduleSheet(
    onDismiss: () -> Unit,
    onViewResult: (String) -> Unit = {},
    viewModel: GuestScheduleActionViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var startDate by rememberSaveable {
        mutableStateOf(LocalDate.now().toString())
    }
    var endDate by rememberSaveable {
        mutableStateOf(LocalDate.now().plusDays(5).toString())
    }
    val startLocalDate = remember(startDate) { LocalDate.parse(startDate) }
    val endLocalDate = remember(endDate) { LocalDate.parse(endDate) }
    var localError by remember { mutableStateOf<String?>(null) }

    fun resetForm() {
        title = ""
        description = ""
        startDate = LocalDate.now().toString()
        endDate = LocalDate.now().plusDays(5).toString()
        localError = null
        viewModel.reset()
    }
    val success = state as? GuestScheduleActionState.Success
    val isLoading = state is GuestScheduleActionState.Loading
    val uniqueLink = success?.schedule?.uniqueLink
    val appLink = uniqueLink?.let { "moa://schedule/$it" }
    val webLink = uniqueLink?.let { link ->
        GuestLinkHelper.resolveWebLink(link, success.schedule.webLink)
    }
    val shareLink = webLink ?: appLink.orEmpty()
    val linkReachable = GuestLinkHelper.isExternalReachable(shareLink)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 28.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .size(width = 40.dp, height = 4.dp)
                .clip(CircleShape)
                .background(MoaInputBorder)
                .align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Moa3DIcon(type = Moa3DIconType.Link, size = 40.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "단기 일정 링크",
                        color = MoaTextPrimary,
                        fontFamily = SBAggroFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "앱 설치 없이 참여할 수 있는 링크를 만들어요",
                        color = MoaTextSecondary,
                        fontFamily = SBAggroFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                    )
                }
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "닫기", tint = MoaTextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        if (success == null) {
            MoaOutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = "일정 제목 (최대 30자)",
                placeholder = "예: 팀플 최종 회의",
                maxLength = 30,
            )
            Spacer(modifier = Modifier.height(14.dp))
            MoaOutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = "설명 (선택, 최대 80자)",
                placeholder = "장소나 안건을 적어주세요",
                maxLength = 80,
                singleLine = false,
            )
            Spacer(modifier = Modifier.height(14.dp))
            MoaDateRangePicker(
                startDate = startLocalDate,
                endDate = endLocalDate,
                onStartDateChange = { startDate = it.toString() },
                onEndDateChange = { endDate = it.toString() },
            )

            val message = localError ?: (state as? GuestScheduleActionState.Error)?.message
            if (message != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = message,
                    color = MoaError,
                    fontFamily = SBAggroFontFamily,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(22.dp))
            MoaPrimaryButton(
                text = "링크 만들기",
                onClick = {
                    localError = if (endLocalDate.isBefore(startLocalDate)) {
                        "종료일은 시작일 이후여야 합니다."
                    } else {
                        viewModel.createGuestSchedule(title, description, startLocalDate, endLocalDate)
                        null
                    }
                },
                enabled = !isLoading,
                loading = isLoading,
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MoaAccentBlueBg)
                    .border(1.dp, MoaBlue.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = MoaBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "링크가 생성되었어요",
                            color = MoaTextPrimary,
                            fontFamily = SBAggroFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = shareLink,
                        color = MoaBlue,
                        fontFamily = SBAggroFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                    if (appLink != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "앱 딥링크: $appLink",
                            color = MoaTextSecondary,
                            fontFamily = SBAggroFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    }
                                        Spacer(modifier = Modifier.height(8.dp))
                    if (linkReachable) {
                        Text(
                            text = "카카오톡·문자 등으로 링크를 보내면 앱 없이 가능한 시간을 입력할 수 있어요.",
                            color = MoaTextSecondary,
                            fontFamily = SBAggroFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                        )
                    } else {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MoaError,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "지금 링크는 이 기기/같은 Wi-Fi에서만 열려요. 다른 사람에게 보내려면 local.properties의 WEB_SHARE_URL(ngrok 등 공개 URL)을 설정하거나 서버를 배포해주세요.",
                                color = MoaError,
                                fontFamily = SBAggroFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = {
                    webLink?.let { link ->
                        com.example.moa_project.util.GuestLinkShareHelper.share(
                            context = context,
                            scheduleTitle = success.schedule.title,
                            scheduleDescription = success.schedule.description,
                            startDate = success.schedule.startDate,
                            endDate = success.schedule.endDate,
                            uniqueLink = success.schedule.uniqueLink,
                            webLink = link,
                        )
                    }
                },
                enabled = linkReachable,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MoaBlue),
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    "공유하기",
                    color = Color.White,
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {
                    success.schedule.uniqueLink.let(onViewResult)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MoaAccentGreen)
            ) {
                Text("조율 결과 보기", color = Color.White, fontFamily = SBAggroFontFamily, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { resetForm() },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MoaAccentBlueBg)
                ) {
                    Text(
                        "새 일정 만들기",
                        color = MoaBlue,
                        fontFamily = SBAggroFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                Button(
                    onClick = {
                        resetForm()
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MoaScreenBackground)
                ) {
                    Text(
                        "완료",
                        color = MoaTextSecondary,
                        fontFamily = SBAggroFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

