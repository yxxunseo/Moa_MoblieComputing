package com.example.moa.controller

import com.example.moa.service.FixedTimeSlotService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

data class CreateFixedSlotRequest(
    val dayOfWeek: Int,
    val startHour: Int,
    val endHour: Int,
    val title: String
)

@RestController
@RequestMapping("/api/users/me/fixed-slots")
class FixedTimeSlotController(
    private val fixedTimeSlotService: FixedTimeSlotService
) {
    @GetMapping
    fun getMyFixedSlots(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<List<com.example.moa.service.FixedTimeSlotDto>> {
        val userId = userDetails.username.toLong()
        return ResponseEntity.ok(fixedTimeSlotService.getMyFixedSlots(userId))
    }

    @PostMapping
    fun addFixedSlot(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody request: CreateFixedSlotRequest
    ): ResponseEntity<com.example.moa.service.FixedTimeSlotDto> {
        val userId = userDetails.username.toLong()
        val slot = fixedTimeSlotService.addFixedSlot(
            userId = userId,
            dayOfWeek = request.dayOfWeek,
            startHour = request.startHour,
            endHour = request.endHour,
            title = request.title
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(slot)
    }

    @DeleteMapping("/{id}")
    fun deleteFixedSlot(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable id: Long
    ): ResponseEntity<Void> {
        val userId = userDetails.username.toLong()
        fixedTimeSlotService.deleteFixedSlot(userId, id)
        return ResponseEntity.noContent().build()
    }
}
