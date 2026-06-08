package com.example.moa_project.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moa_project.network.CreateFixedSlotRequest
import com.example.moa_project.network.RetrofitClient
import com.example.moa_project.network.SignupRequest
import com.example.moa_project.network.TokenManager
import com.example.moa_project.util.MoaErrorLog
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SignUpPurposeOption(val label: String) {
    MEET_FRIENDS("친구들과 약속 잡기"),
    GROUP_COORDINATION("모임 일정을 한눈에 조율하기"),
    FIXED_SCHEDULE("수업·알바 같은 고정 일정 관리"),
    GUEST_LINK("게스트 링크로 비회원도 일정 맞추기"),
    CUSTOM("직접 입력"),
}

enum class SignUpBusyTimeOption(val label: String) {
    WEEKDAY_MORNING("평일 오전 (8–12시)"),
    WEEKDAY_AFTERNOON("평일 오후 (13–18시)"),
    WEEKDAY_EVENING("평일 저녁 (19–22시)"),
    WEEKEND("주말 (8–22시)"),
    CUSTOM("직접 입력"),
    SKIP("나중에 설정할게요"),
}

enum class FieldAvailability {
    Idle,
    Checking,
    Available,
    Duplicate,
    Failed,
}

data class SignUpUiState(
    val step: Int = 0,
    val loginId: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nickname: String = "",
    val loginIdAvailability: FieldAvailability = FieldAvailability.Idle,
    val emailAvailability: FieldAvailability = FieldAvailability.Idle,
    val selectedPurpose: SignUpPurposeOption? = null,
    val customPurpose: String = "",
    val selectedBusyTime: SignUpBusyTimeOption? = null,
    val customDays: Set<Int> = emptySet(),
    val customStartHour: Int = 9,
    val customEndHour: Int = 12,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
) {
    val hasLower: Boolean get() = password.any { it in 'a'..'z' }
    val hasUpper: Boolean get() = password.any { it in 'A'..'Z' }
    val hasSpecial: Boolean get() = password.any { !it.isLetterOrDigit() }
    val isPasswordValid: Boolean get() = hasLower && hasUpper && hasSpecial

    val purposeDisplay: String
        get() = when (selectedPurpose) {
            SignUpPurposeOption.CUSTOM -> customPurpose.trim()
            null -> ""
            else -> selectedPurpose.label
        }

    val busyTimeDisplay: String
        get() = when (selectedBusyTime) {
            SignUpBusyTimeOption.CUSTOM -> {
                if (customDays.isEmpty()) "직접 입력"
                else {
                    val dayLabels = listOf("월", "화", "수", "목", "금", "토", "일")
                    val days = customDays.sorted().joinToString(", ") { dayLabels[it - 1] }
                    "$days ${customStartHour}–${customEndHour}시"
                }
            }
            SignUpBusyTimeOption.SKIP -> "나중에 설정"
            null -> ""
            else -> selectedBusyTime.label
        }
}

class SignUpViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    private var loginIdCheckJob: Job? = null
    private var emailCheckJob: Job? = null
    private var loginIdCheckSeq = 0
    private var emailCheckSeq = 0

    fun updateLoginId(value: String) {
        _uiState.update {
            it.copy(
                loginId = value,
                loginIdAvailability = if (value.trim().isBlank()) FieldAvailability.Idle else FieldAvailability.Checking,
                errorMessage = null,
            )
        }
        scheduleLoginIdCheck(value)
    }

    fun updateEmail(value: String) {
        val trimmed = value.trim()
        _uiState.update {
            it.copy(
                email = value,
                emailAvailability = when {
                    trimmed.isBlank() -> FieldAvailability.Idle
                    !isValidEmail(trimmed) -> FieldAvailability.Idle
                    else -> FieldAvailability.Checking
                },
                errorMessage = null,
            )
        }
        scheduleEmailCheck(value)
    }

    fun updatePassword(value: String) = _uiState.update { it.copy(password = value, errorMessage = null) }
    fun updateConfirmPassword(value: String) = _uiState.update { it.copy(confirmPassword = value, errorMessage = null) }

    fun updateNickname(value: String) {
        if (value.length <= 20) _uiState.update { it.copy(nickname = value, errorMessage = null) }
    }

    fun selectPurpose(option: SignUpPurposeOption) =
        _uiState.update { it.copy(selectedPurpose = option, errorMessage = null) }

    fun updateCustomPurpose(value: String) {
        if (value.length <= 20) _uiState.update { it.copy(customPurpose = value, errorMessage = null) }
    }

    fun selectBusyTime(option: SignUpBusyTimeOption) =
        _uiState.update { it.copy(selectedBusyTime = option, errorMessage = null) }

    fun toggleCustomDay(day: Int) {
        _uiState.update { state ->
            val next = state.customDays.toMutableSet()
            if (next.contains(day)) next.remove(day) else next.add(day)
            state.copy(customDays = next, errorMessage = null)
        }
    }

    fun updateCustomStartHour(hour: Int) =
        _uiState.update { it.copy(customStartHour = hour.coerceIn(0, 23), errorMessage = null) }

    fun updateCustomEndHour(hour: Int) =
        _uiState.update { it.copy(customEndHour = hour.coerceIn(1, 24), errorMessage = null) }

    fun consumeErrorMessage() = _uiState.update { it.copy(errorMessage = null) }

    fun goBack(onExit: () -> Unit) {
        val state = _uiState.value
        if (state.step == 0) onExit() else _uiState.update { it.copy(step = it.step - 1, errorMessage = null) }
    }

    fun goNext(onComplete: () -> Unit) {
        val state = _uiState.value
        when (state.step) {
            0 -> {
                viewModelScope.launch {
                    val loginId = state.loginId.trim()
                    val email = state.email.trim()
                    val loginAvailability = fetchLoginIdAvailability(loginId)
                    val emailAvailability = if (isValidEmail(email)) fetchEmailAvailability(email) else FieldAvailability.Idle
                    _uiState.update {
                        it.copy(
                            loginIdAvailability = loginAvailability,
                            emailAvailability = emailAvailability,
                        )
                    }
                    val latest = _uiState.value
                    validateAccount(latest)?.let { setError(it) } ?: advance()
                }
            }
            1 -> validateNickname(state)?.let { setError(it) } ?: advance()
            2 -> validatePurpose(state)?.let { setError(it) } ?: advance()
            3 -> validateBusyTime(state)?.let { setError(it) } ?: advance()
            4 -> submit(onComplete)
        }
    }

    private suspend fun fetchLoginIdAvailability(loginId: String): FieldAvailability {
        if (loginId.isBlank()) return FieldAvailability.Idle
        return runCatching { RetrofitClient.instance.checkLoginIdAvailability(loginId) }
            .map { if (it.available) FieldAvailability.Available else FieldAvailability.Duplicate }
            .getOrElse {
                MoaErrorLog.log("SignUpViewModel", "fetchLoginIdAvailability", it)
                FieldAvailability.Failed
            }
    }

    private suspend fun fetchEmailAvailability(email: String): FieldAvailability {
        if (email.isBlank()) return FieldAvailability.Idle
        return runCatching { RetrofitClient.instance.checkEmailAvailability(email) }
            .map { if (it.available) FieldAvailability.Available else FieldAvailability.Duplicate }
            .getOrElse {
                MoaErrorLog.log("SignUpViewModel", "fetchEmailAvailability", it)
                FieldAvailability.Failed
            }
    }

    private fun scheduleLoginIdCheck(raw: String) {
        loginIdCheckJob?.cancel()
        val trimmed = raw.trim()
        if (trimmed.isBlank()) {
            _uiState.update { it.copy(loginIdAvailability = FieldAvailability.Idle) }
            return
        }
        val seq = ++loginIdCheckSeq
        loginIdCheckJob = viewModelScope.launch {
            delay(CHECK_DEBOUNCE_MS)
            if (seq != loginIdCheckSeq || trimmed != _uiState.value.loginId.trim()) return@launch
            val availability = fetchLoginIdAvailability(trimmed)
            if (seq != loginIdCheckSeq || trimmed != _uiState.value.loginId.trim()) return@launch
            _uiState.update { it.copy(loginIdAvailability = availability) }
        }
    }

    private fun scheduleEmailCheck(raw: String) {
        emailCheckJob?.cancel()
        val trimmed = raw.trim()
        if (trimmed.isBlank()) {
            _uiState.update { it.copy(emailAvailability = FieldAvailability.Idle) }
            return
        }
        if (!isValidEmail(trimmed)) {
            _uiState.update { it.copy(emailAvailability = FieldAvailability.Idle) }
            return
        }
        val seq = ++emailCheckSeq
        emailCheckJob = viewModelScope.launch {
            delay(CHECK_DEBOUNCE_MS)
            if (seq != emailCheckSeq || trimmed != _uiState.value.email.trim()) return@launch
            val availability = fetchEmailAvailability(trimmed)
            if (seq != emailCheckSeq || trimmed != _uiState.value.email.trim()) return@launch
            _uiState.update { it.copy(emailAvailability = availability) }
        }
    }

    private fun advance() = _uiState.update { it.copy(step = it.step + 1, errorMessage = null) }

    private fun setError(message: String) = _uiState.update { it.copy(errorMessage = message) }

    private fun validateAccount(state: SignUpUiState): String? = when {
        state.loginId.isBlank() || state.email.isBlank() || state.password.isBlank() ->
            "모든 항목을 입력해 주세요."
        !isValidEmail(state.email.trim()) -> "올바른 이메일 형식을 입력해 주세요."
        state.loginIdAvailability == FieldAvailability.Checking ||
            state.emailAvailability == FieldAvailability.Checking ->
            "중복 확인 중입니다. 잠시만 기다려 주세요."
        state.loginIdAvailability == FieldAvailability.Failed -> "아이디 중복 확인에 실패했습니다."
        state.loginIdAvailability == FieldAvailability.Duplicate -> "중복된 아이디입니다."
        state.loginIdAvailability != FieldAvailability.Available -> "아이디 중복 확인이 필요합니다."
        state.emailAvailability == FieldAvailability.Failed -> "이메일 중복 확인에 실패했습니다."
        state.emailAvailability == FieldAvailability.Duplicate -> "중복된 이메일입니다."
        state.emailAvailability != FieldAvailability.Available -> "이메일 중복 확인이 필요합니다."
        !state.isPasswordValid -> "비밀번호 조건을 모두 충족해 주세요."
        state.password != state.confirmPassword -> "비밀번호가 일치하지 않습니다."
        else -> null
    }

    private fun validateNickname(state: SignUpUiState): String? =
        if (state.nickname.trim().isBlank()) "닉네임을 입력해 주세요." else null

    private fun validatePurpose(state: SignUpUiState): String? = when {
        state.selectedPurpose == null -> "사용 목적을 선택해 주세요."
        state.selectedPurpose == SignUpPurposeOption.CUSTOM && state.customPurpose.trim().isBlank() ->
            "목적을 입력해 주세요."
        else -> null
    }

    private fun validateBusyTime(state: SignUpUiState): String? = when {
        state.selectedBusyTime == null -> "바쁜 시간을 선택해 주세요."
        state.selectedBusyTime == SignUpBusyTimeOption.CUSTOM && state.customDays.isEmpty() ->
            "요일을 하나 이상 선택해 주세요."
        state.selectedBusyTime == SignUpBusyTimeOption.CUSTOM &&
            state.customStartHour >= state.customEndHour -> "종료 시간은 시작 시간보다 늦어야 해요."
        else -> null
    }

    private fun submit(onComplete: () -> Unit) {
        val state = _uiState.value
        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.signup(
                    SignupRequest(
                        loginId = state.loginId.trim(),
                        email = state.email.trim(),
                        password = state.password,
                        nickname = state.nickname.trim(),
                    ),
                )
                TokenManager.saveTokens(response.token, response.refreshToken)
                TokenManager.saveUserInfo(response.user.id, response.user.nickname, response.user.profileImageUrl)
                createBusyTimeSlots(state)
                _uiState.update { it.copy(isSubmitting = false) }
                onComplete()
            } catch (e: retrofit2.HttpException) {
                MoaErrorLog.log("SignUpViewModel", "submit", e, mapOf("httpCode" to e.code().toString()))
                val msg = when (e.code()) {
                    409 -> "이미 사용 중인 아이디 또는 이메일입니다."
                    400 -> "입력 정보를 확인해 주세요."
                    else -> "회원가입 실패 (${e.code()})"
                }
                _uiState.update { it.copy(isSubmitting = false, errorMessage = msg) }
            } catch (e: Exception) {
                MoaErrorLog.log("SignUpViewModel", "submit", e)
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = MoaErrorLog.userMessage(e, "서버에 연결할 수 없습니다."),
                    )
                }
            }
        }
    }

    private suspend fun createBusyTimeSlots(state: SignUpUiState) {
        val slots = when (state.selectedBusyTime) {
            SignUpBusyTimeOption.WEEKDAY_MORNING -> weekdaySlots(8, 12)
            SignUpBusyTimeOption.WEEKDAY_AFTERNOON -> weekdaySlots(13, 18)
            SignUpBusyTimeOption.WEEKDAY_EVENING -> weekdaySlots(19, 22)
            SignUpBusyTimeOption.WEEKEND -> (6..7).map { CreateFixedSlotRequest(it, 8, 22, "바쁜 시간") }
            SignUpBusyTimeOption.CUSTOM -> state.customDays.map { day ->
                CreateFixedSlotRequest(day, state.customStartHour, state.customEndHour, "바쁜 시간")
            }
            SignUpBusyTimeOption.SKIP, null -> emptyList()
        }
        slots.forEach { slot -> runCatching { RetrofitClient.instance.addFixedTimeSlot(slot) } }
    }

    private fun weekdaySlots(start: Int, end: Int) =
        (1..5).map { CreateFixedSlotRequest(it, start, end, "바쁜 시간") }

    private fun isValidEmail(email: String): Boolean =
        android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

    companion object {
        private const val CHECK_DEBOUNCE_MS = 400L
    }
}
