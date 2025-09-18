package com.esteban.ruano.onboarding_data.datasources

import com.esteban.ruano.onboarding_domain.model.LoginModel

interface AuthDataSource {

    suspend fun login(email: String, password: String, fcmToken: String?, timezone: String?): LoginModel

    suspend fun register(email: String, password: String,name: String): Unit

    suspend fun forgotPassword(
        email: String
    ): Unit

    suspend fun verifyResetPin(
        email: String,
        pin: String
    ): String

    suspend fun resetPassword(
        resetToken: String,
        newPassword: String
    ): Unit
}
