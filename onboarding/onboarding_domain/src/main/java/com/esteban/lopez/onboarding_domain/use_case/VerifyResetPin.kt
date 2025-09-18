package com.esteban.lopez.onboarding_domain.use_case

import com.esteban.ruano.onboarding_domain.repository.AuthRepository

class VerifyResetPin(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email:String,pin: String): Result<String> {
        return repository.verifyResetPin(email,pin)
    }
}
