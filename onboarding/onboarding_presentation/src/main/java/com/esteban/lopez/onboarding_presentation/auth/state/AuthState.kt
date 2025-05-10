package com.esteban.ruano.onboarding_presentation.auth.state

import com.esteban.ruano.core_ui.view_model.ViewState

sealed class AuthState: ViewState {

    data object Idle : AuthState()
    data object Loading : AuthState()
    data class Authenticated(
        val authToken: String,
    ) : AuthState()

    data class Error(
        val message: String,
    ) : AuthState()


}