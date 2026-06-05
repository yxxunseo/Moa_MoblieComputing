package com.example.moa_project.ui.home

import com.example.moa_project.network.GroupResponse
import com.example.moa_project.network.GuestScheduleResponse
import com.example.moa_project.network.RetrofitClient
import com.example.moa_project.network.ScheduleDetailResponse
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

object HomeEventLoader {

    fun parseDateTime(text: String): LocalDateTime? {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return null
        return runCatching { LocalDateTime.parse(trimmed) }.getOrNull()
            ?: runCatching {
                LocalDateTime.parse(trimmed, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
            }.getOrNull()
            ?: runCatching {
                LocalDateTime.parse(trimmed.substringBefore('.').substringBefore('Z'))
            }.getOrNull()
    }

    suspend fun loadUpcomingEvents(groups: List<GroupResponse>): List<HomeEventItem> {
        // 오늘 0시 기준: 오늘 이미 지난 시각의 일정도 "다가오는 일정"에 포함
        val todayStart = LocalDate.now().atStartOfDay()
        val fromCalendar = loadCalendarEvents()
        val fromGroups = loadConfirmedGroupSchedules(groups)
        val fromGuest = loadConfirmedGuestSchedules()

        return (fromCalendar + fromGroups + fromGuest)
            .distinctBy { "${it.title}|${it.start}" }
            .filter { !it.start.toLocalDate().isBefore(todayStart.toLocalDate()) }
            .sortedBy { it.start }
    }

    private suspend fun loadCalendarEvents(): List<HomeEventItem> {
        val months = listOf(YearMonth.now(), YearMonth.now().plusMonths(1))
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM")
        return months.flatMap { month ->
            runCatching {
                RetrofitClient.instance.getMonthlyEvents(month.format(formatter))
            }.getOrNull()?.let { parseEventsMap(it) }.orEmpty()
        }
    }

    private suspend fun loadConfirmedGroupSchedules(groups: List<GroupResponse>): List<HomeEventItem> {
        val items = mutableListOf<HomeEventItem>()
        groups.forEach { group ->
            runCatching { RetrofitClient.instance.getGroupSchedules(group.id) }
                .getOrDefault(emptyList())
                .forEach { schedule ->
                    schedule.toHomeEventIfConfirmed(group.color, group.name)?.let { items += it }
                }
        }
        return items
    }

    private suspend fun loadConfirmedGuestSchedules(): List<HomeEventItem> {
        return runCatching { RetrofitClient.instance.getMyGuestSchedules() }
            .getOrDefault(emptyList())
            .mapNotNull { it.toHomeEventIfConfirmed() }
    }

    private fun ScheduleDetailResponse.toHomeEventIfConfirmed(
        groupColor: String,
        groupName: String,
    ): HomeEventItem? {
        if (status != "CONFIRMED" && status != "DONE") return null
        val startText = confirmedStart ?: return null
        val start = parseDateTime(startText) ?: return null
        return HomeEventItem(
            title = title,
            start = start,
            color = groupColor,
            subtitle = groupName,
        )
    }

    private fun GuestScheduleResponse.toHomeEventIfConfirmed(): HomeEventItem? {
        if (status != "CONFIRMED" && status != "DONE") return null
        val start = confirmedStart?.let(::parseDateTime) ?: return null
        return HomeEventItem(
            title = title,
            start = start,
            color = "#2179FE",
            subtitle = "단기 일정",
        )
    }

    fun parseEventsMap(response: Map<String, Any>): List<HomeEventItem> {
        val data = (response["events"] as? List<*>) ?: (response["data"] as? List<*>) ?: return emptyList()
        return data.mapNotNull { raw ->
            val item = raw as? Map<*, *> ?: return@mapNotNull null
            val title = item["title"] as? String ?: return@mapNotNull null
            val startText = item["start"] as? String ?: return@mapNotNull null
            val color = item["color"] as? String ?: "#2179FE"
            val subtitle = (item["groupName"] as? String)
                ?: (item["source"] as? String)?.let { sourceLabel(it) }
            val start = parseDateTime(startText) ?: return@mapNotNull null
            HomeEventItem(title, start, color, subtitle)
        }
    }

    private fun sourceLabel(source: String): String? = when (source) {
        "GROUP" -> "모임 일정"
        "GUEST" -> "단기 일정"
        "MANUAL" -> "직접 등록"
        else -> null
    }

    fun countEventsThisWeek(events: List<HomeEventItem>): Int {
        val monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val sunday = monday.plusDays(6)
        return events.count {
            val d = it.start.toLocalDate()
            !d.isBefore(monday) && !d.isAfter(sunday)
        }
    }
}
