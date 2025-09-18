package ui.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import services.auth.AuthService
import ui.state.AuthState


enum class ForgotStep { RequestEmail, VerifyPin, SetPassword }


class AuthViewModel(
    private val authService: AuthService,
) : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated(
        name = "",
        email = "",
        password = "",
        errorMessage = "",
        isLoading = false
    ))
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _isSignUp = MutableStateFlow(false)
    val isSignUp: StateFlow<Boolean> = _isSignUp.asStateFlow()

    private val _isForgettingPassword = MutableStateFlow(false)
    val isForgettingPassword: StateFlow<Boolean> = _isForgettingPassword.asStateFlow()

    private val _forgotStep = MutableStateFlow(ForgotStep.RequestEmail)
    val forgotStep: StateFlow<ForgotStep> = _forgotStep.asStateFlow()

    private val _resetToken = MutableStateFlow<String?>(null)
    val resetToken: StateFlow<String?> = _resetToken.asStateFlow()


    init {
        viewModelScope.launch {
            if (authService.isAuthenticated()) {
                setAuthenticated()
            }
        }
    }

    fun setForgettingPassword(enabled: Boolean) { _isForgettingPassword.value = enabled }
    fun setForgotStep(step: ForgotStep) { _forgotStep.value = step }


    // 1) Request PIN (unchanged behavior)
    fun forgotPassword() {
        val current = authState.value as? AuthState.Unauthenticated ?: return
        viewModelScope.launch {
            _authState.value = current.copy(isLoading = true, errorMessage = null)
            runCatching {
                authService.forgotPassword(current.email)
            }.onSuccess {
                _authState.value = current.copy(isLoading = false, errorMessage = null)
            }.onFailure {
                _authState.value = current.copy(
                    isLoading = false,
                    errorMessage = it.message ?: "Failed to request reset"
                )
            }
        }
    }


    // 2) Verify PIN (uses email + pin) → stores reset session token
    fun verifyResetPin(pin: String) {
        val current = authState.value as? AuthState.Unauthenticated ?: return
        viewModelScope.launch {
            _authState.value = current.copy(isLoading = true, errorMessage = null)
            runCatching {
                authService.verifyResetPin(email = current.email, pin = pin)
            }.onSuccess { token ->
                _resetToken.value = token
                _authState.value = current.copy(isLoading = false, errorMessage = null)
            }.onFailure {
                _authState.value = current.copy(
                    isLoading = false,
                    errorMessage = it.message ?: "Invalid or expired code"
                )
            }
        }
    }


    // 3) Reset password using the stored reset session token
    fun resetPassword(newPassword: String) {
        val current = authState.value as? AuthState.Unauthenticated ?: return
        val token = _resetToken.value
        if (token.isNullOrBlank()) {
            // No verified token yet
            _authState.value = current.copy(
                isLoading = false,
                errorMessage = "Please verify the code first."
            )
            return
        }

        viewModelScope.launch {
            _authState.value = current.copy(isLoading = true, errorMessage = null)
            runCatching {
                authService.resetPasswordWithSession(resetToken = token, newPassword = newPassword)
            }.onSuccess {
                // Clear the token after successful reset
                _resetToken.value = null
                _authState.value = current.copy(isLoading = false, errorMessage = null)
            }.onFailure {
                _authState.value = current.copy(
                    isLoading = false,
                    errorMessage = it.message ?: "Failed to reset password"
                )
            }
        }
    }


    fun updateEmail(newEmail: String) {
        val currentState = authState.value as? AuthState.Unauthenticated
        currentState?.let {
            _authState.value = it.copy(
                email = newEmail,
                errorMessage = it.errorMessage,
                isLoading = it.isLoading
            )
        }
    }

    fun setSignUp(isSignUp: Boolean) {
        _isSignUp.value = isSignUp
    }

    fun updateName(newName: String) {
        val currentState = authState.value as? AuthState.Unauthenticated
        currentState?.let {
            _authState.value = it.copy(
                name = newName,
                errorMessage = it.errorMessage,
                isLoading = it.isLoading
            )
        }
    }

    fun updatePassword(newPassword: String) {
        val currentState = authState.value as? AuthState.Unauthenticated
        currentState?.let {
            _authState.value = it.copy(
                password = newPassword,
                errorMessage = it.errorMessage,
                isLoading = it.isLoading
            )
        }
    }

    fun login() {
        viewModelScope.launch {
            try {
                setLoading()
                val currentState = authState.value as? AuthState.Unauthenticated
                currentState?.let {
                    authService.login(it.email, it.password)
                    setAuthenticated()
                }
                setLoading(false)
            } catch (e: Exception) {
                setError("Failed to login: ${e.message}")
            }
        }
    }

    fun signUp() {
        viewModelScope.launch {
            try {
                setLoading()
                val currentState = authState.value as? AuthState.Unauthenticated
                currentState?.let {
                    authService.signUp(it.name,it.email, it.password)
                    setSignUp(false)
                }
                setLoading(false)
            } catch (e: Exception) {
                setError("Failed to sign up: ${e.message}")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authService.logout()
            _authState.value = AuthState.Unauthenticated(
                email = "",
                password = "",
                errorMessage = null,
                isLoading = false
            )
        }
    }

    fun setLoading(value: Boolean = true) {
        val currentState = authState.value as? AuthState.Unauthenticated
        currentState?.let {
            _authState.value = it.copy(
                isLoading = value,
            )
        }
    }

    fun setAuthenticated() {
        _authState.value = AuthState.Authenticated
    }


    fun setError(message: String) {
        val currentState = authState.value as? AuthState.Unauthenticated
        currentState?.let {
            _authState.value = it.copy(
                errorMessage = message,
                isLoading = false
            )
        }
    }

    fun clearError() {
        val currentState = authState.value as? AuthState.Unauthenticated
        currentState?.let {
            _authState.value = it.copy(
                errorMessage = null,
                isLoading = false
            )
        }
    }
} 