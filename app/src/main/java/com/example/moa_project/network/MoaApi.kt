package com.example.moa_project.network

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body

data class HealthResponse(
    val status: String,
    val message: String
)

interface MoaApi {
    // 백엔드가 켜져 있는지 확인하는 헬스체크 API
    @GET("api/health")
    suspend fun checkHealth(): HealthResponse

    // --- 일정 조율 (Schedule) API ---

    @GET("api/schedules/{id}")
    suspend fun getScheduleDetail(@retrofit2.http.Path("id") scheduleId: Long): ScheduleDetailResponse

    @POST("api/schedules/{id}/timeslots")
    suspend fun submitTimeSlots(
        @retrofit2.http.Path("id") scheduleId: Long, 
        @Body request: TimeSlotRequest
    ): TimeSlotResponse

    @GET("api/schedules/{id}/analysis")
    suspend fun getScheduleAnalysis(@retrofit2.http.Path("id") scheduleId: Long): ScheduleAnalysisResponse

    @retrofit2.http.PUT("api/schedules/{id}/confirm")
    suspend fun confirmSchedule(
        @retrofit2.http.Path("id") scheduleId: Long, 
        @Body request: ConfirmScheduleRequest
    ): ConfirmScheduleResponse

    @GET("api/schedules/{id}/reactions")
    suspend fun getScheduleReactions(@retrofit2.http.Path("id") scheduleId: Long): List<ReactionDto>

    @retrofit2.http.PUT("api/schedules/{id}/reactions")
    suspend fun upsertScheduleReaction(
        @retrofit2.http.Path("id") scheduleId: Long,
        @Body request: UpsertReactionRequest
    ): ReactionDto

    @retrofit2.http.DELETE("api/schedules/{id}/reactions")
    suspend fun deleteScheduleReaction(@retrofit2.http.Path("id") scheduleId: Long): retrofit2.Response<Void>

    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): AuthResponse

    // --- 비회원(Guest) 일정 조율 API ---
    @POST("api/guest-schedules")
    suspend fun createGuestSchedule(@Body request: CreateGuestScheduleRequest): GuestScheduleResponse

    @GET("api/guest-schedules/{link}")
    suspend fun getGuestScheduleByLink(@retrofit2.http.Path("link") link: String): GuestScheduleResponse

    @POST("api/guest-schedules/{link}/timeslots")
    suspend fun addGuestTimeSlots(
        @retrofit2.http.Path("link") link: String,
        @Body request: AddGuestTimeSlotsRequest
    ): GuestTimeSlotResponse

    @GET("api/guest-schedules/{link}/analysis")
    suspend fun getGuestScheduleAnalysis(@retrofit2.http.Path("link") link: String): GuestScheduleAnalysisResponse

    @retrofit2.http.PUT("api/guest-schedules/{link}/confirm")
    suspend fun confirmGuestSchedule(
        @retrofit2.http.Path("link") link: String,
        @Body request: ConfirmScheduleRequest
    ): ConfirmScheduleResponse

    @GET("api/guest-schedules/mine/list")
    suspend fun getMyGuestSchedules(): List<GuestScheduleResponse>

    @retrofit2.http.PUT("api/guest-schedules/{link}/complete")
    suspend fun completeGuestSchedule(
        @retrofit2.http.Path("link") link: String
    ): ConfirmScheduleResponse

    // --- 로그인 (Auth) API ---
    @POST("api/auth/login")
    suspend fun loginWithEmail(@Body request: EmailLoginRequest): AuthResponse

    @POST("api/auth/signup")
    suspend fun signup(@Body request: SignupRequest): AuthResponse

    @POST("api/auth/google")
    suspend fun loginWithGoogle(@Body request: GoogleLoginRequest): AuthResponse

    @POST("api/auth/kakao")
    suspend fun loginWithKakao(@Body request: KakaoLoginRequest): AuthResponse

    // --- 사용자 (User) API ---
    @GET("api/users/me")
    suspend fun getMyProfile(): UserResponse

    @retrofit2.http.PUT("api/users/me")
    suspend fun updateMyProfile(@Body request: UpdateProfileRequest): UserResponse

    @GET("api/users/me/groups")
    suspend fun getMyGroups(): List<GroupResponse>

    // --- 그룹 (Group) API ---
    @POST("api/groups")
    suspend fun createGroup(@Body request: CreateGroupRequest): GroupResponse

    @GET("api/groups/{id}")
    suspend fun getGroupDetail(@retrofit2.http.Path("id") groupId: Long): GroupResponse

    @POST("api/groups/join")
    suspend fun joinGroup(@Body request: JoinGroupRequest): Map<String, Any>

    @GET("api/groups/{id}/schedules")
    suspend fun getGroupSchedules(@retrofit2.http.Path("id") groupId: Long): List<ScheduleDetailResponse>

    @GET("api/groups/{id}/members")
    suspend fun getGroupMembers(@retrofit2.http.Path("id") groupId: Long): List<GroupMemberResponse>

    @POST("api/groups/{id}/schedules")
    suspend fun createGroupSchedule(
        @retrofit2.http.Path("id") groupId: Long,
        @Body request: CreateScheduleRequest
    ): ScheduleDetailResponse

    @retrofit2.http.DELETE("api/groups/{id}/leave")
    suspend fun leaveGroup(@retrofit2.http.Path("id") groupId: Long): retrofit2.Response<Void>

    @POST("api/schedules/{id}/timeslots")
    suspend fun submitGroupTimeSlots(
        @retrofit2.http.Path("id") scheduleId: Long,
        @Body request: TimeSlotRequest
    ): TimeSlotResponse

    // --- 캘린더 (Calendar) API ---
    @GET("api/calendar/events")
    suspend fun getMonthlyEvents(@retrofit2.http.Query("month") month: String): Map<String, Any>

    @POST("api/calendar/events")
    suspend fun addManualEvent(@Body request: AddEventRequest): CalendarEventDto

    @retrofit2.http.DELETE("api/calendar/events/{id}")
    suspend fun deleteEvent(@retrofit2.http.Path("id") eventId: Long): retrofit2.Response<Void>

    @retrofit2.http.PUT("api/calendar/events/{id}")
    suspend fun updateEvent(
        @retrofit2.http.Path("id") eventId: Long,
        @Body request: UpdateEventRequest
    ): CalendarEventDto

    // --- Google Calendar API ---
    @POST("api/calendar/google/connect")
    suspend fun connectGoogleCalendar(@Body request: GoogleConnectRequest): Map<String, Any>

    @retrofit2.http.DELETE("api/calendar/google/disconnect")
    suspend fun disconnectGoogleCalendar(): retrofit2.Response<Void>

    @GET("api/calendar/google/status")
    suspend fun getGoogleCalendarStatus(): Map<String, Any>

    @GET("api/calendar/google/events")
    suspend fun getGoogleCalendarEvents(@retrofit2.http.Query("month") month: String): Map<String, Any>

    @POST("api/calendar/google/sync")
    suspend fun syncGoogleCalendar(@Body request: GoogleSyncRequest): Map<String, Any>

    // --- 고정 시간표 API ---
    @GET("api/users/me/fixed-slots")
    suspend fun getFixedTimeSlots(): List<FixedTimeSlotDto>

    @POST("api/users/me/fixed-slots")
    suspend fun addFixedTimeSlot(@Body request: CreateFixedSlotRequest): FixedTimeSlotDto

    @retrofit2.http.DELETE("api/users/me/fixed-slots/{id}")
    suspend fun deleteFixedTimeSlot(@retrofit2.http.Path("id") slotId: Long): retrofit2.Response<Void>
}
