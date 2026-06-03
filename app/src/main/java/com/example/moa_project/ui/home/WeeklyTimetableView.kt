package com.example.moa_project.ui.home

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moa_project.ui.theme.MoaBlue
import com.example.moa_project.ui.theme.MoaRadius
import com.example.moa_project.ui.theme.MoaTextPrimary
import com.example.moa_project.ui.theme.MoaTextSecondary
import com.example.moa_project.ui.theme.SBAggroFontFamily
import com.example.moa_project.ui.theme.moaCard
import com.example.moa_project.util.ImageSaveHelper
import com.example.moa_project.util.TimetableCapture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val dayHeaders = listOf("월", "화", "수", "목", "금")
private const val GRID_START_HOUR = 9
private const val GRID_END_HOUR = 22

@Composable
fun WeeklyTimetableDashboardCard(
    data: WeeklyTimetableData?,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var saving by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .moaCard(padding = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "이번 주 시간표",
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MoaTextPrimary,
            )
            if (data != null && data.hasContent) {
                TextButton(
                    onClick = {
                        if (saving) return@TextButton
                        val activity = context as? Activity ?: return@TextButton
                        scope.launch {
                            saving = true
                            try {
                                val bitmap = TimetableCapture.capture(
                                    activity,
                                    data.weekLabel,
                                    data.blocks,
                                ) ?: run {
                                    Toast.makeText(context, "이미지 생성에 실패했어요.", Toast.LENGTH_SHORT).show()
                                    return@launch
                                }
                                val name = "moa_week_${System.currentTimeMillis()}.png"
                                val ok = withContext(Dispatchers.IO) {
                                    ImageSaveHelper.saveToGallery(context, bitmap, name)
                                }
                                bitmap.recycle()
                                Toast.makeText(
                                    context,
                                    if (ok) "갤러리에 저장했어요." else "저장에 실패했어요.",
                                    Toast.LENGTH_SHORT,
                                ).show()
                            } catch (_: Exception) {
                                Toast.makeText(context, "저장 중 오류가 발생했어요.", Toast.LENGTH_SHORT).show()
                            } finally {
                                saving = false
                            }
                        }
                    },
                    enabled = !saving,
                ) {
                    if (saving) {
                        CircularProgressIndicator(
                            color = MoaBlue,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Icon(Icons.Default.SaveAlt, contentDescription = null, tint = MoaBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("저장", fontFamily = SBAggroFontFamily, fontSize = 12.sp, color = MoaBlue)
                    }
                }
            }
        }

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxWidth().height(72.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = MoaBlue, modifier = Modifier.height(24.dp))
                }
            }
            data == null || !data.hasContent -> {
                Text(
                    text = "고정·확정 일정이 없어요",
                    fontFamily = SBAggroFontFamily,
                    fontSize = 13.sp,
                    color = MoaTextSecondary,
                )
            }
            else -> {
                WeeklyTimetableGrid(
                    weekLabel = data.weekLabel,
                    blocks = data.blocks,
                    forExport = false,
                    compact = true,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    LegendDot(color = Color(0xFF6B7FD7), label = "고정")
                    LegendDot(color = MoaBlue, label = "확정")
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, fontFamily = SBAggroFontFamily, fontSize = 11.sp, color = MoaTextSecondary)
    }
}

@Composable
fun WeeklyTimetableGrid(
    weekLabel: String,
    blocks: List<WeeklyTimetableBlock>,
    forExport: Boolean,
    compact: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val hourHeight: Dp = when {
        forExport -> 52.dp
        compact -> 24.dp
        else -> 44.dp
    }
    val dayWidth: Dp = when {
        forExport -> 168.dp
        compact -> 54.dp
        else -> 56.dp
    }
    val timeColWidth: Dp = when {
        forExport -> 44.dp
        compact -> 22.dp
        else -> 28.dp
    }
    val gridHeight = hourHeight * (GRID_END_HOUR - GRID_START_HOUR)
    val bg = if (forExport) Color(0xFF12141A) else Color(0xFFF3F5FA)
    val gridLine = if (forExport) Color(0xFF2A2F3A) else Color(0xFFE2E6EF)
    val headerText = if (forExport) Color(0xFFB8BFCF) else MoaTextSecondary
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .padding(if (forExport) 16.dp else if (compact) 8.dp else 10.dp),
    ) {
        Text(
            text = weekLabel,
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = when {
                forExport -> 18.sp
                compact -> 12.sp
                else -> 13.sp
            },
            color = if (forExport) Color.White else MoaTextPrimary,
        )
        Spacer(modifier = Modifier.height(if (compact) 6.dp else 10.dp))
        Row(modifier = Modifier.horizontalScroll(scrollState)) {
            Column(modifier = Modifier.width(timeColWidth)) {
                Spacer(modifier = Modifier.height(if (compact) 18.dp else 22.dp))
                (GRID_START_HOUR until GRID_END_HOUR).forEach { hour ->
                    Box(
                        modifier = Modifier.height(hourHeight),
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        Text(
                            text = "$hour",
                            fontFamily = SBAggroFontFamily,
                            fontSize = when {
                                forExport -> 12.sp
                                compact -> 8.sp
                                else -> 9.sp
                            },
                            color = headerText,
                        )
                    }
                }
            }
            Column {
                Row {
                    dayHeaders.forEach { label ->
                        Box(
                            modifier = Modifier.width(dayWidth),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = label,
                                fontFamily = SBAggroFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = when {
                                    forExport -> 14.sp
                                    compact -> 10.sp
                                    else -> 11.sp
                                },
                                color = headerText,
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .height(gridHeight)
                        .border(1.dp, gridLine, RoundedCornerShape(8.dp)),
                ) {
                    Row {
                        (1..5).forEach { _ ->
                            Box(
                                modifier = Modifier
                                    .width(dayWidth)
                                    .fillMaxHeight()
                                    .border(0.5.dp, gridLine),
                            )
                        }
                    }
                    blocks.filter { it.dayOfWeek in 1..5 }.forEach { block ->
                        val dayIndex = block.dayOfWeek - 1
                        val top = hourHeight * (block.startHour - GRID_START_HOUR).coerceAtLeast(0)
                        val height = hourHeight * (block.endHour - block.startHour).coerceAtLeast(1)
                        val chipColor = if (block.isFixed) Color(0xFF6B7FD7) else MoaBlue
                        Box(
                            modifier = Modifier
                                .offset(x = dayWidth * dayIndex + 2.dp, y = top + 2.dp)
                                .width(dayWidth - 4.dp)
                                .height(height - 4.dp)
                                .clip(RoundedCornerShape(if (compact) 4.dp else 6.dp))
                                .background(chipColor.copy(alpha = if (forExport) 0.92f else 0.88f))
                                .padding(horizontal = 4.dp, vertical = if (compact) 2.dp else 4.dp),
                        ) {
                            Text(
                                text = block.title,
                                fontFamily = SBAggroFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = when {
                                    forExport -> 13.sp
                                    compact -> 8.sp
                                    else -> 9.sp
                                },
                                color = Color.White,
                                maxLines = if (compact) 1 else 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = when {
                                    forExport -> 16.sp
                                    compact -> 10.sp
                                    else -> 12.sp
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
