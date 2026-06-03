package com.example.moa.service

import com.example.moa.entity.User
import com.example.moa.repository.UserRepository
import com.google.api.client.auth.oauth2.ClientParametersAuthentication
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
    @Value("\${google.redirect-uri}") private val webRedirectUri: String,
    @Value("\${google.calendar-redirect-uri:}") private val calendarRedirectUri: String,
    @Value("\${google.client-id}") private val clientId: String,
    @Value("\${google.client-secret}") private val clientSecret: String
) {
    private val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
    private val jsonFactory = GsonFactory.getDefaultInstance()

    @Transactional
    fun connect(userId: Long, authCode: String): Map<String, Any> {
        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }

        val redirectCandidates = listOf(
            calendarRedirectUri,
            "",
            webRedirectUri
        ).distinct()

        var lastError: Exception? = null
        for (redirect in redirectCandidates) {
            try {
                val request = flow.newTokenRequest(authCode)
                if (redirect.isNotEmpty()) {
                    request.setRedirectUri(redirect)
                }
                val response = request.execute()
                user.googleAccessToken = response.accessToken
                if (!response.refreshToken.isNullOrBlank()) {
                    user.googleRefreshToken = response.refreshToken
                }
                return mapOf(
                    "connected" to true,
                    "message" to "구글 캘린더가 성공적으로 연동되었습니다!"
                )
            } catch (e: Exception) {
                lastError = e
            }
        }
        throw IllegalArgumentException(
            "구글 캘린더 연동에 실패했습니다. Google Cloud에서 Web OAuth 클라이언트 ID를 local.properties GOOGLE_CLIENT_ID에 설정했는지 확인해 주세요. (${lastError?.message ?: "unknown"})"
        )
    }

    @Transactional
    fun disconnect(userId: Long) {
        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }
        user.googleAccessToken = null
        user.googleRefreshToken = null
    }

    @Transactional
    fun getEvents(userId: Long, year: Int, month: Int): Map<String, Any> {
        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }

        if (user.googleAccessToken == null) {
            return mapOf("source" to "GOOGLE", "events" to emptyList<Any>())
        }

        val calendarService = buildCalendarService(user)
        val startDateTime = LocalDateTime.of(year, month, 1, 0, 0)
        val endDateTime = startDateTime.plusMonths(1).minusSeconds(1)

        val timeMin = DateTime(startDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
        val timeMax = DateTime(endDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())

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
                "color" to "#2179FE",
                "source" to "GOOGLE"
            )
        }

        return mapOf("source" to "GOOGLE", "events" to mappedEvents)
    }

    @Transactional(readOnly = true)
    fun getConnectionStatus(userId: Long): Map<String, Any> {
        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }
        return mapOf("connected" to (user.googleAccessToken != null))
    }

    @Transactional
    fun syncEvent(userId: Long, title: String, start: String, end: String): Map<String, Any> {
        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }
        if (user.googleAccessToken == null) {
            return mapOf("synced" to false, "message" to "구글 캘린더가 연동되지 않았습니다.")
        }

        val calendarService = buildCalendarService(user)
        val startDateTime = LocalDateTime.parse(start)
        val endDateTime = LocalDateTime.parse(end)
        val zone = ZoneId.systemDefault()

        val event = Event()
            .setSummary(title)
            .setStart(
                EventDateTime()
                    .setDateTime(DateTime(startDateTime.atZone(zone).toInstant().toEpochMilli()))
                    .setTimeZone(zone.id)
            )
            .setEnd(
                EventDateTime()
                    .setDateTime(DateTime(endDateTime.atZone(zone).toInstant().toEpochMilli()))
                    .setTimeZone(zone.id)
            )

        calendarService.events().insert("primary", event).execute()
        return mapOf("synced" to true, "message" to "구글 캘린더에 일정이 추가되었습니다.")
    }

    private fun buildCalendarService(user: User): Calendar {
        val credential = userCredential(user)
        refreshAccessTokenIfNeeded(user, credential)
        return Calendar.Builder(httpTransport, jsonFactory, credential)
            .setApplicationName("Moa Application")
            .build()
    }

    private fun userCredential(user: User): Credential {
        return Credential.Builder(
            com.google.api.client.auth.oauth2.BearerToken.authorizationHeaderAccessMethod()
        )
            .setTransport(httpTransport)
            .setJsonFactory(jsonFactory)
            .setClientAuthentication(ClientParametersAuthentication(clientId, clientSecret))
            .build()
            .setAccessToken(user.googleAccessToken)
            .setRefreshToken(user.googleRefreshToken)
    }

    @Transactional
    private fun refreshAccessTokenIfNeeded(user: User, credential: Credential) {
        if (user.googleRefreshToken.isNullOrBlank()) return
        val expires = credential.expirationTimeMilliseconds
        if (expires != null && expires > System.currentTimeMillis() + 60_000) return
        runCatching {
            credential.refreshToken()
            user.googleAccessToken = credential.accessToken
        }
    }
}
