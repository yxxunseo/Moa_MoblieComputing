package com.example.moa_project.util

import android.util.Log
import com.example.moa_project.network.RetrofitClient
import com.google.gson.JsonParseException
import com.google.gson.JsonSyntaxException
import org.json.JSONException
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * API/기능 실패 시 Logcat에 원인을 명확히 남긴다.
 * Logcat 필터: `MoaError` 또는 `MoaApi`
 */
object MoaErrorLog {
    const val TAG = "MoaError"
    const val API_TAG = "MoaApi"

    fun log(component: String, action: String, throwable: Throwable, extra: Map<String, String> = emptyMap()) {
        val http = findCause<HttpException>(throwable)
        val errorBody = http?.let { ex ->
            runCatching { ex.response()?.errorBody()?.string()?.take(2048) }.getOrNull()
        }
        val detail = buildString {
            append("component=").append(component)
            append(" | action=").append(action)
            append(" | SERVER_URL=").append(RetrofitClient.BASE_URL)
            extra.forEach { (k, v) -> append(" | ").append(k).append('=').append(v) }
            append(" | type=").append(throwable.javaClass.name)
            append(" | message=").append(throwable.message ?: "(none)")
            if (http != null) {
                append(" | httpCode=").append(http.code())
                append(" | httpUrl=").append(http.response()?.raw()?.request?.url)
                if (!errorBody.isNullOrBlank()) append(" | errorBody=").append(errorBody)
            }
            append(" | causeChain=").append(formatCauseChain(throwable))
        }
        Log.e(TAG, detail, throwable)
    }

    fun log(component: String, action: String, message: String) {
        Log.e(
            TAG,
            "component=$component | action=$action | SERVER_URL=${RetrofitClient.BASE_URL} | $message",
        )
    }

    fun userMessage(throwable: Throwable, fallback: String = "요청 처리에 실패했습니다."): String {
        val root = generateSequence(throwable) { it.cause }.last()
        val url = RetrofitClient.BASE_URL
        val rootMessage = root.message.orEmpty()
        if (isNetworkFailureMessage(rootMessage)) {
            return networkFailureMessage(url)
        }
        return when (root) {
            is UnknownHostException,
            is ConnectException,
            is SocketTimeoutException,
            is IOException -> networkFailureMessage(url)
            is HttpException -> {
                val serverMsg = extractServerMessage(root)
                when (root.code()) {
                401 -> serverMsg ?: "인증이 필요합니다. 다시 로그인해주세요."
                403 -> serverMsg ?: "접근 권한이 없습니다."
                404 -> "요청한 리소스를 찾을 수 없습니다."
                in 500..599 -> serverMsg ?: "서버 오류 (${root.code()})"
                else -> serverMsg ?: "서버 오류 (${root.code()})"
                }
            }
            is JsonSyntaxException, is JsonParseException ->
                "서버 응답 형식 오류. SERVER_URL($url) 확인"
            is JSONException ->
                "저장된 알림 데이터를 읽지 못했습니다. 다시 시도해주세요."
            else -> root.message?.takeIf { it.isNotBlank() } ?: fallback
        }
    }

    private fun extractServerMessage(http: HttpException): String? =
        runCatching {
            http.response()?.errorBody()?.string()?.let { body ->
                org.json.JSONObject(body).optString("message").takeIf { it.isNotBlank() }
            }
        }.getOrNull()

    private fun isNetworkFailureMessage(message: String): Boolean {
        val lower = message.lowercase()
        return lower.contains("ehostunreach") ||
            lower.contains("enetunreach") ||
            lower.contains("no route to host") ||
            lower.contains("failed to connect") ||
            lower.contains("isconnected failed") ||
            lower.contains("connection refused") ||
            lower.contains("network is unreachable")
    }

    private fun networkFailureMessage(url: String): String {
        val hint = ServerUrlResolver.connectionHint()
        return "서버에 연결할 수 없습니다 ($url).\n$hint"
    }

    private fun formatCauseChain(t: Throwable): String =
        generateSequence(t) { it.cause }
            .mapIndexed { i, c -> "#$i ${c.javaClass.simpleName}: ${c.message}" }
            .joinToString(" <- ")

    private inline fun <reified T : Throwable> findCause(throwable: Throwable): T? =
        generateSequence(throwable) { it.cause }.filterIsInstance<T>().firstOrNull()
}
