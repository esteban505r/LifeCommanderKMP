package com.esteban.ruano.onboarding_data.datasources

import com.esteban.ruano.onboarding_domain.model.LoginModel

interface AuthDataSource {

    suspend fun login(email: String, password: String): LoginModel

    suspend fun register(email: String, password: String,name: String): Unit
}
