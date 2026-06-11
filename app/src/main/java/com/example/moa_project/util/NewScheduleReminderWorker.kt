package com.example.moa_project.util

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

class NewScheduleReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            NewScheduleReminderChecker.checkAndNotify(applicationContext)
            Result.success()
        } catch (e: Exception) {
            Log.w(TAG, "new schedule reminder check failed: ${e.message}")
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "NewScheduleReminderWorker"
        private const val WORK_NAME = "moa_new_schedule_reminder"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<NewScheduleReminderWorker>(15, TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }
    }
}
