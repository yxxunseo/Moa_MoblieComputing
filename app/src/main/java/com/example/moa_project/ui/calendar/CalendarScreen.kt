package com.example.moa_project.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import kotlinx.coroutines.launch
import java.time.temporal.TemporalAdjusters

private val LineColor = MoaDivider
private val GreenEvent = CalendarEventColor(Color(0xFFE3FADB), Color(0xFF20B83B))
private val BlueEvent = CalendarEventColor(Color(0xFFD5F0FF), Color(0xFF007AFF))
private val YellowEvent = CalendarEventColor(Color(0xFFFFF0A8), Color(0xFF8A6810))
private val OrangeEvent = CalendarEventColor(Color(0xFFFFD9A7), Color(0xFFFF9500))
private val PurpleEvent = CalendarEventColor(Color(0xFFE4D8FF), Color(0xFF5E35B1))

private val CalendarTodayRed = Color(0xFFFF3B30)
private val CalendarSelectedBlack = Color(0xFF1A1A1A)
private val WeekBlobPast = Color(0xFFE3F0C0)
private val WeekBlobFuture = Color(0xFFF0F0F0)
private val TimelinePurple = Color(0xFF9B7EDE)
private val TimelineDash = Color(0xFFD8DCE6)
private val CurrentTimeRed = Color(0xFFFF3B30)

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
                    viewMode = if (viewMode == CalendarViewMode.Month) CalendarViewMode.Day else CalendarViewMode.Month
                },
                onAddClick = { showAddDialog = true },
            )

            if (viewMode == CalendarViewMode.Month) {
                Box(modifier = Modifier.weight(1f)) {
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
                    MonthBottomBar(
                        onTodayClick = {
                            selectedDate = today
                            scope.launch {
                                calendarState.animateScrollToMonth(YearMonth.from(today))
                            }
                        },
                        onWeekViewClick = {
                            selectedDate = today
                            viewMode = CalendarViewMode.Day
                        },
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }
            } else {
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
            IconButton(onClick = {}, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Default.Search, contentDescription = "검색", tint = MoaTextPrimary, modifier = Modifier.size(28.dp))
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
        fontSize = 44.sp,
        letterSpacing = (-1).sp,
        modifier = Modifier.padding(start = 20.dp, top = 4.dp, bottom = 16.dp),
    )
    DaysOfWeekHeader(daysOfWeek)
    HorizontalCalendar(
        state = state,
        modifier = Modifier.padding(bottom = 72.dp),
        dayContent = { day ->
            MonthDayCell(
                day = day,
                today = today,
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
            .padding(horizontal = 4.dp, vertical = 6.dp),
    ) {
        daysOfWeek.forEach { dayOfWeek ->
            Text(
                text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = if (dayOfWeek == DayOfWeek.SUNDAY || dayOfWeek == DayOfWeek.SATURDAY) {
                    MoaTextSecondary
                } else {
                    MoaTextPrimary.copy(alpha = 0.55f)
                },
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
            )
        }
    }
}

@Composable
private fun MonthDayCell(
    day: CalendarDay,
    today: LocalDate,
    events: List<CalendarEvent>,
    onClick: () -> Unit,
) {
    val isMonthDate = day.position == DayPosition.MonthDate
    val isToday = day.date == today
    val dateColor = when {
        isToday -> Color.White
        !isMonthDate -> Color(0xFFD0D0D4)
        day.date.dayOfWeek == DayOfWeek.SUNDAY || day.date.dayOfWeek == DayOfWeek.SATURDAY -> MoaTextSecondary
        else -> MoaTextPrimary
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(118.dp)
            .clickable(enabled = isMonthDate, onClick = onClick),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 3.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isToday) CalendarTodayRed else Color.Transparent),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = day.date.dayOfMonth.toString(),
                    color = dateColor,
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            events.take(2).forEach { event ->
                EventChip(event = event, compact = true)
                Spacer(modifier = Modifier.height(3.dp))
            }
            if (events.size > 2) {
                Text(
                    text = "+${events.size - 2}개",
                    color = MoaTextSecondary,
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp,
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
private fun MonthBottomBar(
    onTodayClick: () -> Unit,
    onWeekViewClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .clickable(onClick = onTodayClick)
                .padding(horizontal = 22.dp, vertical = 12.dp),
        ) {
            Text(
                text = "오늘",
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MoaTextPrimary,
            )
        }
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(CalendarSelectedBlack)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = "월간",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp),
                )
            }
            IconButton(onClick = onWeekViewClick, modifier = Modifier.size(40.dp)) {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = "주간",
                    tint = MoaTextPrimary,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
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
        val start = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        (0..6).map { start.plusDays(it.toLong()) }
    }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 4.dp),
    ) {
        Text(
            text = "Calendar",
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = MoaTextPrimary,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        )
        Text(
            text = "주간 일정",
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = MoaTextSecondary,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 12.dp),
        )
        WeekBlobStrip(
            weekDates = weekDates,
            selectedDate = selectedDate,
            today = today,
            onDateClick = onDateClick,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp),
        ) {
            WeekDayScheduleSection(
                sectionTitle = if (selectedDate == today) "오늘의 일정" else null,
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
            Spacer(modifier = Modifier.height(24.dp))
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
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        weekDates.forEach { date ->
            WeekBlobDayItem(
                date = date,
                selected = date == selectedDate,
                today = today,
                onClick = { onDateClick(date) },
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
) {
    val dayLetter = date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.ENGLISH).uppercase()
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
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Text(
            text = dayLetter,
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = MoaTextSecondary,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .size(width = 44.dp, height = 52.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(blobColor),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "%02d".format(date.dayOfMonth),
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = textColor,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WeekDayScheduleSection(
    sectionTitle: String?,
    date: LocalDate,
    events: List<CalendarEvent>,
    showCurrentTime: Boolean,
    onDeleteEvent: (CalendarEvent) -> Unit,
    onEditEvent: (CalendarEvent) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(48.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(TimelinePurple),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "%02d".format(date.dayOfMonth),
                    color = Color.White,
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                )
            }
            Text(
                text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).uppercase(),
                color = MoaTextSecondary,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
            Canvas(
                modifier = Modifier
                    .width(2.dp)
                    .height(120.dp)
                    .padding(top = 8.dp),
            ) {
                drawLine(
                    color = TimelineDash,
                    start = Offset(size.width / 2f, 0f),
                    end = Offset(size.width / 2f, size.height),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f)),
                )
            }
        }

        Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
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
                Text(
                    text = "일정이 없어요",
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = MoaTextSecondary,
                    modifier = Modifier.padding(vertical = 16.dp),
                )
            } else {
                events.forEachIndexed { index, event ->
                    WeekTaskListItem(index = index + 1, event = event)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalTimeRuler(
                    startHour = 9,
                    endHour = 20,
                    showCurrentTime = showCurrentTime,
                )
                Spacer(modifier = Modifier.height(12.dp))

                events.forEach { event ->
                    HorizontalPillEvent(
                        event = event,
                        startHour = 9,
                        endHour = 20,
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
}

@Composable
private fun WeekTaskListItem(index: Int, event: CalendarEvent) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(event.color.background),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = index.toString(),
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = event.color.content,
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = event.title,
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = MoaTextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun HorizontalTimeRuler(
    startHour: Int,
    endHour: Int,
    showCurrentTime: Boolean,
) {
    val now = remember { java.time.LocalTime.now() }
    val totalHours = (endHour - startHour).coerceAtLeast(1)

    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            (startHour..endHour).forEach { hour ->
                Text(
                    text = hour.toString(),
                    fontFamily = SBAggroFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = MoaTextSecondary,
                )
            }
        }
        if (showCurrentTime && now.hour in startHour..endHour) {
            val fraction = (now.hour + now.minute / 60f - startHour) / totalHours
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .height(48.dp),
            ) {
                Column(
                    modifier = Modifier.align(Alignment.TopEnd),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(CurrentTimeRed)
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    ) {
                        Text(
                            text = now.format(DateTimeFormatter.ofPattern("h:mma", Locale.ENGLISH)),
                            color = Color.White,
                            fontFamily = SBAggroFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(36.dp)
                            .background(CurrentTimeRed),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HorizontalPillEvent(
    event: CalendarEvent,
    startHour: Int,
    endHour: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val totalMinutes = (endHour - startHour) * 60
    val startMinutes = (event.startHour * 60 + event.startMinute) - startHour * 60
    val endMinutes = (event.endHour * 60 + event.endMinute) - startHour * 60
    val startFraction = (startMinutes.toFloat() / totalMinutes).coerceIn(0f, 1f)
    val widthFraction = ((endMinutes - startMinutes).coerceAtLeast(30).toFloat() / totalMinutes).coerceIn(0.08f, 1f - startFraction)

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val barWidth = maxWidth * widthFraction
        val offsetX = maxWidth * startFraction
        Row(
            modifier = Modifier
                .padding(start = offsetX)
                .width(barWidth)
                .clip(RoundedCornerShape(50))
                .background(event.color.background)
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = event.title,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = event.color.content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${formatTimeAmPm(event.startHour, event.startMinute)} - ${formatTimeAmPm(event.endHour, event.endMinute)}",
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = event.color.content.copy(alpha = 0.85f),
            )
        }
    }
}

private fun formatTimeAmPm(hour: Int, minute: Int): String {
    val h = if (hour == 0 || hour == 24) 12 else if (hour > 12) hour - 12 else hour
    val amPm = if (hour < 12) "AM" else "PM"
    return if (minute == 0) "$h$amPm" else "$h:${"%02d".format(minute)}$amPm"
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
