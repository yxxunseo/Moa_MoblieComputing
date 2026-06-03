package com.example.moa_project.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moa_project.network.GuestParticipantDto
import com.example.moa_project.util.HeatmapMembersResolver
import com.example.moa_project.ui.theme.MoaBlue
import com.example.moa_project.ui.theme.MoaTextPrimary
import com.example.moa_project.ui.theme.MoaTextSecondary
import com.example.moa_project.ui.theme.SBAggroFontFamily

@Composable
fun ScheduleHeatmapCard(
    heatmap: Map<String, Map<String, Int>>? = null,
    heatmapMembers: Map<String, Map<String, List<String>>>? = null,
    participants: List<GuestParticipantDto>? = null,
    modifier: Modifier = Modifier,
) {
    val effectiveMembers = heatmapMembers?.takeIf { it.isNotEmpty() }
        ?: HeatmapMembersResolver.rebuildFromParticipants(participants)
    val dates = heatmap?.keys?.sorted().orEmpty()
    val allHours = (8..22).map { "%02d:00".format(it) }
    val maxCount = heatmap?.values?.flatMap { it.values }?.maxOrNull()?.coerceAtLeast(1) ?: 1
    val hasData = dates.isNotEmpty() && heatmap?.values?.any { it.isNotEmpty() } == true

    var selectedCell by remember { mutableStateOf<Triple<String, String, Int>?>(null) }
    val selectedMembers = selectedCell?.let { (date, hour, _) ->
        HeatmapMembersResolver.resolveAt(date, hour, effectiveMembers, participants)
    }.orEmpty()
    val selectedLabel = selectedCell?.let { (date, hour, count) ->
        "${date.substring(5).replace("-", "/")} $hour · ${count}명"
    }

    if (selectedCell != null) {
        AlertDialog(
            onDismissRequest = { selectedCell = null },
            title = {
                Text(
                    text = selectedLabel ?: "가능한 멤버",
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MoaTextPrimary,
                )
            },
            text = {
                Text(
                    text = if (selectedMembers.isEmpty()) {
                        "이 시간에 가능한 멤버 정보가 없어요."
                    } else {
                        selectedMembers.joinToString("\n") { "· $it" }
                    },
                    fontFamily = SBAggroFontFamily,
                    fontSize = 14.sp,
                    color = MoaTextSecondary,
                    lineHeight = 22.sp,
                )
            },
            confirmButton = {
                TextButton(onClick = { selectedCell = null }) {
                    Text("닫기", fontFamily = SBAggroFontFamily, color = MoaBlue, fontWeight = FontWeight.Bold)
                }
            },
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = Color(0xFFDDE4F2))
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(20.dp),
    ) {
        Text(
            text = "날짜·시간별 참여 현황",
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MoaTextPrimary,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "숫자를 누르면 가능한 멤버 이름을 볼 수 있어요",
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = MoaTextSecondary,
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (!hasData) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF7F8FC))
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "참여자가 시간을 입력하면\n현황이 표시됩니다.",
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = MoaTextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                )
            }
            return@Column
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.width(46.dp)) {
                Spacer(modifier = Modifier.height(28.dp))
                dates.forEach { date ->
                    Box(modifier = Modifier.height(32.dp), contentAlignment = Alignment.CenterStart) {
                        Text(
                            text = date.substring(5).replace("-", "/"),
                            fontFamily = SBAggroFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MoaTextSecondary,
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
            ) {
                Column {
                    Row {
                        allHours.forEach { hour ->
                            Box(
                                modifier = Modifier.width(30.dp).height(28.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = hour.take(2),
                                    fontFamily = SBAggroFontFamily,
                                    fontSize = 10.sp,
                                    color = MoaTextSecondary,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                    dates.forEach { date ->
                        Row(modifier = Modifier.height(32.dp)) {
                            allHours.forEach { hour ->
                                val count = heatmap?.get(date)?.get(hour) ?: 0
                                val intensity = (count.toFloat() / maxCount).coerceIn(0f, 1f)
                                val isHighIntensity = intensity > 0.55f
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 1.dp, vertical = 3.dp)
                                        .width(28.dp)
                                        .height(26.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(
                                            if (count > 0) MoaBlue.copy(alpha = 0.12f + intensity * 0.78f)
                                            else Color(0xFFF0F2F8),
                                        )
                                        .then(
                                            if (count > 0) {
                                                Modifier.clickable {
                                                    selectedCell = Triple(date, hour, count)
                                                }
                                            } else Modifier,
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (count > 0) {
                                        Text(
                                            text = "$count",
                                            fontFamily = SBAggroFontFamily,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isHighIntensity) Color.White else MoaBlue,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
