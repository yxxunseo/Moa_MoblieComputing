package com.example.moa_project.util

import android.content.Context
import com.example.moa_project.network.TokenManager

/**
 * 계정별 온보딩 완료 여부를 관리합니다.
 * 로그아웃 시 moa_settings가 초기화되어도 온보딩 기록은 유지됩니다.
 */
object OnboardingManager {
    private const val PREFS = "moa_onboarding"
    private const val KEY_COMPLETED_USERS = "completed_user_ids"
    private const val KEY_FORCE_SHOW = "force_show_onboarding"

    /** 회원가입 직후 온보딩을 반드시 보여주기 위한 플래그 */
    fun markPendingForCurrentSession(context: Context) {
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_FORCE_SHOW, true)
            .commit()
    }

    fun shouldShow(context: Context): Boolean {
        if (!TokenManager.isLoggedIn()) return false
        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_FORCE_SHOW, false)) return true
        val userId = TokenManager.getUserId()
        if (userId <= 0L) return true
        return prefs.getStringSet(KEY_COMPLETED_USERS, emptySet())?.contains(userId.toString()) != true
    }

    fun markCompleted(context: Context) {
        val appContext = context.applicationContext
        val prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val userId = TokenManager.getUserId()
        val completed = prefs.getStringSet(KEY_COMPLETED_USERS, emptySet())
            ?.toMutableSet()
            ?: mutableSetOf()
        if (userId > 0L) {
            completed.add(userId.toString())
        }
        prefs.edit()
            .putStringSet(KEY_COMPLETED_USERS, completed)
            .remove(KEY_FORCE_SHOW)
            .commit()
    }
}
