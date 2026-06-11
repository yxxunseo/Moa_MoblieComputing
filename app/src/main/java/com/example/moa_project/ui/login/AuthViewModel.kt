package com.example.moa_project.ui.login

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moa_project.network.RetrofitClient
import com.example.moa_project.network.GoogleLoginRequest
import com.example.moa_project.network.KakaoLoginRequest
import com.example.moa_project.BuildConfig
import com.example.moa_project.network.TokenManager
import com.example.moa_project.util.AuthDebugHelper
import com.example.moa_project.util.MoaErrorLog
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

    fun loginWithKakao(context: Context) {
        _loginState.value = LoginState.Loading
        if (BuildConfig.DEBUG) {
            AuthDebugHelper.logStartupDiagnostics(context)
            Log.i("AuthViewModel", "loginWithKakao 시작")
        }

        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                MoaErrorLog.log("AuthViewModel", "loginWithKakaoAccount", error)
                if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                    _loginState.value = LoginState.Error(
                        "카카오 로그인 창이 닫혔어요. 에뮬레이터면 Chrome·인터넷을 확인하거나 실기기(카카오톡)로 시도해 주세요.",
                    )
                } else {
                    _loginState.value = LoginState.Error(formatKakaoError(error))
                }
            } else if (token != null) {
                Log.i("AuthViewModel", "카카오계정 로그인 SDK 성공 | tokenLen=${token.accessToken.length}")
                sendKakaoTokenToServer(context, token.accessToken)
            }
        }

        val kakaoTalkAvailable = UserApiClient.instance.isKakaoTalkLoginAvailable(context)
        if (BuildConfig.DEBUG) {
            Log.i(
                "AuthViewModel",
                "loginWithKakao | kakaoTalk=$kakaoTalkAvailable | redirect=kakao${BuildConfig.KAKAO_APP_KEY}://oauth",
            )
        }

        if (kakaoTalkAvailable) {
            UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                if (error != null) {
                    MoaErrorLog.log("AuthViewModel", "loginWithKakaoTalk", error)
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        _loginState.value = LoginState.Idle
                        return@loginWithKakaoTalk
                    }
                    UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
                } else if (token != null) {
                    Log.i("AuthViewModel", "카카오톡 로그인 SDK 성공 | tokenLen=${token.accessToken.length}")
                    sendKakaoTokenToServer(context, token.accessToken)
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
        }
    }

    private fun sendKakaoTokenToServer(context: Context, accessToken: String) {
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                MoaErrorLog.log("AuthViewModel", "kakaoMe", error)
            }
            val nickname = user?.kakaoAccount?.profile?.nickname
            val profileImageUrl = user?.kakaoAccount?.profile?.profileImageUrl
            if (BuildConfig.DEBUG) {
                Log.i("AuthViewModel", "카카오 프로필 | nickname=$nickname")
            }
            submitKakaoLogin(context, accessToken, nickname, profileImageUrl)
        }
    }

    private fun submitKakaoLogin(
        context: Context,
        accessToken: String,
        nickname: String?,
        profileImageUrl: String?,
    ) {
        viewModelScope.launch {
            try {
                Log.i("AuthViewModel", "서버 카카오 인증 요청 | url=${RetrofitClient.BASE_URL}api/auth/kakao")
                val response = RetrofitClient.instance.loginWithKakao(
                    KakaoLoginRequest(
                        accessToken = accessToken,
                        nickname = nickname,
                        profileImageUrl = profileImageUrl,
                    ),
                )
                TokenManager.saveTokens(response.token, response.refreshToken)
                TokenManager.saveUserInfo(response.user.id, response.user.nickname, response.user.profileImageUrl)
                val needsNickname = isPlaceholderNickname(response.user.nickname)
                _loginState.value = LoginState.Success(
                    provider = "kakao",
                    token = response.token,
                    isNewUser = response.isNewUser,
                    needsNicknameSetup = needsNickname,
                )
                Log.i(
                    "AuthViewModel",
                    "카카오 로그인 완료 | userId=${response.user.id} | isNew=${response.isNewUser} | needsName=$needsNickname",
                )
            } catch (e: Exception) {
                MoaErrorLog.log("AuthViewModel", "loginWithKakao(server)", e)
                val msg = MoaErrorLog.userMessage(e, "서버 인증 실패")
                val hint = if (msg.contains("카카오") || (e as? retrofit2.HttpException)?.code() == 401) {
                    "\n\n${AuthDebugHelper.kakaoSetupHint(context)}"
                } else {
                    ""
                }
                _loginState.value = LoginState.Error(msg + hint)
            }
        }
    }

    fun reportGoogleConfigError(context: Context) {
        _loginState.value = LoginState.Error(
            "구글 로그인 설정이 비어 있습니다.\nlocal.properties의 GOOGLE_CLIENT_ID를 확인해 주세요.",
        )
        if (BuildConfig.DEBUG) AuthDebugHelper.logStartupDiagnostics(context)
    }

    fun handleGoogleSignInResult(context: Context, intent: android.content.Intent?, onSuccess: () -> Unit = {}) {
        _loginState.value = LoginState.Loading
        if (BuildConfig.DEBUG) {
            AuthDebugHelper.logStartupDiagnostics(context)
            Log.i("AuthViewModel", "handleGoogleSignInResult 시작 | clientId=${BuildConfig.GOOGLE_CLIENT_ID.take(20)}…")
        }
        try {
            val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(intent)
            val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                Log.i("AuthViewModel", "구글 로그인 SDK 성공")
                viewModelScope.launch {
                    try {
                        val response = RetrofitClient.instance.loginWithGoogle(GoogleLoginRequest(idToken))
                        TokenManager.saveTokens(response.token, response.refreshToken)
                        TokenManager.saveUserInfo(response.user.id, response.user.nickname, response.user.profileImageUrl)
                        _loginState.value = LoginState.Success("google", response.token)
                        onSuccess()
                    } catch (e: Exception) {
                        MoaErrorLog.log("AuthViewModel", "loginWithGoogle(server)", e)
                        _loginState.value = LoginState.Error(MoaErrorLog.userMessage(e, "서버 인증 실패"))
                    }
                }
            } else {
                MoaErrorLog.log("AuthViewModel", "handleGoogleSignInResult", "idToken is null")
                _loginState.value = LoginState.Error("구글 로그인 실패: 토큰이 없습니다.")
            }
        } catch (e: com.google.android.gms.common.api.ApiException) {
            MoaErrorLog.log("AuthViewModel", "handleGoogleSignInResult", e, mapOf("statusCode" to e.statusCode.toString()))
            when (e.statusCode) {
                12501 -> {
                    // 사용자가 로그인 창을 직접 닫음 — 오류 메시지 표시 불필요
                    _loginState.value = LoginState.Idle
                }
                10 -> {
                    _loginState.value = LoginState.Error(
                        "구글 로그인 설정 오류(DEVELOPER_ERROR).\n\n${AuthDebugHelper.googleSetupHint(context)}",
                    )
                }
                else -> {
                    _loginState.value = LoginState.Error("구글 로그인 실패 (코드: ${e.statusCode})")
                }
            }
        } catch (e: Exception) {
            MoaErrorLog.log("AuthViewModel", "handleGoogleSignInResult", e)
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
                TokenManager.saveUserInfo(response.user.id, response.user.nickname, response.user.profileImageUrl)
                _loginState.value = LoginState.Success("email", response.token)
                onSuccess()
            } catch (e: retrofit2.HttpException) {
                MoaErrorLog.log("AuthViewModel", "loginWithEmail", e, mapOf("httpCode" to e.code().toString()))
                val msg = if (e.code() == 401) "아이디 또는 비밀번호가 올바르지 않습니다." else "로그인 실패 (${e.code()})"
                _loginState.value = LoginState.Error(msg)
            } catch (e: Exception) {
                MoaErrorLog.log("AuthViewModel", "loginWithEmail", e)
                _loginState.value = LoginState.Error(MoaErrorLog.userMessage(e, "서버에 연결할 수 없습니다."))
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
                TokenManager.saveUserInfo(response.user.id, response.user.nickname, response.user.profileImageUrl)
                _loginState.value = LoginState.Success("email", response.token)
                onSuccess()
            } catch (e: retrofit2.HttpException) {
                MoaErrorLog.log("AuthViewModel", "signup", e, mapOf("httpCode" to e.code().toString()))
                val msg = when (e.code()) {
                    409 -> "이미 사용 중인 아이디 또는 이메일입니다."
                    400 -> "입력 정보를 확인해주세요."
                    else -> "회원가입 실패 (${e.code()})"
                }
                _loginState.value = LoginState.Error(msg)
            } catch (e: Exception) {
                MoaErrorLog.log("AuthViewModel", "signup", e)
                _loginState.value = LoginState.Error(MoaErrorLog.userMessage(e, "서버에 연결할 수 없습니다."))
            }
        }
    }

    fun consumeLoginState() {
        _loginState.value = LoginState.Idle
    }

    fun logout() {
        TokenManager.clear()
        _loginState.value = LoginState.Idle
    }

    private fun formatKakaoError(error: Throwable): String {
        val friendly = MoaErrorLog.userMessage(error, fallback = "")
        if (friendly.isNotBlank() && !friendly.startsWith("요청")) return friendly
        return "카카오 로그인 실패: ${error.message ?: "알 수 없는 오류"}"
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(
        val provider: String,
        val token: String,
        val isNewUser: Boolean = false,
        val needsNicknameSetup: Boolean = false,
    ) : LoginState()
    data class Error(val message: String) : LoginState()
}
