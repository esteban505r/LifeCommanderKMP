package com.esteban.ruano.onboarding_presentation.auth.intent

import com.esteban.ruano.core_ui.utils.SnackbarType
import com.esteban.ruano.core_ui.view_model.Effect
import com.esteban.ruano.core_ui.view_model.UserIntent

sealed class AuthIntent : UserIntent {
    data class Login(
        val email: String,
        val password: String
    ) : AuthIntent()

    data object Logout : AuthIntent()
    data object Idle : AuthIntent()

}

sealed class AuthEffect : Effect {
    data class ShowSnackBar(
        val message: String,
        val type: SnackbarType = SnackbarType.INFO
    ) : AuthEffect()
    data object AuthenticationSuccess : AuthEffect()
}