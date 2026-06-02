package com.example.moa_project.ui.login

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moa_project.network.RetrofitClient
import com.example.moa_project.network.GoogleLoginRequest
import com.example.moa_project.network.KakaoLoginRequest
import com.example.moa_project.network.TokenManager
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun loginWithKakao(context: Context, onSuccess: () -> Unit = {}) {
        _loginState.value = LoginState.Loading

        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Log.e("AuthViewModel", "카카오계정으로 로그인 실패", error)
                _loginState.value = LoginState.Error("카카오 로그인 실패: ${error.message}")
            } else if (token != null) {
                Log.i("AuthViewModel", "카카오계정으로 로그인 성공 ${token.accessToken}")
                viewModelScope.launch {
                    try {
                        val response = RetrofitClient.instance.loginWithKakao(KakaoLoginRequest(token.accessToken))
                        // ✅ 토큰 및 유저 정보 저장
                        TokenManager.saveTokens(response.token, response.refreshToken)
                        TokenManager.saveUserInfo(response.user.id, response.user.nickname)
                        _loginState.value = LoginState.Success("kakao", response.token)
                        onSuccess()
                    } catch (e: Exception) {
                        Log.e("AuthViewModel", "서버로 카카오 토큰 전송 실패", e)
                        _loginState.value = LoginState.Error("서버 인증 실패")
                    }
                }
            }
        }

        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                if (error != null) {
                    Log.e("AuthViewModel", "카카오톡으로 로그인 실패", error)
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        _loginState.value = LoginState.Idle
                        return@loginWithKakaoTalk
                    }
                    UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
                } else if (token != null) {
                    Log.i("AuthViewModel", "카카오톡으로 로그인 성공 ${token.accessToken}")
                    viewModelScope.launch {
                        try {
                            val response = RetrofitClient.instance.loginWithKakao(KakaoLoginRequest(token.accessToken))
                            // ✅ 토큰 및 유저 정보 저장
                            TokenManager.saveTokens(response.token, response.refreshToken)
                            TokenManager.saveUserInfo(response.user.id, response.user.nickname)
                            _loginState.value = LoginState.Success("kakao", response.token)
                            onSuccess()
                        } catch (e: Exception) {
                            Log.e("AuthViewModel", "서버로 카카오 토큰 전송 실패", e)
                            _loginState.value = LoginState.Error("서버 인증 실패")
                        }
                    }
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
        }
    }

    fun handleGoogleSignInResult(intent: android.content.Intent?, onSuccess: () -> Unit = {}) {
        _loginState.value = LoginState.Loading
        try {
            val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(intent)
            val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                Log.i("AuthViewModel", "구글 로그인 성공: $idToken")
                viewModelScope.launch {
                    try {
                        val response = RetrofitClient.instance.loginWithGoogle(GoogleLoginRequest(idToken))
                        // ✅ 토큰 및 유저 정보 저장
                        TokenManager.saveTokens(response.token, response.refreshToken)
                        TokenManager.saveUserInfo(response.user.id, response.user.nickname)
                        _loginState.value = LoginState.Success("google", response.token)
                        onSuccess()
                    } catch (e: Exception) {
                        Log.e("AuthViewModel", "서버로 구글 토큰 전송 실패", e)
                        _loginState.value = LoginState.Error("서버 인증 실패")
                    }
                }
            } else {
                Log.e("AuthViewModel", "구글 로그인 실패: idToken is null")
                _loginState.value = LoginState.Error("구글 로그인 실패: 토큰이 없습니다.")
            }
        } catch (e: com.google.android.gms.common.api.ApiException) {
            Log.e("AuthViewModel", "구글 로그인 에러 code: ${e.statusCode}", e)
            _loginState.value = LoginState.Error("구글 로그인 실패 (코드: ${e.statusCode})")
        } catch (e: Exception) {
            Log.e("AuthViewModel", "구글 로그인 에러", e)
            _loginState.value = LoginState.Error("구글 로그인 실패: ${e.message}")
        }
    }

    fun loginWithEmail(loginId: String, password: String, onSuccess: () -> Unit = {}) {
        _loginState.value = LoginState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.loginWithEmail(
                    com.example.moa_project.network.EmailLoginRequest(loginId, password)
                )
                TokenManager.saveTokens(response.token, response.refreshToken)
                TokenManager.saveUserInfo(response.user.id, response.user.nickname)
                _loginState.value = LoginState.Success("email", response.token)
                onSuccess()
            } catch (e: retrofit2.HttpException) {
                val msg = if (e.code() == 401) "아이디 또는 비밀번호가 올바르지 않습니다." else "로그인 실패 (${e.code()})"
                Log.e("AuthViewModel", "Email login HTTP ${e.code()}", e)
                _loginState.value = LoginState.Error(msg)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Email login error", e)
                _loginState.value = LoginState.Error("서버에 연결할 수 없습니다.")
            }
        }
    }

    fun signup(loginId: String, email: String, password: String, nickname: String, onSuccess: () -> Unit = {}) {
        _loginState.value = LoginState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.signup(
                    com.example.moa_project.network.SignupRequest(loginId, email, password, nickname)
                )
                TokenManager.saveTokens(response.token, response.refreshToken)
                TokenManager.saveUserInfo(response.user.id, response.user.nickname)
                _loginState.value = LoginState.Success("email", response.token)
                onSuccess()
            } catch (e: retrofit2.HttpException) {
                val msg = when (e.code()) {
                    409 -> "이미 사용 중인 아이디 또는 이메일입니다."
                    400 -> "입력 정보를 확인해주세요."
                    else -> "회원가입 실패 (${e.code()})"
                }
                Log.e("AuthViewModel", "Signup HTTP ${e.code()}", e)
                _loginState.value = LoginState.Error(msg)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Signup error", e)
                _loginState.value = LoginState.Error("서버에 연결할 수 없습니다.")
            }
        }
    }

    fun logout() {
        TokenManager.clear()
        _loginState.value = LoginState.Idle
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val provider: String, val token: String) : LoginState()
    data class Error(val message: String) : LoginState()
}
