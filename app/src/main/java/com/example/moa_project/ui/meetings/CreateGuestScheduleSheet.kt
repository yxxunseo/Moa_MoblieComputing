package com.example.moa_project.ui.meetings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.moa_project.ui.theme.SBAggroFontFamily
import java.time.LocalDate

private val MoaBlue = Color(0xFF2179FE)
private val TextPrimary = Color(0xFF101B33)
private val TextSecondary = Color(0xFF737C99)
private val BorderColor = Color(0xFFE8EBF2)

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
    var startDateText by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    var endDateText by rememberSaveable { mutableStateOf(LocalDate.now().plusDays(5).toString()) }
    var localError by remember { mutableStateOf<String?>(null) }

    fun resetForm() {
        title = ""
        description = ""
        startDateText = LocalDate.now().toString()
        endDateText = LocalDate.now().plusDays(5).toString()
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
                .background(Color(0xFFE0E4F0))
                .align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "단기 일정 링크",
                    color = TextPrimary,
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "앱 설치 없이 참여할 수 있는 링크를 만들어요",
                    color = TextSecondary,
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "닫기", tint = TextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        if (success == null) {
            GuestTextField(title, { title = it }, "일정 제목", "예: 팀플 최종 회의")
            Spacer(modifier = Modifier.height(14.dp))
            GuestTextField(description, { description = it }, "설명", "장소나 안건을 적어주세요", singleLine = false)
            Spacer(modifier = Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GuestTextField(
                    value = startDateText,
                    onValueChange = { startDateText = it },
                    label = "시작일",
                    placeholder = "YYYY-MM-DD",
                    modifier = Modifier.weight(1f)
                )
                GuestTextField(
                    value = endDateText,
                    onValueChange = { endDateText = it },
                    label = "종료일",
                    placeholder = "YYYY-MM-DD",
                    modifier = Modifier.weight(1f)
                )
            }

            val message = localError ?: (state as? GuestScheduleActionState.Error)?.message
            if (message != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = message,
                    color = Color(0xFFFF6262),
                    fontFamily = SBAggroFontFamily,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(22.dp))
            Button(
                enabled = !isLoading,
                onClick = {
                    localError = try {
                        val startDate = LocalDate.parse(startDateText)
                        val endDate = LocalDate.parse(endDateText)
                        viewModel.createGuestSchedule(title, description, startDate, endDate)
                        null
                    } catch (e: Exception) {
                        "날짜는 YYYY-MM-DD 형식으로 입력해주세요."
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MoaBlue)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("링크 만들기", color = Color.White, fontFamily = SBAggroFontFamily, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFEAF1FF))
                    .border(1.dp, MoaBlue.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = MoaBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "링크가 생성되었어요",
                            color = TextPrimary,
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
                            color = TextSecondary,
                            fontFamily = SBAggroFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (linkReachable) {
                            "카카오톡·문자 등으로 링크를 보내면 앱 없이 가능한 시간을 입력할 수 있어요."
                        } else {
                            "⚠️ 지금 링크는 이 기기/같은 Wi-Fi에서만 열려요. 다른 사람에게 보내려면 local.properties의 WEB_SHARE_URL(ngrok 등 공개 URL)을 설정하거나 서버를 배포해주세요."
                        },
                        color = if (linkReachable) TextSecondary else Color(0xFFFF6262),
                        fontFamily = SBAggroFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        copyToClipboard(context, shareLink)
                        Toast.makeText(context, "링크를 복사했어요.", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MoaBlue)
                ) {
                    Text("복사", color = Color.White, fontFamily = SBAggroFontFamily, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { shareText(context, shareLink) },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B556B))
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("공유", color = Color.White, fontFamily = SBAggroFontFamily, fontWeight = FontWeight.Bold)
                }
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
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF35A96D))
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEAF1FF))
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0F2F8))
                ) {
                    Text(
                        "완료",
                        color = TextSecondary,
                        fontFamily = SBAggroFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun GuestTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontFamily = SBAggroFontFamily, fontSize = 12.sp) },
        placeholder = { Text(placeholder, fontFamily = SBAggroFontFamily, fontSize = 12.sp) },
        singleLine = singleLine,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MoaBlue,
            unfocusedBorderColor = BorderColor,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        )
    )
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("Moa schedule link", text))
}

private fun shareText(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "일정 링크 공유"))
}
