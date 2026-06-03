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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

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
                val tempFile = copyUriToTempFile(context, uri)
                val body = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("file", tempFile.name, body)
                val response = RetrofitClient.instance.uploadProfileImage(part)
                tempFile.delete()
                TokenManager.saveUserInfo(response.id, response.nickname)
                _uiState.value = UserState.Success(response)
                onSuccess()
            } catch (e: Exception) {
                Log.e("UserViewModel", "Failed to upload profile image", e)
                _uiState.value = UserState.Error("프로필 사진 업로드에 실패했습니다.")
            }
        }
    }

    private fun copyUriToTempFile(context: Context, uri: Uri): File {
        val temp = File.createTempFile("profile_", ".jpg", context.cacheDir)
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(temp).use { output -> input.copyTo(output) }
        } ?: throw IllegalArgumentException("이미지를 읽을 수 없습니다.")
        return temp
    }
}
