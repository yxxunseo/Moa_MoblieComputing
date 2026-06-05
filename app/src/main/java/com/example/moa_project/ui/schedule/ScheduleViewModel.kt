package com.example.moa_project.ui.schedule

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moa_project.network.ConfirmScheduleRequest
import com.example.moa_project.network.GoogleSyncRequest
import com.example.moa_project.network.ReactionDto
import com.example.moa_project.network.RetrofitClient
import com.example.moa_project.network.ScheduleAnalysisResponse
import com.example.moa_project.network.ScheduleDetailResponse
import com.example.moa_project.network.TimeSlotDto
import com.example.moa_project.network.TimeSlotRequest
import com.example.moa_project.network.UpsertReactionRequest
import com.example.moa_project.network.TokenManager
import com.example.moa_project.util.MoaErrorLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

sealed class ScheduleState {
    object Loading : ScheduleState()
    data class DetailSuccess(val schedule: ScheduleDetailResponse) : ScheduleState()
    data class AnalysisSuccess(val analysis: ScheduleAnalysisResponse) : ScheduleState()
    data class SubmitSuccess(val message: String) : ScheduleState()
    data class ConfirmSuccess(val message: String) : ScheduleState()
    data class Error(val message: String) : ScheduleState()
}

class ScheduleViewModel(private val scheduleId: Long) : ViewModel() {
    private val _uiState = MutableStateFlow<ScheduleState>(ScheduleState.Loading)
    val uiState: StateFlow<ScheduleState> = _uiState

    private val _reactions = MutableStateFlow<List<ReactionDto>>(emptyList())
    val reactions: StateFlow<List<ReactionDto>> = _reactions

    fun fetchDetail() {
        viewModelScope.launch {
            _uiState.value = ScheduleState.Loading
            try {
                _uiState.value = ScheduleState.DetailSuccess(
                    RetrofitClient.instance.getScheduleDetail(scheduleId)
                )
            } catch (e: Exception) {
                MoaErrorLog.log("ScheduleViewModel", "fetchDetail", e, mapOf("scheduleId" to scheduleId.toString()))
                _uiState.value = ScheduleState.Error(MoaErrorLog.userMessage(e, "일정 정보를 불러오지 못했습니다."))
            }
        }
    }

    fun submitTimeSlots(selectedSlots: List<TimeSlot>, onSuccess: () -> Unit) {
        if (selectedSlots.isEmpty()) {
            _uiState.value = ScheduleState.Error("가능한 시간을 1개 이상 선택해주세요.")
            return
        }

        viewModelScope.launch {
            _uiState.value = ScheduleState.Loading
            try {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
                val slots = selectedSlots.map { slot ->
                    val start = slot.date.atTime(slot.hour, 0)
                    TimeSlotDto(
                        start = start.format(formatter),
                        end = start.plusHours(1).format(formatter)
                    )
                }

                val response = RetrofitClient.instance.submitGroupTimeSlots(
                    scheduleId = scheduleId,
                    request = TimeSlotRequest(slots)
                )
                _uiState.value = ScheduleState.SubmitSuccess(response.message)
                onSuccess()
            } catch (e: Exception) {
                MoaErrorLog.log("ScheduleViewModel", "submitTimeSlots", e, mapOf("scheduleId" to scheduleId.toString()))
                _uiState.value = ScheduleState.Error(MoaErrorLog.userMessage(e, "가능 시간 등록에 실패했습니다."))
            }
        }
    }

    fun fetchAnalysis() {
        viewModelScope.launch {
            _uiState.value = ScheduleState.Loading
            try {
                _uiState.value = ScheduleState.AnalysisSuccess(
                    RetrofitClient.instance.getScheduleAnalysis(scheduleId)
                )
            } catch (e: Exception) {
                MoaErrorLog.log("ScheduleViewModel", "fetchAnalysis", e, mapOf("scheduleId" to scheduleId.toString()))
                _uiState.value = ScheduleState.Error(MoaErrorLog.userMessage(e, "분석 결과를 불러오지 못했습니다."))
            }
        }
    }

    fun confirm(start: String, end: String, title: String, syncGoogle: Boolean, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = ScheduleState.Loading
            try {
                val response = RetrofitClient.instance.confirmSchedule(
                    scheduleId = scheduleId,
                    request = ConfirmScheduleRequest(
                        confirmedStart = start,
                        confirmedEnd = end
                    )
                )
                if (syncGoogle) {
                    runCatching {
                        RetrofitClient.instance.syncGoogleCalendar(
                            GoogleSyncRequest(title = title, start = start, end = end)
                        )
                    }
                }
                _uiState.value = ScheduleState.ConfirmSuccess(response.message)
                onSuccess()
            } catch (e: Exception) {
                MoaErrorLog.log("ScheduleViewModel", "confirm", e, mapOf("scheduleId" to scheduleId.toString()))
                _uiState.value = ScheduleState.Error(MoaErrorLog.userMessage(e, "일정 확정에 실패했습니다."))
            }
        }
    }

    fun fetchReactions() {
        viewModelScope.launch {
            runCatching {
                _reactions.value = RetrofitClient.instance.getScheduleReactions(scheduleId)
            }
        }
    }

    fun toggleReaction(emoji: String) {
        viewModelScope.launch {
            try {
                val mine = _reactions.value.firstOrNull { it.userId == TokenManager.getUserId() }
                if (mine?.emoji == emoji) {
                    RetrofitClient.instance.deleteScheduleReaction(scheduleId)
                } else {
                    RetrofitClient.instance.upsertScheduleReaction(
                        scheduleId,
                        UpsertReactionRequest(emoji)
                    )
                }
                fetchReactions()
            } catch (e: Exception) {
                MoaErrorLog.log("ScheduleViewModel", "toggleReaction", e, mapOf("scheduleId" to scheduleId.toString()))
            }
        }
    }

    class Factory(private val scheduleId: Long) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ScheduleViewModel(scheduleId) as T
        }
    }
}
