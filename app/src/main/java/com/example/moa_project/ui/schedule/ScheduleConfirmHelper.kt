package com.example.moa_project.ui.schedule

import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object ScheduleConfirmHelper {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")

    fun maxConsecutiveHours(
        startIso: String,
        heatmap: Map<String, Map<String, Int>>?,
        minAvailableCount: Int,
        latestStartHour: Int = 22,
    ): Int {
        val start = runCatching { LocalDateTime.parse(startIso, formatter) }.getOrNull() ?: return 1
        var hours = 1
        var next = start.plusHours(1)
        while (hours < 8) {
            if (start.hour + hours > latestStartHour) break
            val dateStr = next.toLocalDate().toString()
            val timeStr = String.format("%02d:00", next.hour)
            val count = heatmap?.get(dateStr)?.get(timeStr) ?: 0
            if (count < minAvailableCount) break
            hours++
            next = next.plusHours(1)
        }
        return hours.coerceAtLeast(1)
    }

    fun buildEndTime(startIso: String, durationHours: Int): String {
        val start = LocalDateTime.parse(startIso, formatter)
        return start.plusHours(durationHours.toLong()).format(formatter)
    }

    fun formatTimeRange(startIso: String, durationHours: Int): String {
        val start = LocalDateTime.parse(startIso, formatter)
        val end = start.plusHours(durationHours.toLong())
        return "${start.toLocalTime().toString().take(5)} - ${end.toLocalTime().toString().take(5)}"
    }

    fun durationHoursBetween(startIso: String, endIso: String): Int {
        val start = LocalDateTime.parse(startIso, formatter)
        val end = LocalDateTime.parse(endIso, formatter)
        return Duration.between(start, end).toHours().toInt().coerceAtLeast(1)
    }
}
