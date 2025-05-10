package com.esteban.ruano.onboarding_domain.use_case

import com.esteban.ruano.core.domain.model.DataException
import com.esteban.ruano.onboarding_domain.model.LoginModel
import com.esteban.ruano.onboarding_domain.repository.AuthRepository

class Login(
    val repository: AuthRepository
){
    suspend operator fun invoke(email: String, password: String): Result<LoginModel> {
        return repository.login(email, password)
    }
}