package com.example.moa_project.ui.home

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import com.example.moa_project.network.FixedTimeSlotDto
import com.example.moa_project.network.RetrofitClient
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

object WeeklyTimetableLoader {
    private val fixedPalette = listOf(
        Color(0xFF5B8DEF),
        Color(0xFF7C6FF0),
        Color(0xFF35A96D),
        Color(0xFFF2994A),
        Color(0xFFEB5E8C),
        Color(0xFF22B8C2),
        Color(0xFFE0A82E),
        Color(0xFF8E7CC3),
    )

    suspend fun loadCurrentWeek(): WeeklyTimetableData {
        val monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val sunday = monday.plusDays(6)
        val blocks = mutableListOf<WeeklyTimetableBlock>()

        runCatching {
            RetrofitClient.instance.getFixedTimeSlots()
        }.getOrDefault(emptyList()).forEach { slot ->
            blocks += slot.toWeeklyBlock()
        }

        val month = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
        runCatching {
            RetrofitClient.instance.getMonthlyEvents(month)
        }.onSuccess { response ->
            blocks += parseCalendarEvents(response, monday, sunday)
        }

        return WeeklyTimetableData(
            weekStart = monday,
            weekEnd = sunday,
            blocks = blocks.sortedWith(compareBy({ it.dayOfWeek }, { it.startHour })),
        )
    }

    private fun FixedTimeSlotDto.toWeeklyBlock(): WeeklyTimetableBlock {
        val color = fixedPalette[(id % fixedPalette.size).toInt()]
        return WeeklyTimetableBlock(
            id = "fixed-$id",
            dayOfWeek = dayOfWeek,
            startHour = startHour,
            endHour = endHour,
            title = title,
            subtitle = "고정",
            color = color,
            isFixed = true,
        )
    }

    private fun parseCalendarEvents(
        response: Map<String, Any>,
        weekStart: LocalDate,
        weekEnd: LocalDate,
    ): List<WeeklyTimetableBlock> {
        val data = (response["events"] as? List<*>) ?: return emptyList()
        val items = mutableListOf<WeeklyTimetableBlock>()
        data.forEach { raw ->
            val item = raw as? Map<*, *> ?: return@forEach
            val title = item["title"] as? String ?: return@forEach
            val startText = item["start"] as? String ?: return@forEach
            val endText = item["end"] as? String ?: return@forEach
            val colorHex = item["color"] as? String ?: "#2179FE"
            val groupName = item["groupName"] as? String
            val start = HomeEventLoader.parseDateTime(startText) ?: return@forEach
            val end = HomeEventLoader.parseDateTime(endText) ?: return@forEach
            val date = start.toLocalDate()
            if (date.isBefore(weekStart) || date.isAfter(weekEnd)) return@forEach
            val dayOfWeek = date.dayOfWeek.value
            items += WeeklyTimetableBlock(
                id = "event-${item["id"]}-$startText",
                dayOfWeek = dayOfWeek,
                startHour = start.hour.coerceIn(9, 21),
                endHour = end.hour.coerceAtLeast(start.hour + 1).coerceAtMost(22),
                title = title,
                subtitle = groupName ?: "확정 일정",
                color = parseColor(colorHex),
                isFixed = false,
            )
        }
        return items
    }

    private fun parseColor(hex: String): Color =
        runCatching { Color(hex.toColorInt()) }.getOrDefault(Color(0xFF2179FE))
}
