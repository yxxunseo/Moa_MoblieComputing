package com.example.moa_project.util

import android.content.Context
import com.example.moa_project.network.RetrofitClient
import com.example.moa_project.ui.home.HomeEventLoader
import com.example.moa_project.ui.schedule.TimeSlot
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

object BusyTimeHelper {
    suspend fun loadBusySlotLabels(
        context: Context,
        startDate: LocalDate,
        endDate: LocalDate,
    ): Map<TimeSlot, String> {
        val labels = mutableMapOf<TimeSlot, String>()
        val months = mutableSetOf<YearMonth>()
        var current = startDate
        while (!current.isAfter(endDate)) {
            months.add(YearMonth.from(current))
            current = current.plusDays(1)
        }

        months.forEach { month ->
            val monthStr = month.format(DateTimeFormatter.ofPattern("yyyy-MM"))
            runCatching {
                val response = RetrofitClient.instance.getMonthlyEvents(monthStr)
                parseEventsToLabels(response, startDate, endDate, labels)
            }
        }

        return labels
    }

    private fun parseEventsToLabels(
        response: Map<String, Any>,
        startDate: LocalDate,
        endDate: LocalDate,
        labels: MutableMap<TimeSlot, String>,
    ) {
        val events = (response["events"] as? List<*>) ?: return
        events.forEach { raw ->
            val item = raw as? Map<*, *> ?: return@forEach
            val startText = item["start"] as? String ?: return@forEach
            val endText = item["end"] as? String ?: return@forEach
            val title = (item["title"] as? String)?.trim()?.takeIf { it.isNotEmpty() } ?: "기존 일정"
            val start = HomeEventLoader.parseDateTime(startText) ?: return@forEach
            val end = HomeEventLoader.parseDateTime(endText) ?: return@forEach

            var slotStart = start.withMinute(0).withSecond(0).withNano(0)
            if (slotStart.isBefore(start)) slotStart = slotStart.plusHours(1)

            while (slotStart.isBefore(end)) {
                val date = slotStart.toLocalDate()
                if (!date.isBefore(startDate) && !date.isAfter(endDate)) {
                    val slot = TimeSlot(date, slotStart.hour)
                    labels[slot] = when (val existing = labels[slot]) {
                        null -> title
                        title -> existing
                        else -> "$existing·$title"
                    }
                }
                slotStart = slotStart.plusHours(1)
            }
        }
    }
}
