package com.example.moa_project.util

import android.os.Build
import android.util.Log
import com.example.moa_project.BuildConfig
import java.net.URI

/**
 * local.properties SERVER_URL을 실행 환경(에뮬레이터/실기기)에 맞게 보정한다.
 * - 에뮬레이터: 항상 10.0.2.2 (Mac localhost). LAN IP(172.x)는 에뮬에서 거절되는 경우가 많음.
 * - 실기기 + ADB reverse: 127.0.0.1 유지
 */
object ServerUrlResolver {
    private const val TAG = "MoaConnection"

    fun configuredUrl(): String = BuildConfig.SERVER_URL

    fun resolvedUrl(): String {
        var url = configuredUrl().trim()
        if (!url.endsWith("/")) url += "/"

        if (isProbablyEmulator()) {
            val port = extractPort(url)
            val resolved = "http://10.0.2.2:$port/"
            if (BuildConfig.DEBUG && resolved != url) {
                Log.i(TAG, "Emulator: $url -> $resolved")
            }
            return resolved
        }

        // 실기기에서 10.0.2.2(에뮬 전용)를 쓰면 EHOSTUNREACH 발생 → ADB reverse용 127.0.0.1로 보정
        if (url.contains("10.0.2.2")) {
            val port = extractPort(url)
            val resolved = "http://127.0.0.1:$port/"
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "Physical device: $url -> $resolved (run: adb reverse tcp:$port tcp:$port)")
            }
            return resolved
        }

        if (isLocalHost(url)) {
            return url
        }

        return url
    }

    /** 현재 기기·URL 조합에 맞는 연결 안내 문구 */
    fun connectionHint(): String = when {
        isProbablyEmulator() ->
            "Mac에서 ./scripts/dev-server.sh 실행 후 에뮬레이터에서 앱을 다시 실행해 주세요."
        configuredUrl().contains("10.0.2.2") || resolvedUrl().contains("127.0.0.1") ->
            "터미널에서 adb reverse tcp:8080 tcp:8080 실행 후, 백엔드(./scripts/dev-server.sh)가 켜져 있는지 확인해 주세요."
        resolvedUrl().contains("ngrok") ->
            "ngrok 터널이 살아 있는지 확인해 주세요."
        else ->
            "Mac과 폰이 같은 Wi-Fi인지, local.properties의 SERVER_URL IP가 Mac IP와 같은지 확인해 주세요."
    }

    private fun extractPort(url: String): Int =
        runCatching { URI(url).port }.getOrDefault(-1).let { if (it > 0) it else 8080 }

    fun isProbablyEmulator(): Boolean =
        Build.FINGERPRINT.startsWith("generic", ignoreCase = true) ||
            Build.FINGERPRINT.startsWith("unknown", ignoreCase = true) ||
            Build.HARDWARE.contains("goldfish", ignoreCase = true) ||
            Build.HARDWARE.contains("ranchu", ignoreCase = true) ||
            Build.HARDWARE.contains("virtio", ignoreCase = true) ||
            Build.MODEL.contains("Emulator", ignoreCase = true) ||
            Build.MODEL.contains("Android SDK built for x86", ignoreCase = true) ||
            Build.MODEL.contains("sdk_gphone", ignoreCase = true) ||
            Build.PRODUCT.contains("sdk", ignoreCase = true) ||
            Build.PRODUCT.contains("google_sdk", ignoreCase = true) ||
            Build.PRODUCT.contains("sdk_gphone", ignoreCase = true) ||
            Build.BRAND.startsWith("generic", ignoreCase = true)

    private fun isLocalHost(url: String): Boolean {
        val lower = url.lowercase()
        return lower.contains("127.0.0.1") || lower.contains("localhost")
    }
}
