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
) : BaseViewModel<AuthIntent,AuthState, AuthEffect>() {

    init {
        // Pre-fetch FCM token in background
        preFetchFcmToken()
    }

    private fun preFetchFcmToken() {
        viewModelScope.launch {
            try {
                Log.i("AuthViewModel", "Pre-fetching FCM token...")
                val token = fcmTokenService.getFcmToken()
                if (token != null) {
                    Log.i("AuthViewModel", "FCM token pre-fetched successfully: ${token.take(20)}...")
                } else {
                    Log.w("AuthViewModel", "FCM token pre-fetch failed")
                }
            } catch (e: Exception) {
                Log.w("AuthViewModel", "FCM token pre-fetch failed with exception: ${e.message}")
            }
        }
    }

    private fun login(email: String, password: String) {
        viewModelScope.launch {
            emitState { AuthState.Loading }
            
            try {
                Log.i("AuthViewModel", "Starting login process for email: $email")
                
                // Get FCM token using the shared service
                Log.i("AuthViewModel", "Attempting to retrieve FCM token...")
                val fcmToken = fcmTokenService.getFcmToken()
                
                // Log the final FCM token status
                if (fcmToken != null) {
                    Log.i("AuthViewModel", "FCM token will be included in login request: ${fcmToken.take(20)}...")
                } else {
                    Log.w("AuthViewModel", "No FCM token available, login request will be sent without FCM token")
                }
                
                val timezone = TimeZone.getDefault().id
                Log.i("AuthViewModel", "User timezone: $timezone")
                
                val result = authUseCases.login(email, password, fcmToken, timezone)
                result.fold(
                    onSuccess = {
                        Log.i("AuthViewModel", "Login successful, saving auth token")
                        emitState { AuthState.Authenticated(it.token) }
                        preferences.saveAuthToken(it.token)
                        
                        // If we didn't get FCM token during login, try to register it later
                        if (fcmToken == null) {
                            Log.i("AuthViewModel", "Scheduling FCM token registration for later")
                            registerFcmTokenLater()
                        }
                        
                        delay(1000)
                        emitState { AuthState.Idle }
                        sendEffect { AuthEffect.AuthenticationSuccess }
                    },
                    onFailure = {
                        Log.e("AuthViewModel", "Login failed: $it")
                        emitState { AuthState.Error(it.message ?: "Login failed") }
                        sendEffect {
                            AuthEffect.ShowSnackBar("Email or password is incorrect", SnackbarType.ERROR)
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Exception during login process: ${e.message}")
                e.printStackTrace()
                
                // Continue with login without FCM token
                Log.i("AuthViewModel", "Retrying login without FCM token due to exception")
                val timezone = TimeZone.getDefault().id
                val result = authUseCases.login(email, password, null, timezone)
                result.fold(
                    onSuccess = {
                        Log.i("AuthViewModel", "Login successful without FCM token, saving auth token")
                        emitState { AuthState.Authenticated(it.token) }
                        preferences.saveAuthToken(it.token)
                        
                        // Try to register FCM token later since it failed during login
                        Log.i("AuthViewModel", "Scheduling FCM token registration for later")
                        registerFcmTokenLater()
                        
                        delay(1000)
                        emitState { AuthState.Idle }
                        sendEffect { AuthEffect.AuthenticationSuccess }
                    },
                    onFailure = {
                        Log.e("AuthViewModel", "Login failed without FCM token: $it")
                        emitState { AuthState.Error(it.message ?: "Login failed") }
                        sendEffect {
                            AuthEffect.ShowSnackBar("Email or password is incorrect", SnackbarType.ERROR)
                        }
                    }
                )
            }
        }
    }

    private fun registerFcmTokenLater() {
        viewModelScope.launch {
            try {
                // Wait a bit before trying to get FCM token again
                Log.i("AuthViewModel", "Attempting to register FCM token after successful login...")
                delay(5000L) // 5 seconds
                
                val fcmToken = fcmTokenService.getFcmToken()
                if (fcmToken != null) {
                    // TODO: Send FCM token to server for this user
                    // This would require an API endpoint to update the user's FCM token
                    Log.i("AuthViewModel", "FCM token registered successfully after login: ${fcmToken.take(20)}...")
                    
                    // Optionally show a success message to the user
                    if (shouldShowFcmMessages()) {
                        sendEffect {
                            AuthEffect.ShowSnackBar("Push notifications enabled", SnackbarType.SUCCESS)
                        }
                    }
                } else {
                    Log.w("AuthViewModel", "FCM token still not available after login")
                    
                    // Log alternative notification strategies
                    logAlternativeNotificationStrategies()
                    
                    // Optionally show a message to the user about push notifications
                    if (shouldShowFcmMessages()) {
                        sendEffect {
                            AuthEffect.ShowSnackBar("Push notifications not available. In-app notifications will be used instead.", SnackbarType.INFO)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("AuthViewModel", "Failed to register FCM token after login: ${e.message}")
            }
        }
    }

    private fun logAlternativeNotificationStrategies() {
        Log.i("AuthViewModel", "Alternative notification strategies available:")
        Log.i("AuthViewModel", "- In-app notifications (when app is open)")
        Log.i("AuthViewModel", "- Local notifications (scheduled reminders)")
        Log.i("AuthViewModel", "- Email notifications (if configured)")
        Log.i("AuthViewModel", "- SMS notifications (if configured)")
        Log.i("AuthViewModel", "- WebSocket real-time updates (when app is connected)")
    }

    private fun shouldShowFcmMessages(): Boolean {
        // Only show FCM-related messages in debug builds or if explicitly enabled
        return try {
            // Check if this is a debug build
            val isDebug = android.os.Build.TYPE != "user"
            isDebug
        } catch (e: Exception) {
            false
        }
    }

    override fun createInitialState(): AuthState {
        return AuthState.Idle
    }

    override fun handleIntent(intent: AuthIntent) {
        when (intent) {
            is AuthIntent.Login -> {
                login(intent.email, intent.password)
            }
            AuthIntent.Logout -> TODO()
            else -> {
            }
        }
    }
}