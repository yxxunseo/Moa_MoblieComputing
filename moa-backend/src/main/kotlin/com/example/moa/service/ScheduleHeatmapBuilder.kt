package com.example.moa.service

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

    fun build(slots: List<Availability>): Result {
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

        return Result(heatmap, heatmapMembers, recommendations)
    }
}
