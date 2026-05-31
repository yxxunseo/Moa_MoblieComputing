package com.example.moa_project

import android.app.Application
import com.example.moa_project.network.TokenManager
import com.kakao.sdk.common.KakaoSdk

class MoaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        KakaoSdk.init(this, BuildConfig.KAKAO_APP_KEY)
        TokenManager.init(this)
        com.example.moa_project.util.MoaNotificationHelper.createChannel(this)
    }
}
