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
    @field:NotBlank val loginId: String,
    @field:Email val email: String,
    @field:NotBlank val password: String,
    @field:NotBlank val nickname: String
)

data class LoginRequest(
    @field:NotBlank val loginId: String,
    @field:NotBlank val password: String
)

data class GoogleLoginRequest(val idToken: String)
data class KakaoLoginRequest(val accessToken: String)

data class RefreshRequest(val refreshToken: String)

// ─── Response DTOs ─────────────────────────────────────────
data class AuthResponse(
    val token: String,
    val refreshToken: String,
    val isNewUser: Boolean = false,
    val user: UserResponse
)

data class UserResponse(
    val id: Long,
    val loginId: String?,
    val email: String?,
    val nickname: String,
    val provider: String,
    val profileImageUrl: String?
)

fun User.toResponse() = UserResponse(id, loginId, email, nickname, provider, profileImageUrl)

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
        if (userRepository.existsByLoginId(request.loginId) || userRepository.existsByEmail(request.email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(null)
        }

        val user = userRepository.save(
            User(
                loginId = request.loginId,
                email = request.email,
                password = passwordEncoder.encode(request.password),
                nickname = request.nickname,
                provider = "LOCAL"
            )
        )

        val token = jwtTokenProvider.generateToken(user.id)
        val refreshToken = jwtTokenProvider.generateRefreshToken(user.id)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(AuthResponse(token = token, refreshToken = refreshToken, isNewUser = true, user = user.toResponse()))
    }

    // 일반 로그인
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
        val user = userRepository.findByLoginId(request.loginId)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        if (user.password == null || !passwordEncoder.matches(request.password, user.password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }

        val token = jwtTokenProvider.generateToken(user.id)
        val refreshToken = jwtTokenProvider.generateRefreshToken(user.id)
        return ResponseEntity.ok(AuthResponse(token = token, refreshToken = refreshToken, user = user.toResponse()))
    }

    // 구글 로그인
    @PostMapping("/google")
    fun googleLogin(@RequestBody request: GoogleLoginRequest): ResponseEntity<Any> {
        return try {
            ResponseEntity.ok(oAuthService.loginWithGoogle(request.idToken))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("message" to (e.message ?: "구글 인증 실패")))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(mapOf("message" to "구글 로그인 처리 중 오류가 발생했습니다."))
        }
    }

    // 카카오 로그인
    @PostMapping("/kakao")
    fun kakaoLogin(@RequestBody request: KakaoLoginRequest): ResponseEntity<Any> {
        return try {
            ResponseEntity.ok(oAuthService.loginWithKakao(request.accessToken))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("message" to (e.message ?: "카카오 인증 실패")))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(mapOf("message" to "카카오 로그인 처리 중 오류가 발생했습니다."))
        }
    }

    // JWT 갱신
    @PostMapping("/refresh")
    fun refresh(@RequestBody request: RefreshRequest): ResponseEntity<AuthResponse> {
        if (!jwtTokenProvider.validateToken(request.refreshToken) ||
            !jwtTokenProvider.isRefreshToken(request.refreshToken)
        ) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }

        val userId = jwtTokenProvider.getUserIdFromToken(request.refreshToken)
        val user = userRepository.findById(userId).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val token = jwtTokenProvider.generateToken(user.id)
        val refreshToken = jwtTokenProvider.generateRefreshToken(user.id)
        return ResponseEntity.ok(AuthResponse(token = token, refreshToken = refreshToken, user = user.toResponse()))
    }
}

// 헬스 체크
@RestController
class HealthController {
    @GetMapping("/api/health")
    fun health() = mapOf("status" to "ok", "message" to "Moa 서버가 정상 작동 중입니다!")
}
