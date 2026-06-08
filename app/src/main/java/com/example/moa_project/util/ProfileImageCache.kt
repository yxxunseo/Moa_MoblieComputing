package com.example.moa_project.util

import android.content.Context
import android.net.Uri
import com.example.moa_project.network.TokenManager
import java.io.File

/**
 * 프로필 사진을 기기에 캐시해, 서버 이미지 URL 로드 실패 시에도 마이페이지 등에서 유지되게 한다.
 */
object ProfileImageCache {
    private const val FILE_NAME = "profile_avatar.jpg"

    fun saveFromFile(context: Context, source: File) {
        val dest = localFile(context) ?: File(context.filesDir, FILE_NAME)
        source.inputStream().use { input ->
            dest.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    fun localFile(context: Context): File? {
        val file = File(context.filesDir, FILE_NAME)
        return file.takeIf { it.exists() && it.length() > 0L }
    }

    fun clear(context: Context) {
        File(context.filesDir, FILE_NAME).delete()
    }

    /** 미리보기 URI → 로컬 캐시 → 서버 URL 순으로 Coil 모델 결정 */
    fun resolveModel(context: Context, serverUrl: String?, previewUri: Uri? = null): Any? {
        previewUri?.let { return it }
        localFile(context)?.let { return it }
        ImageUrlHelper.resolve(serverUrl)?.let { return it }
        return ImageUrlHelper.resolve(TokenManager.getProfileImageUrl())
    }
}
