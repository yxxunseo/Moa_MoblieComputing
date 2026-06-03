package com.example.moa.controller

import com.example.moa.service.*
import jakarta.servlet.http.HttpServletRequest
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
    val slots: List<GuestTimeSlotDto>,
    val visitorId: String? = null,
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
    
    @GetMapping("/{link}")
    fun getScheduleByLink(@PathVariable link: String): ResponseEntity<GuestScheduleResponse> {
        return ResponseEntity.ok(guestScheduleService.getScheduleByLink(link))
    }
    
    @PostMapping("/{link}/timeslots")
    fun addGuestTimeSlots(
        @PathVariable link: String,
        @RequestBody request: AddGuestTimeSlotsRequest,
        @RequestHeader(value = "X-Moa-Visitor-Id", required = false) visitorHeader: String?,
        httpRequest: HttpServletRequest,
    ): ResponseEntity<Map<String, Any>> {
        val visitorId = request.visitorId ?: visitorHeader
        return ResponseEntity.ok(
            guestScheduleService.addGuestTimeSlots(
                uniqueLink = link,
                guestName = request.guestName,
                slots = request.slots,
                visitorId = visitorId,
                clientIp = resolveClientIp(httpRequest),
            )
        )
    }
    
    @GetMapping("/{link}/analysis")
    fun analyzeGuestSchedule(
        @PathVariable link: String,
        @RequestHeader(value = "X-Moa-Visitor-Id", required = false) visitorId: String?,
        httpRequest: HttpServletRequest,
    ): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.ok(
            guestScheduleService.analyzeGuestSchedule(
                uniqueLink = link,
                visitorId = visitorId,
                clientIp = resolveClientIp(httpRequest),
            )
        )
    }

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

    private fun resolveClientIp(request: HttpServletRequest): String? {
        val forwarded = request.getHeader("X-Forwarded-For")?.split(",")?.firstOrNull()?.trim()
        if (!forwarded.isNullOrBlank()) return forwarded
        return request.remoteAddr?.takeIf { it.isNotBlank() }
    }
}
