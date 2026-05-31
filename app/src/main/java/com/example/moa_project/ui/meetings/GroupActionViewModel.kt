package com.example.moa_project.ui.meetings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moa_project.network.CreateGroupRequest
import com.example.moa_project.network.JoinGroupRequest
import com.example.moa_project.network.GroupResponse
import com.example.moa_project.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class GroupActionState {
    object Idle : GroupActionState()
    object Loading : GroupActionState()
    data class CreateSuccess(val group: GroupResponse) : GroupActionState()
    data class JoinSuccess(val groupName: String, val groupId: Long) : GroupActionState()
    data class Error(val message: String) : GroupActionState()
}

class GroupActionViewModel : ViewModel() {
    private val _state = MutableStateFlow<GroupActionState>(GroupActionState.Idle)
    val state: StateFlow<GroupActionState> = _state

    fun createGroup(name: String, description: String, color: String) {
        if (name.isBlank()) {
            _state.value = GroupActionState.Error("모임 이름을 입력해주세요.")
            return
        }
        viewModelScope.launch {
            _state.value = GroupActionState.Loading
            try {
                val response = RetrofitClient.instance.createGroup(
                    CreateGroupRequest(name = name, description = description.ifBlank { null }, color = color)
                )
                _state.value = GroupActionState.CreateSuccess(response)
            } catch (e: Exception) {
                Log.e("GroupActionVM", "그룹 생성 실패", e)
                _state.value = GroupActionState.Error("그룹 생성에 실패했습니다: ${e.message}")
            }
        }
    }

    fun joinGroup(inviteCode: String) {
        if (inviteCode.isBlank()) {
            _state.value = GroupActionState.Error("초대 코드를 입력해주세요.")
            return
        }
        viewModelScope.launch {
            _state.value = GroupActionState.Loading
            try {
                val response = RetrofitClient.instance.joinGroup(JoinGroupRequest(inviteCode = inviteCode.trim()))
                val groupId = (response["groupId"] as? Double)?.toLong() ?: 0L
                val groupName = response["groupName"] as? String ?: "모임"
                _state.value = GroupActionState.JoinSuccess(groupName, groupId)
            } catch (e: Exception) {
                Log.e("GroupActionVM", "그룹 입장 실패", e)
                _state.value = GroupActionState.Error("그룹 입장에 실패했습니다. 초대 코드를 확인해주세요.")
            }
        }
    }

    fun resetState() {
        _state.value = GroupActionState.Idle
    }
}
