package com.example.moa_project.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // 안드로이드 에뮬레이터에서 로컬 호스트(PC) 서버에 접근할 때는 10.0.2.2를 사용합니다.
    // (실제 기기 테스트 시에는 PC의 IP 주소(예: 192.168.0.x)로 변경해야 합니다.)
    private const val BASE_URL = "http://10.0.2.2:8080/"

    val instance: MoaApi by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(MoaApi::class.java)
    }
}
