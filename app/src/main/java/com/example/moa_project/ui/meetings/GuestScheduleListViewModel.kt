package com.example.moa_project.ui.meetings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moa_project.network.GuestScheduleResponse
import com.example.moa_project.network.RetrofitClient
import com.example.moa_project.util.MoaErrorLog
import com.example.moa_project.util.ServerConnectionHelper
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

    companion object {
        private const val TAG = "GuestScheduleListVM"
    }

    fun fetchMySchedules() {
        viewModelScope.launch {
            _state.value = GuestScheduleListState.Loading
            Log.i(TAG, "fetchMySchedules start | url=${RetrofitClient.BASE_URL}")

            val diagnosis = ServerConnectionHelper.diagnose()
            if (!diagnosis.healthOk) {
                val message = ServerConnectionHelper.connectionErrorMessage(diagnosis)
                MoaErrorLog.log(TAG, "fetchMySchedules", message)
                _state.value = GuestScheduleListState.Error(message)
                return@launch
            }

            try {
                val list = RetrofitClient.instance.getMyGuestSchedules()
                Log.i(TAG, "Guest schedules loaded: count=${list.size}")
                _state.value = GuestScheduleListState.Success(list)
            } catch (e: Exception) {
                MoaErrorLog.log(TAG, "fetchMySchedules", e)
                _state.value = GuestScheduleListState.Error(MoaErrorLog.userMessage(e, "단기 일정 목록을 불러오지 못했습니다."))
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
                MoaErrorLog.log("GuestScheduleListViewModel", "completeSchedule", e, mapOf("link" to link))
            }
        }
    }
}
