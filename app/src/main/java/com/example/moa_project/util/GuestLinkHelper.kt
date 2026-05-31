package com.example.moa_project.util

import com.example.moa_project.BuildConfig

object GuestLinkHelper {
    /**
     * 서버가 내려준 webLink 우선, 없으면 WEB_SHARE_URL / SERVER_URL로 조합.
     * 10.0.2.2는 에뮬레이터 전용이라 외부 브라우저에서 열 수 없음.
     */
    fun resolveWebLink(uniqueLink: String, serverWebLink: String? = null): String {
        if (!serverWebLink.isNullOrBlank()) {
            return serverWebLink
        }
        val base = when {
            BuildConfig.WEB_SHARE_URL.isNotBlank() -> BuildConfig.WEB_SHARE_URL.trimEnd('/')
            !BuildConfig.SERVER_URL.contains("10.0.2.2") -> BuildConfig.SERVER_URL.trimEnd('/')
            else -> ""
        }
        return if (base.isNotBlank()) {
            "$base/guest.html?link=$uniqueLink"
        } else {
            "http://PC_IP:8080/guest.html?link=$uniqueLink"
        }
    }

    fun isExternalReachable(url: String): Boolean {
        val lower = url.lowercase()
        return !lower.contains("10.0.2.2") &&
            !lower.contains("localhost") &&
            !lower.contains("127.0.0.1")
    }
}
