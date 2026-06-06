package com.example.moa_project.util

import android.content.Context
import com.example.moa_project.network.RetrofitClient
import com.example.moa_project.ui.home.HomeEventLoader
import com.example.moa_project.ui.schedule.TimeSlot
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

object BusyTimeHelper {
    suspend fun loadBlockedSlots(
        context: Context,
        startDate: LocalDate,
        endDate: LocalDate
    ): Set<TimeSlot> {
        val blocked = mutableSetOf<TimeSlot>()
        val months = mutableSetOf<YearMonth>()
        var current = startDate
        while (!current.isAfter(endDate)) {
            months.add(YearMonth.from(current))
            current = current.plusDays(1)
        }

        val includeGoogle = context.getSharedPreferences("moa_settings", Context.MODE_PRIVATE)
            .getBoolean("google_calendar", false)

        months.forEach { month ->
            val monthStr = month.format(DateTimeFormatter.ofPattern("yyyy-MM"))
            runCatching {
                val response = RetrofitClient.instance.getMonthlyEvents(monthStr)
                parseEventsToBlocked(response, startDate, endDate, blocked)
            }
            if (includeGoogle) {
                runCatching {
                    val googleResponse = RetrofitClient.instance.getGoogleCalendarEvents(monthStr)
                    parseEventsToBlocked(googleResponse, startDate, endDate, blocked)
                }
            }
        }

        // 고정 시간표는 주간 보기·조율 참고용으로만 쓰고, 가능 시간 선택은 막지 않음

        return blocked
    }

    private fun parseEventsToBlocked(
        response: Map<String, Any>,
        startDate: LocalDate,
        endDate: LocalDate,
        blocked: MutableSet<TimeSlot>
    ) {
        val events = (response["events"] as? List<*>) ?: return
        events.forEach { raw ->
            val item = raw as? Map<*, *> ?: return@forEach
            val startText = item["start"] as? String ?: return@forEach
            val endText = item["end"] as? String ?: return@forEach
            val start = HomeEventLoader.parseDateTime(startText) ?: return@forEach
            val end = HomeEventLoader.parseDateTime(endText) ?: return@forEach

            var slotStart = start.withMinute(0).withSecond(0).withNano(0)
            if (slotStart.isBefore(start)) slotStart = slotStart.plusHours(1)

            while (slotStart.isBefore(end)) {
                val date = slotStart.toLocalDate()
                if (!date.isBefore(startDate) && !date.isAfter(endDate)) {
                    blocked.add(TimeSlot(date, slotStart.hour))
                }
                slotStart = slotStart.plusHours(1)
            }
        }
    }
}
