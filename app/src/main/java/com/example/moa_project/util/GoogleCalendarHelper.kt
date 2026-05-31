package com.example.moa_project.util

import android.content.Context
import com.example.moa_project.BuildConfig
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope

object GoogleCalendarHelper {
    private const val CALENDAR_SCOPE = "https://www.googleapis.com/auth/calendar"

    fun createConnectClient(context: Context) =
        GoogleSignIn.getClient(context, createConnectOptions())

    fun createConnectOptions(): GoogleSignInOptions {
        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestServerAuthCode(BuildConfig.GOOGLE_CLIENT_ID, true)
            .requestScopes(Scope(CALENDAR_SCOPE))
            .build()
    }

    fun extractServerAuthCode(data: android.content.Intent?): String? {
        return runCatching {
            GoogleSignIn.getSignedInAccountFromIntent(data)
                .getResult(com.google.android.gms.common.api.ApiException::class.java)
                ?.serverAuthCode
        }.getOrNull()
    }
}
