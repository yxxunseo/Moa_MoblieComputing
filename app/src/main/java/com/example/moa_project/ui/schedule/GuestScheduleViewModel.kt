package com.example.moa_project.ui.schedule

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moa_project.network.AddGuestTimeSlotsRequest
import com.example.moa_project.network.GuestScheduleAnalysisResponse
import com.example.moa_project.network.GuestScheduleResponse
import com.example.moa_project.network.RetrofitClient
import com.example.moa_project.network.TimeSlotDto
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

    fun fetchSchedule(link: String) {
        viewModelScope.launch {
            _uiState.value = GuestScheduleState.Loading
            try {
                val response = RetrofitClient.instance.getGuestScheduleByLink(link)
                _uiState.value = GuestScheduleState.Success(response)
            } catch (e: Exception) {
                Log.e("GuestScheduleVM", "Failed to fetch schedule", e)
                _uiState.value = GuestScheduleState.Error("일정 정보를 불러오지 못했습니다.")
            }
        }
    }

    fun submitTimeSlots(link: String, guestName: String, selectedSlots: List<TimeSlot>) {
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
                _uiState.value = GuestScheduleState.SubmitSuccess(response.message)
            } catch (e: Exception) {
                Log.e("GuestScheduleVM", "Failed to submit time slots", e)
                _uiState.value = GuestScheduleState.Error("시간 등록에 실패했습니다.")
            }
        }
    }

    fun fetchAnalysis(link: String) {
        viewModelScope.launch {
            _uiState.value = GuestScheduleState.Loading
            try {
                val response = RetrofitClient.instance.getGuestScheduleAnalysis(link)
                _uiState.value = GuestScheduleState.AnalysisSuccess(response)
            } catch (e: Exception) {
                Log.e("GuestScheduleVM", "Failed to fetch analysis", e)
                _uiState.value = GuestScheduleState.Error("분석 결과를 불러오지 못했습니다.")
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
                Log.e("GuestScheduleVM", "Failed to confirm guest schedule", e)
                _uiState.value = GuestScheduleState.Error("일정 확정에 실패했습니다.")
            }
        }
    }
}
