package ui.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import services.auth.AuthService
import ui.state.AuthState


enum class ForgotStep { RequestEmail, VerifyToken, SetPassword }


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


    init {
        viewModelScope.launch {
            if (authService.isAuthenticated()) {
                setAuthenticated()
            }
        }
    }

    fun setForgettingPassword(enabled: Boolean) { _isForgettingPassword.value = enabled }
    fun setForgotStep(step: ForgotStep) { _forgotStep.value = step }


    fun verifyResetToken(token: String) {
        viewModelScope.launch {
            val current = authState.value as? AuthState.Unauthenticated ?: return@launch
            _authState.value = current.copy(isLoading = true, errorMessage = null)
            runCatching {
                authService.verifyResetToken(token)
            }.onSuccess {
                _authState.value = current.copy(isLoading = false)
            }.onFailure {
                _authState.value = current.copy(isLoading = false, errorMessage = it.message)
            }
        }
    }

    fun forgotPassword() {
        val currentState = authState.value as? AuthState.Unauthenticated
        currentState?.let { state ->
            viewModelScope.launch {
                _authState.value = state.copy(isLoading = true, errorMessage = null)
                try {
                    authService.forgotPassword(state.email)
                    // success → maybe set a flag or message
                    _authState.value = state.copy(
                        isLoading = false,
                        errorMessage = null
                    )
                } catch (e: Exception) {
                    _authState.value = state.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to request reset"
                    )
                }
            }
        }
    }

    fun resetPassword(token: String, newPassword: String) {
        val currentState = authState.value as? AuthState.Unauthenticated
        currentState?.let { state ->
            viewModelScope.launch {
                _authState.value = state.copy(isLoading = true, errorMessage = null)
                try {
                    authService.resetPassword(token, newPassword)
                    // success → you might want to navigate to login screen
                    _authState.value = state.copy(
                        isLoading = false,
                        errorMessage = null
                    )
                } catch (e: Exception) {
                    _authState.value = state.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to reset password"
                    )
                }
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