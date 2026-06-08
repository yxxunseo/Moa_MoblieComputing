package com.example.moa_project.network

import com.example.moa_project.util.MoaErrorLog
import com.example.moa_project.util.ServerUrlResolver
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object TokenRefresher {
    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .addInterceptor(NgrokInterceptor())
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    @Synchronized
    fun tryRefresh(): Boolean {
        val refreshToken = TokenManager.getRefreshToken() ?: return false
        return try {
            val body = gson.toJson(RefreshTokenRequest(refreshToken))
                .toRequestBody("application/json".toMediaType())
            // resolvedUrl()은 항상 "/"로 끝남. 기존엔 BuildConfig.SERVER_URL을 trimEnd 후
            // 슬래시 없이 이어붙여 "...8080api/auth/refresh" 같은 잘못된 URL을 만들었고,
            // 에뮬레이터 호스트 보정(10.0.2.2)도 적용되지 않아 토큰 갱신이 실패했음.
            val request = Request.Builder()
                .url("${ServerUrlResolver.resolvedUrl()}api/auth/refresh")
                .post(body)
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return false
                val parsed = gson.fromJson(response.body?.string(), AuthResponse::class.java)
                TokenManager.saveTokens(parsed.token, parsed.refreshToken)
                true
            }
        } catch (e: Exception) {
            MoaErrorLog.log("TokenRefresher", "tryRefresh", e)
            false
        }
    }
}

data class RefreshTokenRequest(val refreshToken: String)
