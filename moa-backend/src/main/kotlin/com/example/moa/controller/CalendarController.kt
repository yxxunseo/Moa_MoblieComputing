package com.example.moa.controller

import com.example.moa.service.CalendarEventDto
import com.example.moa.service.CalendarService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

data class AddEventRequest(
    val title: String,
    val start: String,
    val end: String,
    val color: String
)

@RestController
@RequestMapping("/api/calendar")
class CalendarController(
    private val calendarService: CalendarService
) {
    @GetMapping("/events")
    fun getMonthlyEvents(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestParam month: String // format: "yyyy-MM"
    ): ResponseEntity<Map<String, Any>> {
        val userId = userDetails.username.toLong()
        val parts = month.split("-")
        return ResponseEntity.ok(calendarService.getMonthlyEvents(userId, parts[0].toInt(), parts[1].toInt()))
    }

    @PostMapping("/events")
    fun addManualEvent(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody request: AddEventRequest
    ): ResponseEntity<CalendarEventDto> {
        val userId = userDetails.username.toLong()
        return ResponseEntity.ok(calendarService.addManualEvent(
            userId = userId, 
            title = request.title, 
            start = request.start, 
            end = request.end, 
            color = request.color
        ))
    }
    
    @DeleteMapping("/events/{id}")
    fun deleteEvent(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable id: Long
    ): ResponseEntity<Void> {
        val userId = userDetails.username.toLong()
        calendarService.deleteEvent(userId, id)
        return ResponseEntity.noContent().build()
    }
}
