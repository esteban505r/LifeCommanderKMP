package com.esteban.ruano.onboarding_domain.use_case

import com.esteban.lopez.onboarding_domain.use_case.RequestReset
import com.esteban.lopez.onboarding_domain.use_case.SetNewPassword
import com.esteban.lopez.onboarding_domain.use_case.VerifyResetPin

data class AuthUseCases (
    val login: Login,
    val signUp: SignUp,
    val requestReset: RequestReset,
    val verifyPin: VerifyResetPin,
    val setNewPassword: SetNewPassword,
)