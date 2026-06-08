package com.example.moa_project.network

import android.content.Context
import android.content.SharedPreferences

/**
 * JWT 토큰을 SharedPreferences에 저장/로드/삭제하는 싱글톤 매니저
 * 앱 시작 시 Application.onCreate()에서 init()을 호출해야 함
 */
object TokenManager {
    private const val PREF_NAME = "moa_prefs"
    private const val KEY_TOKEN = "jwt_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_NICKNAME = "nickname"
    private const val KEY_PROFILE_IMAGE_URL = "profile_image_url"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString(KEY_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
    }

    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun saveUserInfo(userId: Long, nickname: String, profileImageUrl: String? = null) {
        prefs.edit()
            .putLong(KEY_USER_ID, userId)
            .putString(KEY_NICKNAME, nickname)
            .apply()
        saveProfileImageUrl(profileImageUrl)
    }

    fun saveProfileImageUrl(url: String?) {
        if (url.isNullOrBlank()) {
            prefs.edit().remove(KEY_PROFILE_IMAGE_URL).apply()
        } else {
            prefs.edit().putString(KEY_PROFILE_IMAGE_URL, url.trim()).apply()
        }
    }

    fun getProfileImageUrl(): String? = prefs.getString(KEY_PROFILE_IMAGE_URL, null)

    fun getUserId(): Long = prefs.getLong(KEY_USER_ID, -1L)
    fun getNickname(): String? = prefs.getString(KEY_NICKNAME, null)

    fun isLoggedIn(): Boolean = getToken() != null

    fun clear() {
        prefs.edit().clear().apply()
    }
}
