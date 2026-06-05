package com.example.moa_project.util

import android.os.Build
import android.util.Log
import com.example.moa_project.BuildConfig

/**
 * local.properties SERVER_URL을 실행 환경(에뮬레이터/실기기)에 맞게 보정한다.
 * - 실기기 + ADB reverse: http://127.0.0.1:8080/
 * - 에뮬레이터: 127.0.0.1 → 10.0.2.2 자동 치환
 */
object ServerUrlResolver {
    private const val TAG = "MoaConnection"

    fun configuredUrl(): String = BuildConfig.SERVER_URL

    fun resolvedUrl(): String {
        var url = configuredUrl().trim()
        if (!url.endsWith("/")) url += "/"

        if (isProbablyEmulator() && isLocalHost(url)) {
            val resolved = url
                .replace("127.0.0.1", "10.0.2.2")
                .replace("localhost", "10.0.2.2")
            if (BuildConfig.DEBUG && resolved != url) {
                Log.i(TAG, "Emulator detected: $url -> $resolved")
            }
            return resolved
        }
        return url
    }

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
