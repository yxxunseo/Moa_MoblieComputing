package com.example.moa_project.network

import android.util.Log
import com.example.moa_project.BuildConfig
import com.example.moa_project.util.ServerUrlResolver
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * JWT 토큰을 모든 요청의 Authorization 헤더에 자동으로 추가하는 인터셉터
 */
class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val request = chain.request()
        // 회원가입·로그인·중복 확인 등 공개 auth API에는 JWT를 붙이지 않음
        if (request.url.encodedPath.startsWith("/api/auth/")) {
            return chain.proceed(request)
        }
        val token = TokenManager.getToken()
        val authedRequest = if (token != null) {
            request.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            request
        }
        return chain.proceed(authedRequest)
    }
}

/**
 * 서버에서 401(토큰 만료/무효) 응답 시 저장된 JWT를 자동 삭제하는 인터셉터.
 * 다음 앱 실행 시 스플래시에서 로그인 화면으로 자동 이동됨.
 */
class TokenExpiredInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val request = chain.request()
        val response = chain.proceed(request)
        if (response.code == 401 && !request.url.encodedPath.contains("/api/auth/")) {
            if (TokenRefresher.tryRefresh()) {
                response.close()
                val token = TokenManager.getToken()
                val newRequest = request.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
                return chain.proceed(newRequest)
            }
            Log.w("TokenExpiredInterceptor", "401 Unauthorized – clearing expired token")
            TokenManager.clear()
        }
        return response
    }
}

/**
 * ngrok 무료 터널에서 API 요청이 차단되지 않도록 헤더 추가
 */
class NgrokInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val request = chain.request()
        val useNgrokHeader = ServerUrlResolver.resolvedUrl().contains("ngrok", ignoreCase = true) ||
            request.url.host.contains("ngrok", ignoreCase = true)
        val newRequest = if (useNgrokHeader) {
            request.newBuilder()
                .header("ngrok-skip-browser-warning", "true")
                .build()
        } else {
            request
        }
        return chain.proceed(newRequest)
    }
}

object RetrofitClient {
    val BASE_URL: String = ServerUrlResolver.resolvedUrl()

    init {
        if (BuildConfig.DEBUG) {
            Log.i("RetrofitClient", "configured=${ServerUrlResolver.configuredUrl()} resolved=$BASE_URL")
        }
    }

    val instance: MoaApi by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(NgrokInterceptor())
            .addInterceptor(ApiErrorLoggingInterceptor())
            .addInterceptor(AuthInterceptor())          // JWT 헤더 자동 주입
            .addInterceptor(TokenExpiredInterceptor())   // 401 시 토큰 자동 삭제
            .apply { if (BuildConfig.DEBUG) addInterceptor(logging) }  // 디버그 빌드에서만 로그
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(MoaApi::class.java)
    }
}
