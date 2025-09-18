package com.esteban.ruano.onboarding_domain.use_case

import com.esteban.ruano.core.domain.model.DataException
import com.esteban.ruano.onboarding_domain.model.LoginModel
import com.esteban.ruano.onboarding_domain.repository.AuthRepository

class SignUp(
    val repository: AuthRepository
){
    suspend operator fun invoke(name:String,email: String, password: String): Result<Unit> {
        return repository.register(email, password,name)
    }
}