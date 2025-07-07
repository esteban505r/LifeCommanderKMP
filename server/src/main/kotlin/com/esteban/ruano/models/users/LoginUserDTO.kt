package com.esteban.ruano.models.users

import kotlinx.serialization.Serializable

@Serializable
data class LoginUserDTO(
    val id: Int? = null,
    val email: String,
    val password: String,
    val fcmToken: String? = null,
    val timezone: String? = null
)
