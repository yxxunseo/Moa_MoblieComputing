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
import com.example.moa_project.util.MoaErrorLog
import com.example.moa_project.util.ProfileImageCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    private val _isUploadingImage = MutableStateFlow(false)
    val isUploadingImage: StateFlow<Boolean> = _isUploadingImage

    private val _uploadError = MutableStateFlow<String?>(null)
    val uploadError: StateFlow<String?> = _uploadError

    init {
        fetchMyProfile()
    }

    fun fetchMyProfile() {
        viewModelScope.launch {
            val keepPrevious = _uiState.value is UserState.Success
            if (!keepPrevious) {
                _uiState.value = UserState.Loading
            }
            try {
                val response = RetrofitClient.instance.getMyProfile()
                TokenManager.saveUserInfo(response.id, response.nickname, response.profileImageUrl)
                _uiState.value = UserState.Success(response)
            } catch (e: Exception) {
                MoaErrorLog.log("UserViewModel", "fetchMyProfile", e)
                if (_uiState.value !is UserState.Success) {
                    _uiState.value = UserState.Error(MoaErrorLog.userMessage(e, "프로필을 불러오지 못했습니다."))
                }
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
                TokenManager.saveUserInfo(response.id, response.nickname, response.profileImageUrl)
                _uiState.value = UserState.Success(response)
                onSuccess()
            } catch (e: Exception) {
                MoaErrorLog.log("UserViewModel", "updateProfile", e)
                _uiState.value = UserState.Error(MoaErrorLog.userMessage(e, "프로필 수정에 실패했습니다."))
            }
        }
    }

    fun uploadProfileImage(context: Context, uri: Uri, nickname: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isUploadingImage.value = true
            _uploadError.value = null
            var tempFile: java.io.File? = null
            try {
                val appContext = context.applicationContext
                tempFile = withContext(Dispatchers.IO) {
                    ImageCompressor.compressToTempFile(appContext, uri, "profile_")
                }
                val body = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("file", tempFile.name, body)
                val response = RetrofitClient.instance.uploadProfileImage(part)
                // 서버 URL 로드 실패해도 마이페이지에서 사진이 유지되도록 로컬 캐시 저장
                withContext(Dispatchers.IO) {
                    ProfileImageCache.saveFromFile(appContext, tempFile)
                }
                TokenManager.saveUserInfo(response.id, response.nickname, response.profileImageUrl)
                _uiState.value = UserState.Success(response)
                Log.i("UserViewModel", "profile uploaded | url=${response.profileImageUrl}")
                onSuccess()
            } catch (e: Exception) {
                MoaErrorLog.log("UserViewModel", "uploadProfileImage", e)
                _uploadError.value = uploadErrorMessage(e)
            } finally {
                tempFile?.delete()
                _isUploadingImage.value = false
            }
        }
    }

    fun clearUploadError() {
        _uploadError.value = null
    }

    private fun uploadErrorMessage(e: Throwable): String = when (e) {
        is IllegalArgumentException -> e.message ?: "이미지를 처리할 수 없습니다."
        is retrofit2.HttpException -> "프로필 사진 업로드 실패 (서버 ${e.code()}). 잠시 후 다시 시도해주세요."
        is java.net.ConnectException -> "서버에 연결할 수 없습니다. 백엔드(bootRun)가 켜져 있는지 확인해주세요."
        is java.net.SocketTimeoutException -> "업로드 시간이 초과됐어요. 네트워크를 확인해주세요."
        is java.io.IOException -> {
            if (e.message?.contains("Failed to connect", ignoreCase = true) == true ||
                e.message?.contains("Connection refused", ignoreCase = true) == true
            ) {
                "서버에 연결할 수 없습니다. 백엔드(bootRun)를 실행해주세요."
            } else {
                "서버에 연결하지 못했어요. 네트워크를 확인해주세요."
            }
        }
        else -> "프로필 사진 업로드에 실패했습니다."
    }
}
