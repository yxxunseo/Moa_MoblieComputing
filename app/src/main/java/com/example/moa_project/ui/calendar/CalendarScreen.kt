package com.example.moa_project.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moa_project.network.RetrofitClient
import com.example.moa_project.ui.components.MoaBottomNavigationBar
import com.example.moa_project.ui.home.HomeEventLoader
import com.example.moa_project.ui.components.MoaDialogButtonText
import com.example.moa_project.ui.components.MoaOutlinedTextField
import com.example.moa_project.ui.theme.MoaBlue
import com.example.moa_project.ui.theme.MoaDivider
import com.example.moa_project.ui.theme.MoaScreenBackground
import com.example.moa_project.ui.theme.MoaTextPrimary
import com.example.moa_project.ui.theme.MoaTextSecondary
import com.example.moa_project.ui.my.UserState
import com.example.moa_project.ui.my.UserViewModel
import com.example.moa_project.ui.theme.Moa_ProjectTheme
import com.example.moa_project.ui.theme.SBAggroFontFamily
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val LineColor = MoaDivider
private val GreenEvent = CalendarEventColor(Color(0xFFE3FADB), Color(0xFF20B83B))
private val BlueEvent = CalendarEventColor(Color(0xFFD5F0FF), Color(0xFF007AFF))
private val YellowEvent = CalendarEventColor(Color(0xFFFFF0A8), Color(0xFF8A6810))
private val OrangeEvent = CalendarEventColor(Color(0xFFFFD9A7), Color(0xFFFF9500))
private val PurpleEvent = CalendarEventColor(Color(0xFFE4D8FF), Color(0xFF5E35B1))

private enum class CalendarViewMode {
    Month,
    Day,
}

data class CalendarEvent(
    val id: Long? = null,
    val title: String,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val color: CalendarEventColor,
    val source: String = "MANUAL",
)

data class CalendarEventColor(
    val background: Color,
    val content: Color,
)

@Composable
fun CalendarScreen(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    viewModel: CalendarViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel(),
) {
    val userState by userViewModel.uiState.collectAsState()
    val profileImageUrl = (userState as? UserState.Success)?.user?.profileImageUrl
    val context = LocalContext.current
    val today = remember { LocalDate.now() }
    var selectedDate by remember { mutableStateOf(today) }
    var viewMode by remember { mutableStateOf(CalendarViewMode.Month) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingEvent by remember { mutableStateOf<CalendarEvent?>(null) }
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(36) }
    val endMonth = remember { currentMonth.plusMonths(36) }
    val daysOfWeek = remember { daysOfWeek(firstDayOfWeek = DayOfWeek.SUNDAY) }
    val calendarState = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = daysOfWeek.first(),
    )
    val uiState by viewModel.uiState.collectAsState()
    val events = remember { mutableStateMapOf<LocalDate, List<CalendarEvent>>() }

    // Fetch events when the visible month changes
    val visibleYearMonth = calendarState.firstVisibleMonth.yearMonth
    LaunchedEffect(visibleYearMonth) {
        val monthStr = visibleYearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))
        viewModel.fetchMonthlyEvents(monthStr)
    }

    // Process the fetched events into the UI format
    LaunchedEffect(uiState) {
        if (uiState is CalendarState.Success) {
            val response = (uiState as CalendarState.Success).events
            val newEventsMap = mutableMapOf<LocalDate, MutableList<CalendarEvent>>()
            
            val dataList = (response["events"] as? List<*>) ?: (response["data"] as? List<*>) ?: emptyList()
            dataList.forEach { raw ->
                val dto = raw as? Map<*, *> ?: return@forEach
                try {
                    val title = dto["title"] as? String ?: ""
                    val startStr = dto["start"] as? String ?: return@forEach
                    val endStr = dto["end"] as? String ?: return@forEach
                    val colorHex = dto["color"] as? String
                    val id = (dto["id"] as? Number)?.toLong()
                    val source = dto["source"] as? String ?: "MANUAL"

                    val startDateTime = HomeEventLoader.parseDateTime(startStr) ?: return@forEach
                    val endDateTime = HomeEventLoader.parseDateTime(endStr) ?: return@forEach
                    val date = startDateTime.toLocalDate()

                    val event = CalendarEvent(
                        id = id,
                        title = title,
                        startHour = startDateTime.hour,
                        startMinute = startDateTime.minute,
                        endHour = endDateTime.hour,
                        endMinute = endDateTime.minute,
                        color = parseEventColor(colorHex),
                        source = source
                    )
                    
                    newEventsMap.putIfAbsent(date, mutableListOf())
                    newEventsMap[date]!!.add(event)
                } catch (e: Exception) {
                    android.util.Log.e("CalendarScreen", "Error parsing event", e)
                }
            }
            
            events.clear()
            newEventsMap.forEach { (k, v) -> events[k] = v }
        }
    }

    editingEvent?.let { event ->
        if (event.id != null && event.source == "MANUAL") {
            var editTitle by remember(event.id) { mutableStateOf(event.title) }
            AlertDialog(
                onDismissRequest = { editingEvent = null },
                title = { Text("일정 수정", fontFamily = SBAggroFontFamily, fontWeight = FontWeight.Bold) },
                text = {
                    MoaOutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = "제목",
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        val startStr = selectedDate.atTime(event.startHour, event.startMinute).toString()
                        val endStr = selectedDate.atTime(event.endHour, event.endMinute).toString()
                        viewModel.updateEvent(
                            eventId = event.id!!,
                            title = editTitle,
                            start = startStr,
                            end = endStr,
                            color = "#2179FE",
                            onComplete = {
                                val monthStr = visibleYearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))
                                viewModel.fetchMonthlyEvents(monthStr)
                                editingEvent = null
                            },
                            onError = { msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() },
                        )
                    }) { MoaDialogButtonText("저장") }
                },
                dismissButton = {
                    TextButton(onClick = { editingEvent = null }) { MoaDialogButtonText("취소", MoaTextSecondary) }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(18.dp),
            )
        }
    }

    if (showAddDialog) {
        AddEventDialog(
            selectedDate = selectedDate,
            onDismiss = { showAddDialog = false },
            onAdd = { event ->
                // Formulate start and end times for the backend request
                val startStr = selectedDate.atTime(event.startHour, event.startMinute).toString()
                val endStr = selectedDate.atTime(event.endHour, event.endMinute).toString()
                
                viewModel.addEvent(
                    title = event.title,
                    start = startStr,
                    end = endStr,
                    color = "#007AFF",
                    onComplete = {
                        val monthStr = visibleYearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))
                        viewModel.fetchMonthlyEvents(monthStr)
                        showAddDialog = false
                        viewMode = CalendarViewMode.Day
                    },
                    onError = { msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() },
                )
            },
        )
    }

    Scaffold(
        bottomBar = {
            MoaBottomNavigationBar(
                currentRoute = currentRoute,
                profileImageUrl = profileImageUrl,
                onNavigate = onNavigate,
            )
        },
        containerColor = MoaScreenBackground,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MoaScreenBackground)
                .padding(innerPadding),
        ) {
            CalendarTopBar(
                viewMode = viewMode,
                visibleMonth = calendarState.firstVisibleMonth.yearMonth,
                selectedDate = selectedDate,
                onBackClick = {
                    viewMode = CalendarViewMode.Month
                },
                onModeClick = {
                    viewMode = if (viewMode == CalendarViewMode.Month) CalendarViewMode.Day else CalendarViewMode.Month
                },
                onAddClick = { showAddDialog = true },
            )

            if (viewMode == CalendarViewMode.Month) {
                MonthCalendarContent(
                    visibleMonth = calendarState.firstVisibleMonth.yearMonth,
                    today = today,
                    selectedDate = selectedDate,
                    daysOfWeek = daysOfWeek,
                    events = events,
                    state = calendarState,
                    onDateClick = { date ->
                        selectedDate = date
                        viewMode = CalendarViewMode.Day
                    },
                )
            } else {
                DayCalendarContent(
                    selectedDate = selectedDate,
                    today = today,
                    events = events[selectedDate].orEmpty(),
                    onDateClick = { date ->
                        selectedDate = date
                    },
                    onDeleteEvent = { event ->
                        event.id?.let { id ->
                            viewModel.deleteEvent(
                                eventId = id,
                                onComplete = {
                                    val monthStr = visibleYearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))
                                    viewModel.fetchMonthlyEvents(monthStr)
                                },
                                onError = { msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() },
                            )
                        }
                    },
                    onEditEvent = { event ->
                        if (event.source == "MANUAL" && event.id != null) {
                            editingEvent = event
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun CalendarTopBar(
    viewMode: CalendarViewMode,
    visibleMonth: YearMonth,
    selectedDate: LocalDate,
    onBackClick: () -> Unit,
    onModeClick: () -> Unit,
    onAddClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
                .clickable(onClick = onBackClick)
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "이전",
                tint = MoaTextPrimary,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(7.dp))
            Text(
                text = if (viewMode == CalendarViewMode.Month) "${visibleMonth.year}년" else "${selectedDate.monthValue}월",
                color = MoaTextPrimary,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp,
            )
        }

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
                .padding(horizontal = 8.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onModeClick, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = if (viewMode == CalendarViewMode.Month) Icons.Default.Menu else Icons.Default.DateRange,
                    contentDescription = "보기 전환",
                    tint = MoaTextPrimary,
                    modifier = Modifier.size(28.dp),
                )
            }
            IconButton(onClick = onAddClick, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Default.Add, contentDescription = "일정 추가", tint = MoaTextPrimary, modifier = Modifier.size(32.dp))
            }
        }
    }
}

@Composable
private fun MonthCalendarContent(
    visibleMonth: YearMonth,
    today: LocalDate,
    selectedDate: LocalDate,
    daysOfWeek: List<DayOfWeek>,
    events: Map<LocalDate, List<CalendarEvent>>,
    state: com.kizitonwose.calendar.compose.CalendarState,
    onDateClick: (LocalDate) -> Unit,
) {
    Text(
        text = "${visibleMonth.monthValue}월",
        color = MoaTextPrimary,
        fontFamily = SBAggroFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 42.sp,
        modifier = Modifier.padding(start = 20.dp, top = 8.dp, bottom = 12.dp),
    )
    DaysOfWeekHeader(daysOfWeek)
    HorizontalDivider(color = LineColor, thickness = 0.8.dp)
    HorizontalCalendar(
        state = state,
        dayContent = { day ->
            MonthDayCell(
                day = day,
                today = today,
                selectedDate = selectedDate,
                events = events[day.date].orEmpty(),
                onClick = {
                    if (day.position == DayPosition.MonthDate) {
                        onDateClick(day.date)
                    }
                },
            )
        },
    )
}

@Composable
private fun DaysOfWeekHeader(daysOfWeek: List<DayOfWeek>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
    ) {
        daysOfWeek.forEach { dayOfWeek ->
            Text(
                text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = if (dayOfWeek == DayOfWeek.SUNDAY || dayOfWeek == DayOfWeek.SATURDAY) MoaTextSecondary else MoaTextPrimary,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
            )
        }
    }
}

@Composable
private fun MonthDayCell(
    day: CalendarDay,
    today: LocalDate,
    selectedDate: LocalDate,
    events: List<CalendarEvent>,
    onClick: () -> Unit,
) {
    val isMonthDate = day.position == DayPosition.MonthDate
    val isToday = day.date == today
    val isSelected = day.date == selectedDate
    val dateColor = when {
        isToday -> Color.White
        !isMonthDate -> Color(0xFFD0D0D4)
        day.date.dayOfWeek == DayOfWeek.SUNDAY || day.date.dayOfWeek == DayOfWeek.SATURDAY -> MoaTextSecondary
        else -> MoaTextPrimary
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp)
            .clickable(enabled = isMonthDate, onClick = onClick),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 2.dp, vertical = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isToday -> MoaBlue
                            isSelected -> MoaBlue
                            else -> Color.Transparent
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = day.date.dayOfMonth.toString(),
                    color = if (isSelected && !isToday) Color.White else dateColor,
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                )
            }
            Spacer(modifier = Modifier.height(5.dp))
            events.take(3).forEach { event ->
                EventChip(event = event, compact = true)
                Spacer(modifier = Modifier.height(2.dp))
            }
            if (events.size > 3) {
                Text(
                    text = "+${events.size - 3}개",
                    color = MoaTextSecondary,
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                )
            }
        }
        HorizontalDivider(
            color = LineColor,
            thickness = 0.8.dp,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun EventChip(event: CalendarEvent, compact: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(5.dp))
            .background(event.color.background)
            .padding(horizontal = if (compact) 4.dp else 8.dp, vertical = 2.dp),
    ) {
        Text(
            text = event.title,
            color = event.color.content,
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = if (compact) 9.sp else 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DayCalendarContent(
    selectedDate: LocalDate,
    today: LocalDate,
    events: List<CalendarEvent>,
    onDateClick: (LocalDate) -> Unit,
    onDeleteEvent: (CalendarEvent) -> Unit = {},
    onEditEvent: (CalendarEvent) -> Unit = {},
) {
    val weekDates = remember(selectedDate) {
        val start = selectedDate.minusDays((selectedDate.dayOfWeek.value % 7).toLong())
        (0..6).map { start.plusDays(it.toLong()) }
    }
    DaysOfWeekHeader(daysOfWeek(firstDayOfWeek = DayOfWeek.SUNDAY))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp),
    ) {
        weekDates.forEach { date ->
            DayStripItem(
                date = date,
                selectedDate = selectedDate,
                today = today,
                onClick = { onDateClick(date) },
                modifier = Modifier.weight(1f),
            )
        }
    }
    HorizontalDivider(color = LineColor, thickness = 0.8.dp)
    Text(
        text = selectedDate.format(DateTimeFormatter.ofPattern("yyyy년 M월 d일 EEEE", Locale.KOREAN)),
        color = MoaTextPrimary,
        fontFamily = SBAggroFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 18.dp),
    )
    HorizontalDivider(color = LineColor, thickness = 0.8.dp)

    DayTimeline(
        events = events,
        onDeleteEvent = onDeleteEvent,
        onEditEvent = onEditEvent,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    )
}

@Composable
private fun DayStripItem(
    date: LocalDate,
    selectedDate: LocalDate,
    today: LocalDate,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selected = date == selectedDate
    val dateColor = when {
        selected -> Color.White
        date == today -> MoaBlue
        date.dayOfWeek == DayOfWeek.SUNDAY || date.dayOfWeek == DayOfWeek.SATURDAY -> MoaTextSecondary
        else -> MoaTextPrimary
    }
    Box(
        modifier = modifier
            .height(46.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(if (selected) MoaBlue else Color.Transparent),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                color = dateColor,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
            )
        }
    }
}

@Composable
private fun DayTimeline(
    events: List<CalendarEvent>,
    onDeleteEvent: (CalendarEvent) -> Unit = {},
    onEditEvent: (CalendarEvent) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val hourHeight = 70.dp
    val startHour = 8
    val endHour = 24
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(hourHeight * (endHour - startHour + 1)),
    ) {
        Column {
            (startHour..endHour).forEach { hour ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(hourHeight),
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        text = if (hour == 24) "00:00" else "%02d:00".format(hour),
                        color = MoaTextSecondary,
                        fontFamily = SBAggroFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .width(68.dp)
                            .padding(top = 9.dp),
                        textAlign = TextAlign.Center,
                    )
                    HorizontalDivider(
                        color = LineColor,
                        thickness = 0.8.dp,
                        modifier = Modifier.padding(top = 18.dp),
                    )
                }
            }
        }
        events.forEachIndexed { index, event ->
            TimelineEventBlock(
                event = event,
                startHour = startHour,
                hourHeight = hourHeight,
                lane = index % 2,
                onClick = {
                    if (event.source == "MANUAL" && event.id != null) {
                        onEditEvent(event)
                    }
                },
                onLongClick = {
                    if (event.source == "MANUAL" && event.id != null) {
                        onDeleteEvent(event)
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TimelineEventBlock(
    event: CalendarEvent,
    startHour: Int,
    hourHeight: Dp,
    lane: Int,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
) {
    val startMinutes = event.startHour * 60 + event.startMinute
    val endMinutes = event.endHour * 60 + event.endMinute
    val top = ((startMinutes - startHour * 60) / 60f * hourHeight.value).dp + 18.dp
    val height = (((endMinutes - startMinutes).coerceAtLeast(30)) / 60f * hourHeight.value).dp
    val left = if (lane == 0) 76.dp else 238.dp
    val width = if (lane == 0) 210.dp else 162.dp

    Box(
        modifier = Modifier
            .offset(x = left, y = top)
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(8.dp))
            .background(event.color.background)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(8.dp),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(4.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(3.dp))
                .background(event.color.content),
        )
        Column(modifier = Modifier.padding(start = 10.dp)) {
            Text(
                text = event.title,
                color = event.color.content,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "${formatTime(event.startHour, event.startMinute)} ~ ${formatTime(event.endHour, event.endMinute)}",
                color = event.color.content,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
            )
        }
    }
}

@Composable
private fun AddEventDialog(
    selectedDate: LocalDate,
    onDismiss: () -> Unit,
    onAdd: (CalendarEvent) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var start by remember { mutableStateOf("19:00") }
    var end by remember { mutableStateOf("20:00") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "일정 추가",
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column {
                Text(
                    text = selectedDate.format(DateTimeFormatter.ofPattern("M월 d일 EEEE", Locale.KOREAN)),
                    color = MoaTextSecondary,
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(modifier = Modifier.height(12.dp))
                MoaOutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = "일정 이름",
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row {
                    MoaOutlinedTextField(
                        value = start,
                        onValueChange = { start = it },
                        label = "시작",
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    MoaOutlinedTextField(
                        value = end,
                        onValueChange = { end = it },
                        label = "종료",
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val startTime = parseTime(start) ?: (19 to 0)
                    val endTime = parseTime(end) ?: (20 to 0)
                    onAdd(
                        CalendarEvent(
                            title = title.ifBlank { "새 일정" },
                            startHour = startTime.first,
                            startMinute = startTime.second,
                            endHour = endTime.first,
                            endMinute = endTime.second,
                            color = BlueEvent,
                        ),
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = MoaBlue),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("추가", fontFamily = SBAggroFontFamily, fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                MoaDialogButtonText("취소", MoaTextSecondary)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(18.dp),
    )
}

private fun seedCalendarEvents(today: LocalDate): Map<LocalDate, List<CalendarEvent>> {
    val base = today.withDayOfMonth(1)
    return mapOf(
        base.plusDays(0) to listOf(
            CalendarEvent(title = "정민휴", startHour = 10, startMinute = 0, endHour = 11, endMinute = 0, color = PurpleEvent),
            CalendarEvent(title = "맥날 알바", startHour = 13, startMinute = 0, endHour = 17, endMinute = 0, color = YellowEvent),
            CalendarEvent(title = "AWS 워크숍", startHour = 15, startMinute = 0, endHour = 16, endMinute = 0, color = BlueEvent),
        ),
        base.plusDays(4) to listOf(CalendarEvent(title = "어린이날", startHour = 9, startMinute = 0, endHour = 10, endMinute = 0, color = OrangeEvent)),
        today to listOf(
            CalendarEvent(title = "멋쟁이사자처럼", startHour = 18, startMinute = 30, endHour = 20, endMinute = 30, color = GreenEvent),
            CalendarEvent(title = "랩미팅", startHour = 19, startMinute = 0, endHour = 20, endMinute = 0, color = GreenEvent),
            CalendarEvent(title = "낭만 인프라 미팅", startHour = 22, startMinute = 0, endHour = 24, endMinute = 0, color = BlueEvent),
        ),
        today.plusDays(1) to listOf(CalendarEvent(title = "해외연수 OT", startHour = 16, startMinute = 0, endHour = 17, endMinute = 0, color = GreenEvent)),
        today.plusDays(6) to listOf(CalendarEvent(title = "AWS 한국세션", startHour = 18, startMinute = 0, endHour = 19, endMinute = 0, color = BlueEvent)),
        today.plusDays(14) to listOf(
            CalendarEvent(title = "다학제 캡스톤", startHour = 14, startMinute = 0, endHour = 15, endMinute = 0, color = GreenEvent),
            CalendarEvent(title = "랩미팅", startHour = 19, startMinute = 0, endHour = 20, endMinute = 0, color = GreenEvent),
        ),
    )
}

private fun formatTime(hour: Int, minute: Int): String {
    val displayHour = if (hour == 24) 0 else hour
    return "%02d:%02d".format(displayHour, minute)
}

private fun parseTime(value: String): Pair<Int, Int>? {
    val parts = value.trim().split(":")
    if (parts.size != 2) return null
    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null
    if (hour !in 0..24 || minute !in 0..59) return null
    return hour to minute
}

private fun parseEventColor(hex: String?): CalendarEventColor {
    if (hex.isNullOrBlank()) return BlueEvent
    return runCatching {
        val color = Color(android.graphics.Color.parseColor(hex))
        CalendarEventColor(
            background = color.copy(alpha = 0.18f),
            content = color
        )
    }.getOrDefault(BlueEvent)
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun CalendarScreenPreview() {
    Moa_ProjectTheme {
        CalendarScreen(currentRoute = "calendar", onNavigate = {})
    }
}
