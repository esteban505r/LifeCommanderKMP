package com.esteban.ruano.onboarding_presentation.auth.state

import com.esteban.ruano.core_ui.view_model.ViewState

data class AuthState(
    val isLoading: Boolean = false,
    val authToken: String? = null,
    val pendingResetEmail: String? = null,
    val resetPasswordToken: String? = null,
    val error: String? = null,
): ViewState