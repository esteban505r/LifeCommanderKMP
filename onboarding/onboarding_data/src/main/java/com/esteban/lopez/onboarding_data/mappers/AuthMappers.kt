package com.esteban.ruano.onboarding_data.mappers

import com.esteban.ruano.onboarding_data.remote.dto.LoginResponse

fun LoginResponse.toDomainModel(): com.esteban.ruano.onboarding_domain.model.LoginModel {
    return com.esteban.ruano.onboarding_domain.model.LoginModel(
        token = token,
    )
}