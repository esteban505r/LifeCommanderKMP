package com.esteban.ruano.models.users

import kotlinx.serialization.Serializable

@Serializable
data class RegisterUserDTO(
    val name: String,
    val email: String,
    val password: String
)