package com.esteban.ruano.onboarding_data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
    val fcmToken: String? = null,
    val timezone: String? = null
)