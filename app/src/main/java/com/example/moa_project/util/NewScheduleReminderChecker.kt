package com.example.moa_project.util

import android.content.Context
import android.util.Log
import com.example.moa_project.network.RetrofitClient
import com.example.moa_project.network.TokenManager
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object NewScheduleReminderChecker {
    private const val TAG = "NewScheduleReminder"
    private const val PREFS = "moa_new_schedule_checker"
    private const val KEY_BASELINE = "baseline_set"

    suspend fun checkAndNotify(context: Context) {
        if (!TokenManager.isLoggedIn()) return

        val reminders = runCatching {
            RetrofitClient.instance.getPendingScheduleReminders()
        }.getOrElse { error ->
            Log.w(TAG, "pending reminders fetch failed: ${error.message}")
            return
        }

        val appContext = context.applicationContext
        val prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        if (!prefs.getBoolean(KEY_BASELINE, false)) {
            reminders.forEach { reminder ->
                val createdAt = parseCreatedAt(reminder.createdAt) ?: LocalDateTime.now()
                MoaInAppNotificationStore.recordReceivedAt(
                    appContext,
                    notificationKey(reminder.scheduleId),
                    createdAt,
                )
            }
            prefs.edit().putBoolean(KEY_BASELINE, true).apply()
            return
        }

        reminders.forEach { reminder ->
            val key = notificationKey(reminder.scheduleId)
            if (MoaInAppNotificationStore.getReceivedAt(appContext, key) != null) return@forEach

            val createdAt = parseCreatedAt(reminder.createdAt) ?: LocalDateTime.now()
            MoaNotificationHelper.notifyNewSchedule(
                context = appContext,
                groupName = reminder.groupName,
                scheduleTitle = reminder.title,
                scheduleId = reminder.scheduleId,
                createdAt = createdAt,
            )
        }
    }

    private fun notificationKey(scheduleId: Long): String = "pending-$scheduleId"

    private fun parseCreatedAt(raw: String): LocalDateTime? {
        return runCatching { LocalDateTime.parse(raw) }.getOrNull()
            ?: runCatching {
                LocalDateTime.parse(raw, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
            }.getOrNull()
    }
}
