package com.example.moa_project.ui.calendar

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moa_project.network.AddEventRequest
import com.example.moa_project.network.RetrofitClient
import com.example.moa_project.network.UpdateEventRequest
import com.example.moa_project.util.MoaErrorLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class CalendarState {
    object Idle : CalendarState()
    object Loading : CalendarState()
    data class Success(val events: Map<String, Any>) : CalendarState()
    data class Error(val message: String) : CalendarState()
}

class CalendarViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<CalendarState>(CalendarState.Idle)
    val uiState: StateFlow<CalendarState> = _uiState

    fun fetchMonthlyEvents(month: String) {
        viewModelScope.launch {
            _uiState.value = CalendarState.Loading
            try {
                val response = RetrofitClient.instance.getMonthlyEvents(month)
                _uiState.value = CalendarState.Success(response)
            } catch (e: Exception) {
                MoaErrorLog.log("CalendarViewModel", "fetchMonthlyEvents", e, mapOf("month" to month))
                _uiState.value = CalendarState.Error(MoaErrorLog.userMessage(e, "일정을 불러오지 못했습니다."))
            }
        }
    }

    fun addEvent(
        title: String,
        start: String,
        end: String,
        color: String,
        onComplete: () -> Unit,
        onError: (String) -> Unit = {},
    ) {
        viewModelScope.launch {
            try {
                RetrofitClient.instance.addManualEvent(AddEventRequest(title, start, end, color))
                onComplete()
            } catch (e: Exception) {
                MoaErrorLog.log("CalendarViewModel", "addEvent", e)
                onError(MoaErrorLog.userMessage(e, "일정을 추가하지 못했습니다."))
            }
        }
    }

    fun deleteEvent(eventId: Long, onComplete: () -> Unit, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            try {
                RetrofitClient.instance.deleteEvent(eventId)
                onComplete()
            } catch (e: Exception) {
                MoaErrorLog.log("CalendarViewModel", "deleteEvent", e, mapOf("eventId" to eventId.toString()))
                onError(MoaErrorLog.userMessage(e, "일정을 삭제하지 못했습니다."))
            }
        }
    }

    fun updateEvent(
        eventId: Long,
        title: String,
        start: String,
        end: String,
        color: String,
        onComplete: () -> Unit,
        onError: (String) -> Unit = {},
    ) {
        viewModelScope.launch {
            try {
                RetrofitClient.instance.updateEvent(
                    eventId,
                    UpdateEventRequest(title, start, end, color)
                )
                onComplete()
            } catch (e: Exception) {
                MoaErrorLog.log("CalendarViewModel", "updateEvent", e, mapOf("eventId" to eventId.toString()))
                onError(MoaErrorLog.userMessage(e, "일정을 수정하지 못했습니다."))
            }
        }
    }
}
