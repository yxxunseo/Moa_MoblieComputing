package com.example.moa_project.util

import com.example.moa_project.BuildConfig
import java.net.URI

/**
 * 서버가 localhost 등 잘못된 호스트로 이미지 URL을 내려줘도
 * 앱의 SERVER_URL 기준으로 다시 붙여서 Coil이 로드할 수 있게 한다.
 */
object ImageUrlHelper {
    fun resolve(url: String?): String? {
        if (url.isNullOrBlank()) return null
        val trimmed = url.trim()
        val serverBase = ServerUrlResolver.resolvedUrl().trimEnd('/')

        if (trimmed.startsWith("/")) {
            return serverBase + trimmed
        }

        val serverUri = runCatching { URI(serverBase) }.getOrNull()
        val parsed = runCatching { URI(trimmed) }.getOrNull()

        if (parsed != null && serverUri != null) {
            val host = parsed.host?.lowercase().orEmpty()
            if (host == "localhost" || host == "127.0.0.1" || host == "10.0.2.2") {
                val path = parsed.rawPath.orEmpty()
                val query = parsed.rawQuery?.let { "?$it" }.orEmpty()
                return serverBase + path + query
            }
        }

        return trimmed
    }
}
