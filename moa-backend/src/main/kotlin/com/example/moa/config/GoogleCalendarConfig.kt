package com.example.moa.config

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.calendar.CalendarScopes
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.StringReader

@Configuration
class GoogleCalendarConfig(
    @Value("\${google.client-id}") private val clientId: String,
    @Value("\${google.client-secret}") private val clientSecret: String
) {
    @Bean
    fun googleAuthorizationCodeFlow(): GoogleAuthorizationCodeFlow {
        val jsonFactory = GsonFactory.getDefaultInstance()
        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        
        // application.yml의 값을 사용하여 시크릿 JSON 구성
        val clientSecretsJson = """
            {
              "web": {
                "client_id": "$clientId",
                "client_secret": "$clientSecret",
                "auth_uri": "https://accounts.google.com/o/oauth2/auth",
                "token_uri": "https://oauth2.googleapis.com/token"
              }
            }
        """.trimIndent()
        
        val clientSecrets = GoogleClientSecrets.load(jsonFactory, StringReader(clientSecretsJson))
        
        return GoogleAuthorizationCodeFlow.Builder(
            httpTransport,
            jsonFactory,
            clientSecrets,
            listOf(CalendarScopes.CALENDAR)
        ).setAccessType("offline").build()
    }
}
