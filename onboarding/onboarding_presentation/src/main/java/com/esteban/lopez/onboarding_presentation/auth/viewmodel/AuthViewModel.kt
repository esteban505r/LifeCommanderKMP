package com.esteban.ruano.onboarding_presentation.auth.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.esteban.ruano.core.domain.preferences.Preferences
import com.esteban.ruano.core_ui.utils.SnackbarType
import com.esteban.ruano.core_ui.view_model.BaseViewModel
import com.esteban.ruano.onboarding_domain.use_case.AuthUseCases
import com.esteban.ruano.onboarding_presentation.auth.intent.AuthIntent
import com.esteban.ruano.onboarding_presentation.auth.intent.AuthEffect
import com.esteban.ruano.onboarding_presentation.auth.state.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authUseCases: AuthUseCases,
    private val preferences: Preferences
) : BaseViewModel<AuthIntent,AuthState, AuthEffect>() {

    private fun login(email: String, password: String) {
        viewModelScope.launch {
            val result = authUseCases.login(email, password)
            result.fold(
                onSuccess = {
                   emitState { AuthState.Authenticated(it.token) }
                    preferences.saveAuthToken(
                        it.token
                    )
                    delay(1000)
                    emitState { AuthState.Idle }
                },
                onFailure = {
                    Log.e("AuthViewModel", "login: $it")
                    sendEffect {
                        AuthEffect.ShowSnackBar("Email or password is incorrect",SnackbarType.ERROR)
                    }
                }
            )
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