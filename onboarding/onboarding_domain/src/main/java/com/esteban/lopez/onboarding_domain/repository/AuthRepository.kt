package com.esteban.ruano.onboarding_domain.repository

import com.esteban.ruano.onboarding_domain.model.LoginModel


interface AuthRepository {

    suspend fun login(email: String, password: String): Result<LoginModel>

    suspend fun register(email: String, password: String, name: String)

}