package com.esteban.lopez.onboarding_presentation.auth.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.esteban.ruano.core_ui.R
import com.esteban.ruano.core_ui.utils.LocalMainIntent
import com.esteban.ruano.core_ui.view_model.intent.MainIntent
import com.esteban.ruano.lifecommander.ui.components.Error
import com.esteban.ruano.lifecommander.ui.components.Loading
import com.esteban.ruano.onboarding_presentation.auth.intent.AuthEffect
import com.esteban.ruano.onboarding_presentation.auth.state.AuthState
import com.esteban.ruano.onboarding_presentation.auth.ui.screens.LoginScreen
import com.esteban.ruano.onboarding_presentation.auth.viewmodel.AuthViewModel

@Composable
fun LoginDestination(
    viewModel: AuthViewModel = hiltViewModel(),
) {

    val state = viewModel.viewState.collectAsState()
    val sendMainIntent = LocalMainIntent.current

    /**
     * Handles navigation based on [AuthEffect].
     *
     * @param effect The navigation event to handle.
     */
    fun handleNavigation(effect: AuthEffect) {
        when (effect) {
            is AuthEffect.ShowSnackBar -> {
                sendMainIntent(MainIntent.ShowSnackBar(effect.message,effect.type))
            }
            is AuthEffect.AuthenticationSuccess -> {
                // Trigger MainViewModel to check authentication status and navigate
                sendMainIntent(MainIntent.CheckAuthentication)
            }
        }
    }

    LaunchedEffect(Unit) {
        // Collect and handle navigation effects.
        viewModel.effect.collect { handleNavigation(it) }
    }

    when (state.value) {
        is AuthState.Idle -> {
            LoginScreen(
                userIntent = { viewModel.performAction(it) }
            )
        }

        is AuthState.Authenticated -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.authenticated),
                )
            }
        }

        is AuthState.Error -> {
            Error(
                message = stringResource(R.string.error_unknown),
            )
        }

        AuthState.Loading -> {
            Loading()
        }
    }


}