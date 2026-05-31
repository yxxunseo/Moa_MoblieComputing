package com.example.moa.controller

import com.example.moa.service.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

data class CreateGuestScheduleRequest(
    val title: String,
    val description: String? = null,
    val startDate: LocalDate,
    val endDate: LocalDate
)

data class AddGuestTimeSlotsRequest(
    val guestName: String,
    val slots: List<GuestTimeSlotDto>
)

data class ConfirmGuestScheduleRequest(
    val confirmedStart: String,
    val confirmedEnd: String
)

@RestController
@RequestMapping("/api/guest-schedules")
class GuestScheduleController(
    private val guestScheduleService: GuestScheduleService
) {
    // 1. 일회성 일정 만들기 (로그인한 주최자 전용)
    @PostMapping
    fun createGuestSchedule(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody request: CreateGuestScheduleRequest
    ): ResponseEntity<GuestScheduleResponse> {
        val userId = userDetails.username.toLong()
        val schedule = guestScheduleService.createGuestSchedule(
            userId = userId,
            title = request.title,
            description = request.description,
            startDate = request.startDate,
            endDate = request.endDate
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(schedule)
    }

    @GetMapping("/mine/list")
    fun getMyGuestSchedules(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<List<GuestScheduleResponse>> {
        val userId = userDetails.username.toLong()
        return ResponseEntity.ok(guestScheduleService.getMyGuestSchedules(userId))
    }
    
    // 2. 링크로 일정 정보 확인 (비회원/링크 받은 사람 누구나 가능)
    @GetMapping("/{link}")
    fun getScheduleByLink(@PathVariable link: String): ResponseEntity<GuestScheduleResponse> {
        return ResponseEntity.ok(guestScheduleService.getScheduleByLink(link))
    }
    
    // 3. 이름 입력하고 내 가능 시간 제출하기 (비회원 누구나 가능)
    @PostMapping("/{link}/timeslots")
    fun addGuestTimeSlots(
        @PathVariable link: String,
        @RequestBody request: AddGuestTimeSlotsRequest
    ): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.ok(guestScheduleService.addGuestTimeSlots(link, request.guestName, request.slots))
    }
    
    // 4. 익명 일정 분석 (어디에 사람이 제일 많이 겹치는지 히트맵 확인)
    @GetMapping("/{link}/analysis")
    fun analyzeGuestSchedule(@PathVariable link: String): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.ok(guestScheduleService.analyzeGuestSchedule(link))
    }

    // 5. 주최자가 최종 시간 확정 (로그인 필요)
    @PutMapping("/{link}/confirm")
    fun confirmGuestSchedule(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable link: String,
        @RequestBody request: ConfirmGuestScheduleRequest
    ): ResponseEntity<Map<String, Any>> {
        val userId = userDetails.username.toLong()
        return ResponseEntity.ok(
            guestScheduleService.confirmGuestSchedule(userId, link, request.confirmedStart, request.confirmedEnd)
        )
    }

    @PutMapping("/{link}/complete")
    fun completeGuestSchedule(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable link: String
    ): ResponseEntity<Map<String, Any>> {
        val userId = userDetails.username.toLong()
        return ResponseEntity.ok(guestScheduleService.completeGuestSchedule(userId, link))
    }
}
