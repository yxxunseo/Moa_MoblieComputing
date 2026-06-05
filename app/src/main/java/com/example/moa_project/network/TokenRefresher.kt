package com.example.moa_project.network

import com.example.moa_project.BuildConfig
import com.example.moa_project.util.MoaErrorLog
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

object TokenRefresher {
    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .addInterceptor(NgrokInterceptor())
        .build()

    @Synchronized
    fun tryRefresh(): Boolean {
        val refreshToken = TokenManager.getRefreshToken() ?: return false
        return try {
            val body = gson.toJson(RefreshTokenRequest(refreshToken))
                .toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("${BuildConfig.SERVER_URL.trimEnd('/')}api/auth/refresh")
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
