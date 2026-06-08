package com.example.moa_project.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moa_project.network.GroupResponse
import com.example.moa_project.network.RetrofitClient
import com.example.moa_project.network.ScheduleDetailResponse
import com.example.moa_project.util.MoaErrorLog
import com.example.moa_project.util.ServerConnectionHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

data class HomeEventItem(
    val title: String,
    val start: LocalDateTime,
    val color: String,
    val subtitle: String? = null,
)

data class HomeActivityItem(
    val scheduleId: Long,
    val groupId: Long,
    val groupName: String,
    val scheduleTitle: String,
    val statusLabel: String,
    val groupColor: String,
    val respondedCount: Int,
    val totalMembers: Int,
)

sealed class HomeDashboardState {
    object Loading : HomeDashboardState()
    data class Success(
        val groups: List<GroupResponse>,
        val upcomingEvents: List<HomeEventItem>,
        val pendingCoordinationCount: Int,
        val confirmedThisWeekCount: Int,
        val pendingCoordinationItems: List<HomeActivityItem>,
        val weeklyTimetable: WeeklyTimetableData,
    ) : HomeDashboardState()
    data class Error(val message: String) : HomeDashboardState()
}

class HomeDashboardViewModel : ViewModel() {
    private val _state = MutableStateFlow<HomeDashboardState>(HomeDashboardState.Loading)
    val state: StateFlow<HomeDashboardState> = _state

    private var refreshJob: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        // 이전 새로고침이 진행 중이면 취소해 응답 경쟁(stale 데이터가 최종 상태로 남는 문제)을 방지
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            _state.value = HomeDashboardState.Loading
            val diagnosis = ServerConnectionHelper.diagnose()
            if (!diagnosis.healthOk) {
                val message = ServerConnectionHelper.connectionErrorMessage(diagnosis)
                MoaErrorLog.log("HomeDashboardViewModel", "refresh", message)
                _state.value = HomeDashboardState.Error(message)
                return@launch
            }
            try {
                val groups = RetrofitClient.instance.getMyGroups()
                val allUpcoming = HomeEventLoader.loadUpcomingEvents(groups)
                val upcomingEvents = allUpcoming.take(5)
                val pendingCoordinationItems = loadPendingCoordinationItems(groups)
                val weeklyTimetable = WeeklyTimetableLoader.loadCurrentWeek()

                _state.value = HomeDashboardState.Success(
                    groups = groups,
                    upcomingEvents = upcomingEvents,
                    pendingCoordinationCount = pendingCoordinationItems.size,
                    confirmedThisWeekCount = HomeEventLoader.countEventsThisWeek(allUpcoming),
                    pendingCoordinationItems = pendingCoordinationItems,
                    weeklyTimetable = weeklyTimetable,
                )
            } catch (e: Exception) {
                MoaErrorLog.log("HomeDashboardViewModel", "refresh", e)
                _state.value = HomeDashboardState.Error(MoaErrorLog.userMessage(e, "홈 정보를 불러오지 못했습니다."))
            }
        }
    }

    private suspend fun loadPendingCoordinationItems(groups: List<GroupResponse>): List<HomeActivityItem> {
        // 그룹별 일정 조회를 순차가 아닌 병렬로 수행 (그룹 수에 비례하던 지연을 단축)
        val items = coroutineScope {
            groups.map { group ->
                async {
                    runCatching {
                        RetrofitClient.instance.getGroupSchedules(group.id)
                    }.getOrDefault(emptyList()).mapNotNull { schedule ->
                        val label = statusToLabel(schedule.status)
                        if (label == "응답 대기" || label == "조율 중") {
                            schedule.toActivityItem(group)
                        } else null
                    }
                }
            }.awaitAll().flatten()
        }
        return items.sortedByDescending { statusPriority(it.statusLabel) }
    }

    private fun ScheduleDetailResponse.toActivityItem(group: GroupResponse): HomeActivityItem {
        return HomeActivityItem(
            scheduleId = id,
            groupId = group.id,
            groupName = group.name,
            scheduleTitle = title,
            statusLabel = statusToLabel(status),
            groupColor = group.color,
            respondedCount = respondedCount,
            totalMembers = totalMembers,
        )
    }

    private fun statusToLabel(status: String): String = when (status) {
        "WAITING" -> "응답 대기"
        "ADJUSTING" -> "조율 중"
        "CONFIRMED" -> "확정됨"
        "DONE" -> "완료됨"
        else -> status
    }

    private fun statusPriority(label: String): Int = when (label) {
        "조율 중" -> 4
        "응답 대기" -> 3
        "확정됨" -> 2
        else -> 1
    }

}
