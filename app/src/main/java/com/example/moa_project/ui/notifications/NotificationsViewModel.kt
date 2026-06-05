package com.example.moa_project.ui.notifications

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moa_project.network.GroupResponse
import com.example.moa_project.network.RetrofitClient
import com.example.moa_project.network.ScheduleDetailResponse
import com.example.moa_project.ui.home.HomeEventLoader
import com.example.moa_project.util.MoaInAppNotificationStore
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
        if (appContext == null) appContext = context.applicationContext
    }

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = NotificationsState.Loading
            try {
                val ctx = appContext
                val local = ctx?.let { MoaInAppNotificationStore.loadAll(it) }.orEmpty()
                val groups = runCatching { RetrofitClient.instance.getMyGroups() }
                    .getOrDefault(emptyList())
                val remote = buildList {
                    addAll(groupNotifications(groups))
                    addAll(guestNotifications())
                }
                val merged = (local + remote)
                    .distinctBy { "${it.type}|${it.title}|${it.body}" }
                    .sortedByDescending { it.timestamp }
                _state.value = NotificationsState.Success(merged)
            } catch (e: Exception) {
                Log.e("NotificationsVM", "Failed to load notifications", e)
                _state.value = NotificationsState.Error("알림을 불러오지 못했습니다.")
            }
        }
    }

    fun markAllRead() {
        appContext?.let { MoaInAppNotificationStore.markAllRead(it) }
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
        return runCatching { RetrofitClient.instance.getMyGuestSchedules() }
            .getOrDefault(emptyList())
            .mapNotNull { guest ->
                when (guest.status) {
                    "CONFIRMED" -> MoaNotification(
                        id = "guest-${guest.id}",
                        type = MoaNotificationType.CONFIRMED,
                        title = "단기 일정이 확정됐어요",
                        body = "${guest.title} · 확정 시간을 확인해보세요.",
                        timestamp = guest.confirmedStart?.let(HomeEventLoader::parseDateTime)
                            ?: LocalDateTime.now(),
                    )
                    else -> MoaNotification(
                        id = "guest-${guest.id}",
                        type = MoaNotificationType.WAITING,
                        title = "단기 일정 응답을 기다리고 있어요",
                        body = "${guest.title} · 참여자들의 가능 시간을 모으는 중이에요.",
                        timestamp = LocalDateTime.now(),
                    )
                }
            }
    }

    private fun scheduleToNotification(
        schedule: ScheduleDetailResponse,
        groupName: String,
    ): MoaNotification? {
        val fmt = DateTimeFormatter.ofPattern("M월 d일 HH:mm")
        return when (schedule.status) {
            "CONFIRMED", "DONE" -> {
                val start = schedule.confirmedStart?.let(HomeEventLoader::parseDateTime)
                MoaNotification(
                    id = "sch-${schedule.id}",
                    type = MoaNotificationType.CONFIRMED,
                    title = "일정이 확정됐어요",
                    body = "$groupName · ${schedule.title}" +
                        (start?.let { " (${it.format(fmt)})" } ?: ""),
                    timestamp = start ?: LocalDateTime.now(),
                )
            }
            "WAITING" -> MoaNotification(
                id = "sch-${schedule.id}",
                type = MoaNotificationType.WAITING,
                title = "가능 시간을 입력해주세요",
                body = "$groupName · ${schedule.title} 조율이 시작됐어요.",
                timestamp = LocalDateTime.now(),
            )
            "ADJUSTING" -> MoaNotification(
                id = "sch-${schedule.id}",
                type = MoaNotificationType.ADJUSTING,
                title = "일정을 조율 중이에요",
                body = "$groupName · ${schedule.title}",
                timestamp = LocalDateTime.now(),
            )
            else -> null
        }
    }
}
