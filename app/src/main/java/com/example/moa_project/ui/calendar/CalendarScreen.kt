package com.example.moa_project.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moa_project.ui.components.MoaBottomNavigationBar
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(100) }
    val endMonth = remember { currentMonth.plusMonths(100) }
    val daysOfWeek = remember { daysOfWeek() }
    
    val today = remember { LocalDate.now() }
    
    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = daysOfWeek.first()
    )
    
    // 빈 캘린더 구성을 위해 더미데이터 제거
    val events = remember {
        emptyMap<LocalDate, List<EventData>>()
    }

    Scaffold(
        bottomBar = {
            MoaBottomNavigationBar(
                currentRoute = currentRoute,
                onNavigate = onNavigate
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
        ) {
            val visibleMonth = state.firstVisibleMonth.yearMonth
            
            // 상단 액션 바
            MoaCalendarTopBar(year = visibleMonth.year)
            
            // 월 타이틀
            Text(
                text = "${visibleMonth.monthValue}월",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Default,
                color = Color.Black,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )

            // 요일 헤더
            MoaDaysOfWeekHeader(daysOfWeek = daysOfWeek)

            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 0.5.dp)

            // 실제 동작하는 캘린더 컴포넌트
            HorizontalCalendar(
                state = state,
                dayContent = { day ->
                    MoaCalendarDayCell(
                        day = day, 
                        isToday = day.date == today,
                        events = events[day.date] ?: emptyList()
                    )
                }
            )
        }
    }
}

@Composable
fun MoaCalendarTopBar(year: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "이전",
                tint = Color.Black,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${year}년",
                fontSize = 17.sp,
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "목록 보기",
                tint = Color.Black,
                modifier = Modifier.size(26.dp).padding(2.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "검색",
                tint = Color.Black,
                modifier = Modifier.size(26.dp).padding(2.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "추가",
                tint = Color.Black,
                modifier = Modifier.size(26.dp).padding(2.dp)
            )
        }
    }
}

@Composable
fun MoaDaysOfWeekHeader(daysOfWeek: List<DayOfWeek>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        for (dayOfWeek in daysOfWeek) {
            val isWeekend = dayOfWeek == DayOfWeek.SUNDAY || dayOfWeek == DayOfWeek.SATURDAY
            Text(
                text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Default,
                color = if (isWeekend) Color.Gray else Color.Black
            )
        }
    }
}

@Composable
fun MoaCalendarDayCell(day: CalendarDay, isToday: Boolean, events: List<EventData>) {
    val textColor = when {
        isToday -> Color.White
        day.position == DayPosition.MonthDate && (day.date.dayOfWeek == DayOfWeek.SUNDAY || day.date.dayOfWeek == DayOfWeek.SATURDAY) -> Color.Gray
        day.position == DayPosition.MonthDate -> Color.Black
        else -> Color.LightGray
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(if (isToday) Color.Red else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day.date.dayOfMonth.toString(),
                    fontSize = 16.sp,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.SemiBold,
                    fontFamily = FontFamily.Default,
                    color = textColor
                )
            }
            
            Spacer(modifier = Modifier.height(2.dp))
            
            val maxVisibleEvents = 3
            val visibleEvents = events.take(maxVisibleEvents)
            val hiddenCount = events.size - maxVisibleEvents

            visibleEvents.forEach { event ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp, vertical = 1.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(event.color)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if(event.hasIcon) "● ${event.title}" else event.title,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Default,
                        color = if (event.color == Color.Transparent) Color.Gray else Color.DarkGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (hiddenCount > 0) {
                Text(
                    text = "+${hiddenCount}개",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Default,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        
        HorizontalDivider(
            color = Color.LightGray.copy(alpha = 0.3f),
            thickness = 0.5.dp,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(0.5.dp)
                .background(Color.LightGray.copy(alpha = 0.3f))
        )
    }
}

data class EventData(
    val title: String, 
    val color: Color,
    val hasIcon: Boolean = false
)
