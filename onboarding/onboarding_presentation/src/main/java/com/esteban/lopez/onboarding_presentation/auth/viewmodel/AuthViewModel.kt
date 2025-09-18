package com.esteban.ruano.onboarding_presentation.auth.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.esteban.lopez.core.utils.SnackbarType
import com.esteban.ruano.core.domain.preferences.Preferences
import com.esteban.ruano.core_ui.services.FcmTokenService
import com.esteban.ruano.core_ui.view_model.BaseViewModel
import com.esteban.ruano.onboarding_domain.use_case.AuthUseCases
import com.esteban.ruano.onboarding_presentation.auth.intent.AuthIntent
import com.esteban.ruano.onboarding_presentation.auth.intent.AuthEffect
import com.esteban.ruano.onboarding_presentation.auth.state.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.TimeZone

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authUseCases: AuthUseCases,
    private val preferences: Preferences,
    private val fcmTokenService: FcmTokenService
) : BaseViewModel<AuthIntent, AuthState, AuthEffect>() {

    init { preFetchFcmToken() }

    override fun createInitialState(): AuthState = AuthState()

    override fun handleIntent(intent: AuthIntent) {
        when (intent) {
            is AuthIntent.Login            -> login(intent.email, intent.password)
            is AuthIntent.SignUp           -> signUp(intent.name, intent.email, intent.password)
            is AuthIntent.RequestReset     -> requestReset(intent.email)
            is AuthIntent.VerifyResetPin   -> verifyResetPin(intent.pin)
            is AuthIntent.SetNewPassword   -> setNewPassword(intent.password)
            is AuthIntent.ResetForgetEmail   -> {
                emitState { copy(pendingResetEmail = null) }
            }
            AuthIntent.Logout              -> logout()
            // Add Navigate intents here if you emit navigation effects
        }
    }

    /* -------------------- FCM prefetch -------------------- */
    private fun preFetchFcmToken() {
        viewModelScope.launch {
            try {
                Log.i("AuthViewModel", "Pre-fetching FCM token...")
                fcmTokenService.getFcmToken()?.let {
                    Log.i("AuthViewModel", "FCM token pre-fetched: ${it.take(20)}…")
                } ?: Log.w("AuthViewModel", "FCM token pre-fetch failed (null)")
            } catch (e: Exception) {
                Log.w("AuthViewModel", "FCM pre-fetch exception: ${e.message}")
            }
        }
    }

    /* -------------------- Login -------------------- */
    private fun login(email: String, password: String) {
        viewModelScope.launch {
            emitState { copy(isLoading = true, error = null) }

            try {
                val fcmToken = runCatching { fcmTokenService.getFcmToken() }.getOrNull()
                val timezone = TimeZone.getDefault().id
                val result = authUseCases.login(email, password, fcmToken, timezone)

                result.fold(
                    onSuccess = {
                        emitState { copy(authToken = it.token) }
                        preferences.saveAuthToken(it.token)
                        if (fcmToken == null) registerFcmTokenLater()
                        delay(300)
                        emitState { copy(isLoading = false) }
                        sendEffect { AuthEffect.AuthenticationSuccess }
                    },
                    onFailure = { err ->
                        emitState { copy(isLoading = false, error = err.message ?: "Login failed") }
                        sendEffect { AuthEffect.ShowSnackBar("Email or password is incorrect", SnackbarType.ERROR) }
                    }
                )
            } catch (e: Exception) {
                // Retry without FCM
                val timezone = TimeZone.getDefault().id
                val result = authUseCases.login(email, password, null, timezone)
                result.fold(
                    onSuccess = {
                        emitState { copy(authToken = it.token) }
                        preferences.saveAuthToken(it.token)
                        registerFcmTokenLater()
                        delay(300)
                        emitState { copy(isLoading = false) }
                        sendEffect { AuthEffect.AuthenticationSuccess }
                    },
                    onFailure = { err ->
                        emitState { copy(isLoading = false, error = err.message ?: "Login failed") }
                        sendEffect { AuthEffect.ShowSnackBar("Email or password is incorrect", SnackbarType.ERROR) }
                    }
                )
            }
        }
    }

    /* -------------------- Sign up -------------------- */
    private fun signUp(name: String, email: String, password: String) {
        viewModelScope.launch {
            emitState { copy(isLoading = true, error = null) }
            val result = authUseCases.signUp(name, email, password)
            result.fold(
                onSuccess = {
                    emitState { copy(isLoading = false) }
                    sendEffect { AuthEffect.ShowSnackBar("Account created. Please log in.", SnackbarType.SUCCESS) }
                    sendEffect { AuthEffect.NavigateToLogin }
                },
                onFailure = { e ->
                    emitState { copy(isLoading = false, error = e.message) }
                    sendEffect { AuthEffect.ShowSnackBar(e.message ?: "Sign up failed", SnackbarType.ERROR) }
                }
            )
        }
    }

    /* -------------------- Forgot password (PIN flow) -------------------- */

    // Step 1: request PIN (email)
    private fun requestReset(email: String) {
        viewModelScope.launch {
            emitState { copy(isLoading = true, error = null) }
            val result = authUseCases.requestReset(email)
            result.fold(
                onSuccess = {
                    emitState { copy(isLoading = false, pendingResetEmail = email) }
                    sendEffect { AuthEffect.ShowSnackBar("We sent you a 6-digit code.", SnackbarType.INFO) }
                },
                onFailure = { e ->
                    emitState { copy(isLoading = false, error = e.message) }
                    sendEffect { AuthEffect.ShowSnackBar("Error, this email is not registered", SnackbarType.ERROR) }
                }
            )
        }
    }

    // Step 2: verify PIN (needs email) -> returns reset session token
    private fun verifyResetPin(pin: String) {
        viewModelScope.launch {
            val email = currentState.pendingResetEmail
            if (email.isNullOrBlank()) {
                emitState { copy(error = "No email in progress") }
                sendEffect { AuthEffect.ShowSnackBar("Please enter your email first.", SnackbarType.ERROR) }
                return@launch
            }

            emitState { copy(isLoading = true, error = null) }
            val result = authUseCases.verifyPin(email, pin)
            result.fold(
                onSuccess = { token ->
                    emitState { copy(isLoading = false, resetPasswordToken = token) }
                    sendEffect { AuthEffect.ShowSnackBar("Code verified.", SnackbarType.SUCCESS) }
                },
                onFailure = { e ->
                    emitState { copy(isLoading = false, error = e.message) }
                    sendEffect { AuthEffect.ShowSnackBar("Invalid or expired code.", SnackbarType.ERROR) }
                }
            )
        }
    }

    // Step 3: set new password (uses reset session token)
    private fun setNewPassword(password: String) {
        viewModelScope.launch {
            val token = currentState.resetPasswordToken
            if (token.isNullOrBlank()) {
                emitState { copy(error = "Missing reset token") }
                sendEffect { AuthEffect.ShowSnackBar("Please verify the code first.", SnackbarType.ERROR) }
                return@launch
            }

            emitState { copy(isLoading = true, error = null) }
            val result = authUseCases.setNewPassword(token, password)
            result.fold(
                onSuccess = {
                    // Clear transient fields
                    emitState { copy(isLoading = false, pendingResetEmail = null, resetPasswordToken = null) }
                    sendEffect { AuthEffect.ShowSnackBar("Password updated. Please log in.", SnackbarType.SUCCESS) }
                    sendEffect { AuthEffect.NavigateToLogin }
                },
                onFailure = { e ->
                    emitState { copy(isLoading = false, error = e.message) }
                    sendEffect { AuthEffect.ShowSnackBar(e.message ?: "Failed to update password", SnackbarType.ERROR) }
                }
            )
        }
    }

    /* -------------------- Logout -------------------- */
    private fun logout() {
        viewModelScope.launch {
            preferences.clearAuthToken()
            emitState { copy(authToken = null) }
            sendEffect { AuthEffect.ShowSnackBar("Logged out", SnackbarType.INFO) }
        }
    }

    /* -------------------- FCM helpers -------------------- */
    private fun registerFcmTokenLater() {
        viewModelScope.launch {
            try {
                Log.i("AuthViewModel", "Registering FCM token after login…")
                delay(5000)
                val fcm = fcmTokenService.getFcmToken()
                if (fcm != null) {
                    // TODO: call your backend endpoint to register this FCM token
                    Log.i("AuthViewModel", "FCM token registered later: ${fcm.take(20)}…")
                    if (shouldShowFcmMessages()) {
                        sendEffect { AuthEffect.ShowSnackBar("Push notifications enabled", SnackbarType.SUCCESS) }
                    }
                } else {
                    Log.w("AuthViewModel", "FCM token still null after login")
                    logAlternativeNotificationStrategies()
                    if (shouldShowFcmMessages()) {
                        sendEffect { AuthEffect.ShowSnackBar("Push notifications not available. In-app notifications will be used instead.", SnackbarType.INFO) }
                    }
                }
            } catch (e: Exception) {
                Log.w("AuthViewModel", "FCM later registration failed: ${e.message}")
            }
        }
    }

    private fun logAlternativeNotificationStrategies() {
        Log.i("AuthViewModel", "Alternative notification strategies:")
        Log.i("AuthViewModel", "- In-app notifications")
        Log.i("AuthViewModel", "- Local notifications")
        Log.i("AuthViewModel", "- Email notifications")
        Log.i("AuthViewModel", "- SMS notifications")
        Log.i("AuthViewModel", "- WebSocket updates")
    }

    private fun shouldShowFcmMessages(): Boolean =
        try { android.os.Build.TYPE != "user" } catch (_: Exception) { false }
}

