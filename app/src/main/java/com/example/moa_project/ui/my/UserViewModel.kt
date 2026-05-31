package com.example.moa_project.ui.my

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moa_project.network.RetrofitClient
import com.example.moa_project.network.UserResponse
import com.example.moa_project.network.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class UserState {
    object Loading : UserState()
    data class Success(val user: UserResponse) : UserState()
    data class Error(val message: String) : UserState()
}

class UserViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<UserState>(UserState.Loading)
    val uiState: StateFlow<UserState> = _uiState

    init {
        fetchMyProfile()
    }

    fun fetchMyProfile() {
        viewModelScope.launch {
            _uiState.value = UserState.Loading
            try {
                val response = RetrofitClient.instance.getMyProfile()
                _uiState.value = UserState.Success(response)
            } catch (e: Exception) {
                Log.e("UserViewModel", "Failed to fetch user profile", e)
                _uiState.value = UserState.Error("프로필을 불러오지 못했습니다.")
            }
        }
    }

    fun updateProfile(nickname: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = UserState.Loading
            try {
                val response = RetrofitClient.instance.updateMyProfile(
                    com.example.moa_project.network.UpdateProfileRequest(nickname = nickname)
                )
                TokenManager.saveUserInfo(response.id, response.nickname)
                _uiState.value = UserState.Success(response)
                onSuccess()
            } catch (e: Exception) {
                Log.e("UserViewModel", "Failed to update profile", e)
                _uiState.value = UserState.Error("프로필 수정에 실패했습니다.")
            }
        }
    }
}
