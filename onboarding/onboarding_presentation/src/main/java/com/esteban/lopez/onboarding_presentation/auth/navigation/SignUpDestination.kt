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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.esteban.lopez.onboarding_presentation.auth.ui.screens.SignUpScreen
import com.esteban.ruano.core_ui.R
import com.esteban.ruano.core_ui.utils.LocalMainIntent
import com.esteban.ruano.core_ui.view_model.intent.MainIntent
import com.esteban.ruano.core_ui.view_model.intent.MainIntent.*
import com.esteban.ruano.lifecommander.ui.components.Error
import com.esteban.ruano.lifecommander.ui.components.Loading
import com.esteban.ruano.onboarding_presentation.auth.intent.AuthEffect
import com.esteban.ruano.onboarding_presentation.auth.state.AuthState
import com.esteban.ruano.onboarding_presentation.auth.viewmodel.AuthViewModel

@Composable
fun SignUpDestination(
    viewModel: AuthViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val state = viewModel.viewState.collectAsState()
    val sendMainIntent = LocalMainIntent.current

    fun handleNavigation(effect: AuthEffect) {
        when (effect) {
            is AuthEffect.ShowSnackBar ->
                sendMainIntent(ShowSnackBar(effect.message, effect.type))

            is AuthEffect.AuthenticationSuccess ->
                sendMainIntent(MainIntent.CheckAuthentication)

            AuthEffect.NavigateToLogin -> {
                onBack()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { handleNavigation(it) }
    }


    if(state.value.isLoading) {
        Loading()
    }
    else{
        SignUpScreen(
            userIntent = { viewModel.performAction(it) },
            onBackToLogin = {
                onBack()
            }
        )
    }


}
