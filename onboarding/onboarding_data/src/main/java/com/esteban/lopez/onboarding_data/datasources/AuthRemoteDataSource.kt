package com.esteban.ruano.onboarding_data.datasources

import com.esteban.ruano.onboarding_data.mappers.toDomainModel
import com.esteban.ruano.onboarding_data.remote.AuthApi
import com.esteban.ruano.onboarding_data.remote.dto.LoginRequest
import com.esteban.ruano.onboarding_domain.model.LoginModel

class AuthRemoteDataSource(
    private val authApi: AuthApi
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
        authApi.register(email, password, name)
    }


}
