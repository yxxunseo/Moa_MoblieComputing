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

    fun connectionHint(serverUrl: String = RetrofitClient.BASE_URL): String = when {
        serverUrl.contains("127.0.0.1") || serverUrl.contains("localhost") ->
            "백엔드 실행 후 터미널에서 adb reverse tcp:8080 tcp:8080 실행 (실기기 USB/무선 디버깅)"
        serverUrl.contains("10.0.2.2") ->
            "백엔드가 Mac에서 ./gradlew :moa-backend:bootRun 으로 실행 중인지 확인 (에뮬레이터)"
        serverUrl.contains("ngrok") ->
            "ngrok 터널이 살아 있는지 확인. 차단되면 ADB 방식(127.0.0.1 + adb reverse) 사용"
        else ->
            "Mac과 폰이 같은 Wi-Fi인지, SERVER_URL IP가 맞는지 확인"
    }
}
