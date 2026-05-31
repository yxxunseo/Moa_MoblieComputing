package com.example.moa_project.ui.meetings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moa_project.network.GroupResponse
import com.example.moa_project.network.RetrofitClient
import com.example.moa_project.network.ScheduleDetailResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class GroupDetailState {
    object Loading : GroupDetailState()
    data class Success(
        val group: GroupResponse,
        val schedules: List<ScheduleDetailResponse>,
        val members: List<com.example.moa_project.network.GroupMemberResponse> = emptyList()
    ) : GroupDetailState()
    data class Error(val message: String) : GroupDetailState()
}

class GroupDetailViewModel(private val groupId: Long) : ViewModel() {
    private val _state = MutableStateFlow<GroupDetailState>(GroupDetailState.Loading)
    val state: StateFlow<GroupDetailState> = _state

    init {
        loadGroupDetail()
    }

    fun loadGroupDetail() {
        viewModelScope.launch {
            _state.value = GroupDetailState.Loading
            try {
                val group = RetrofitClient.instance.getGroupDetail(groupId)
                val schedules = RetrofitClient.instance.getGroupSchedules(groupId)
                val members = runCatching {
                    RetrofitClient.instance.getGroupMembers(groupId)
                }.getOrDefault(emptyList())
                _state.value = GroupDetailState.Success(group, schedules, members)
            } catch (e: Exception) {
                Log.e("GroupDetailVM", "그룹 상세 로드 실패", e)
                _state.value = GroupDetailState.Error("그룹 정보를 불러오지 못했습니다.")
            }
        }
    }

    fun leaveGroup(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                RetrofitClient.instance.leaveGroup(groupId)
                onSuccess()
            } catch (e: Exception) {
                Log.e("GroupDetailVM", "그룹 나가기 실패", e)
                onError("그룹 나가기에 실패했습니다.")
            }
        }
    }

    class Factory(private val groupId: Long) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return GroupDetailViewModel(groupId) as T
        }
    }
}
