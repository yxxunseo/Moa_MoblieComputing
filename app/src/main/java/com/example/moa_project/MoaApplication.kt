package com.example.moa_project

import android.app.Application
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.util.DebugLogger
import com.example.moa_project.network.TokenManager
import com.example.moa_project.util.ServerConnectionHelper
import com.example.moa_project.util.ServerUrlResolver
import com.example.moa_project.util.NewScheduleReminderWorker
import com.example.moa_project.util.WeeklyReminderWorker
import com.example.moa_project.util.AuthDebugHelper
import com.kakao.sdk.common.KakaoSdk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MoaApplication : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.KAKAO_APP_KEY.isNotBlank()) {
            KakaoSdk.init(this, BuildConfig.KAKAO_APP_KEY)
            if (BuildConfig.DEBUG) {
                Log.i("MoaApp", "Kakao redirectUri=kakao${BuildConfig.KAKAO_APP_KEY}://oauth")
            }
        } else if (BuildConfig.DEBUG) {
            Log.w("MoaApp", "KAKAO_APP_KEY is blank — set it in local.properties and rebuild")
        }
        TokenManager.init(this)
        com.example.moa_project.util.MoaNotificationHelper.createChannel(this)
        WeeklyReminderWorker.schedule(this)
        NewScheduleReminderWorker.schedule(this)
        if (BuildConfig.DEBUG) {
            AuthDebugHelper.logStartupDiagnostics(this)
            Log.i("MoaApp", "SERVER configured=${ServerUrlResolver.configuredUrl()} resolved=${ServerUrlResolver.resolvedUrl()}")
            CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                ServerConnectionHelper.diagnose()
            }
        }
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
