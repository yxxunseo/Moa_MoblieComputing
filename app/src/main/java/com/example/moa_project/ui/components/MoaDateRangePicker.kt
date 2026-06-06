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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moa_project.ui.theme.MoaAccentBlueBg
import com.example.moa_project.ui.theme.MoaBlue
import com.example.moa_project.ui.theme.MoaDivider
import com.example.moa_project.ui.theme.MoaTextPrimary
import com.example.moa_project.ui.theme.MoaTextSecondary
import com.example.moa_project.ui.theme.SBAggroFontFamily
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
import java.util.Locale

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

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
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

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "달력에서 ${if (activeField == DateRangeField.START) "시작일" else "종료일"}을 선택하세요",
            color = MoaTextSecondary,
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "${visibleMonth.year}년 ${visibleMonth.monthValue}월",
            color = MoaTextPrimary,
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        DateRangeDaysOfWeekHeader(daysOfWeek)
        HorizontalDivider(color = MoaDivider, thickness = 0.8.dp)
        HorizontalCalendar(
            state = calendarState,
            dayContent = { day ->
                DateRangeDayCell(
                    day = day,
                    today = today,
                    startDate = startDate,
                    endDate = endDate,
                    activeField = activeField,
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
}

@Composable
private fun DateRangeChip(
    label: String,
    dateLabel: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (isActive) MoaBlue else MoaDivider
    val backgroundColor = if (isActive) MoaAccentBlueBg else Color.White

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text(
            text = label,
            color = if (isActive) MoaBlue else MoaTextSecondary,
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = dateLabel,
            color = MoaTextPrimary,
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
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
                    MoaTextSecondary
                } else {
                    MoaTextPrimary
                },
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
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
    activeField: DateRangeField,
    onClick: () -> Unit,
) {
    val isMonthDate = day.position == DayPosition.MonthDate
    val date = day.date
    val isToday = date == today
    val isStart = date == startDate
    val isEnd = date == endDate
    val inRange = isMonthDate && !date.isBefore(startDate) && !date.isAfter(endDate)
    val isActiveSelection = when (activeField) {
        DateRangeField.START -> isStart
        DateRangeField.END -> isEnd
    }
    val dateColor = when {
        isToday && !isStart && !isEnd -> Color.White
        !isMonthDate -> Color(0xFFD0D0D4)
        isStart || isEnd -> Color.White
        date.dayOfWeek == DayOfWeek.SUNDAY || date.dayOfWeek == DayOfWeek.SATURDAY -> MoaTextSecondary
        else -> MoaTextPrimary
    }
    val circleColor = when {
        isStart || isEnd -> MoaBlue
        isActiveSelection -> MoaBlue.copy(alpha = 0.85f)
        isToday -> MoaBlue.copy(alpha = 0.35f)
        else -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(
                if (inRange && !isStart && !isEnd) MoaBlue.copy(alpha = 0.12f) else Color.Transparent,
            )
            .clickable(enabled = isMonthDate, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(circleColor),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                color = dateColor,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
            )
        }
    }
}
