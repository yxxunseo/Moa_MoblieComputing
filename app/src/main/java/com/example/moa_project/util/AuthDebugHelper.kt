package com.example.moa_project.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.example.moa_project.BuildConfig
import com.kakao.sdk.common.util.Utility
import java.security.MessageDigest

/**
 * 소셜 로그인 설정 확인용 진단 로그.
 * Android Studio Logcat 필터: `MoaAuth`
 */
object AuthDebugHelper {
    const val TAG = "MoaAuth"

    fun logStartupDiagnostics(context: Context) {
        if (!BuildConfig.DEBUG) return
        Log.i(TAG, "========== OAuth 진단 (앱 시작) ==========")
        Log.i(TAG, "패키지명: ${context.packageName}")
        Log.i(TAG, "Kakao Native Key: ${BuildConfig.KAKAO_APP_KEY.take(8)}…")
        Log.i(TAG, "Kakao keyHash: ${kakaoKeyHash(context)}")
        Log.i(TAG, "  → developers.kakao.com > 내 애플리케이션 > 플랫폼 > Android > 키 해시에 위 값 등록")
        Log.i(TAG, "Google Web Client ID: ${BuildConfig.GOOGLE_CLIENT_ID}")
        Log.i(TAG, "Google SHA-1: ${sha1Fingerprint(context)}")
        Log.i(TAG, "  → console.cloud.google.com > 사용자 인증 정보 > Android OAuth 클라이언트")
        Log.i(TAG, "  → 패키지명 ${context.packageName} + 위 SHA-1 등록 후, Web Client ID는 앱에 그대로 사용")
        Log.i(TAG, "==========================================")
    }

    fun kakaoKeyHash(context: Context): String =
        runCatching { Utility.getKeyHash(context) }.getOrNull() ?: "(조회 실패)"

    fun sha1Fingerprint(context: Context): String =
        signingCertDigests(context, "SHA-1")
            .firstOrNull()
            ?.chunked(2)
            ?.joinToString(":") { it.uppercase() }
            ?: "(조회 실패)"

    fun googleSetupHint(context: Context): String {
        val sha1 = sha1Fingerprint(context)
        return "Google Cloud Console에 Android OAuth 클라이언트를 추가하세요.\n" +
            "패키지: ${context.packageName}\n" +
            "SHA-1: $sha1\n" +
            "(Logcat 태그 MoaAuth 에서도 확인 가능)"
    }

    fun kakaoSetupHint(context: Context): String {
        val hash = kakaoKeyHash(context)
        return "카카오 개발자 콘솔 > 플랫폼 > Android에 키 해시를 등록하세요.\n" +
            "패키지: ${context.packageName}\n" +
            "키 해시: $hash\n" +
            "(Logcat 태그 MoaAuth 에서도 확인 가능)"
    }

    private fun signingCertDigests(context: Context, algorithm: String): List<String> {
        return try {
            val pm = context.packageManager
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                PackageManager.GET_SIGNING_CERTIFICATES
            } else {
                @Suppress("DEPRECATION")
                PackageManager.GET_SIGNATURES
            }
            val packageInfo = pm.getPackageInfo(context.packageName, flags)
            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
            } ?: return emptyList()

            signatures.map { signature ->
                val md = MessageDigest.getInstance(algorithm)
                md.digest(signature.toByteArray()).joinToString("") { "%02x".format(it) }
            }
        } catch (e: Exception) {
            Log.w(TAG, "signingCertDigests failed: ${e.message}")
            emptyList()
        }
    }
}
