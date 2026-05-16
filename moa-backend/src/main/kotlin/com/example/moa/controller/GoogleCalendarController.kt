package com.example.moa.controller

import com.example.moa.service.GoogleCalendarService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

data class GoogleConnectRequest(
    val authCode: String
)

@RestController
@RequestMapping("/api/calendar/google")
class GoogleCalendarController(
    private val googleCalendarService: GoogleCalendarService
) {
    @PostMapping("/connect")
    fun connect(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody request: GoogleConnectRequest
    ): ResponseEntity<Map<String, Any>> {
        val userId = userDetails.username.toLong()
        return ResponseEntity.ok(googleCalendarService.connect(userId, request.authCode))
    }

    @DeleteMapping("/disconnect")
    fun disconnect(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<Void> {
        val userId = userDetails.username.toLong()
        googleCalendarService.disconnect(userId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/events")
    fun getEvents(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestParam month: String // format: "yyyy-MM"
    ): ResponseEntity<Map<String, Any>> {
        val userId = userDetails.username.toLong()
        val parts = month.split("-")
        return ResponseEntity.ok(googleCalendarService.getEvents(userId, parts[0].toInt(), parts[1].toInt()))
    }
}
