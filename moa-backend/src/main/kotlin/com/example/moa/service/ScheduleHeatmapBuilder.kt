package com.example.moa.service

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 그룹 일정(ScheduleService)과 단기 링크 일정(GuestScheduleService)에서 거의 동일하게
 * 중복돼 있던 히트맵/추천 생성 로직을 한 곳으로 모은 유틸.
 *
 * 1시간 단위로 슬롯을 펼쳐 (날짜→시간→인원수) 히트맵, (날짜→시간→참여자명) 맵,
 * 그리고 가장 많이 겹치는 상위 3개 시간대 추천을 계산한다.
 * 기존 두 구현의 동작을 그대로 보존한다.
 */
object ScheduleHeatmapBuilder {

    data class Availability(val name: String, val start: LocalDateTime, val end: LocalDateTime)

    data class Result(
        val heatmap: Map<String, Map<String, Int>>,
        val heatmapMembers: Map<String, Map<String, List<String>>>,
        val recommendations: List<RecommendationDto>,
    )

    private val defaultHours = (0..23).map { "%02d:00".format(it) }

    fun build(
        slots: List<Availability>,
        rangeStart: LocalDate? = null,
        rangeEnd: LocalDate? = null,
    ): Result {
        val heatmap = mutableMapOf<String, MutableMap<String, Int>>()
        val heatmapMembers = mutableMapOf<String, MutableMap<String, MutableList<String>>>()
        val hourlyAvailability = mutableMapOf<LocalDateTime, MutableList<String>>()

        slots.forEach { slot ->
            var current = slot.start
            while (current.isBefore(slot.end)) {
                val dateStr = current.toLocalDate().toString()
                val timeStr = String.format("%02d:00", current.hour)

                heatmap.getOrPut(dateStr) { mutableMapOf() }
                    .merge(timeStr, 1, Int::plus)

                val members = heatmapMembers
                    .getOrPut(dateStr) { mutableMapOf() }
                    .getOrPut(timeStr) { mutableListOf() }
                if (!members.contains(slot.name)) members.add(slot.name)

                hourlyAvailability.getOrPut(current) { mutableListOf() }.add(slot.name)

                current = current.plusHours(1)
            }
        }

        val recommendations = hourlyAvailability.entries
            .sortedWith(
                compareByDescending<Map.Entry<LocalDateTime, MutableList<String>>> { it.value.size }
                    .thenBy { it.key }
            )
            .take(3)
            .mapIndexed { index, entry ->
                RecommendationDto(
                    rank = index + 1,
                    start = entry.key.toString(),
                    end = entry.key.plusHours(1).toString(),
                    availableCount = entry.value.size,
                    availableMembers = entry.value.distinct()
                )
            }

        val filledHeatmap = if (rangeStart != null && rangeEnd != null && !rangeEnd.isBefore(rangeStart)) {
            fillDateRange(rangeStart, rangeEnd, heatmap)
        } else {
            heatmap
        }

        return Result(filledHeatmap, heatmapMembers, recommendations)
    }

    /** 조율 기간 전체 날짜를 히트맵에 포함하고, 투표 없는 칸은 0으로 채운다. */
    private fun fillDateRange(
        start: LocalDate,
        end: LocalDate,
        heatmap: Map<String, Map<String, Int>>,
    ): Map<String, Map<String, Int>> {
        val result = linkedMapOf<String, Map<String, Int>>()
        var current = start
        while (!current.isAfter(end)) {
            val dateStr = current.toString()
            val existing = heatmap[dateStr].orEmpty()
            val day = linkedMapOf<String, Int>()
            defaultHours.forEach { hour ->
                day[hour] = existing[hour] ?: 0
            }
            result[dateStr] = day
            current = current.plusDays(1)
        }
        return result
    }
}
