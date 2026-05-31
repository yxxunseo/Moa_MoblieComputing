package com.example.moa_project.ui.meetings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moa_project.network.GuestScheduleResponse
import com.example.moa_project.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class GuestScheduleListState {
    object Loading : GuestScheduleListState()
    data class Success(val schedules: List<GuestScheduleResponse>) : GuestScheduleListState()
    data class Error(val message: String) : GuestScheduleListState()
}

class GuestScheduleListViewModel : ViewModel() {
    private val _state = MutableStateFlow<GuestScheduleListState>(GuestScheduleListState.Loading)
    val state: StateFlow<GuestScheduleListState> = _state

    fun fetchMySchedules() {
        viewModelScope.launch {
            _state.value = GuestScheduleListState.Loading
            try {
                val list = RetrofitClient.instance.getMyGuestSchedules()
                _state.value = GuestScheduleListState.Success(list)
            } catch (e: Exception) {
                Log.e("GuestScheduleListVM", "Failed to load guest schedules", e)
                _state.value = GuestScheduleListState.Error("단기 일정 목록을 불러오지 못했습니다.")
            }
        }
    }

    fun completeSchedule(link: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                RetrofitClient.instance.completeGuestSchedule(link)
                fetchMySchedules()
                onDone()
            } catch (e: Exception) {
                Log.e("GuestScheduleListVM", "Failed to complete schedule", e)
            }
        }
    }
}
