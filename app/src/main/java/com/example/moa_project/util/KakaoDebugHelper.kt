package com.example.moa_project.util

import android.content.Context
import android.util.Log
import com.example.moa_project.BuildConfig
import com.kakao.sdk.common.util.Utility

object KakaoDebugHelper {
    private const val TAG = "KakaoShareHelper"

    fun logShareContext(context: Context, webLink: String, imageUrl: String) {
        if (!BuildConfig.DEBUG) return
        Log.i(TAG, "share webLink=$webLink")
        Log.i(TAG, "share imageUrl=$imageUrl")
        Log.i(TAG, "share WEB_SHARE_URL=${BuildConfig.WEB_SHARE_URL}")
        runCatching {
            Log.i(TAG, "Android keyHash=${Utility.getKeyHash(context)}")
        }
    }
}
