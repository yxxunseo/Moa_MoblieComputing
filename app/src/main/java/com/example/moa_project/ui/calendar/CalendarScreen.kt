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
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.moa_project.ui.components.MoaBottomNavigationBar
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
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import java.time.temporal.TemporalAdjusters

private val GreenEvent = CalendarEventColor(Color(0xFFE3FADB), Color(0xFF20B83B))
private val BlueEvent = CalendarEventColor(Color(0xFFD5F0FF), Color(0xFF007AFF))
private val YellowEvent = CalendarEventColor(Color(0xFFFFF0A8), Color(0xFF8A6810))
private val OrangeEvent = CalendarEventColor(Color(0xFFFFD9A7), Color(0xFFFF9500))
private val PurpleEvent = CalendarEventColor(Color(0xFFE4D8FF), Color(0xFF5E35B1))

private val CalendarTodayRed = Color(0xFFFF3B30)
private val CalendarSelectedBlack = Color(0xFF1A1A1A)
private val WeekBlobPast = Color(0xFFE3F0C0)
private val WeekBlobFuture = Color(0xFFF0F0F0)
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

@OptIn(ExperimentalMaterial3Api::class)
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
    val sharedPreferences = remember {
        context.getSharedPreferences("moa_settings", Context.MODE_PRIVATE)
    }
    val includeGoogle = remember {
        sharedPreferences.getBoolean("google_calendar", false)
    }
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
    val scope = rememberCoroutineScope()

    // Fetch events when the visible month changes
    val visibleYearMonth = calendarState.firstVisibleMonth.yearMonth
    LaunchedEffect(visibleYearMonth, includeGoogle) {
        val monthStr = visibleYearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))
        viewModel.fetchMonthlyEvents(monthStr, includeGoogleEvents = includeGoogle)
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

                    val startDateTime = java.time.LocalDateTime.parse(startStr)
                    val endDateTime = java.time.LocalDateTime.parse(endStr)
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
                            color = "#2179FE"
                        ) {
                            val monthStr = visibleYearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))
                            viewModel.fetchMonthlyEvents(monthStr, includeGoogleEvents = includeGoogle)
                            editingEvent = null
                        }
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
                    color = "#007AFF"
                ) {
                    val monthStr = visibleYearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))
                    viewModel.fetchMonthlyEvents(monthStr, includeGoogleEvents = includeGoogle)
                    showAddDialog = false
                    viewMode = CalendarViewMode.Day
                }
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
                    viewMode = when (viewMode) {
                        CalendarViewMode.Month -> CalendarViewMode.Day
                        CalendarViewMode.Day -> CalendarViewMode.Month
                    }
                },
                onAddClick = { showAddDialog = true },
            )

            when (viewMode) {
                CalendarViewMode.Month -> {
                    MonthCalendarContent(
                        modifier = Modifier.weight(1f),
                        visibleMonth = calendarState.firstVisibleMonth.yearMonth,
                        today = today,
                        selectedDate = selectedDate,
                        daysOfWeek = daysOfWeek,
                        events = events,
                        state = calendarState,
                        onTodayClick = {
                            selectedDate = today
                            scope.launch {
                                calendarState.animateScrollToMonth(YearMonth.from(today))
                            }
                        },
                        onDateClick = { date ->
                            selectedDate = date
                            viewMode = CalendarViewMode.Day
                        },
                    )
                }
                CalendarViewMode.Day -> {
                WeekCalendarContent(
                    selectedDate = selectedDate,
                    today = today,
                    events = events,
                    onDateClick = { date ->
                        selectedDate = date
                    },
                    onDeleteEvent = { event ->
                        event.id?.let { id ->
                            viewModel.deleteEvent(id) {
                                val monthStr = visibleYearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))
                                viewModel.fetchMonthlyEvents(monthStr, includeGoogleEvents = includeGoogle)
                            }
                        }
                    },
                    onEditEvent = { event ->
                        if (event.source == "MANUAL" && event.id != null) {
                            editingEvent = event
                        }
                    },
                )
                }
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
                text = when (viewMode) {
                    CalendarViewMode.Month -> "${visibleMonth.year}년 ${visibleMonth.monthValue}월"
                    CalendarViewMode.Day -> selectedDate.format(DateTimeFormatter.ofPattern("M월 d일", Locale.KOREAN))
                },
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
                    imageVector = when (viewMode) {
                        CalendarViewMode.Month -> Icons.Default.Schedule
                        CalendarViewMode.Day -> Icons.Default.DateRange
                    },
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
    onTodayClick: () -> Unit,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${visibleMonth.monthValue}월",
                color = MoaTextPrimary,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .clickable(onClick = onTodayClick)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(
                    text = "오늘",
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MoaBlue,
                )
            }
        }
        DaysOfWeekHeader(
            daysOfWeek = daysOfWeek,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        HorizontalCalendar(
            state = state,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 8.dp),
            monthHeader = {},
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
}

@Composable
private fun DaysOfWeekHeader(
    daysOfWeek: List<DayOfWeek>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
    ) {
        daysOfWeek.forEach { dayOfWeek ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(28.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN),
                    textAlign = TextAlign.Center,
                    color = if (dayOfWeek == DayOfWeek.SUNDAY || dayOfWeek == DayOfWeek.SATURDAY) {
                        MoaTextSecondary
                    } else {
                        MoaTextPrimary.copy(alpha = 0.55f)
                    },
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    maxLines = 1,
                )
            }
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
    val isSelected = day.date == selectedDate && isMonthDate
    val dateColor = when {
        isToday || isSelected -> Color.White
        !isMonthDate -> Color(0xFFD0D0D4)
        day.date.dayOfWeek == DayOfWeek.SUNDAY || day.date.dayOfWeek == DayOfWeek.SATURDAY -> MoaTextSecondary
        else -> MoaTextPrimary
    }
    val circleColor = when {
        isToday -> CalendarTodayRed
        isSelected -> MoaBlue
        else -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(enabled = isMonthDate, onClick = onClick),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 2.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(circleColor),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = day.date.dayOfMonth.toString(),
                    color = dateColor,
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                )
            }
            if (events.isNotEmpty()) {
                Spacer(modifier = Modifier.height(3.dp))
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(if (isToday || isSelected) CalendarTodayRed else MoaBlue),
                )
            }
        }
    }
}

@Composable
private fun EventChip(event: CalendarEvent, compact: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(if (compact) 8.dp else 14.dp))
            .background(event.color.background)
            .padding(horizontal = if (compact) 5.dp else 10.dp, vertical = if (compact) 3.dp else 6.dp),
    ) {
        Text(
            text = event.title,
            color = event.color.content,
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = if (compact) 9.sp else 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun WeekCalendarContent(
    selectedDate: LocalDate,
    today: LocalDate,
    events: Map<LocalDate, List<CalendarEvent>>,
    onDateClick: (LocalDate) -> Unit,
    onDeleteEvent: (CalendarEvent) -> Unit = {},
    onEditEvent: (CalendarEvent) -> Unit = {},
) {
    val weekDates = remember(selectedDate) {
        val start = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
        (0..6).map { start.plusDays(it.toLong()) }
    }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 4.dp),
    ) {
        WeekBlobStrip(
            weekDates = weekDates,
            selectedDate = selectedDate,
            today = today,
            onDateClick = onDateClick,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp),
        ) {
            WeekDayScheduleSection(
                sectionTitle = if (selectedDate == today) "오늘의 일정" else selectedDate.format(
                    DateTimeFormatter.ofPattern("M월 d일 EEEE", Locale.KOREAN),
                ),
                date = selectedDate,
                events = events[selectedDate].orEmpty(),
                showCurrentTime = selectedDate == today,
                onDeleteEvent = onDeleteEvent,
                onEditEvent = onEditEvent,
            )
            val tomorrow = selectedDate.plusDays(1)
            if (events[tomorrow]?.isNotEmpty() == true) {
                Spacer(modifier = Modifier.height(24.dp))
                WeekDayScheduleSection(
                    sectionTitle = "내일",
                    date = tomorrow,
                    events = events[tomorrow].orEmpty(),
                    showCurrentTime = false,
                    onDeleteEvent = onDeleteEvent,
                    onEditEvent = onEditEvent,
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun WeekBlobStrip(
    weekDates: List<LocalDate>,
    selectedDate: LocalDate,
    today: LocalDate,
    onDateClick: (LocalDate) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
    ) {
        weekDates.forEach { date ->
            WeekBlobDayItem(
                date = date,
                selected = date == selectedDate,
                today = today,
                onClick = { onDateClick(date) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun WeekBlobDayItem(
    date: LocalDate,
    selected: Boolean,
    today: LocalDate,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dayLetter = date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.KOREAN)
    val blobColor = when {
        selected -> CalendarSelectedBlack
        date.isBefore(today) -> WeekBlobPast
        date.isAfter(today) -> WeekBlobFuture
        else -> WeekBlobPast
    }
    val textColor = when {
        selected -> Color.White
        date.dayOfWeek == DayOfWeek.SUNDAY || date.dayOfWeek == DayOfWeek.SATURDAY -> MoaTextSecondary
        else -> MoaTextPrimary
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(horizontal = 2.dp)
            .clickable(onClick = onClick),
    ) {
        Text(
            text = dayLetter,
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            color = MoaTextSecondary,
            maxLines = 1,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .size(width = 40.dp, height = 48.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(blobColor),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = textColor,
                maxLines = 1,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WeekDayScheduleSection(
    sectionTitle: String?,
    @Suppress("UNUSED_PARAMETER") date: LocalDate,
    events: List<CalendarEvent>,
    showCurrentTime: Boolean,
    onDeleteEvent: (CalendarEvent) -> Unit,
    onEditEvent: (CalendarEvent) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (sectionTitle != null) {
            Text(
                text = sectionTitle,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MoaTextPrimary,
                modifier = Modifier.padding(bottom = 12.dp),
            )
        }

        if (events.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(vertical = 28.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "일정이 없어요",
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = MoaTextSecondary,
                )
            }
        } else {
            events.sortedBy { it.startHour * 60 + it.startMinute }.forEach { event ->
                WeekEventCard(
                    event = event,
                    startHour = 8,
                    endHour = 22,
                    onClick = {
                        if (event.source == "MANUAL" && event.id != null) onEditEvent(event)
                    },
                    onLongClick = {
                        if (event.source == "MANUAL" && event.id != null) onDeleteEvent(event)
                    },
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WeekEventCard(
    event: CalendarEvent,
    startHour: Int,
    endHour: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(52.dp),
        ) {
            Text(
                text = formatTimeAmPm(event.startHour, event.startMinute),
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
                color = MoaTextSecondary,
                maxLines = 1,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(event.color.content.copy(alpha = 0.35f)),
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(event.color.background)
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Text(
                text = event.title,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = event.color.content,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${formatTimeAmPm(event.startHour, event.startMinute)} – ${formatTimeAmPm(event.endHour, event.endMinute)}",
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = event.color.content.copy(alpha = 0.8f),
                maxLines = 1,
            )
        }
    }
}

private fun formatTimeAmPm(hour: Int, minute: Int): String {
    val period = if (hour < 12) "오전" else "오후"
    val h = when {
        hour == 0 || hour == 24 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return if (minute == 0) "$period ${h}시" else "$period ${h}:${"%02d".format(minute)}"
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
