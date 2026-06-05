package com.example.moa.service

import com.example.moa.controller.AuthResponse
import com.example.moa.controller.toResponse
import com.example.moa.entity.User
import com.example.moa.repository.UserRepository
import com.example.moa.security.JwtTokenProvider
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class OAuthService(
    private val userRepository: UserRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    @Value("\${google.client-id}") private val googleClientId: String,
    @Value("\${kakao.rest-api-key}") private val kakaoRestApiKey: String
) {
    // 구글 로그인 처리
    fun loginWithGoogle(idToken: String): AuthResponse {
        val verifier = GoogleIdTokenVerifier.Builder(NetHttpTransport(), GsonFactory.getDefaultInstance())
            .setAudience(listOf(googleClientId))
            .build()

        val googleIdToken: GoogleIdToken = verifier.verify(idToken)
            ?: throw IllegalArgumentException("유효하지 않은 구글 토큰입니다.")

        val payload = googleIdToken.payload
        val googleId = payload.subject
        val email = payload.email
        val name = payload["name"] as? String ?: "구글유저"
        val picture = payload["picture"] as? String

        val (user, isNew) = findOrCreateSocialUser(
            provider = "GOOGLE",
            providerId = googleId,
            email = email,
            nickname = name,
            profileImageUrl = picture
        )

        val token = jwtTokenProvider.generateToken(user.id)
        val refreshToken = jwtTokenProvider.generateRefreshToken(user.id)
        return AuthResponse(token = token, refreshToken = refreshToken, isNewUser = isNew, user = user.toResponse())
    }

    // 카카오 로그인 처리
    fun loginWithKakao(accessToken: String): AuthResponse {
        val webClient = WebClient.create("https://kapi.kakao.com")

        val kakaoUser = try {
            webClient.get()
                .uri("/v2/user/me")
                .header("Authorization", "Bearer $accessToken")
                .retrieve()
                .bodyToMono(Map::class.java)
                .block()
        } catch (_: Exception) {
            throw IllegalArgumentException("카카오 토큰이 유효하지 않습니다.")
        } ?: throw IllegalArgumentException("카카오 사용자 정보를 가져올 수 없습니다.")

        val kakaoId = kakaoUser["id"].toString()
        val kakaoAccount = kakaoUser["kakao_account"] as? Map<*, *>
        val profile = kakaoAccount?.get("profile") as? Map<*, *>
        val nickname = profile?.get("nickname") as? String ?: "카카오유저"
        val profileImage = profile?.get("profile_image_url") as? String
        val email = kakaoAccount?.get("email") as? String

        val (user, isNew) = findOrCreateSocialUser(
            provider = "KAKAO",
            providerId = kakaoId,
            email = email,
            nickname = nickname,
            profileImageUrl = profileImage
        )

        val token = jwtTokenProvider.generateToken(user.id)
        val refreshToken = jwtTokenProvider.generateRefreshToken(user.id)
        return AuthResponse(token = token, refreshToken = refreshToken, isNewUser = isNew, user = user.toResponse())
    }

    private fun findOrCreateSocialUser(
        provider: String,
        providerId: String,
        email: String?,
        nickname: String,
        profileImageUrl: String?
    ): Pair<User, Boolean> {
        val existing = userRepository.findByProviderAndProviderId(provider, providerId)
        if (existing != null) {
            return Pair(existing, false)
        }

        val newUser = userRepository.save(
            User(
                email = email,
                nickname = nickname,
                profileImageUrl = profileImageUrl,
                provider = provider,
                providerId = providerId
            )
        )
        return Pair(newUser, true)
    }
}
