package com.esteban.lopez.onboarding_domain.use_case

import com.esteban.ruano.onboarding_domain.repository.AuthRepository

class RequestReset(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<Unit> {
        return repository.forgotPassword(email)
    }
}
