package com.example.moa.service

import com.example.moa.entity.CalendarEvent
import com.example.moa.entity.Schedule
import com.example.moa.entity.TimeSlot
import com.example.moa.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class ScheduleResponse(
    val id: Long,
    val title: String,
    val description: String?,
    val status: String,
    val startDate: String,
    val endDate: String,
    val confirmedStart: String? = null,
    val confirmedEnd: String? = null,
    val respondedCount: Long,
    val totalMembers: Long
)

data class TimeSlotDto(val start: String, val end: String)

data class RecommendationDto(
    val rank: Int,
    val start: String,
    val end: String,
    val availableCount: Int,
    val availableMembers: List<String>
)

data class ScheduleAnalysisResponse(
    val scheduleId: Long,
    val title: String,
    val totalMembers: Long,
    val recommendations: List<RecommendationDto>,
    val heatmap: Map<String, Map<String, Int>>,
    val heatmapMembers: Map<String, Map<String, List<String>>> = emptyMap(),
)

fun Schedule.toResponse(respondedCount: Long, totalMembers: Long): ScheduleResponse {
    val displayStatus = if (
        status == "CONFIRMED" &&
        confirmedEnd != null &&
        confirmedEnd!!.isBefore(LocalDateTime.now())
    ) {
        "DONE"
    } else {
        status
    }

    return ScheduleResponse(
        id = id,
        title = title,
        description = description,
        status = displayStatus,
        startDate = startDate.toString(),
        endDate = endDate.toString(),
        confirmedStart = confirmedStart?.toString(),
        confirmedEnd = confirmedEnd?.toString(),
        respondedCount = respondedCount,
        totalMembers = totalMembers
    )
}

@Service
class ScheduleService(
    private val scheduleRepository: ScheduleRepository,
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository,
    private val groupMemberRepository: GroupMemberRepository,
    private val timeSlotRepository: TimeSlotRepository,
    private val calendarEventRepository: CalendarEventRepository
) {
    @Transactional
    fun createSchedule(userId: Long, groupId: Long, title: String, description: String?, startDate: LocalDate, endDate: LocalDate): ScheduleResponse {
        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }
        val group = groupRepository.findById(groupId).orElseThrow { IllegalArgumentException("그룹을 찾을 수 없습니다.") }
        
        if (!groupMemberRepository.existsByGroupAndUser(group, user)) {
            throw IllegalArgumentException("해당 그룹의 멤버가 아닙니다.")
        }
        
        val schedule = scheduleRepository.save(
            Schedule(
                group = group,
                createdBy = user,
                title = title,
                description = description,
                startDate = startDate,
                endDate = endDate,
                status = "WAITING"
            )
        )
        
        val totalMembers = groupMemberRepository.countByGroup(group)
        return schedule.toResponse(0, totalMembers)
    }

    @Transactional(readOnly = true)
    fun getGroupSchedules(userId: Long, groupId: Long): List<ScheduleResponse> {
        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }
        val group = groupRepository.findById(groupId).orElseThrow { IllegalArgumentException("그룹을 찾을 수 없습니다.") }
        
        if (!groupMemberRepository.existsByGroupAndUser(group, user)) {
            throw IllegalArgumentException("해당 그룹의 멤버가 아닙니다.")
        }
        
        val schedules = scheduleRepository.findAllByGroup(group)
        val totalMembers = groupMemberRepository.countByGroup(group)
        
        return schedules.map { schedule ->
            val respondedCount = timeSlotRepository.findAllBySchedule(schedule)
                .map { it.user!!.id }.distinct().count().toLong()
            schedule.toResponse(respondedCount, totalMembers)
        }
    }

    @Transactional(readOnly = true)
    fun getScheduleDetail(userId: Long, scheduleId: Long): ScheduleResponse {
        val schedule = scheduleRepository.findById(scheduleId).orElseThrow { IllegalArgumentException("일정을 찾을 수 없습니다.") }
        val totalMembers = groupMemberRepository.countByGroup(schedule.group!!)
        val respondedCount = timeSlotRepository.findAllBySchedule(schedule)
            .map { it.user!!.id }.distinct().count().toLong()
        return schedule.toResponse(respondedCount, totalMembers)
    }

    
    @Transactional
    fun addTimeSlots(userId: Long, scheduleId: Long, slots: List<TimeSlotDto>): Map<String, Any> {
        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }
        val schedule = scheduleRepository.findById(scheduleId).orElseThrow { IllegalArgumentException("일정을 찾을 수 없습니다.") }
        
        // 기존에 입력한 시간이 있다면 초기화
        timeSlotRepository.deleteAllByScheduleAndUser(schedule, user)
        
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
        
        slots.forEach { slot ->
            timeSlotRepository.save(
                TimeSlot(
                    schedule = schedule,
                    user = user,
                    slotStart = LocalDateTime.parse(slot.start, formatter),
                    slotEnd = LocalDateTime.parse(slot.end, formatter)
                )
            )
        }
        
        // 상태 업데이트
        schedule.status = "ADJUSTING"
        
        val respondedCount = timeSlotRepository.findAllBySchedule(schedule)
            .map { it.user!!.id }.distinct().count().toLong()
        val totalMembers = groupMemberRepository.countByGroup(schedule.group!!)
        
        return mapOf(
            "message" to "가능 시간이 등록되었습니다.",
            "respondedCount" to respondedCount,
            "totalMembers" to totalMembers
        )
    }

    // ⭐️ 겹치는 시간 분석 알고리즘 (히트맵)
    @Transactional(readOnly = true)
    fun analyzeSchedule(userId: Long, scheduleId: Long): ScheduleAnalysisResponse {
        val schedule = scheduleRepository.findById(scheduleId).orElseThrow { IllegalArgumentException("일정을 찾을 수 없습니다.") }
        val totalMembers = groupMemberRepository.countByGroup(schedule.group!!)
        val allSlots = timeSlotRepository.findAllBySchedule(schedule)
        
        val heatmap = mutableMapOf<String, MutableMap<String, Int>>()
        val heatmapMembers = mutableMapOf<String, MutableMap<String, MutableList<String>>>()
        val userAvailability = mutableMapOf<LocalDateTime, MutableList<String>>()
        
        // 1시간 단위로 나누어 가능한 인원수 카운트
        allSlots.forEach { slot ->
            val nickname = slot.user!!.nickname
            var current = slot.slotStart
            while (current.isBefore(slot.slotEnd)) {
                val dateStr = current.toLocalDate().toString()
                val timeStr = String.format("%02d:00", current.hour)
                
                heatmap.putIfAbsent(dateStr, mutableMapOf())
                heatmap[dateStr]!![timeStr] = heatmap[dateStr]!!.getOrDefault(timeStr, 0) + 1

                heatmapMembers.putIfAbsent(dateStr, mutableMapOf())
                heatmapMembers[dateStr]!!.putIfAbsent(timeStr, mutableListOf())
                if (!heatmapMembers[dateStr]!![timeStr]!!.contains(nickname)) {
                    heatmapMembers[dateStr]!![timeStr]!!.add(nickname)
                }
                
                userAvailability.putIfAbsent(current, mutableListOf())
                userAvailability[current]!!.add(nickname)
                
                current = current.plusHours(1)
            }
        }
        
        // 가장 많이 겹치는 시간대 상위 3개 추천
        val recommendations = userAvailability.entries
            .sortedWith(
                compareByDescending<Map.Entry<LocalDateTime, MutableList<String>>> { it.value.size }
                    .thenBy { it.key }
            )
            .take(3)
            .mapIndexed { index, entry ->
                RecommendationDto(
                    rank = index + 1,
                    start = entry.key.toString(),
                    end = entry.key.plusHours(1).toString(), // 1시간 단위
                    availableCount = entry.value.size,
                    availableMembers = entry.value.distinct()
                )
            }
            
        return ScheduleAnalysisResponse(
            scheduleId = schedule.id,
            title = schedule.title,
            totalMembers = totalMembers,
            recommendations = recommendations,
            heatmap = heatmap,
            heatmapMembers = heatmapMembers,
        )
    }
    
    @Transactional
    fun confirmSchedule(userId: Long, scheduleId: Long, start: String, end: String): Map<String, Any> {
        val schedule = scheduleRepository.findById(scheduleId).orElseThrow { IllegalArgumentException("일정을 찾을 수 없습니다.") }
        
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
        schedule.confirmedStart = LocalDateTime.parse(start, formatter)
        schedule.confirmedEnd = LocalDateTime.parse(end, formatter)
        schedule.status = "CONFIRMED"
        
        // 그룹의 모든 멤버 캘린더에 일정 자동 추가
        val groupMembers = groupMemberRepository.findAllByGroup(schedule.group!!)
        groupMembers.forEach { member ->
            calendarEventRepository.save(
                CalendarEvent(
                    user = member.user,
                    group = schedule.group,
                    schedule = schedule,
                    title = schedule.title,
                    eventStart = schedule.confirmedStart!!,
                    eventEnd = schedule.confirmedEnd!!,
                    color = schedule.group!!.color,
                    source = "GROUP"
                )
            )
        }
        
        return mapOf(
            "message" to "일정이 확정되었습니다!",
            "status" to "CONFIRMED",
            "calendarEventCreated" to true
        )
    }
}
