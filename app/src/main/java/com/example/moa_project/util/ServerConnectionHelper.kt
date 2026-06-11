package com.example.moa_project.util

import android.util.Log
import com.example.moa_project.network.RetrofitClient
import com.example.moa_project.network.TokenManager
import com.example.moa_project.util.ServerUrlResolver

/**
 * API 호출 전 서버 연결 상태를 Logcat에 남긴다.
 * Logcat 필터: `MoaConnection`
 */
object ServerConnectionHelper {
    private const val TAG = "MoaConnection"

    data class Diagnosis(
        val serverUrl: String,
        val hasToken: Boolean,
        val healthOk: Boolean,
        val healthMessage: String?,
        val hint: String?,
    )

    suspend fun diagnose(): Diagnosis {
        val serverUrl = RetrofitClient.BASE_URL
        val hasToken = TokenManager.isLoggedIn()
        Log.i(TAG, "diagnose start | configured=${ServerUrlResolver.configuredUrl()} | resolved=$serverUrl | hasToken=$hasToken")

        return try {
            val health = RetrofitClient.instance.checkHealth()
            Log.i(TAG, "health OK | status=${health.status} | message=${health.message}")
            Diagnosis(
                serverUrl = serverUrl,
                hasToken = hasToken,
                healthOk = true,
                healthMessage = health.message,
                hint = null,
            )
        } catch (e: Exception) {
            MoaErrorLog.log("ServerConnectionHelper", "checkHealth", e)
            val hint = connectionHint(serverUrl)
            Log.e(TAG, "health FAIL | hint=$hint")
            Diagnosis(
                serverUrl = serverUrl,
                hasToken = hasToken,
                healthOk = false,
                healthMessage = e.message,
                hint = hint,
            )
        }
    }

    fun connectionErrorMessage(diagnosis: Diagnosis): String =
        "서버에 연결할 수 없습니다 (${diagnosis.serverUrl}).\n${diagnosis.hint ?: connectionHint()}"

    fun connectionHint(serverUrl: String = RetrofitClient.BASE_URL): String =
        ServerUrlResolver.connectionHint()
}
