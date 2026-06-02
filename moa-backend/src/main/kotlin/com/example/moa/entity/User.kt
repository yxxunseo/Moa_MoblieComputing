package com.example.moa.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    // 일반 로그인 아이디 (LOCAL 가입 시 사용)
    @Column(name = "login_id", unique = true, nullable = true)
    var loginId: String? = null,

    @Column(unique = true, nullable = true)
    var email: String? = null,

    @Column(nullable = true)
    var password: String? = null,

    @Column(nullable = false)
    var nickname: String = "",

    @Column(name = "profile_image_url")
    var profileImageUrl: String? = null,

    // 소셜 로그인 구분 (LOCAL / GOOGLE / KAKAO)
    @Column(nullable = false)
    var provider: String = "LOCAL",

    // 소셜 로그인 고유 ID
    @Column(name = "provider_id")
    var providerId: String? = null,

    // 구글 캘린더 연동 토큰
    @Column(name = "google_access_token", columnDefinition = "TEXT")
    var googleAccessToken: String? = null,

    @Column(name = "google_refresh_token", columnDefinition = "TEXT")
    var googleRefreshToken: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
