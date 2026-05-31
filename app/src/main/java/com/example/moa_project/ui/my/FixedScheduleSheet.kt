package com.example.moa_project.ui.my

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moa_project.network.CreateFixedSlotRequest
import com.example.moa_project.network.FixedTimeSlotDto
import com.example.moa_project.network.RetrofitClient
import com.example.moa_project.ui.components.MoaOutlinedTextField
import com.example.moa_project.ui.components.moaCard
import com.example.moa_project.ui.theme.MoaBlue
import com.example.moa_project.ui.theme.MoaError
import com.example.moa_project.ui.theme.MoaTextPrimary
import com.example.moa_project.ui.theme.MoaTextSecondary
import com.example.moa_project.ui.theme.SBAggroFontFamily
import kotlinx.coroutines.launch

private val dayLabels = listOf("월", "화", "수", "목", "금", "토", "일")

@Composable
fun FixedScheduleSheet(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val slots = remember { mutableStateListOf<FixedTimeSlotDto>() }
    var title by remember { mutableStateOf("수업") }
    var dayOfWeek by remember { mutableStateOf(1) }
    var startHour by remember { mutableStateOf("9") }
    var endHour by remember { mutableStateOf("12") }

    fun reload() {
        scope.launch {
            runCatching {
                slots.clear()
                slots.addAll(RetrofitClient.instance.getFixedTimeSlots())
            }
        }
    }

    LaunchedEffect(Unit) { reload() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Text(
            text = "고정 일정 / 시간표",
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MoaTextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "매주 반복되는 수업·알바 등을 등록하면 조율 시 자동으로 막혀요.",
            fontFamily = SBAggroFontFamily,
            fontSize = 12.sp,
            color = MoaTextSecondary
        )
        Spacer(modifier = Modifier.height(16.dp))

        MoaOutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = "제목",
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            dayLabels.forEachIndexed { index, label ->
                val day = index + 1
                val selected = dayOfWeek == day
                TextButton(onClick = { dayOfWeek = day }) {
                    Text(
                        text = label,
                        color = if (selected) MoaBlue else MoaTextSecondary,
                        fontFamily = SBAggroFontFamily,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MoaOutlinedTextField(
                value = startHour,
                onValueChange = { startHour = it },
                label = "시작(시)",
                modifier = Modifier.weight(1f),
            )
            MoaOutlinedTextField(
                value = endHour,
                onValueChange = { endHour = it },
                label = "종료(시)",
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = {
                scope.launch {
                    runCatching {
                        RetrofitClient.instance.addFixedTimeSlot(
                            CreateFixedSlotRequest(
                                dayOfWeek = dayOfWeek,
                                startHour = startHour.toInt(),
                                endHour = endHour.toInt(),
                                title = title
                            )
                        )
                        reload()
                        Toast.makeText(context, "고정 일정이 추가됐어요.", Toast.LENGTH_SHORT).show()
                    }.onFailure {
                        Toast.makeText(context, "입력값을 확인해주세요.", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MoaBlue),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("추가", color = Color.White, fontFamily = SBAggroFontFamily, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(slots, key = { it.id }) { slot ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .moaCard(cornerRadius = 14.dp)
                        .background(Color.White, RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = slot.title,
                            fontFamily = SBAggroFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MoaTextPrimary
                        )
                        Text(
                            text = "${dayLabels[slot.dayOfWeek - 1]} ${slot.startHour}:00~${slot.endHour}:00",
                            fontFamily = SBAggroFontFamily,
                            fontSize = 12.sp,
                            color = MoaTextSecondary
                        )
                    }
                    TextButton(onClick = {
                        scope.launch {
                            runCatching {
                                RetrofitClient.instance.deleteFixedTimeSlot(slot.id)
                                reload()
                            }
                        }
                    }) {
                        Text("삭제", color = MoaError, fontFamily = SBAggroFontFamily, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
            Text("닫기", fontFamily = SBAggroFontFamily, color = MoaTextSecondary)
        }
    }
}
