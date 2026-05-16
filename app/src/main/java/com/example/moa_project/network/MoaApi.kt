package com.example.moa_project.network

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body

data class HealthResponse(
    val status: String,
    val message: String
)

interface MoaApi {
    // 백엔드가 켜져 있는지 확인하는 헬스체크 API
    @GET("api/health")
    suspend fun checkHealth(): HealthResponse
}
