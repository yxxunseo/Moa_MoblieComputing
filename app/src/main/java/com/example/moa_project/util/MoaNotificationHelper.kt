package com.example.moa_project.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.moa_project.R
import com.example.moa_project.ui.notifications.MoaNotificationType
import java.time.LocalDateTime

object MoaNotificationHelper {
    private const val CHANNEL_ID = "moa_schedule"
    private const val CHANNEL_NAME = "일정 알림"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "일정 확정 및 캘린더 추가 알림"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun notifyScheduleConfirmed(
        context: Context,
        title: String,
        timeText: String,
        notificationKey: String? = null,
        targetRoute: String? = null,
    ) {
        val receivedAt = LocalDateTime.now()
        notificationKey?.let { MoaInAppNotificationStore.recordReceivedAt(context, it, receivedAt) }
        val resolvedRoute = targetRoute ?: notificationKey
            ?.removePrefix("sch-")
            ?.takeIf { notificationKey.startsWith("sch-") && it.all(Char::isDigit) }
            ?.let { "schedule_result_group/$it" }
        MoaInAppNotificationStore.add(
            context,
            MoaNotificationType.CONFIRMED,
            "일정이 확정됐어요",
            "$title · $timeText",
            receivedAt = receivedAt,
            id = notificationKey ?: "confirmed-${System.currentTimeMillis()}",
            targetRoute = resolvedRoute,
        )
        if (!isEnabled(context, "schedule_confirmed_push")) return
        show(
            context = context,
            notificationId = 1001,
            title = "일정이 확정됐어요",
            body = "$title · $timeText"
        )
    }

    fun notifyNewSchedule(
        context: Context,
        groupName: String,
        scheduleTitle: String,
        scheduleId: Long,
        createdAt: LocalDateTime = LocalDateTime.now(),
    ) {
        val notificationKey = "pending-$scheduleId"
        MoaInAppNotificationStore.recordReceivedAt(context, notificationKey, createdAt)
        val title = "새 일정이 추가됐어요"
        val body = "$groupName · $scheduleTitle · 일정 조율을 위해 가능한 시간을 등록해주세요"
        MoaInAppNotificationStore.add(
            context,
            MoaNotificationType.WAITING,
            title,
            body,
            receivedAt = createdAt,
            id = notificationKey,
            targetRoute = "schedule_coordination_group/$scheduleId",
        )
        if (!isEnabled(context, "new_schedule_push")) return
        show(
            context = context,
            notificationId = notificationKey.hashCode(),
            title = title,
            body = body,
        )
    }

    fun notifyWeeklyReminder(
        context: Context,
        title: String,
        body: String,
        notificationKey: String,
    ) {
        val receivedAt = LocalDateTime.now()
        MoaInAppNotificationStore.recordReceivedAt(context, notificationKey, receivedAt)
        val scheduleId = notificationKey.removePrefix("weekly_")
        MoaInAppNotificationStore.add(
            context,
            com.example.moa_project.ui.notifications.MoaNotificationType.WEEKLY_REMINDER,
            title,
            body,
            receivedAt = receivedAt,
            id = notificationKey.replace('_', '-'),
            targetRoute = scheduleId.takeIf { it.all(Char::isDigit) }
                ?.let { "schedule_coordination_group/$it" },
        )
        if (!isEnabled(context, "weekly_reminder_push")) return
        show(
            context = context,
            notificationId = notificationKey.hashCode(),
            title = title,
            body = body,
        )
    }

    fun notifyCalendarAdded(
        context: Context,
        title: String,
        timeText: String,
        notificationKey: String? = null,
    ) {
        val receivedAt = LocalDateTime.now()
        notificationKey?.let { MoaInAppNotificationStore.recordReceivedAt(context, it, receivedAt) }
        MoaInAppNotificationStore.add(
            context,
            MoaNotificationType.UPCOMING,
            "캘린더에 일정이 추가됐어요",
            "$title · $timeText",
            receivedAt = receivedAt,
            targetRoute = "calendar",
        )
        if (!isEnabled(context, "calendar_added_push")) return
        show(
            context = context,
            notificationId = 1002,
            title = "캘린더에 일정이 추가됐어요",
            body = "$title · $timeText"
        )
    }

    private fun isEnabled(context: Context, key: String): Boolean {
        return context.getSharedPreferences("moa_settings", Context.MODE_PRIVATE)
            .getBoolean(key, true)
    }

    private fun show(context: Context, notificationId: Int, title: String, body: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
}
