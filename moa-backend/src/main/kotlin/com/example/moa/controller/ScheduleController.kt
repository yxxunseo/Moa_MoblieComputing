package com.example.moa.controller

import com.example.moa.service.*
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

data class CreateScheduleRequest(
    @field:NotBlank(message = "일정 제목을 입력해주세요.") val title: String,
    val description: String? = null,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val isWeeklyRecurring: Boolean = false,
)

data class AddTimeSlotsRequest(
    val slots: List<TimeSlotDto>
)

data class ConfirmScheduleRequest(
    @field:NotBlank val confirmedStart: String,
    @field:NotBlank val confirmedEnd: String
)

data class UpsertReactionRequest(@field:NotBlank val emoji: String)

@RestController
@RequestMapping("/api")
class ScheduleController(
    private val scheduleService: ScheduleService,
    private val scheduleReactionService: ScheduleReactionService
) {
    // 1. 일정 조율 생성
    @PostMapping("/groups/{groupId}/schedules")
    fun createSchedule(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable groupId: Long,
        @Valid @RequestBody request: CreateScheduleRequest
    ): ResponseEntity<ScheduleResponse> {
        val userId = userDetails.username.toLong()
        val schedule = scheduleService.createSchedule(
            userId = userId,
            groupId = groupId,
            title = request.title,
            description = request.description,
            startDate = request.startDate,
            endDate = request.endDate,
            isWeeklyRecurring = request.isWeeklyRecurring,
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(schedule)
    }

    @GetMapping("/schedules/reminders/weekly")
    fun getWeeklyReminders(
        @AuthenticationPrincipal userDetails: UserDetails,
    ): ResponseEntity<List<WeeklyReminderResponse>> {
        val userId = userDetails.username.toLong()
        return ResponseEntity.ok(scheduleService.getWeeklyReminders(userId))
    }

    @GetMapping("/schedules/reminders/pending")
    fun getPendingScheduleReminders(
        @AuthenticationPrincipal userDetails: UserDetails,
    ): ResponseEntity<List<PendingScheduleReminderResponse>> {
        val userId = userDetails.username.toLong()
        return ResponseEntity.ok(scheduleService.getPendingScheduleReminders(userId))
    }

    // 2. 그룹 내 일정 목록
    @GetMapping("/groups/{groupId}/schedules")
    fun getGroupSchedules(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable groupId: Long
    ): ResponseEntity<List<ScheduleResponse>> {
        val userId = userDetails.username.toLong()
        return ResponseEntity.ok(scheduleService.getGroupSchedules(userId, groupId))
    }

    // 2.4. 완료된 일정 삭제
    @DeleteMapping("/schedules/{id}")
    fun deleteSchedule(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable id: Long,
    ): ResponseEntity<Map<String, String>> {
        val userId = userDetails.username.toLong()
        return ResponseEntity.ok(scheduleService.deleteCompletedSchedule(userId, id))
    }

    // 2.5. 일정 상세 조회 (단건)
    @GetMapping("/schedules/{id}")
    fun getScheduleDetail(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable id: Long
    ): ResponseEntity<ScheduleResponse> {
        val userId = userDetails.username.toLong()
        return ResponseEntity.ok(scheduleService.getScheduleDetail(userId, id))
    }

    // 3. 내가 등록한 가능 시간 조회
    @GetMapping("/schedules/{id}/timeslots/mine")
    fun getMyTimeSlots(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable id: Long,
    ): ResponseEntity<List<TimeSlotDto>> {
        val userId = userDetails.username.toLong()
        return ResponseEntity.ok(scheduleService.getMyTimeSlots(userId, id))
    }

    // 4. 내 가능 시간 입력
    @PostMapping("/schedules/{id}/timeslots")
    fun addTimeSlots(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable id: Long,
        @RequestBody request: AddTimeSlotsRequest
    ): ResponseEntity<Map<String, Any>> {
        val userId = userDetails.username.toLong()
        return ResponseEntity.ok(scheduleService.addTimeSlots(userId, id, request.slots))
    }

    // 4. ⭐ 겹치는 시간 분석 결과 (히트맵 & 추천)
    @GetMapping("/schedules/{id}/analysis")
    fun analyzeSchedule(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable id: Long
    ): ResponseEntity<ScheduleAnalysisResponse> {
        val userId = userDetails.username.toLong()
        return ResponseEntity.ok(scheduleService.analyzeSchedule(userId, id))
    }

    // 5. 최종 시간 확정
    @PutMapping("/schedules/{id}/confirm")
    fun confirmSchedule(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable id: Long,
        @Valid @RequestBody request: ConfirmScheduleRequest
    ): ResponseEntity<Map<String, Any>> {
        val userId = userDetails.username.toLong()
        return ResponseEntity.ok(scheduleService.confirmSchedule(
            userId = userId, 
            scheduleId = id, 
            start = request.confirmedStart, 
            end = request.confirmedEnd
        ))
    }

    @GetMapping("/schedules/{id}/reactions")
    fun getReactions(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable id: Long
    ): ResponseEntity<List<ReactionDto>> {
        val userId = userDetails.username.toLong()
        return ResponseEntity.ok(scheduleReactionService.getReactions(userId, id))
    }

    @PutMapping("/schedules/{id}/reactions")
    fun upsertReaction(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable id: Long,
        @Valid @RequestBody request: UpsertReactionRequest
    ): ResponseEntity<ReactionDto> {
        val userId = userDetails.username.toLong()
        return ResponseEntity.ok(scheduleReactionService.upsertReaction(userId, id, request.emoji))
    }

    @DeleteMapping("/schedules/{id}/reactions")
    fun deleteReaction(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable id: Long
    ): ResponseEntity<Void> {
        val userId = userDetails.username.toLong()
        scheduleReactionService.deleteReaction(userId, id)
        return ResponseEntity.noContent().build()
    }
}
