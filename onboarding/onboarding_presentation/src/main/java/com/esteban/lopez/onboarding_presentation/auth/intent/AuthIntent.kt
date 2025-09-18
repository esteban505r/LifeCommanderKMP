package com.esteban.ruano.onboarding_presentation.auth.intent


import com.esteban.lopez.core.utils.SnackbarType
import com.esteban.ruano.core_ui.view_model.Effect
import com.esteban.ruano.core_ui.view_model.UserIntent

sealed class AuthIntent : UserIntent {
    data class Login(
        val email: String,
        val password: String
    ) : AuthIntent()

    data object Logout : AuthIntent()
    data class SignUp(
        val name: String,
        val email: String,
        val password: String
    ) : AuthIntent()

    data class RequestReset(val email: String) : AuthIntent()

    data class VerifyResetPin(val pin: String) : AuthIntent()
    data class SetNewPassword(val password: String) : AuthIntent()
    data object ResetForgetEmail : AuthIntent()


}

sealed class AuthEffect : Effect {
    data object NavigateToLogin: AuthEffect()
    data class ShowSnackBar(
        val message: String,
        val type: SnackbarType = SnackbarType.INFO
    ) : AuthEffect()
    data object AuthenticationSuccess : AuthEffect()
}