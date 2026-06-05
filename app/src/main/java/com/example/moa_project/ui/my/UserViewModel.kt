package com.example.moa_project.ui.my

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import android.net.Uri
import com.example.moa_project.network.RetrofitClient
import com.example.moa_project.network.UpdateProfileRequest
import com.example.moa_project.network.UserResponse
import com.example.moa_project.network.TokenManager
import com.example.moa_project.util.ImageCompressor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

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

    fun updateProfile(nickname: String, profileImageUrl: String? = null, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = UserState.Loading
            try {
                val response = RetrofitClient.instance.updateMyProfile(
                    UpdateProfileRequest(nickname = nickname, profileImageUrl = profileImageUrl)
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

    fun uploadProfileImage(context: Context, uri: Uri, nickname: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = UserState.Loading
            try {
                // 갤러리 원본은 너무 커서 그대로 올리면 실패 → 리사이즈/압축 후 업로드
                val tempFile = ImageCompressor.compressToTempFile(context, uri, "profile_")
                val body = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("file", tempFile.name, body)
                val response = RetrofitClient.instance.uploadProfileImage(part)
                tempFile.delete()
                TokenManager.saveUserInfo(response.id, response.nickname)
                _uiState.value = UserState.Success(response)
                onSuccess()
            } catch (e: Exception) {
                Log.e("UserViewModel", "Failed to upload profile image", e)
                _uiState.value = UserState.Error(uploadErrorMessage(e))
            }
        }
    }

    private fun uploadErrorMessage(e: Throwable): String = when (e) {
        is retrofit2.HttpException -> "프로필 사진 업로드 실패 (서버 ${e.code()}). 잠시 후 다시 시도해주세요."
        is java.net.SocketTimeoutException -> "업로드 시간이 초과됐어요. 네트워크를 확인해주세요."
        is java.io.IOException -> "서버에 연결하지 못했어요. 네트워크를 확인해주세요."
        else -> "프로필 사진 업로드에 실패했습니다."
    }
}
