package com.example.moa_project.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moa_project.network.GroupResponse
import com.example.moa_project.network.RetrofitClient
import com.example.moa_project.network.ScheduleDetailResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter

data class HomeEventItem(
    val title: String,
    val start: LocalDateTime,
    val color: String
)

data class HomeActivityItem(
    val groupName: String,
    val scheduleTitle: String,
    val statusLabel: String,
    val groupColor: String
)

sealed class HomeDashboardState {
    object Loading : HomeDashboardState()
    data class Success(
        val groups: List<GroupResponse>,
        val upcomingEvents: List<HomeEventItem>,
        val recentActivities: List<HomeActivityItem>
    ) : HomeDashboardState()
    data class Error(val message: String) : HomeDashboardState()
}

class HomeDashboardViewModel : ViewModel() {
    private val _state = MutableStateFlow<HomeDashboardState>(HomeDashboardState.Loading)
    val state: StateFlow<HomeDashboardState> = _state

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = HomeDashboardState.Loading
            try {
                val groups = RetrofitClient.instance.getMyGroups()
                val month = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
                val eventsResponse = RetrofitClient.instance.getMonthlyEvents(month)
                val events = parseEvents(eventsResponse)
                    .filter { !it.start.isBefore(LocalDateTime.now().minusMinutes(1)) }
                    .sortedBy { it.start }
                    .take(3)

                val activities = loadRecentActivities(groups)

                _state.value = HomeDashboardState.Success(groups, events, activities)
            } catch (e: Exception) {
                Log.e("HomeDashboardVM", "Failed to load dashboard", e)
                _state.value = HomeDashboardState.Error("홈 정보를 불러오지 못했습니다.")
            }
        }
    }

    private suspend fun loadRecentActivities(groups: List<GroupResponse>): List<HomeActivityItem> {
        val items = mutableListOf<HomeActivityItem>()
        groups.forEach { group ->
            runCatching {
                RetrofitClient.instance.getGroupSchedules(group.id)
            }.getOrDefault(emptyList()).forEach { schedule ->
                items.add(schedule.toActivityItem(group))
            }
        }
        return items
            .sortedByDescending { statusPriority(it.statusLabel) }
            .take(5)
    }

    private fun ScheduleDetailResponse.toActivityItem(group: GroupResponse): HomeActivityItem {
        return HomeActivityItem(
            groupName = group.name,
            scheduleTitle = title,
            statusLabel = statusToLabel(status),
            groupColor = group.color
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

    private fun parseEvents(response: Map<String, Any>): List<HomeEventItem> {
        val data = (response["events"] as? List<*>) ?: (response["data"] as? List<*>) ?: return emptyList()
        return data.mapNotNull { raw ->
            val item = raw as? Map<*, *> ?: return@mapNotNull null
            val title = item["title"] as? String ?: return@mapNotNull null
            val startText = item["start"] as? String ?: return@mapNotNull null
            val color = item["color"] as? String ?: "#2179FE"
            val start = runCatching { LocalDateTime.parse(startText) }.getOrNull() ?: return@mapNotNull null
            HomeEventItem(title, start, color)
        }
    }
}
