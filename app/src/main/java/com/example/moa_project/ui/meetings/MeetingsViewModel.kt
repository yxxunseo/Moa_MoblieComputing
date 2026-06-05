package com.example.moa_project.ui.meetings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moa_project.BuildConfig
import com.example.moa_project.network.GroupResponse
import com.example.moa_project.network.RetrofitClient
import com.example.moa_project.network.TokenManager
import com.example.moa_project.util.MoaErrorLog
import com.example.moa_project.util.ServerConnectionHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

private const val TAG = "MeetingsViewModel"

sealed class MeetingsState {
    object Loading : MeetingsState()
    data class Success(val groups: List<GroupResponse>) : MeetingsState()
    data class Error(val message: String) : MeetingsState()
    /** 토큰 만료/무효 → 로그인 화면으로 이동 필요 */
    object NeedsReLogin : MeetingsState()
}

class MeetingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<MeetingsState>(MeetingsState.Loading)
    val uiState: StateFlow<MeetingsState> = _uiState

    init {
        fetchMyGroups()
    }

    fun fetchMyGroups() {
        viewModelScope.launch {
            _uiState.value = MeetingsState.Loading

            Log.i(TAG, "fetchMyGroups start | SERVER_URL=${BuildConfig.SERVER_URL} | loggedIn=${TokenManager.isLoggedIn()}")

            if (!TokenManager.isLoggedIn()) {
                Log.w(TAG, "No JWT token – redirecting to login")
                _uiState.value = MeetingsState.NeedsReLogin
                return@launch
            }

            val diagnosis = ServerConnectionHelper.diagnose()
            if (!diagnosis.healthOk) {
                val message = ServerConnectionHelper.connectionErrorMessage(diagnosis)
                MoaErrorLog.log(TAG, "fetchMyGroups", message)
                _uiState.value = MeetingsState.Error(message)
                return@launch
            }

            try {
                Log.d(TAG, "GET api/users/me/groups …")
                val response = RetrofitClient.instance.getMyGroups()
                Log.i(TAG, "Groups loaded: count=${response.size} | ids=${response.map { it.id }}")
                _uiState.value = MeetingsState.Success(response)
            } catch (e: HttpException) {
                MoaErrorLog.log(TAG, "fetchMyGroups", e, mapOf("httpCode" to e.code().toString()))
                when (e.code()) {
                    401 -> _uiState.value = MeetingsState.NeedsReLogin
                    403 -> {
                        TokenManager.clear()
                        _uiState.value = MeetingsState.NeedsReLogin
                    }
                    else -> _uiState.value = MeetingsState.Error(MoaErrorLog.userMessage(e, "모임 목록을 불러오지 못했습니다."))
                }
            } catch (e: Exception) {
                MoaErrorLog.log(TAG, "fetchMyGroups", e)
                _uiState.value = MeetingsState.Error(MoaErrorLog.userMessage(e, "모임 목록을 불러오지 못했습니다."))
            }
        }
    }
}
