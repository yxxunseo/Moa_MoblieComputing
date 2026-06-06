package com.example.moa_project.ui.notifications

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moa_project.network.GroupResponse
import com.example.moa_project.network.RetrofitClient
import com.example.moa_project.network.ScheduleDetailResponse
import com.example.moa_project.ui.home.HomeEventLoader
import com.example.moa_project.util.MoaInAppNotificationStore
import com.example.moa_project.util.MoaErrorLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class MoaNotificationType { CONFIRMED, WAITING, ADJUSTING, UPCOMING, INFO }

data class MoaNotification(
    val id: String,
    val type: MoaNotificationType,
    val title: String,
    val body: String,
    val timestamp: LocalDateTime,
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

    fun attachContext(context: Context) {
        appContext = context.applicationContext
    }

    fun refresh(markReadAfterLoad: Boolean = false) {
        viewModelScope.launch {
            _state.value = NotificationsState.Loading
            try {
                val merged = loadNotifications()
                _state.value = NotificationsState.Success(merged)
                if (markReadAfterLoad) {
                    appContext?.let { MoaInAppNotificationStore.markAllRead(it) }
                }
            } catch (e: Exception) {
                MoaErrorLog.log("NotificationsViewModel", "refresh", e)
                _state.value = NotificationsState.Error(MoaErrorLog.userMessage(e, "알림을 불러오지 못했습니다."))
            }
        }
    }

    private suspend fun loadNotifications(): List<MoaNotification> {
        val ctx = appContext
            ?: throw IllegalStateException("알림 화면 컨텍스트가 준비되지 않았습니다.")
        val local = MoaInAppNotificationStore.loadAll(ctx)
        val groups = runCatching { RetrofitClient.instance.getMyGroups() }
            .getOrDefault(emptyList())
        val remote = buildList {
            addAll(groupNotifications(groups))
            addAll(guestNotifications())
        }
        return (local + remote)
            .filter { it.type == MoaNotificationType.CONFIRMED }
            .distinctBy { it.id }
            .sortedByDescending { it.timestamp }
    }

    private suspend fun groupNotifications(groups: List<GroupResponse>): List<MoaNotification> {
        val result = mutableListOf<MoaNotification>()
        groups.forEach { group ->
            runCatching { RetrofitClient.instance.getGroupSchedules(group.id) }
                .getOrDefault(emptyList())
                .forEach { schedule ->
                    scheduleToNotification(schedule, group.name)?.let { result += it }
                }
        }
        return result
    }

    private suspend fun guestNotifications(): List<MoaNotification> {
        val ctx = appContext ?: return emptyList()
        return runCatching { RetrofitClient.instance.getMyGuestSchedules() }
            .getOrDefault(emptyList())
            .mapNotNull { guest ->
                if (guest.status != "CONFIRMED") return@mapNotNull null
                val key = "guest-${guest.id}"
                val receivedAt = MoaInAppNotificationStore.getOrRecordReceivedAt(ctx, key)
                MoaNotification(
                    id = key,
                    type = MoaNotificationType.CONFIRMED,
                    title = "단기 일정이 확정됐어요",
                    body = "${guest.title} · 확정 시간을 확인해보세요.",
                    timestamp = receivedAt,
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
        )
    }
}
