package com.esteban.ruano.onboarding_data.datasources

import com.esteban.ruano.onboarding_data.mappers.toDomainModel
import com.esteban.ruano.onboarding_data.remote.AuthApi
import com.esteban.ruano.onboarding_data.remote.dto.LoginRequest
import com.esteban.ruano.onboarding_domain.model.LoginModel
import services.auth.AuthService

class AuthRemoteDataSource(
    private val authApi: AuthApi,
    private val authService: AuthService
) : AuthDataSource {
    override suspend fun login(email: String, password: String, fcmToken: String?, timezone: String?): LoginModel {
        return authApi.login(
            LoginRequest(
                email = email,
                password = password,
                fcmToken = fcmToken,
                timezone = timezone
            )
        ).toDomainModel()
    }

    override suspend fun register(email: String, password: String, name: String) {
        authService.signUp(email = email, password =  password, name =  name)
    }

    override suspend fun forgotPassword(email: String) {
        authService.forgotPassword(email)
    }

    override suspend fun verifyResetPin(email: String, pin: String): String {
        return authService.verifyResetPin(email, pin)
    }

    override suspend fun resetPassword(resetToken: String, newPassword: String) {
        authService.resetPasswordWithSession(
            resetToken,
            newPassword
        )
    }


}
