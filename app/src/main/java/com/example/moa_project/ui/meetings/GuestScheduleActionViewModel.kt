package com.example.moa_project.ui.meetings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moa_project.network.CreateGuestScheduleRequest
import com.example.moa_project.network.GuestScheduleResponse
import com.example.moa_project.network.RetrofitClient
import com.example.moa_project.util.MoaErrorLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

sealed class GuestScheduleActionState {
    object Idle : GuestScheduleActionState()
    object Loading : GuestScheduleActionState()
    data class Success(val schedule: GuestScheduleResponse) : GuestScheduleActionState()
    data class Error(val message: String) : GuestScheduleActionState()
}

class GuestScheduleActionViewModel : ViewModel() {
    private val _state = MutableStateFlow<GuestScheduleActionState>(GuestScheduleActionState.Idle)
    val state: StateFlow<GuestScheduleActionState> = _state

    fun createGuestSchedule(
        title: String,
        description: String,
        startDate: LocalDate,
        endDate: LocalDate
    ) {
        if (title.isBlank()) {
            _state.value = GuestScheduleActionState.Error("일정 제목을 입력해주세요.")
            return
        }
        if (endDate.isBefore(startDate)) {
            _state.value = GuestScheduleActionState.Error("종료일은 시작일 이후여야 합니다.")
            return
        }

        viewModelScope.launch {
            _state.value = GuestScheduleActionState.Loading
            try {
                val response = RetrofitClient.instance.createGuestSchedule(
                    CreateGuestScheduleRequest(
                        title = title,
                        description = description.ifBlank { null },
                        startDate = startDate.toString(),
                        endDate = endDate.toString()
                    )
                )
                _state.value = GuestScheduleActionState.Success(response)
            } catch (e: Exception) {
                MoaErrorLog.log("GuestScheduleActionViewModel", "createGuestSchedule", e)
                _state.value = GuestScheduleActionState.Error(MoaErrorLog.userMessage(e, "링크 일정 생성에 실패했습니다."))
            }
        }
    }

    fun reset() {
        _state.value = GuestScheduleActionState.Idle
    }
}
