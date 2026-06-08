package com.example.moa_project.util

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.moa_project.network.RetrofitClient
import com.example.moa_project.network.TokenManager
import java.util.concurrent.TimeUnit

class WeeklyReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (!TokenManager.isLoggedIn()) return Result.success()

        return try {
            val reminders = RetrofitClient.instance.getWeeklyReminders()
            reminders.forEach { reminder ->
                val body = when (reminder.daysUntilDeadline) {
                    0 -> "${reminder.groupName} · 오늘이 ${reminder.deadlineLabel} 마감이에요! 서둘러 일정을 등록해주세요 🔥"
                    1 -> "${reminder.groupName} · ${reminder.deadlineLabel} 마감까지 1일 남았어요! 일정 등록 잊지 마세요"
                    else -> "${reminder.groupName} · ${reminder.deadlineLabel}까지 일정 등록까지 ${reminder.daysUntilDeadline}일 남았어요!"
                }
                MoaNotificationHelper.notifyWeeklyReminder(
                    context = applicationContext,
                    title = reminder.title,
                    body = body,
                    notificationKey = "weekly_${reminder.scheduleId}",
                )
            }
            Result.success()
        } catch (e: Exception) {
            Log.w(TAG, "weekly reminder check failed: ${e.message}")
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "WeeklyReminderWorker"
        private const val WORK_NAME = "moa_weekly_schedule_reminder"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<WeeklyReminderWorker>(1, TimeUnit.DAYS)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }
    }
}
