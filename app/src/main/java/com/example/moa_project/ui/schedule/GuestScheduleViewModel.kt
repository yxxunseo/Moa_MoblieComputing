package com.example.moa_project.ui.schedule

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moa_project.network.AddGuestTimeSlotsRequest
import com.example.moa_project.network.GuestParticipantDto
import com.example.moa_project.network.GuestScheduleAnalysisResponse
import com.example.moa_project.network.GuestScheduleResponse
import com.example.moa_project.network.RetrofitClient
import com.example.moa_project.network.TimeSlotDto
import com.example.moa_project.util.GuestVoteStore
import com.example.moa_project.util.MoaErrorLog
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

sealed class GuestScheduleState {
    object Idle : GuestScheduleState()
    object Loading : GuestScheduleState()
    data class Success(val schedule: GuestScheduleResponse) : GuestScheduleState()
    data class AnalysisSuccess(val analysis: GuestScheduleAnalysisResponse) : GuestScheduleState()
    data class SubmitSuccess(val message: String) : GuestScheduleState()
    data class ConfirmSuccess(val message: String) : GuestScheduleState()
    data class Error(val message: String) : GuestScheduleState()
}

class GuestScheduleViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<GuestScheduleState>(GuestScheduleState.Idle)
    val uiState: StateFlow<GuestScheduleState> = _uiState

    private val _scheduleStartDate = MutableStateFlow<String?>(null)
    val scheduleStartDate: StateFlow<String?> = _scheduleStartDate

    private val _scheduleEndDate = MutableStateFlow<String?>(null)
    val scheduleEndDate: StateFlow<String?> = _scheduleEndDate

    private val _participants = MutableStateFlow<List<GuestParticipantDto>?>(null)
    val participants: StateFlow<List<GuestParticipantDto>?> = _participants

    fun loadParticipants(link: String) {
        viewModelScope.launch {
            runCatching {
                RetrofitClient.instance.getGuestScheduleAnalysis(link).participants
            }.onSuccess { _participants.value = it }
        }
    }

    fun fetchSchedule(link: String) {
        viewModelScope.launch {
            _uiState.value = GuestScheduleState.Loading
            try {
                val response = RetrofitClient.instance.getGuestScheduleByLink(link)
                _scheduleStartDate.value = response.startDate
                _scheduleEndDate.value = response.endDate
                _uiState.value = GuestScheduleState.Success(response)
            } catch (e: Exception) {
                MoaErrorLog.log("GuestScheduleViewModel", "fetchSchedule", e, mapOf("link" to link))
                _uiState.value = GuestScheduleState.Error(MoaErrorLog.userMessage(e, "일정 정보를 불러오지 못했습니다."))
            }
        }
    }

    fun submitTimeSlots(
        link: String,
        guestName: String,
        selectedSlots: List<TimeSlot>,
        context: Context? = null,
    ) {
        viewModelScope.launch {
            if (guestName.isBlank()) {
                _uiState.value = GuestScheduleState.Error("이름을 입력해주세요.")
                return@launch
            }
            
            _uiState.value = GuestScheduleState.Loading
            try {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
                val dtoSlots = selectedSlots.map { slot ->
                    val startDateTime = slot.date.atTime(slot.hour, 0)
                    val endDateTime = startDateTime.plusHours(1)
                    TimeSlotDto(
                        start = startDateTime.format(formatter),
                        end = endDateTime.format(formatter)
                    )
                }

                val response = RetrofitClient.instance.addGuestTimeSlots(
                    link = link,
                    request = AddGuestTimeSlotsRequest(guestName, dtoSlots)
                )
                context?.let { GuestVoteStore.saveGuestName(it, link, guestName) }
                _uiState.value = GuestScheduleState.SubmitSuccess(response.message)
            } catch (e: Exception) {
                MoaErrorLog.log("GuestScheduleViewModel", "submitTimeSlots", e, mapOf("link" to link))
                _uiState.value = GuestScheduleState.Error(MoaErrorLog.userMessage(e, "시간 등록에 실패했습니다."))
            }
        }
    }

    fun fetchAnalysis(link: String) {
        viewModelScope.launch {
            _uiState.value = GuestScheduleState.Loading
            try {
                val response = RetrofitClient.instance.getGuestScheduleAnalysis(link)
                response.startDate?.takeIf { it.isNotBlank() }?.let { _scheduleStartDate.value = it }
                response.endDate?.takeIf { it.isNotBlank() }?.let { _scheduleEndDate.value = it }
                _uiState.value = GuestScheduleState.AnalysisSuccess(response)
            } catch (e: Exception) {
                MoaErrorLog.log("GuestScheduleViewModel", "fetchAnalysis", e, mapOf("link" to link))
                _uiState.value = GuestScheduleState.Error(MoaErrorLog.userMessage(e, "분석 결과를 불러오지 못했습니다."))
            }
        }
    }

    /** 확정 대기 중 주기 갱신 (로딩 화면 없이) */
    fun refreshAnalysis(link: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getGuestScheduleAnalysis(link)
                response.startDate?.takeIf { it.isNotBlank() }?.let { _scheduleStartDate.value = it }
                response.endDate?.takeIf { it.isNotBlank() }?.let { _scheduleEndDate.value = it }
                _uiState.value = GuestScheduleState.AnalysisSuccess(response)
            } catch (e: Exception) {
                MoaErrorLog.log("GuestScheduleViewModel", "refreshAnalysis", e, mapOf("link" to link))
            }
        }
    }

    fun confirm(link: String, start: String, end: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = GuestScheduleState.Loading
            try {
                val response = RetrofitClient.instance.confirmGuestSchedule(
                    link = link,
                    request = com.example.moa_project.network.ConfirmScheduleRequest(
                        confirmedStart = start,
                        confirmedEnd = end
                    )
                )
                _uiState.value = GuestScheduleState.ConfirmSuccess(response.message)
                onSuccess()
            } catch (e: Exception) {
                MoaErrorLog.log("GuestScheduleViewModel", "confirm", e, mapOf("link" to link))
                _uiState.value = GuestScheduleState.Error(MoaErrorLog.userMessage(e, "일정 확정에 실패했습니다."))
            }
        }
    }
}
