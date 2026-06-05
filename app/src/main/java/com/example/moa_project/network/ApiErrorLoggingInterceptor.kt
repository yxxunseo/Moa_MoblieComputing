package com.example.moa_project.network

import android.util.Log
import com.example.moa_project.BuildConfig
import com.example.moa_project.util.MoaErrorLog
import okhttp3.Interceptor
import okhttp3.Response

/**
 * 실패한 API 요청/응답을 Logcat(MoaApi)에 기록한다.
 */
class ApiErrorLoggingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val started = System.currentTimeMillis()
        return try {
            val response = chain.proceed(request)
            val elapsed = System.currentTimeMillis() - started
            if (!response.isSuccessful) {
                val body = response.peekBody(8 * 1024).string()
                Log.e(
                    MoaErrorLog.API_TAG,
                    "FAIL ${request.method} ${request.url} | status=${response.code} | ${elapsed}ms | body=${body.take(2048)}",
                )
            } else if (BuildConfig.DEBUG) {
                Log.d(
                    MoaErrorLog.API_TAG,
                    "OK ${request.method} ${request.url} | status=${response.code} | ${elapsed}ms",
                )
            }
            response
        } catch (e: Exception) {
            val elapsed = System.currentTimeMillis() - started
            MoaErrorLog.log(
                component = "ApiErrorLoggingInterceptor",
                action = "${request.method} ${request.url}",
                throwable = e,
                extra = mapOf("elapsedMs" to elapsed.toString()),
            )
            throw e
        }
    }
}
