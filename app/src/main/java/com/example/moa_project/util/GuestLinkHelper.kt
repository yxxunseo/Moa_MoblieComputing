package com.example.moa_project.util

import com.example.moa_project.BuildConfig

object GuestLinkHelper {
    /**
     * 공유용 웹 링크 조합.
     * 1) local.properties WEB_SHARE_URL (ngrok·배포 URL) — 외부 공유용 최우선
     * 2) 서버 webLink — localhost/10.0.2.2가 아닐 때만
     * 3) SERVER_URL — 에뮬레이터 전용 IP가 아닐 때
     */
    fun resolveWebLink(uniqueLink: String, serverWebLink: String? = null): String {
        val appShareBase = BuildConfig.WEB_SHARE_URL.trim().trimEnd('/')
        if (appShareBase.isNotBlank() && isExternalReachable(appShareBase)) {
            return "$appShareBase/guest.html?link=$uniqueLink"
        }

        if (!serverWebLink.isNullOrBlank() && isExternalReachable(serverWebLink)) {
            return serverWebLink
        }

        val serverBase = BuildConfig.SERVER_URL.trim().trimEnd('/')
        if (serverBase.isNotBlank() && isExternalReachable(serverBase)) {
            return "$serverBase/guest.html?link=$uniqueLink"
        }

        if (!serverWebLink.isNullOrBlank()) {
            return serverWebLink
        }

        return if (serverBase.isNotBlank()) {
            "$serverBase/guest.html?link=$uniqueLink"
        } else {
            "http://YOUR_PUBLIC_URL/guest.html?link=$uniqueLink"
        }
    }

    /** localhost·에뮬레이터 IP가 아니면 외부(카톡 등) 공유 가능 */
    fun isExternalReachable(url: String): Boolean {
        val lower = url.lowercase()
        return !lower.contains("10.0.2.2") &&
            !lower.contains("localhost") &&
            !lower.contains("127.0.0.1") &&
            !lower.contains("your_public_url")
    }
}
