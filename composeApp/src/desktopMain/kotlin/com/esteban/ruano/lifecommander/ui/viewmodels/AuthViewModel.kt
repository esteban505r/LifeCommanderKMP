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

class AuthViewModel(
    private val authService: AuthService,
) : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated(
        email = "",
        password = "",
        errorMessage = "",
        isLoading = false
    ))
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _isSignUp = MutableStateFlow(false)
    val isSignUp: StateFlow<Boolean> = _isSignUp.asStateFlow()

    init {
        viewModelScope.launch {
            if (authService.isAuthenticated()) {
                setAuthenticated()
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
                    authService.signUp(it.email, it.password)
                    setAuthenticated()
                }
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

    fun setLoading() {
        val currentState = authState.value as? AuthState.Unauthenticated
        currentState?.let {
            _authState.value = it.copy(
                isLoading = true,
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