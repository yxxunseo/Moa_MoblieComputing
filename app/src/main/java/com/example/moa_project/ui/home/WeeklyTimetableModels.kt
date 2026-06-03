package com.example.moa_project.ui.home

import androidx.compose.ui.graphics.Color
import java.time.LocalDate

data class WeeklyTimetableBlock(
    val id: String,
    val dayOfWeek: Int,
    val startHour: Int,
    val endHour: Int,
    val title: String,
    val subtitle: String?,
    val color: Color,
    val isFixed: Boolean,
)

data class WeeklyTimetableData(
    val weekStart: LocalDate,
    val weekEnd: LocalDate,
    val blocks: List<WeeklyTimetableBlock>,
) {
    val weekLabel: String
        get() {
            val fmt = java.time.format.DateTimeFormatter.ofPattern("M/d")
            val dayFmt = java.time.format.DateTimeFormatter.ofPattern("(E)", java.util.Locale.KOREAN)
            return "${weekStart.format(fmt)}${weekStart.format(dayFmt)} ~ ${weekEnd.format(fmt)}${weekEnd.format(dayFmt)}"
        }

    val hasContent: Boolean get() = blocks.isNotEmpty()
}
