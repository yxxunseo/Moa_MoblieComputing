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
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Service
class OAuthService(
    private val userRepository: UserRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    @Value("\${google.client-id}") private val googleClientId: String,
    @Value("\${kakao.rest-api-key}") private val kakaoRestApiKey: String
) {
    private val log = LoggerFactory.getLogger(OAuthService::class.java)

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
    fun loginWithKakao(
        accessToken: String,
        clientNickname: String? = null,
        clientProfileImageUrl: String? = null,
    ): AuthResponse {
        val kakaoAccessToken = accessToken.trim()
        require(kakaoAccessToken.isNotBlank()) { "카카오 액세스 토큰이 비어 있습니다." }

        val kakaoUser = fetchKakaoUserProfile(kakaoAccessToken)
            ?: throw IllegalArgumentException("카카오 사용자 정보를 가져올 수 없습니다.")

        val kakaoId = kakaoUser["id"].toString()
        val kakaoAccount = kakaoUser["kakao_account"] as? Map<*, *>
        val profile = kakaoAccount?.get("profile") as? Map<*, *>
        val nickname = resolveKakaoNickname(
            apiNickname = extractKakaoNickname(kakaoUser, kakaoAccount, profile),
            clientNickname = clientNickname,
        )
        val profileImage = clientProfileImageUrl?.trim()?.takeIf { it.isNotBlank() }
            ?: profile?.get("profile_image_url") as? String
            ?: profile?.get("thumbnail_image_url") as? String
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

    private fun fetchKakaoUserProfile(accessToken: String): Map<*, *>? {
        val webClient = WebClient.create("https://kapi.kakao.com")
        val propertyKeys = URLEncoder.encode(
            "[\"kakao_account.profile\",\"kakao_account.email\",\"properties\"]",
            StandardCharsets.UTF_8,
        )
        val paths = listOf("/v2/user/me", "/v2/user/me?property_keys=$propertyKeys")

        for (path in paths) {
            try {
                val body = webClient.get()
                    .uri(path)
                    .header("Authorization", "Bearer $accessToken")
                    .retrieve()
                    .bodyToMono(Map::class.java)
                    .block()
                if (body != null) {
                    log.info("Kakao /v2/user/me OK | path={}", path.substringBefore("?"))
                    return body
                }
            } catch (e: WebClientResponseException) {
                log.warn(
                    "Kakao {} failed | status={} | body={}",
                    path,
                    e.statusCode.value(),
                    e.responseBodyAsString.take(512),
                )
            } catch (e: Exception) {
                log.error("Kakao {} request failed", path, e)
            }
        }
        throw IllegalArgumentException(
            "카카오 토큰이 유효하지 않습니다. 실기기 키 해시가 카카오 개발자 콘솔에 등록됐는지 확인해 주세요.",
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun extractKakaoNickname(
        kakaoUser: Map<*, *>,
        kakaoAccount: Map<*, *>?,
        profile: Map<*, *>?,
    ): String {
        (profile?.get("nickname") as? String)?.trim()?.takeIf { it.isNotBlank() }?.let { return it }

        val properties = kakaoUser["properties"] as? Map<*, *>
        (properties?.get("nickname") as? String)?.trim()?.takeIf { it.isNotBlank() }?.let { return it }

        (kakaoAccount?.get("name") as? String)?.trim()?.takeIf { it.isNotBlank() }?.let { return it }

        return "카카오유저"
    }

    private fun resolveKakaoNickname(apiNickname: String, clientNickname: String?): String {
        val fromClient = clientNickname?.trim()?.takeIf { it.isNotBlank() }
        if (apiNickname != "카카오유저") return apiNickname
        return fromClient ?: apiNickname
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
            var changed = false
            val shouldUpdateNickname = nickname.isNotBlank() && nickname != "카카오유저" &&
                (existing.nickname == "카카오유저" || existing.nickname != nickname)
            if (shouldUpdateNickname) {
                existing.nickname = nickname
                changed = true
            }
            if (!profileImageUrl.isNullOrBlank() && existing.profileImageUrl != profileImageUrl) {
                existing.profileImageUrl = profileImageUrl
                changed = true
            }
            if (!email.isNullOrBlank() && existing.email != email) {
                existing.email = email
                changed = true
            }
            if (changed) {
                userRepository.save(existing)
            }
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
