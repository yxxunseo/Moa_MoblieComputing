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
    val totalMembers: Long,
    val isWeeklyRecurring: Boolean = false,
)

data class WeeklyReminderResponse(
    val scheduleId: Long,
    val groupName: String,
    val title: String,
    val daysUntilDeadline: Int,
    val hasSubmitted: Boolean,
    val deadlineLabel: String = "일요일",
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
    val startDate: String,
    val endDate: String,
    val totalMembers: Long,
    val recommendations: List<RecommendationDto>,
    val heatmap: Map<String, Map<String, Int>>,
    val heatmapMembers: Map<String, Map<String, List<String>>> = emptyMap(),
    val allMembers: List<String> = emptyList(),
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
        totalMembers = totalMembers,
        isWeeklyRecurring = isWeeklyRecurring,
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
    fun createSchedule(
        userId: Long,
        groupId: Long,
        title: String,
        description: String?,
        startDate: LocalDate,
        endDate: LocalDate,
        isWeeklyRecurring: Boolean = false,
    ): ScheduleResponse {
        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }
        val group = groupRepository.findById(groupId).orElseThrow { IllegalArgumentException("그룹을 찾을 수 없습니다.") }
        
        if (!groupMemberRepository.existsByGroupAndUser(group, user)) {
            throw IllegalArgumentException("해당 그룹의 멤버가 아닙니다.")
        }

        require(!endDate.isBefore(startDate)) { "종료일은 시작일보다 빠를 수 없습니다." }

        val schedule = scheduleRepository.save(
            Schedule(
                group = group,
                createdBy = user,
                title = title,
                description = description,
                startDate = startDate,
                endDate = endDate,
                status = "WAITING",
                isWeeklyRecurring = isWeeklyRecurring,
                lastWeeklyResetAt = if (isWeeklyRecurring) LocalDateTime.now() else null,
            )
        )
        
        val totalMembers = groupMemberRepository.countByGroup(group)
        return schedule.toResponse(0, totalMembers)
    }

    @Transactional
    fun getGroupSchedules(userId: Long, groupId: Long): List<ScheduleResponse> {
        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }
        val group = groupRepository.findById(groupId).orElseThrow { IllegalArgumentException("그룹을 찾을 수 없습니다.") }
        
        if (!groupMemberRepository.existsByGroupAndUser(group, user)) {
            throw IllegalArgumentException("해당 그룹의 멤버가 아닙니다.")
        }
        
        val schedules = scheduleRepository.findAllByGroup(group)
        val totalMembers = groupMemberRepository.countByGroup(group)
        if (schedules.isEmpty()) return emptyList()

        schedules.filter { it.isWeeklyRecurring }.forEach { ensureWeeklyReset(it) }

        // 일정마다 COUNT 쿼리(N+1) 대신 단일 GROUP BY 쿼리로 응답자 수를 한 번에 조회
        val respondedCountById = timeSlotRepository.countDistinctRespondedUsersGrouped(schedules)
            .associate { (it[0] as Long) to (it[1] as Long) }

        return schedules.map { schedule ->
            val respondedCount = respondedCountById[schedule.id] ?: 0L
            schedule.toResponse(respondedCount, totalMembers)
        }
    }

    @Transactional(readOnly = true)
    fun getScheduleDetail(userId: Long, scheduleId: Long): ScheduleResponse {
        val schedule = scheduleRepository.findById(scheduleId).orElseThrow { IllegalArgumentException("일정을 찾을 수 없습니다.") }
        val group = schedule.group ?: throw IllegalArgumentException("일정의 그룹 정보가 없습니다.")
        val totalMembers = groupMemberRepository.countByGroup(group)
        val respondedCount = timeSlotRepository.countDistinctRespondedUsers(schedule)
        return schedule.toResponse(respondedCount, totalMembers)
    }

    @Transactional(readOnly = true)
    fun getMyTimeSlots(userId: Long, scheduleId: Long): List<TimeSlotDto> {
        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }
        val schedule = scheduleRepository.findById(scheduleId).orElseThrow { IllegalArgumentException("일정을 찾을 수 없습니다.") }
        val group = schedule.group ?: throw IllegalArgumentException("일정의 그룹 정보가 없습니다.")

        if (!groupMemberRepository.existsByGroupAndUser(group, user)) {
            throw IllegalArgumentException("해당 그룹의 멤버가 아닙니다.")
        }

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
        return timeSlotRepository.findAllByScheduleAndUser(schedule, user).map { slot ->
            TimeSlotDto(
                start = slot.slotStart.format(formatter),
                end = slot.slotEnd.format(formatter),
            )
        }
    }

    @Transactional
    fun addTimeSlots(userId: Long, scheduleId: Long, slots: List<TimeSlotDto>): Map<String, Any> {
        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }
        val schedule = scheduleRepository.findById(scheduleId).orElseThrow { IllegalArgumentException("일정을 찾을 수 없습니다.") }
        val group = schedule.group ?: throw IllegalArgumentException("일정의 그룹 정보가 없습니다.")

        // 그룹 멤버만 가능 시간을 입력할 수 있다 (기존엔 검증이 없어 외부인도 입력 가능했음)
        if (!groupMemberRepository.existsByGroupAndUser(group, user)) {
            throw IllegalArgumentException("해당 그룹의 멤버가 아닙니다.")
        }

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
        val parsedSlots = slots.map { slot ->
            val start = LocalDateTime.parse(slot.start, formatter)
            val end = LocalDateTime.parse(slot.end, formatter)
            require(end.isAfter(start)) { "종료 시간은 시작 시간보다 늦어야 합니다." }
            start to end
        }

        // 기존에 입력한 시간이 있다면 초기화 (파싱/검증 통과 후 삭제하여 실패 시 데이터 보존)
        timeSlotRepository.deleteAllByScheduleAndUser(schedule, user)

        parsedSlots.forEach { (start, end) ->
            timeSlotRepository.save(
                TimeSlot(
                    schedule = schedule,
                    user = user,
                    slotStart = start,
                    slotEnd = end
                )
            )
        }

        // 상태 업데이트
        schedule.status = "ADJUSTING"

        val respondedCount = timeSlotRepository.countDistinctRespondedUsers(schedule)
        val totalMembers = groupMemberRepository.countByGroup(group)
        
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
        val group = schedule.group ?: throw IllegalArgumentException("일정의 그룹 정보가 없습니다.")
        val totalMembers = groupMemberRepository.countByGroup(group)
        val allSlots = timeSlotRepository.findAllByScheduleWithUser(schedule)

        val allMemberNames = groupMemberRepository.findAllByGroup(group)
            .mapNotNull { it.user?.nickname?.takeIf { name -> name.isNotBlank() } }
            .sorted()

        val result = ScheduleHeatmapBuilder.build(
            slots = allSlots.map { ScheduleHeatmapBuilder.Availability(it.user!!.nickname, it.slotStart, it.slotEnd) },
            rangeStart = schedule.startDate,
            rangeEnd = schedule.endDate,
        )

        return ScheduleAnalysisResponse(
            scheduleId = schedule.id,
            title = schedule.title,
            startDate = schedule.startDate.toString(),
            endDate = schedule.endDate.toString(),
            totalMembers = totalMembers,
            recommendations = result.recommendations,
            heatmap = result.heatmap,
            heatmapMembers = result.heatmapMembers,
            allMembers = allMemberNames,
        )
    }

    @Transactional
    fun getWeeklyReminders(userId: Long): List<WeeklyReminderResponse> {
        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }
        val memberships = groupMemberRepository.findAllByUser(user)
        val today = LocalDate.now()
        val daysUntilSunday = (7 - today.dayOfWeek.value) % 7

        return memberships.flatMap { membership ->
            val group = membership.group ?: return@flatMap emptyList()
            scheduleRepository.findAllByGroup(group)
                .filter { it.isWeeklyRecurring && it.status in listOf("WAITING", "ADJUSTING") }
                .onEach { ensureWeeklyReset(it) }
                .mapNotNull { schedule ->
                    val hasSubmitted = timeSlotRepository.findAllByScheduleAndUser(schedule, user).isNotEmpty()
                    if (hasSubmitted) return@mapNotNull null
                    WeeklyReminderResponse(
                        scheduleId = schedule.id,
                        groupName = group.name,
                        title = schedule.title,
                        daysUntilDeadline = daysUntilSunday,
                        hasSubmitted = false,
                    )
                }
        }
    }

    @Transactional
    fun ensureWeeklyReset(schedule: Schedule) {
        if (!schedule.isWeeklyRecurring) return
        val now = LocalDateTime.now()
        val thisMonday = LocalDate.now()
            .with(java.time.DayOfWeek.MONDAY)
            .atStartOfDay()
        val lastReset = schedule.lastWeeklyResetAt
        if (lastReset != null && !lastReset.isBefore(thisMonday)) return

        timeSlotRepository.deleteAllBySchedule(schedule)
        schedule.status = "WAITING"
        schedule.confirmedStart = null
        schedule.confirmedEnd = null
        schedule.lastWeeklyResetAt = now
        scheduleRepository.save(schedule)
    }
    
    @Transactional
    fun confirmSchedule(userId: Long, scheduleId: Long, start: String, end: String): Map<String, Any> {
        val schedule = scheduleRepository.findById(scheduleId).orElseThrow { IllegalArgumentException("일정을 찾을 수 없습니다.") }
        val group = schedule.group ?: throw IllegalArgumentException("일정의 그룹 정보가 없습니다.")
        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }

        // 그룹 멤버만, 그리고 일정 생성자가 있다면 생성자만 확정할 수 있다 (기존엔 누구나 확정 가능했음)
        if (!groupMemberRepository.existsByGroupAndUser(group, user)) {
            throw IllegalArgumentException("해당 그룹의 멤버가 아닙니다.")
        }
        val creatorId = schedule.createdBy?.id
        if (creatorId != null && creatorId != userId) {
            throw IllegalArgumentException("일정 확정 권한이 없습니다. (일정 생성자만 확정할 수 있습니다)")
        }

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
        val confirmedStart = LocalDateTime.parse(start, formatter)
        val confirmedEnd = LocalDateTime.parse(end, formatter)
        require(confirmedEnd.isAfter(confirmedStart)) { "종료 시간은 시작 시간보다 늦어야 합니다." }

        schedule.confirmedStart = confirmedStart
        schedule.confirmedEnd = confirmedEnd
        schedule.status = "CONFIRMED"
        scheduleRepository.save(schedule)

        // 그룹의 모든 멤버 캘린더에 일정 자동 추가
        val groupMembers = groupMemberRepository.findAllByGroup(group)
        groupMembers.forEach { member ->
            calendarEventRepository.save(
                CalendarEvent(
                    user = member.user,
                    group = group,
                    schedule = schedule,
                    title = schedule.title,
                    eventStart = confirmedStart,
                    eventEnd = confirmedEnd,
                    color = group.color,
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
