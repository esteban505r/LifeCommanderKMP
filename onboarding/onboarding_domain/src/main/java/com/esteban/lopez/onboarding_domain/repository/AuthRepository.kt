package com.esteban.ruano.onboarding_domain.repository

import com.esteban.ruano.onboarding_domain.model.LoginModel


interface AuthRepository {

    suspend fun login(
        email: String,
        password: String,
        fcmToken: String?,
        timezone: String?
    ): Result<LoginModel>

    suspend fun register(
        email: String,
        password: String,
        name: String
    ): Result<Unit>

    // Step 1: request reset PIN
    suspend fun forgotPassword(
        email: String
    ): Result<Unit>

    // Step 2: verify PIN -> returns reset session token
    suspend fun verifyResetPin(
        email: String,
        pin: String
    ): Result<String> // reset_token

    // Step 3: consume reset session token to set new password
    suspend fun resetPassword(
        resetToken: String,
        newPassword: String
    ): Result<Unit>
}
