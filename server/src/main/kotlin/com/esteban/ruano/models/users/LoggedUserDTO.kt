package com.esteban.ruano.models.users

import io.ktor.server.auth.*
import kotlinx.serialization.Serializable

@Serializable
data class LoggedUserDTO(
    val id: Int,
    val email: String,
)
