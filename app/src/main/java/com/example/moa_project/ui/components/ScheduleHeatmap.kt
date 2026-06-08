package com.example.moa_project.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moa_project.network.GuestParticipantDto
import com.example.moa_project.util.HeatmapMembersResolver
import com.example.moa_project.ui.theme.MoaBlue
import com.example.moa_project.ui.theme.MoaBlueSoft
import com.example.moa_project.ui.theme.MoaDivider
import com.example.moa_project.ui.theme.moaCardSurface
import com.example.moa_project.ui.theme.MoaTextPrimary
import com.example.moa_project.ui.theme.MoaTextSecondary
import com.example.moa_project.ui.theme.SBAggroFontFamily
import java.time.LocalDate

private val HeatEmpty = MoaDivider
private val HeatLevel1 = MoaBlueSoft
private val HeatLevel2 = MoaBlue.copy(alpha = 0.45f)
private val HeatLevel3 = MoaBlue.copy(alpha = 0.72f)
private val HeatLevel4 = MoaBlue

private fun heatColor(count: Int, totalMembers: Int): Color {
    if (count <= 0) return HeatEmpty
    if (totalMembers <= 0) {
        return when {
            count >= 4 -> HeatLevel4
            count == 3 -> HeatLevel3
            count == 2 -> HeatLevel2
            else -> HeatLevel1
        }
    }
    val ratio = count.toFloat() / totalMembers
    return when {
        ratio >= 1f -> HeatLevel4
        ratio >= 0.75f -> HeatLevel3
        ratio >= 0.5f -> HeatLevel2
        ratio > 0f -> HeatLevel1
        else -> HeatEmpty
    }
}

private fun buildDateRange(startDate: String?, endDate: String?): List<String> {
    if (startDate.isNullOrBlank() || endDate.isNullOrBlank()) return emptyList()
    return runCatching {
        val start = LocalDate.parse(startDate)
        val end = LocalDate.parse(endDate)
        buildList {
            var current = start
            while (!current.isAfter(end)) {
                add(current.toString())
                current = current.plusDays(1)
            }
        }
    }.getOrDefault(emptyList())
}

private data class HeatmapCellMetrics(
    val cellWidth: Dp,
    val cellHeight: Dp,
    val rowGap: Dp,
    val timeLabelWidth: Dp,
    val countFontSize: Int,
    val needsHorizontalScroll: Boolean,
)

private fun computeHeatmapCellMetrics(
    availableWidth: Dp,
    dateCount: Int,
    hourCount: Int,
): HeatmapCellMetrics {
    if (dateCount <= 0) {
        return HeatmapCellMetrics(
            cellWidth = 36.dp,
            cellHeight = 28.dp,
            rowGap = 4.dp,
            timeLabelWidth = 36.dp,
            countFontSize = 11,
            needsHorizontalScroll = false,
        )
    }

    val rowGap = 4.dp
    val timeLabelWidth = 36.dp
    val dateGap = 4.dp
    val gridWidth = (availableWidth - timeLabelWidth).coerceAtLeast(0.dp)

    val minCellWidth = 32.dp
    val maxCellWidth = 52.dp
    val minCellHeight = 22.dp
    val maxCellHeight = 34.dp

    val totalDateGaps = dateGap * (dateCount - 1).coerceAtLeast(0)
    val fitWidth = if (dateCount > 0) (gridWidth - totalDateGaps) / dateCount else maxCellWidth
    val cellWidth = fitWidth.coerceIn(minCellWidth, maxCellWidth)
    val needsHorizontalScroll = cellWidth * dateCount + totalDateGaps > gridWidth + 0.5.dp

    val widthRatio = (cellWidth - minCellWidth) / (maxCellWidth - minCellWidth)
    val dateFactor = when {
        dateCount <= 5 -> 1f
        dateCount <= 10 -> 0.92f
        dateCount <= 14 -> 0.85f
        else -> 0.78f
    }
    val hourFactor = when {
        hourCount <= 12 -> 1f
        hourCount <= 18 -> 0.95f
        else -> 0.88f
    }
    val targetHeight = (minCellHeight.value + (maxCellHeight.value - minCellHeight.value) *
        (0.55f * widthRatio + 0.45f * dateFactor) * hourFactor).dp
    val cellHeight = targetHeight.coerceIn(minCellHeight, maxCellHeight)

    val countFontSize = when {
        cellWidth >= 44.dp && cellHeight >= 30.dp -> 12
        cellWidth >= 36.dp -> 11
        else -> 10
    }

    return HeatmapCellMetrics(
        cellWidth = cellWidth,
        cellHeight = cellHeight,
        rowGap = rowGap,
        timeLabelWidth = timeLabelWidth,
        countFontSize = countFontSize,
        needsHorizontalScroll = needsHorizontalScroll,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleHeatmapCard(
    heatmap: Map<String, Map<String, Int>>? = null,
    heatmapMembers: Map<String, Map<String, List<String>>>? = null,
    participants: List<GuestParticipantDto>? = null,
    totalMembers: Int = 0,
    allMemberNames: List<String>? = null,
    startDate: String? = null,
    endDate: String? = null,
    modifier: Modifier = Modifier,
) {
    val effectiveMembers = heatmapMembers?.takeIf { it.isNotEmpty() }
        ?: HeatmapMembersResolver.rebuildFromParticipants(participants)
    val dates = remember(startDate, endDate, heatmap) {
        val rangeDates = buildDateRange(startDate, endDate)
        if (rangeDates.isNotEmpty()) {
            rangeDates
        } else {
            heatmap?.keys?.sorted().orEmpty()
        }
    }
    val allHours = (0..23).map { "%02d:00".format(it) }
    val effectiveTotal = totalMembers.takeIf { it > 0 }
        ?: participants?.size
        ?: allMemberNames?.size
        ?: heatmap?.values?.flatMap { it.values }?.maxOrNull()
        ?: 1
    val hasData = dates.isNotEmpty()

    var selectedCell by remember { mutableStateOf<Triple<String, String, Int>?>(null) }
    val selectedAvailable = selectedCell?.let { (date, hour, _) ->
        HeatmapMembersResolver.resolveAt(date, hour, effectiveMembers, participants)
    }.orEmpty()
    val allNames = allMemberNames
        ?: participants?.map { it.name }
        ?: selectedAvailable
    val selectedUnavailable = allNames.filter { it !in selectedAvailable }
    val selectedLabel = selectedCell?.let { (date, hour, _) ->
        "${date.substring(5).replace("-", "/")} $hour"
    }
    val selectedCount = selectedCell?.third ?: 0

    if (selectedCell != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { selectedCell = null },
            sheetState = sheetState,
            containerColor = Color.White,
        ) {
            HeatmapDetailSheet(
                label = selectedLabel ?: "",
                availableCount = selectedCount,
                totalCount = effectiveTotal,
                availableMembers = selectedAvailable,
                unavailableMembers = selectedUnavailable,
                onDismiss = { selectedCell = null },
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .moaCardSurface(elevation = 2.dp, cornerRadius = 20.dp)
            .padding(20.dp),
    ) {
        Text(
            text = "가능 시간 히트맵",
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MoaTextPrimary,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "겹치는 시간이 많을수록 진한 파란색 · 회색은 아직 가능 인원이 없는 시간",
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = MoaTextSecondary,
        )
        Spacer(modifier = Modifier.height(12.dp))
        HeatLegendRow(totalMembers = effectiveTotal)
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
                    text = "일정 기간이 설정되면\n히트맵이 표시됩니다.",
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

        val dateScroll = rememberScrollState()
        val dateGap = 4.dp

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val metrics = remember(maxWidth, dates.size, allHours.size) {
                computeHeatmapCellMetrics(
                    availableWidth = maxWidth,
                    dateCount = dates.size,
                    hourCount = allHours.size,
                )
            }
            val rowHeight = metrics.cellHeight + metrics.rowGap
            val cornerRadius = (metrics.cellWidth.value * 0.14f).coerceIn(4f, 8f).dp
            val dateRowModifier = if (metrics.needsHorizontalScroll) {
                Modifier.horizontalScroll(dateScroll)
            } else {
                Modifier.fillMaxWidth()
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Spacer(modifier = Modifier.width(metrics.timeLabelWidth))
                    Row(
                        modifier = dateRowModifier,
                        horizontalArrangement = Arrangement.spacedBy(dateGap),
                    ) {
                        dates.forEach { date ->
                            val dateCellModifier = if (metrics.needsHorizontalScroll) {
                                Modifier.width(metrics.cellWidth)
                            } else {
                                Modifier.weight(1f)
                            }
                            Box(
                                modifier = dateCellModifier.height(24.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = date.substring(8),
                                    fontFamily = SBAggroFontFamily,
                                    fontSize = if (metrics.cellWidth >= 40.dp) 11.sp else 10.sp,
                                    color = MoaTextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(metrics.rowGap))
                allHours.forEach { hour ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(rowHeight),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier.width(metrics.timeLabelWidth),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            Text(
                                text = hour.substring(0, 2),
                                fontFamily = SBAggroFontFamily,
                                fontSize = if (metrics.cellHeight >= 28.dp) 11.sp else 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = MoaTextSecondary,
                            )
                        }
                        Row(
                            modifier = dateRowModifier,
                            horizontalArrangement = Arrangement.spacedBy(dateGap),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            dates.forEach { date ->
                                val count = heatmap?.get(date)?.get(hour) ?: 0
                                val cellColor = heatColor(count, effectiveTotal)
                                val intensity = if (effectiveTotal > 0) {
                                    (count.toFloat() / effectiveTotal).coerceIn(0f, 1f)
                                } else {
                                    (count.toFloat() / (heatmap?.values?.flatMap { it.values }?.maxOrNull()?.coerceAtLeast(1) ?: 1))
                                        .coerceIn(0f, 1f)
                                }
                                val showWhiteText = intensity > 0.55f && count > 0
                                val cellModifier = if (metrics.needsHorizontalScroll) {
                                    Modifier.width(metrics.cellWidth)
                                } else {
                                    Modifier.weight(1f)
                                }
                                Box(
                                    modifier = cellModifier
                                        .height(metrics.cellHeight)
                                        .clip(RoundedCornerShape(cornerRadius))
                                        .background(cellColor)
                                        .clickable {
                                            selectedCell = Triple(date, hour, count)
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (count > 0) {
                                        Text(
                                            text = "$count",
                                            fontFamily = SBAggroFontFamily,
                                            fontSize = metrics.countFontSize.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (showWhiteText) Color.White else MoaBlue,
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

@Composable
private fun HeatLegendRow(totalMembers: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "없음",
            fontFamily = SBAggroFontFamily,
            fontSize = 10.sp,
            color = MoaTextSecondary,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf(HeatEmpty, HeatLevel1, HeatLevel2, HeatLevel3, HeatLevel4).forEach { color ->
                Box(
                    modifier = Modifier
                        .size(width = 20.dp, height = 16.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(color),
                )
            }
        }
        Text(
            text = if (totalMembers > 0) "전원(${totalMembers}명)" else "많음",
            fontFamily = SBAggroFontFamily,
            fontSize = 10.sp,
            color = MoaTextSecondary,
        )
    }
}

@Composable
private fun HeatmapDetailSheet(
    label: String,
    availableCount: Int,
    totalCount: Int,
    availableMembers: List<String>,
    unavailableMembers: List<String>,
    onDismiss: () -> Unit,
) {
    val progress = if (totalCount > 0) availableCount.toFloat() / totalCount else 0f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
    ) {
        Text(
            text = label,
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MoaTextPrimary,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "$availableCount / $totalCount 명 가능",
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = MoaBlue,
        )
        Spacer(modifier = Modifier.height(10.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = MoaBlue,
            trackColor = HeatEmpty,
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (availableMembers.isNotEmpty()) {
            Text(
                text = "가능한 멤버",
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MoaBlue,
            )
            Spacer(modifier = Modifier.height(10.dp))
            availableMembers.forEach { name ->
                MemberStatusRow(name = name, available = true)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        if (unavailableMembers.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "불가능한 멤버",
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MoaTextSecondary,
            )
            Spacer(modifier = Modifier.height(10.dp))
            unavailableMembers.forEach { name ->
                MemberStatusRow(name = name, available = false)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        if (availableMembers.isEmpty() && unavailableMembers.isEmpty()) {
            Text(
                text = "이 시간에 가능한 멤버 정보가 없어요.",
                fontFamily = SBAggroFontFamily,
                fontSize = 14.sp,
                color = MoaTextSecondary,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "닫기",
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MoaBlue,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onDismiss)
                .padding(vertical = 8.dp),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun MemberStatusRow(name: String, available: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (available) MoaBlueSoft else Color(0xFFF5F6FA))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProfileAvatar(
            imageUrl = null,
            nickname = name,
            size = 36.dp,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = name,
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            color = MoaTextPrimary,
            modifier = Modifier.weight(1f),
        )
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (available) MoaBlue else Color(0xFFE0E3EB)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (available) Icons.Filled.Check else Icons.Filled.Close,
                contentDescription = if (available) "가능" else "불가",
                tint = Color.White,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}
