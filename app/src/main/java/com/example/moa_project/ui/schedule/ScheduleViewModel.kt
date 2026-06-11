package com.example.moa_project.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moa_project.network.ConfirmScheduleRequest
import com.example.moa_project.network.ReactionDto
import com.example.moa_project.network.RetrofitClient
import com.example.moa_project.network.ScheduleAnalysisResponse
import com.example.moa_project.network.ScheduleDetailResponse
import com.example.moa_project.network.TimeSlotDto
import com.example.moa_project.network.TimeSlotRequest
import com.example.moa_project.network.UpsertReactionRequest
import com.example.moa_project.network.TokenManager
import com.example.moa_project.util.MoaErrorLog
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
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

    private val _myTimeSlots = MutableStateFlow<List<TimeSlot>>(emptyList())
    val myTimeSlots: StateFlow<List<TimeSlot>> = _myTimeSlots

    private val _scheduleStartDate = MutableStateFlow<String?>(null)
    val scheduleStartDate: StateFlow<String?> = _scheduleStartDate

    private val _scheduleEndDate = MutableStateFlow<String?>(null)
    val scheduleEndDate: StateFlow<String?> = _scheduleEndDate

    private val _scheduleStatus = MutableStateFlow<String?>(null)
    val scheduleStatus: StateFlow<String?> = _scheduleStatus

    private val _respondedCount = MutableStateFlow(0)
    val respondedCount: StateFlow<Int> = _respondedCount

    private val _totalMembers = MutableStateFlow(0)
    val totalMembers: StateFlow<Int> = _totalMembers

    private val _scheduleTitle = MutableStateFlow<String?>(null)
    val scheduleTitle: StateFlow<String?> = _scheduleTitle

    private val _confirmedStart = MutableStateFlow<String?>(null)
    val confirmedStart: StateFlow<String?> = _confirmedStart

    private val _confirmedEnd = MutableStateFlow<String?>(null)
    val confirmedEnd: StateFlow<String?> = _confirmedEnd

    private val _canConfirm = MutableStateFlow(false)
    val canConfirm: StateFlow<Boolean> = _canConfirm

    private val _isConfirming = MutableStateFlow(false)
    val isConfirming: StateFlow<Boolean> = _isConfirming

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _displayAnalysis = MutableStateFlow<ScheduleAnalysisResponse?>(null)
    val displayAnalysis: StateFlow<ScheduleAnalysisResponse?> = _displayAnalysis

    private var analysisJob: Job? = null

    private fun cacheAnalysis(analysis: ScheduleAnalysisResponse) {
        _displayAnalysis.value = analysis
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    private suspend fun loadScheduleDetail() {
        val schedule = RetrofitClient.instance.getScheduleDetail(scheduleId)
        _scheduleTitle.value = schedule.title
        _scheduleStartDate.value = schedule.startDate
        _scheduleEndDate.value = schedule.endDate
        _scheduleStatus.value = schedule.status
        _confirmedStart.value = schedule.confirmedStart
        _confirmedEnd.value = schedule.confirmedEnd
        _respondedCount.value = schedule.respondedCount
        _totalMembers.value = schedule.totalMembers
        _canConfirm.value = schedule.canConfirm
        if (_uiState.value !is ScheduleState.AnalysisSuccess &&
            _uiState.value !is ScheduleState.ConfirmSuccess
        ) {
            _uiState.value = ScheduleState.DetailSuccess(schedule)
        }
    }

    fun fetchDetail() {
        viewModelScope.launch {
            try {
                loadScheduleDetail()
            } catch (e: Exception) {
                MoaErrorLog.log("ScheduleViewModel", "fetchDetail", e, mapOf("scheduleId" to scheduleId.toString()))
                if (_uiState.value !is ScheduleState.AnalysisSuccess &&
                    _uiState.value !is ScheduleState.ConfirmSuccess
                ) {
                    _uiState.value = ScheduleState.Error(MoaErrorLog.userMessage(e, "일정 정보를 불러오지 못했습니다."))
                }
            }
        }
    }

    suspend fun fetchMyTimeSlots() {
        try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
            val slots = RetrofitClient.instance.getMyGroupTimeSlots(scheduleId)
            _myTimeSlots.value = slots.mapNotNull { dto ->
                runCatching {
                    val start = LocalDateTime.parse(dto.start, formatter)
                    TimeSlot(date = start.toLocalDate(), hour = start.hour)
                }.getOrNull()
            }
        } catch (e: Exception) {
            MoaErrorLog.log("ScheduleViewModel", "fetchMyTimeSlots", e, mapOf("scheduleId" to scheduleId.toString()))
            _myTimeSlots.value = emptyList()
        }
    }

    fun saveProgress(onSuccess: () -> Unit) {
        val slots = _myTimeSlots.value
        if (slots.isNotEmpty()) {
            submitTimeSlots(slots, onSuccess)
        } else {
            viewModelScope.launch {
                fetchMyTimeSlots()
                onSuccess()
            }
        }
    }

    fun submitTimeSlots(selectedSlots: List<TimeSlot>, onSuccess: () -> Unit) {
        if (selectedSlots.isEmpty()) {
            _errorMessage.value = "가능한 시간을 1개 이상 선택해주세요."
            return
        }

        viewModelScope.launch {
            val previous = _displayAnalysis.value
            if (previous == null) {
                _uiState.value = ScheduleState.Loading
            }
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
                _myTimeSlots.value = selectedSlots
                _uiState.value = if (previous != null) {
                    ScheduleState.AnalysisSuccess(previous)
                } else {
                    ScheduleState.SubmitSuccess(response.message)
                }
                onSuccess()
            } catch (e: Exception) {
                MoaErrorLog.log("ScheduleViewModel", "submitTimeSlots", e, mapOf("scheduleId" to scheduleId.toString()))
                val message = MoaErrorLog.userMessage(e, "가능 시간 등록에 실패했습니다.")
                _errorMessage.value = message
                if (previous != null) {
                    _uiState.value = ScheduleState.AnalysisSuccess(previous)
                } else {
                    _uiState.value = ScheduleState.Error(message)
                }
            }
        }
    }

    fun fetchAnalysis() {
        analysisJob?.cancel()
        analysisJob = viewModelScope.launch {
            val previous = _displayAnalysis.value
            if (previous == null) {
                _uiState.value = ScheduleState.Loading
            }
            try {
                val analysis = RetrofitClient.instance.getScheduleAnalysis(scheduleId)
                analysis.startDate?.let { _scheduleStartDate.value = it }
                analysis.endDate?.let { _scheduleEndDate.value = it }
                cacheAnalysis(analysis)
                _uiState.value = ScheduleState.AnalysisSuccess(analysis)
            } catch (e: Exception) {
                MoaErrorLog.log("ScheduleViewModel", "fetchAnalysis", e, mapOf("scheduleId" to scheduleId.toString()))
                val message = MoaErrorLog.userMessage(e, "분석 결과를 불러오지 못했습니다.")
                if (previous != null) {
                    _uiState.value = ScheduleState.AnalysisSuccess(previous)
                    _errorMessage.value = message
                } else {
                    _uiState.value = ScheduleState.Error(message)
                }
            }
        }
    }

    fun confirm(start: String, end: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val previousAnalysis = _displayAnalysis.value
                ?: (_uiState.value as? ScheduleState.AnalysisSuccess)?.analysis
            _isConfirming.value = true
            try {
                val response = RetrofitClient.instance.confirmSchedule(
                    scheduleId = scheduleId,
                    request = ConfirmScheduleRequest(
                        confirmedStart = start,
                        confirmedEnd = end
                    )
                )
                _scheduleStatus.value = response.status.ifBlank { "CONFIRMED" }
                _confirmedStart.value = start
                _confirmedEnd.value = end
                _canConfirm.value = false
                runCatching { loadScheduleDetail() }
                _uiState.value = ScheduleState.ConfirmSuccess(response.message)
                onSuccess()
            } catch (e: Exception) {
                MoaErrorLog.log("ScheduleViewModel", "confirm", e, mapOf("scheduleId" to scheduleId.toString()))
                val message = MoaErrorLog.userMessage(e, "일정 확정에 실패했습니다.")
                _errorMessage.value = message
                if (previousAnalysis != null) {
                    cacheAnalysis(previousAnalysis)
                    _uiState.value = ScheduleState.AnalysisSuccess(previousAnalysis)
                } else {
                    _uiState.value = ScheduleState.Error(message)
                }
            } finally {
                _isConfirming.value = false
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

    fun deleteSchedule(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                RetrofitClient.instance.deleteSchedule(scheduleId)
                onSuccess()
            } catch (e: Exception) {
                MoaErrorLog.log("ScheduleViewModel", "deleteSchedule", e, mapOf("scheduleId" to scheduleId.toString()))
                onError(MoaErrorLog.userMessage(e, "일정 삭제에 실패했습니다."))
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
