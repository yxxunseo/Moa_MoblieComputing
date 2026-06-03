package com.example.moa_project

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.util.DebugLogger
import com.example.moa_project.network.TokenManager
import com.kakao.sdk.common.KakaoSdk

class MoaApplication : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.KAKAO_APP_KEY.isNotBlank()) {
            KakaoSdk.init(this, BuildConfig.KAKAO_APP_KEY)
        }
        TokenManager.init(this)
        com.example.moa_project.util.MoaNotificationHelper.createChannel(this)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.15)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02)
                    .build()
            }
            .apply {
                if (BuildConfig.DEBUG) logger(DebugLogger())
            }
            .crossfade(true)
            .build()
    }
}
