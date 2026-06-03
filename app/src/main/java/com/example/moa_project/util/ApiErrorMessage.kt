package com.example.moa_project.util

import retrofit2.HttpException

fun Throwable.userMessage(default: String): String {
    if (this is HttpException) {
        val body = response()?.errorBody()?.string()
        if (!body.isNullOrBlank()) {
            val fromJson = Regex(""""message"\s*:\s*"([^"]+)"""").find(body)?.groupValues?.getOrNull(1)
            if (!fromJson.isNullOrBlank()) return fromJson
            if (body.length <= 120) return body
        }
        return when (code()) {
            401 -> "로그인이 만료되었습니다. 다시 로그인해 주세요."
            403 -> "권한이 없습니다."
            else -> default
        }
    }
    return message?.takeIf { it.isNotBlank() } ?: default
}
