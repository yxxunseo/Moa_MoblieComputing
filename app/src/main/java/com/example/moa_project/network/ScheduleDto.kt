package com.example.moa_project.network

data class CreateScheduleRequest(
    val title: String,
    val description: String?,
    val startDate: String,
    val endDate: String
)

data class ScheduleDetailResponse(
    val id: Long,
    val title: String,
    val description: String?,
    val startDate: String,
    val endDate: String,
    val status: String
)

data class TimeSlotDto(
    val start: String,
    val end: String
)

data class TimeSlotRequest(
    val slots: List<TimeSlotDto>
)

data class TimeSlotResponse(
    val message: String,
    val respondedCount: Int,
    val totalMembers: Int
)

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
    val totalMembers: Int,
    val recommendations: List<RecommendationDto>,
    val heatmap: Map<String, Map<String, Int>>
)

data class ConfirmScheduleRequest(
    val confirmedStart: String,
    val confirmedEnd: String
)

data class ConfirmScheduleResponse(
    val message: String,
    val status: String,
    val calendarEventCreated: Boolean = false
)

// --- Guest Schedule DTOs ---

data class GuestScheduleResponse(
    val id: Long,
    val uniqueLink: String,
    val title: String,
    val description: String?,
    val startDate: String,
    val endDate: String,
    val status: String = "WAITING",
    val confirmedStart: String? = null,
    val confirmedEnd: String? = null,
    val webLink: String? = null
)

data class CreateGuestScheduleRequest(
    val title: String,
    val description: String?,
    val startDate: String,
    val endDate: String
)

data class AddGuestTimeSlotsRequest(
    val guestName: String,
    val slots: List<TimeSlotDto>
)

data class GuestTimeSlotResponse(
    val message: String
)

data class GuestScheduleAnalysisResponse(
    val scheduleId: Long,
    val title: String,
    val uniqueLink: String,
    val totalParticipants: Int,
    val recommendations: List<RecommendationDto>,
    val heatmap: Map<String, Map<String, Int>>
)
