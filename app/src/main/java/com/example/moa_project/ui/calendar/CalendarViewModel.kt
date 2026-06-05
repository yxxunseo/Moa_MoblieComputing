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

    fun fetchMonthlyEvents(month: String, includeGoogleEvents: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = CalendarState.Loading
            try {
                val response = RetrofitClient.instance.getMonthlyEvents(month).toMutableMap()
                if (includeGoogleEvents) {
                    runCatching {
                        val googleResponse = RetrofitClient.instance.getGoogleCalendarEvents(month)
                        val googleEvents = googleResponse["events"] as? List<*> ?: emptyList<Any>()
                        val moaEvents = (response["events"] as? List<*>) ?: emptyList<Any>()
                        response["events"] = moaEvents + googleEvents
                    }
                }
                _uiState.value = CalendarState.Success(response)
            } catch (e: Exception) {
                MoaErrorLog.log("CalendarViewModel", "fetchMonthlyEvents", e, mapOf("month" to month))
                _uiState.value = CalendarState.Error(MoaErrorLog.userMessage(e, "일정을 불러오지 못했습니다."))
            }
        }
    }

    fun addEvent(title: String, start: String, end: String, color: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                RetrofitClient.instance.addManualEvent(AddEventRequest(title, start, end, color))
                onComplete()
            } catch (e: Exception) {
                MoaErrorLog.log("CalendarViewModel", "addEvent", e)
            }
        }
    }

    fun deleteEvent(eventId: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                RetrofitClient.instance.deleteEvent(eventId)
                onComplete()
            } catch (e: Exception) {
                MoaErrorLog.log("CalendarViewModel", "deleteEvent", e, mapOf("eventId" to eventId.toString()))
            }
        }
    }

    fun updateEvent(eventId: Long, title: String, start: String, end: String, color: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                RetrofitClient.instance.updateEvent(
                    eventId,
                    UpdateEventRequest(title, start, end, color)
                )
                onComplete()
            } catch (e: Exception) {
                MoaErrorLog.log("CalendarViewModel", "updateEvent", e, mapOf("eventId" to eventId.toString()))
            }
        }
    }
}
