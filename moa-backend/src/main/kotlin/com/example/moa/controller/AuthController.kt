package com.example.moa.controller

import com.example.moa.entity.User
import com.example.moa.repository.UserRepository
import com.example.moa.security.JwtTokenProvider
import com.example.moa.service.OAuthService
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*

// ─── Request DTOs ──────────────────────────────────────────
data class SignupRequest(
    @field:Email val email: String,
    @field:NotBlank val password: String,
    @field:NotBlank val nickname: String
)

data class LoginRequest(
    @field:Email val email: String,
    @field:NotBlank val password: String
)

data class GoogleLoginRequest(val idToken: String)
data class KakaoLoginRequest(val accessToken: String)

// ─── Response DTOs ─────────────────────────────────────────
data class AuthResponse(
    val token: String,
    val isNewUser: Boolean = false,
    val user: UserResponse
)

data class UserResponse(
    val id: Long,
    val email: String?,
    val nickname: String,
    val provider: String,
    val profileImageUrl: String?
)

fun User.toResponse() = UserResponse(id, email, nickname, provider, profileImageUrl)

// ─── Controller ────────────────────────────────────────────
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val oAuthService: OAuthService
) {
    // 일반 회원가입
    @PostMapping("/signup")
    fun signup(@Valid @RequestBody request: SignupRequest): ResponseEntity<AuthResponse> {
        if (userRepository.existsByEmail(request.email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(null)
        }

        val user = userRepository.save(
            User(
                email = request.email,
                password = passwordEncoder.encode(request.password),
                nickname = request.nickname,
                provider = "LOCAL"
            )
        )

        val token = jwtTokenProvider.generateToken(user.id)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(AuthResponse(token = token, isNewUser = true, user = user.toResponse()))
    }

    // 일반 로그인
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
        val user = userRepository.findByEmail(request.email)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        if (user.password == null || !passwordEncoder.matches(request.password, user.password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }

        val token = jwtTokenProvider.generateToken(user.id)
        return ResponseEntity.ok(AuthResponse(token = token, user = user.toResponse()))
    }

    // 구글 로그인
    @PostMapping("/google")
    fun googleLogin(@RequestBody request: GoogleLoginRequest): ResponseEntity<AuthResponse> {
        val result = oAuthService.loginWithGoogle(request.idToken)
        return ResponseEntity.ok(result)
    }

    // 카카오 로그인
    @PostMapping("/kakao")
    fun kakaoLogin(@RequestBody request: KakaoLoginRequest): ResponseEntity<AuthResponse> {
        val result = oAuthService.loginWithKakao(request.accessToken)
        return ResponseEntity.ok(result)
    }
}

// 헬스 체크
@RestController
class HealthController {
    @GetMapping("/api/health")
    fun health() = mapOf("status" to "ok", "message" to "Moa 서버가 정상 작동 중입니다!")
}
