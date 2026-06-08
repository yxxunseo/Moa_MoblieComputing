package com.example.moa_project.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moa_project.ui.theme.MoaAccentBlueBg
import com.example.moa_project.ui.theme.MoaBlue
import com.example.moa_project.ui.theme.MoaBlueSoft
import com.example.moa_project.ui.theme.MoaDivider
import com.example.moa_project.ui.theme.MoaRadius
import com.example.moa_project.ui.theme.MoaTextPrimary
import com.example.moa_project.ui.theme.MoaTextSecondary
import com.example.moa_project.ui.theme.MoaTextTertiary
import com.example.moa_project.ui.theme.SBAggroFontFamily
import com.example.moa_project.ui.theme.moaCardSurface
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlinx.coroutines.launch

private enum class DateRangeField {
    START,
    END,
}

@Composable
fun MoaDateRangePicker(
    startDate: LocalDate,
    endDate: LocalDate,
    onStartDateChange: (LocalDate) -> Unit,
    onEndDateChange: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    var activeField by remember { mutableStateOf(DateRangeField.START) }
    val today = remember { LocalDate.now() }
    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("M월 d일 (E)", Locale.KOREAN)
    }
    val daysOfWeek = remember { daysOfWeek(firstDayOfWeek = DayOfWeek.SUNDAY) }
    val anchorMonth = remember(startDate, endDate, activeField) {
        when (activeField) {
            DateRangeField.START -> YearMonth.from(startDate)
            DateRangeField.END -> YearMonth.from(endDate)
        }
    }
    val calendarState = rememberCalendarState(
        startMonth = YearMonth.now().minusMonths(12),
        endMonth = YearMonth.now().plusMonths(24),
        firstVisibleMonth = anchorMonth,
        firstDayOfWeek = daysOfWeek.first(),
    )
    val visibleMonth = calendarState.firstVisibleMonth.yearMonth
    val scope = rememberCoroutineScope()
    val dayCount = remember(startDate, endDate) {
        ChronoUnit.DAYS.between(startDate, endDate).toInt() + 1
    }

    LaunchedEffect(anchorMonth) {
        calendarState.animateScrollToMonth(anchorMonth)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "조율 기간",
                color = MoaTextPrimary,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(MoaRadius.chip))
                    .background(MoaAccentBlueBg)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    text = "${dayCount}일",
                    color = MoaBlue,
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(MoaRadius.button))
                .background(Color(0xFFF0F2F7))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            DateRangeChip(
                label = "시작일",
                dateLabel = startDate.format(dateFormatter),
                isActive = activeField == DateRangeField.START,
                onClick = { activeField = DateRangeField.START },
                modifier = Modifier.weight(1f),
            )
            DateRangeChip(
                label = "종료일",
                dateLabel = endDate.format(dateFormatter),
                isActive = activeField == DateRangeField.END,
                onClick = { activeField = DateRangeField.END },
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .moaCardSurface(elevation = 0.dp, cornerRadius = MoaRadius.card)
                .border(1.dp, MoaDivider, RoundedCornerShape(MoaRadius.card))
                .padding(horizontal = 12.dp, vertical = 14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = {
                        scope.launch {
                            calendarState.animateScrollToMonth(visibleMonth.minusMonths(1))
                        }
                    },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "이전 달",
                        tint = MoaTextSecondary,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Text(
                    text = "${visibleMonth.year}년 ${visibleMonth.monthValue}월",
                    color = MoaTextPrimary,
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                )
                IconButton(
                    onClick = {
                        scope.launch {
                            calendarState.animateScrollToMonth(visibleMonth.plusMonths(1))
                        }
                    },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "다음 달",
                        tint = MoaTextSecondary,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            DateRangeDaysOfWeekHeader(daysOfWeek)
            HorizontalDivider(color = MoaDivider, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(2.dp))

            HorizontalCalendar(
                state = calendarState,
                dayContent = { day ->
                    DateRangeDayCell(
                        day = day,
                        today = today,
                        startDate = startDate,
                        endDate = endDate,
                        onClick = {
                            if (day.position != DayPosition.MonthDate) return@DateRangeDayCell
                            when (activeField) {
                                DateRangeField.START -> {
                                    onStartDateChange(day.date)
                                    if (day.date.isAfter(endDate)) {
                                        onEndDateChange(day.date)
                                    }
                                }
                                DateRangeField.END -> {
                                    if (day.date.isBefore(startDate)) {
                                        onEndDateChange(startDate)
                                        onStartDateChange(day.date)
                                    } else {
                                        onEndDateChange(day.date)
                                    }
                                }
                            }
                        },
                    )
                },
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (activeField == DateRangeField.START) {
                "달력에서 시작일을 탭하세요"
            } else {
                "달력에서 종료일을 탭하세요"
            },
            color = MoaTextTertiary,
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun DateRangeChip(
    label: String,
    dateLabel: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isActive) Color.White else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            color = if (isActive) MoaBlue else MoaTextTertiary,
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = dateLabel,
            color = if (isActive) MoaTextPrimary else MoaTextSecondary,
            fontFamily = SBAggroFontFamily,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun DateRangeDaysOfWeekHeader(daysOfWeek: List<DayOfWeek>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp),
    ) {
        daysOfWeek.forEach { dayOfWeek ->
            Text(
                text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = if (dayOfWeek == DayOfWeek.SUNDAY || dayOfWeek == DayOfWeek.SATURDAY) {
                    MoaTextTertiary
                } else {
                    MoaTextSecondary
                },
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
            )
        }
    }
}

@Composable
private fun DateRangeDayCell(
    day: CalendarDay,
    today: LocalDate,
    startDate: LocalDate,
    endDate: LocalDate,
    onClick: () -> Unit,
) {
    val isMonthDate = day.position == DayPosition.MonthDate
    val date = day.date
    val isToday = date == today
    val isStart = date == startDate
    val isEnd = date == endDate
    val inRange = isMonthDate && !date.isBefore(startDate) && !date.isAfter(endDate)
    val isSingleDay = startDate == endDate

    val rangeShape = when {
        isStart && isEnd -> RoundedCornerShape(18.dp)
        isStart -> RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp)
        isEnd -> RoundedCornerShape(topEnd = 18.dp, bottomEnd = 18.dp)
        inRange -> RoundedCornerShape(0.dp)
        else -> RoundedCornerShape(0.dp)
    }

    val textColor = when {
        !isMonthDate -> Color(0xFFD0D0D4)
        isStart || isEnd -> Color.White
        isToday -> MoaBlue
        date.dayOfWeek == DayOfWeek.SUNDAY -> Color(0xFFE05C5C)
        date.dayOfWeek == DayOfWeek.SATURDAY -> MoaBlue.copy(alpha = 0.75f)
        else -> MoaTextPrimary
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clickable(enabled = isMonthDate, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (inRange && !isSingleDay) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .background(MoaBlueSoft, rangeShape),
            )
        }

        when {
            isStart || isEnd -> {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(MoaBlue),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = date.dayOfMonth.toString(),
                        color = Color.White,
                        fontFamily = SBAggroFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                    )
                }
            }
            isToday -> {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, MoaBlue.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = date.dayOfMonth.toString(),
                        color = textColor,
                        fontFamily = SBAggroFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                    )
                }
            }
            else -> {
                Text(
                    text = date.dayOfMonth.toString(),
                    color = textColor,
                    fontFamily = SBAggroFontFamily,
                    fontWeight = if (inRange) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp,
                )
            }
        }
    }
}
