package com.example.moa.service

import com.example.moa.repository.UserRepository
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId

@Service
class GoogleCalendarService(
    private val userRepository: UserRepository,
    private val flow: GoogleAuthorizationCodeFlow,
    @Value("\${google.redirect-uri}") private val redirectUri: String
) {
    @Transactional
    fun connect(userId: Long, authCode: String): Map<String, Any> {
        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }
        
        // 안드로이드에서 받은 authCode를 실제 Token으로 교환
        val response = flow.newTokenRequest(authCode).setRedirectUri(redirectUri).execute()
        
        user.googleAccessToken = response.accessToken
        user.googleRefreshToken = response.refreshToken
        
        return mapOf(
            "connected" to true,
            "message" to "구글 캘린더가 성공적으로 연동되었습니다!"
        )
    }

    @Transactional
    fun disconnect(userId: Long) {
        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }
        user.googleAccessToken = null
        user.googleRefreshToken = null
    }

    @Transactional(readOnly = true)
    fun getEvents(userId: Long, year: Int, month: Int): Map<String, Any> {
        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }
        
        if (user.googleAccessToken == null) {
            return mapOf("source" to "GOOGLE", "events" to emptyList<Any>())
        }
        
        // 캘린더 서비스 객체 생성
        val credential = Credential(com.google.api.client.auth.oauth2.BearerToken.authorizationHeaderAccessMethod()).setAccessToken(user.googleAccessToken)
        val calendarService = Calendar.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName("Moa Application").build()
        
        // 검색할 시간 범위 (해당 월의 1일부터 말일까지)
        val startDateTime = LocalDateTime.of(year, month, 1, 0, 0)
        val endDateTime = startDateTime.plusMonths(1).minusSeconds(1)
        
        val timeMin = DateTime(startDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
        val timeMax = DateTime(endDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
        
        // 구글 캘린더 API 호출
        val events = calendarService.events().list("primary")
            .setTimeMin(timeMin)
            .setTimeMax(timeMax)
            .setOrderBy("startTime")
            .setSingleEvents(true)
            .execute()
            
        val mappedEvents = events.items.map { event ->
            val start = event.start.dateTime ?: event.start.date
            val end = event.end.dateTime ?: event.end.date
            mapOf(
                "id" to event.id,
                "title" to (event.summary ?: "제목 없음"),
                "start" to start.toString(),
                "end" to end.toString(),
                "color" to "#4285F4", // 구글 기본 파란색
                "source" to "GOOGLE"
            )
        }
        
        return mapOf("source" to "GOOGLE", "events" to mappedEvents)
    }
}
