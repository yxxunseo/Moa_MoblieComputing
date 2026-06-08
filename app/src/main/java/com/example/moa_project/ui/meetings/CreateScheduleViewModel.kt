package com.example.moa_project.ui.meetings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moa_project.network.RetrofitClient
import com.example.moa_project.network.ScheduleDetailResponse
import com.example.moa_project.util.MoaErrorLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

sealed class CreateScheduleState {
    object Idle : CreateScheduleState()
    object Loading : CreateScheduleState()
    data class Success(val schedule: ScheduleDetailResponse) : CreateScheduleState()
    data class Error(val message: String) : CreateScheduleState()
}

data class CreateScheduleRequest(
    val title: String,
    val description: String?,
    val startDate: String,
    val endDate: String
)

class CreateScheduleViewModel(private val groupId: Long) : ViewModel() {
    private val _state = MutableStateFlow<CreateScheduleState>(CreateScheduleState.Idle)
    val state: StateFlow<CreateScheduleState> = _state

    fun createSchedule(
        title: String,
        description: String,
        startDate: LocalDate,
        endDate: LocalDate,
        isWeeklyRecurring: Boolean = false,
    ) {
        if (title.isBlank()) {
            _state.value = CreateScheduleState.Error("일정 제목을 입력해주세요.")
            return
        }
        if (!endDate.isAfter(startDate) && endDate != startDate) {
            _state.value = CreateScheduleState.Error("종료일은 시작일 이후여야 합니다.")
            return
        }
        viewModelScope.launch {
            _state.value = CreateScheduleState.Loading
            try {
                val response = RetrofitClient.instance.createGroupSchedule(
                    groupId = groupId,
                    request = com.example.moa_project.network.CreateScheduleRequest(
                        title = title,
                        description = description.ifBlank { null },
                        startDate = startDate.toString(),
                        endDate = endDate.toString(),
                        isWeeklyRecurring = isWeeklyRecurring,
                    )
                )
                _state.value = CreateScheduleState.Success(response)
            } catch (e: Exception) {
                MoaErrorLog.log("CreateScheduleViewModel", "createSchedule", e, mapOf("groupId" to groupId.toString()))
                _state.value = CreateScheduleState.Error(MoaErrorLog.userMessage(e, "일정 생성에 실패했습니다."))
            }
        }
    }

    fun resetState() {
        _state.value = CreateScheduleState.Idle
    }

    class Factory(private val groupId: Long) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return CreateScheduleViewModel(groupId) as T
        }
    }
}
