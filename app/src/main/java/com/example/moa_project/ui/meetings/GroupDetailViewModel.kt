package com.example.moa_project.ui.meetings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import android.content.Context
import android.net.Uri
import com.example.moa_project.network.GroupResponse
import com.example.moa_project.network.RetrofitClient
import com.example.moa_project.network.ScheduleDetailResponse
import com.example.moa_project.util.ImageCompressor
import com.example.moa_project.util.MoaErrorLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

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
                MoaErrorLog.log("GroupDetailViewModel", "loadGroupDetail", e)
                _state.value = GroupDetailState.Error(MoaErrorLog.userMessage(e, "그룹 정보를 불러오지 못했습니다."))
            }
        }
    }

    /**
     * @param onSuccess groupDeleted=true이면 관리자 나가기(그룹 삭제), false이면 일반 멤버 나가기
     */
    fun uploadCoverImage(context: Context, uri: Uri, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            var tempFile: java.io.File? = null
            try {
                tempFile = withContext(Dispatchers.IO) {
                    ImageCompressor.compressToTempFile(context.applicationContext, uri, "group_cover_")
                }
                val body = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("file", tempFile.name, body)
                val group = RetrofitClient.instance.uploadGroupCoverImage(groupId, part)
                val current = _state.value
                if (current is GroupDetailState.Success) {
                    _state.value = current.copy(group = group)
                } else {
                    loadGroupDetail()
                }
                onSuccess()
            } catch (e: Exception) {
                MoaErrorLog.log("GroupDetailViewModel", "uploadCoverImage", e)
                val message = when (e) {
                    is IllegalArgumentException -> e.message ?: "이미지를 처리할 수 없습니다."
                    else -> MoaErrorLog.userMessage(e, "모임 사진 업로드에 실패했습니다.")
                }
                onError(message)
            } finally {
                tempFile?.delete()
            }
        }
    }

    fun deleteSchedule(scheduleId: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                RetrofitClient.instance.deleteSchedule(scheduleId)
                loadGroupDetail()
                onSuccess()
            } catch (e: Exception) {
                MoaErrorLog.log("GroupDetailViewModel", "deleteSchedule", e)
                onError(MoaErrorLog.userMessage(e, "일정 삭제에 실패했습니다."))
            }
        }
    }

    fun leaveGroup(onSuccess: (groupDeleted: Boolean) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val result = RetrofitClient.instance.leaveGroup(groupId)
                val groupDeleted = result["groupDeleted"] as? Boolean ?: false
                onSuccess(groupDeleted)
            } catch (e: Exception) {
                MoaErrorLog.log("GroupDetailViewModel", "leaveGroup", e)
                onError(MoaErrorLog.userMessage(e, "그룹 나가기에 실패했습니다."))
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
