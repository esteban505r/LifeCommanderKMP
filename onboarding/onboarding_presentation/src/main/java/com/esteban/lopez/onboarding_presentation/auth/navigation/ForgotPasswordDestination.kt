package com.esteban.lopez.onboarding_presentation.auth.navigation


import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.esteban.lopez.onboarding_presentation.auth.ui.screens.ForgotPasswordScreen
import com.esteban.ruano.core_ui.utils.LocalMainIntent
import com.esteban.ruano.core_ui.view_model.intent.MainIntent
import com.esteban.ruano.core_ui.view_model.intent.MainIntent.*
import com.esteban.ruano.onboarding_presentation.auth.intent.AuthEffect
import com.esteban.ruano.onboarding_presentation.auth.intent.AuthIntent
import com.esteban.ruano.onboarding_presentation.auth.viewmodel.AuthViewModel


@Composable
fun ForgotPasswordDestination(
    viewModel: AuthViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val state by viewModel.viewState.collectAsState()
    val sendMainIntent = LocalMainIntent.current

    fun handleNavigation(effect: AuthEffect) {
        when (effect) {
            is AuthEffect.ShowSnackBar -> {
                sendMainIntent(ShowSnackBar(effect.message, effect.type))
            }
            is AuthEffect.AuthenticationSuccess -> {
                // After reset success, typically navigate back to login
                sendMainIntent(MainIntent.CheckAuthentication)
            }

            AuthEffect.NavigateToLogin -> {
                onBack()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { handleNavigation(it) }
    }

    ForgotPasswordScreen(
        isLoading = state.isLoading,
        errorMessage = state.error,
        onBackToLogin = {
            onBack()
        },
        onRequestCode = { email ->
            viewModel.performAction(AuthIntent.RequestReset(email))
        },
        onVerifyPin = { pin ->
            viewModel.performAction(AuthIntent.VerifyResetPin(pin))
        },
        onSetPassword = { newPassword ->
            viewModel.performAction(AuthIntent.SetNewPassword(newPassword))
        },
        state = state,
        onResetEmail = {
            viewModel.performAction(AuthIntent.ResetForgetEmail)
        }
    )
}
