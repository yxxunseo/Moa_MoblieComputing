package com.example.moa_project.network

data class GoogleLoginRequest(val idToken: String)

data class KakaoLoginRequest(
    val accessToken: String,
    val nickname: String? = null,
    val profileImageUrl: String? = null,
)

data class EmailLoginRequest(val loginId: String, val password: String)

data class SignupRequest(val loginId: String, val email: String, val password: String, val nickname: String)

data class UpdateProfileRequest(val nickname: String, val profileImageUrl: String? = null)

data class UserResponse(
    val id: Long,
    val loginId: String?,
    val email: String?,
    val nickname: String,
    val provider: String,
    val profileImageUrl: String?
)

data class AuthResponse(
    val token: String,
    val refreshToken: String = "",
    val isNewUser: Boolean = false,
    val user: UserResponse
)

data class AvailabilityResponse(val available: Boolean)
