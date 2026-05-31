package com.example.moa_project.network

data class AddEventRequest(
    val title: String,
    val start: String,
    val end: String,
    val color: String
)

data class UpdateEventRequest(
    val title: String,
    val start: String,
    val end: String,
    val color: String
)

data class CalendarEventDto(
    val id: Long,
    val title: String,
    val start: String,
    val end: String,
    val color: String
)

data class GoogleConnectRequest(val authCode: String)

data class GoogleSyncRequest(
    val title: String,
    val start: String,
    val end: String
)

data class ReactionDto(
    val emoji: String,
    val nickname: String,
    val userId: Long
)

data class UpsertReactionRequest(val emoji: String)

data class FixedTimeSlotDto(
    val id: Long,
    val dayOfWeek: Int,
    val startHour: Int,
    val endHour: Int,
    val title: String
)

data class CreateFixedSlotRequest(
    val dayOfWeek: Int,
    val startHour: Int,
    val endHour: Int,
    val title: String
)
