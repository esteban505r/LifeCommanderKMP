package com.esteban.ruano.lifecommander.models

import kotlinx.serialization.Serializable

@Serializable
data class ForgotPasswordRequest(val email: String)

@Serializable
data class ResetPasswordRequest(val token: String, val newPassword: String)

@Serializable
data class VerifyTokenRequest(val token: String)

@kotlinx.serialization.Serializable
data class VerifyPinRequest(val email: String, val pin: String)

@kotlinx.serialization.Serializable
data class ResetWithSessionRequest(val resetToken: String, val newPassword: String)

@Serializable
data class ResetVerifyResponse(val reset_token: String)