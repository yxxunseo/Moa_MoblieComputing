package com.example.moa_project.ui.meetings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moa_project.network.GroupResponse
import com.example.moa_project.network.RetrofitClient
import com.example.moa_project.network.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

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

            // 토큰이 없으면 API 호출 자체를 막음
            if (!TokenManager.isLoggedIn()) {
                Log.w("MeetingsViewModel", "No JWT token – redirecting to login")
                _uiState.value = MeetingsState.NeedsReLogin
                return@launch
            }

            try {
                val response = RetrofitClient.instance.getMyGroups()
                Log.d("MeetingsViewModel", "Groups loaded: ${response.size}")
                _uiState.value = MeetingsState.Success(response)
            } catch (e: HttpException) {
                val code = e.code()
                Log.e("MeetingsViewModel", "HTTP $code – ${e.message()}", e)
                when (code) {
                    401 -> {
                        // TokenExpiredInterceptor가 이미 토큰을 삭제함
                        _uiState.value = MeetingsState.NeedsReLogin
                    }
                    403 -> {
                        // 서버가 아직 401을 안 주는 경우 대비
                        TokenManager.clear()
                        _uiState.value = MeetingsState.NeedsReLogin
                    }
                    500 -> _uiState.value = MeetingsState.Error("서버 내부 오류가 발생했습니다. (500)")
                    else -> _uiState.value = MeetingsState.Error("서버 오류: HTTP $code")
                }
            } catch (e: ConnectException) {
                Log.e("MeetingsViewModel", "Connection refused", e)
                _uiState.value = MeetingsState.Error("서버에 연결할 수 없습니다.\n서버가 실행 중인지 확인해 주세요.")
            } catch (e: SocketTimeoutException) {
                Log.e("MeetingsViewModel", "Timeout", e)
                _uiState.value = MeetingsState.Error("서버 응답 시간이 초과됐습니다.\n네트워크 상태를 확인해 주세요.")
            } catch (e: UnknownHostException) {
                Log.e("MeetingsViewModel", "Unknown host", e)
                _uiState.value = MeetingsState.Error("서버 주소를 찾을 수 없습니다.\n네트워크 연결을 확인해 주세요.")
            } catch (e: Exception) {
                Log.e("MeetingsViewModel", "Unexpected error: ${e::class.simpleName}", e)
                _uiState.value = MeetingsState.Error("모임 목록을 불러오지 못했습니다.\n(${e::class.simpleName})")
            }
        }
    }
}
