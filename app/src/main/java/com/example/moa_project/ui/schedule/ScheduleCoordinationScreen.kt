package com.example.moa_project.ui.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moa_project.ui.components.MoaOutlinedTextField
import com.example.moa_project.ui.theme.MoaBlue
import com.example.moa_project.ui.theme.MoaButtonSpec
import com.example.moa_project.ui.theme.MoaDivider
import com.example.moa_project.ui.theme.MoaRadius
import com.example.moa_project.ui.theme.MoaScreenBackground
import com.example.moa_project.ui.theme.MoaTextPrimary
import com.example.moa_project.ui.theme.MoaTextSecondary
import com.example.moa_project.ui.theme.Moa_ProjectTheme
import com.example.moa_project.ui.theme.SBAggroFontFamily
import com.example.moa_project.ui.theme.moaCardSurface
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val BusyColor = Color(0xFFE8ECF4)
private val UnselectedColor = Color(0xFFF0F2F8)
private const val GRID_COLUMNS = 3

data class TimeSlot(val date: LocalDate, val hour: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleCoordinationScreen(
    scheduleTitle: String = "백엔드 세미나",
    startDate: LocalDate = LocalDate.of(2026, 5, 20),
    endDate: LocalDate = LocalDate.of(2026, 5, 25),
    isGuest: Boolean = true,
    busySlotLabels: Map<TimeSlot, String> = emptyMap(),
    initialSelectedSlots: List<TimeSlot> = emptyList(),
    initialGuestName: String = "",
    coordinationKey: String = scheduleTitle,
    onBackClick: () -> Unit = {},
    onSubmitClick: (String, List<TimeSlot>) -> Unit = { _, _ -> }
) {
    val dates = remember(startDate, endDate) {
        buildList {
            var current = startDate
            while (!current.isAfter(endDate)) {
                add(current)
                current = current.plusDays(1)
            }
        }
    }

    var selectedDate by remember(startDate, endDate, coordinationKey) {
        mutableStateOf(dates.firstOrNull() ?: startDate)
    }
    val selectedTimeSlots = remember(coordinationKey) { mutableStateListOf<TimeSlot>() }
    var guestName by remember(coordinationKey) { mutableStateOf(initialGuestName) }

    LaunchedEffect(coordinationKey, initialSelectedSlots) {
        selectedTimeSlots.clear()
        selectedTimeSlots.addAll(initialSelectedSlots)
    }

    LaunchedEffect(coordinationKey, initialGuestName) {
        if (initialGuestName.isNotBlank()) {
            guestName = initialGuestName
        }
    }

    val isEditingExisting = initialSelectedSlots.isNotEmpty()
    val dateIndex = dates.indexOf(selectedDate).coerceAtLeast(0)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "일정 조율",
                        fontFamily = SBAggroFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MoaTextPrimary
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
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White),
                modifier = Modifier.shadow(elevation = 2.dp, spotColor = Color(0x1A000000))
            )
        },
        bottomBar = {
            BottomSubmitBar(
                selectedCount = selectedTimeSlots.size,
                submitEnabled = selectedTimeSlots.isNotEmpty() && (!isGuest || guestName.isNotBlank()),
                submitLabel = if (isEditingExisting) "수정 완료" else "입력 완료",
                onSubmit = { onSubmitClick(guestName, selectedTimeSlots) }
            )
        },
        containerColor = MoaScreenBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = scheduleTitle,
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = MoaTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                if (isEditingExisting) {
                    Text(
                        text = "이전에 선택한 시간을 바꿀 수 있어요. 다시 탭하거나 드래그해서 수정해주세요.",
                        fontFamily = SBAggroFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = MoaBlue,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
                Text(
                    text = if (busySlotLabels.isNotEmpty()) {
                        "탭하거나 드래그해서 가능한 시간을 선택해주세요 (회색은 기존 일정, 선택 가능)"
                    } else {
                        "탭하거나 드래그해서 가능한 시간을 선택해주세요"
                    },
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = MoaTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isGuest) {
                CoordinationCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                    MoaOutlinedTextField(
                        value = guestName,
                        onValueChange = { guestName = it },
                        label = "이름",
                        placeholder = "예: 홍길동",
                        maxLength = 20,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            CoordinationCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                DateNavRow(
                    dateLabel = selectedDate.format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd (E)", Locale.KOREAN)
                    ),
                    canGoPrev = dateIndex > 0,
                    canGoNext = dateIndex < dates.lastIndex,
                    onPrev = { if (dateIndex > 0) selectedDate = dates[dateIndex - 1] },
                    onNext = { if (dateIndex < dates.lastIndex) selectedDate = dates[dateIndex + 1] }
                )
                Spacer(modifier = Modifier.height(12.dp))
                HourPickerGrid(
                    selectedDate = selectedDate,
                    selectedTimeSlots = selectedTimeSlots,
                    busySlotLabels = busySlotLabels,
                    onSlotToggled = { slot, isSelected ->
                        if (isSelected && !selectedTimeSlots.contains(slot)) {
                            selectedTimeSlots.add(slot)
                        } else if (!isSelected) {
                            selectedTimeSlots.remove(slot)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun CoordinationCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .moaCardSurface()
            .padding(18.dp)
    ) {
        content()
    }
}

@Composable
private fun DateNavRow(
    dateLabel: String,
    canGoPrev: Boolean,
    canGoNext: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onPrev,
            enabled = canGoPrev,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(MoaScreenBackground)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "이전 날",
                tint = if (canGoPrev) MoaTextPrimary else MoaTextSecondary.copy(alpha = 0.4f)
            )
        }
        Text(
            text = dateLabel,
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MoaTextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = onNext,
            enabled = canGoNext,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(MoaScreenBackground)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "다음 날",
                tint = if (canGoNext) MoaTextPrimary else MoaTextSecondary.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
private fun HourPickerGrid(
    selectedDate: LocalDate,
    selectedTimeSlots: List<TimeSlot>,
    busySlotLabels: Map<TimeSlot, String>,
    onSlotToggled: (TimeSlot, Boolean) -> Unit
) {
    val hours = (0..23).toList()
    val touchSlop = LocalViewConfiguration.current.touchSlop
    val currentSlots = rememberUpdatedState(selectedTimeSlots)
    val chipBounds = remember(selectedDate) { mutableMapOf<Int, Rect>() }
    var gridCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var dragPreviewHours by remember(selectedDate) { mutableStateOf<IntRange?>(null) }
    var dragSelecting by remember(selectedDate) { mutableStateOf<Boolean?>(null) }

    fun hourAtRoot(rootOffset: Offset): Int? =
        chipBounds.entries.firstOrNull { (_, rect) -> rect.contains(rootOffset) }?.key

    fun applyRange(range: IntRange, select: Boolean) {
        range.forEach { hour ->
            onSlotToggled(TimeSlot(selectedDate, hour), select)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { gridCoords = it }
            .pointerInput(selectedDate, touchSlop) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val rootPos = gridCoords?.localToRoot(down.position) ?: return@awaitEachGesture
                    val anchorHour = hourAtRoot(rootPos) ?: return@awaitEachGesture
                    val anchorSlot = TimeSlot(selectedDate, anchorHour)

                    val select = !currentSlots.value.contains(anchorSlot)
                    var dragged = false
                    dragSelecting = select
                    dragPreviewHours = anchorHour..anchorHour

                    do {
                        val event = awaitPointerEvent()
                        val change = event.changes.first()
                        if (change.positionChange().getDistance() > touchSlop) {
                            dragged = true
                        }
                        val currentRoot = gridCoords?.localToRoot(change.position) ?: rootPos
                        val currentHour = hourAtRoot(currentRoot) ?: anchorHour
                        dragPreviewHours = minOf(anchorHour, currentHour)..maxOf(anchorHour, currentHour)
                        if (change.pressed) change.consume()
                    } while (event.changes.any { it.pressed })

                    if (dragged) {
                        dragPreviewHours?.let { applyRange(it, select) }
                    } else {
                        onSlotToggled(anchorSlot, select)
                    }
                    dragPreviewHours = null
                    dragSelecting = null
                }
            },
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        hours.chunked(GRID_COLUMNS).forEach { rowHours ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowHours.forEach { hour ->
                    HourChip(
                        hour = hour,
                        selectedDate = selectedDate,
                        selectedTimeSlots = selectedTimeSlots,
                        busyLabel = busySlotLabels[TimeSlot(selectedDate, hour)],
                        dragPreviewHours = dragPreviewHours,
                        dragSelecting = dragSelecting,
                        onBoundsChanged = { rect -> chipBounds[hour] = rect },
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(GRID_COLUMNS - rowHours.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun HourChip(
    hour: Int,
    selectedDate: LocalDate,
    selectedTimeSlots: List<TimeSlot>,
    busyLabel: String?,
    dragPreviewHours: IntRange?,
    dragSelecting: Boolean?,
    onBoundsChanged: (Rect) -> Unit,
    modifier: Modifier = Modifier
) {
    val slot = TimeSlot(selectedDate, hour)
    val isSelected = selectedTimeSlots.contains(slot)
    val isBusy = busyLabel != null
    val inDragPreview = dragPreviewHours?.contains(hour) == true
    val previewSelected = inDragPreview && dragSelecting == true
    val previewDeselected = inDragPreview && dragSelecting == false

    Box(
        modifier = modifier
            .height(if (isBusy) 52.dp else 42.dp)
            .onGloballyPositioned { coords ->
                val pos = coords.positionInRoot()
                val size = coords.size
                onBoundsChanged(
                    Rect(
                        left = pos.x,
                        top = pos.y,
                        right = pos.x + size.width,
                        bottom = pos.y + size.height,
                    )
                )
            }
            .clip(RoundedCornerShape(10.dp))
            .background(
                when {
                    previewSelected -> MoaBlue.copy(alpha = 0.65f)
                    previewDeselected -> Color(0xFFE8F0FF)
                    isSelected -> MoaBlue
                    isBusy -> BusyColor
                    else -> UnselectedColor
                }
            )
            .border(
                width = 1.dp,
                color = when {
                    isSelected -> MoaBlue
                    isBusy -> Color(0xFFD5DCEA)
                    else -> Color.Transparent
                },
                shape = RoundedCornerShape(10.dp)
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (isBusy && !isSelected) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 4.dp),
            ) {
                Text(
                    text = String.format("%02d:00", hour),
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MoaTextSecondary,
                )
                Text(
                    text = busyLabel,
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp,
                    color = MoaTextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    lineHeight = 12.sp,
                )
            }
        } else {
            Text(
                text = String.format("%02d:00", hour),
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = if (isSelected) Color.White else MoaTextPrimary,
            )
        }
    }
}

@Composable
private fun BottomSubmitBar(
    selectedCount: Int,
    submitEnabled: Boolean = selectedCount > 0,
    submitLabel: String = "입력 완료",
    onSubmit: () -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 16.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "선택된 시간",
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = MoaTextSecondary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${selectedCount}개",
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MoaBlue
                )
            }

            Button(
                onClick = onSubmit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MoaBlue,
                    disabledContainerColor = Color(0xFFDCE5F5)
                ),
                enabled = submitEnabled,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .width(160.dp)
                    .height(52.dp)
            ) {
                Text(
                    text = submitLabel,
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
fun ScheduleCoordinationScreenPreview() {
    Moa_ProjectTheme {
        ScheduleCoordinationScreen()
    }
}
