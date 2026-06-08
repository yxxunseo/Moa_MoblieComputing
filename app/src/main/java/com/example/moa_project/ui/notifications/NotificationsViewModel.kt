package com.example.moa_project.ui.notifications

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moa_project.network.GroupResponse
import com.example.moa_project.network.RetrofitClient
import com.example.moa_project.network.ScheduleDetailResponse
import com.example.moa_project.network.TokenManager
import com.example.moa_project.network.WeeklyReminderDto
import com.example.moa_project.ui.home.HomeEventLoader
import com.example.moa_project.util.MoaInAppNotificationStore
import com.example.moa_project.util.MoaErrorLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class MoaNotificationType { CONFIRMED, WAITING, ADJUSTING, UPCOMING, WEEKLY_REMINDER, INFO }

data class MoaNotification(
    val id: String,
    val type: MoaNotificationType,
    val title: String,
    val body: String,
    val timestamp: LocalDateTime,
    val targetRoute: String? = null,
)

sealed class NotificationsState {
    object Loading : NotificationsState()
    data class Success(val items: List<MoaNotification>) : NotificationsState()
    data class Error(val message: String) : NotificationsState()
}

class NotificationsViewModel : ViewModel() {
    private val _state = MutableStateFlow<NotificationsState>(NotificationsState.Loading)
    val state: StateFlow<NotificationsState> = _state
    private var appContext: Context? = null

    fun refresh(context: Context, markReadAfterLoad: Boolean = false) {
        appContext = context.applicationContext
        viewModelScope.launch {
            val ctx = appContext ?: return@launch
            _state.value = NotificationsState.Loading
            var local = emptyList<MoaNotification>()
            var remote = emptyList<MoaNotification>()
            try {
                local = MoaInAppNotificationStore.loadAll(ctx)
                if (local.isNotEmpty()) {
                    _state.value = NotificationsState.Success(local.sortedByDescending { it.timestamp })
                }

                remote = if (TokenManager.isLoggedIn()) {
                    loadRemoteNotifications(ctx)
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                MoaErrorLog.log("NotificationsViewModel", "refresh", e)
                local = runCatching { MoaInAppNotificationStore.loadAll(ctx) }.getOrDefault(emptyList())
            }

            val merged = mergeNotifications(local, remote)
            if (merged.isNotEmpty()) {
                merged.forEach { MoaInAppNotificationStore.upsert(ctx, it) }
                _state.value = NotificationsState.Success(merged)
            } else if (_state.value !is NotificationsState.Success) {
                _state.value = NotificationsState.Error("알림을 불러오지 못했습니다.")
            }

            if (markReadAfterLoad) {
                MoaInAppNotificationStore.markAllRead(ctx)
            }
        }
    }

    private suspend fun loadRemoteNotifications(context: Context): List<MoaNotification> = buildList {
        addAll(weeklyReminderNotifications())
        addAll(groupNotifications(safeGroups()))
        addAll(guestNotifications(context))
    }

    private suspend fun weeklyReminderNotifications(): List<MoaNotification> {
        return runCatching {
            withTimeoutOrNull(REMOTE_TIMEOUT_MS) {
                RetrofitClient.instance.getWeeklyReminders()
            }.orEmpty()
        }.getOrDefault(emptyList()).map { reminder ->
            MoaNotification(
                id = "weekly-${reminder.scheduleId}",
                type = MoaNotificationType.WEEKLY_REMINDER,
                title = reminder.title,
                body = weeklyReminderBody(reminder),
                timestamp = LocalDateTime.now(),
                targetRoute = "schedule_coordination_group/${reminder.scheduleId}",
            )
        }
    }

    private suspend fun safeGroups(): List<GroupResponse> =
        runCatching {
            withTimeoutOrNull(REMOTE_TIMEOUT_MS) {
                RetrofitClient.instance.getMyGroups()
            }.orEmpty()
        }.getOrDefault(emptyList())

    private suspend fun groupNotifications(groups: List<GroupResponse>): List<MoaNotification> {
        val result = mutableListOf<MoaNotification>()
        groups.forEach { group ->
            val schedules = runCatching {
                withTimeoutOrNull(REMOTE_TIMEOUT_MS) {
                    RetrofitClient.instance.getGroupSchedules(group.id)
                }.orEmpty()
            }.getOrDefault(emptyList())

            schedules.forEach { schedule ->
                scheduleToNotification(schedule, group.name)?.let { result += it }
                pendingScheduleNotification(schedule, group.name)?.let { result += it }
            }
        }
        return result
    }

    private suspend fun guestNotifications(context: Context): List<MoaNotification> {
        return runCatching {
            withTimeoutOrNull(REMOTE_TIMEOUT_MS) {
                RetrofitClient.instance.getMyGuestSchedules()
            }.orEmpty()
        }.getOrDefault(emptyList()).mapNotNull { guest ->
            if (guest.status != "CONFIRMED" && guest.status != "DONE") return@mapNotNull null
            val key = "guest-${guest.id}"
            val receivedAt = MoaInAppNotificationStore.getOrRecordReceivedAt(context, key)
            MoaNotification(
                id = key,
                type = MoaNotificationType.CONFIRMED,
                title = "단기 일정이 확정됐어요",
                body = "${guest.title} · 확정 시간을 확인해보세요.",
                timestamp = receivedAt,
                targetRoute = "schedule_result/${guest.uniqueLink}",
            )
        }
    }

    private fun scheduleToNotification(
        schedule: ScheduleDetailResponse,
        groupName: String,
    ): MoaNotification? {
        if (schedule.status != "CONFIRMED" && schedule.status != "DONE") return null
        val ctx = appContext ?: return null
        val key = "sch-${schedule.id}"
        val fmt = DateTimeFormatter.ofPattern("M월 d일 HH:mm")
        val start = schedule.confirmedStart?.let(HomeEventLoader::parseDateTime)
        val receivedAt = MoaInAppNotificationStore.getReceivedAt(ctx, key)
            ?: start
            ?: LocalDateTime.now()
        return MoaNotification(
            id = key,
            type = MoaNotificationType.CONFIRMED,
            title = "일정이 확정됐어요",
            body = "$groupName · ${schedule.title}" +
                (start?.let { " (${it.format(fmt)})" } ?: ""),
            timestamp = receivedAt,
            targetRoute = "schedule_result_group/${schedule.id}",
        )
    }

    private fun pendingScheduleNotification(
        schedule: ScheduleDetailResponse,
        groupName: String,
    ): MoaNotification? {
        if (schedule.status != "WAITING" && schedule.status != "ADJUSTING") return null
        val key = "pending-${schedule.id}"
        return MoaNotification(
            id = key,
            type = if (schedule.status == "WAITING") MoaNotificationType.WAITING else MoaNotificationType.ADJUSTING,
            title = schedule.title,
            body = "$groupName · 가능한 시간을 등록해주세요 (${schedule.respondedCount}/${schedule.totalMembers}명 응답)",
            timestamp = LocalDateTime.now(),
            targetRoute = "schedule_coordination_group/${schedule.id}",
        )
    }

    private fun weeklyReminderBody(reminder: WeeklyReminderDto): String = when (reminder.daysUntilDeadline) {
        0 -> "${reminder.groupName} · 오늘이 ${reminder.deadlineLabel} 마감이에요! 서둘러 일정을 등록해주세요"
        1 -> "${reminder.groupName} · ${reminder.deadlineLabel} 마감까지 1일 남았어요"
        else -> "${reminder.groupName} · ${reminder.deadlineLabel}까지 ${reminder.daysUntilDeadline}일 남았어요"
    }

    private fun mergeNotifications(
        local: List<MoaNotification>,
        remote: List<MoaNotification>,
    ): List<MoaNotification> {
        val byId = linkedMapOf<String, MoaNotification>()
        remote.forEach { byId[it.id] = it }
        local.forEach { existing ->
            val current = byId[existing.id]
            byId[existing.id] = when {
                current == null -> existing
                existing.timestamp.isAfter(current.timestamp) -> existing
                else -> current.copy(
                    title = existing.title.ifBlank { current.title },
                    body = existing.body.ifBlank { current.body },
                    timestamp = maxOf(existing.timestamp, current.timestamp),
                    targetRoute = existing.targetRoute ?: current.targetRoute,
                )
            }
        }
        return byId.values.sortedByDescending { it.timestamp }
    }

    fun onNotificationClicked(notification: MoaNotification, onNavigate: (String) -> Unit) {
        viewModelScope.launch {
            resolveNavigationRoute(notification)?.let(onNavigate)
        }
    }

    private suspend fun resolveNavigationRoute(notification: MoaNotification): String? {
        notification.navigationRoute()?.let { return it }

        if (!notification.id.startsWith("guest-")) return null
        val suffix = notification.id.removePrefix("guest-")
        suffix.toLongOrNull()?.let { guestId ->
            val guest = runCatching {
                withTimeoutOrNull(REMOTE_TIMEOUT_MS) {
                    RetrofitClient.instance.getMyGuestSchedules()
                }.orEmpty().find { it.id == guestId }
            }.getOrNull()
            if (guest != null) return "schedule_result/${guest.uniqueLink}"
        }
        if (suffix.isNotBlank()) return "schedule_result/$suffix"
        return null
    }

    companion object {
        private const val REMOTE_TIMEOUT_MS = 8_000L
    }
}
