package com.esteban.lopez.onboarding_domain.use_case

import com.esteban.ruano.onboarding_domain.repository.AuthRepository

class SetNewPassword(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(resetToken: String, newPassword: String): Result<Unit> {
        return repository.resetPassword(resetToken, newPassword)
    }
}