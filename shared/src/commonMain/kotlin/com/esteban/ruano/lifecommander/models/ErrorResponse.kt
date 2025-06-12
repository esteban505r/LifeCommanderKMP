package com.esteban.ruano.lifecommander.models

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val message: String? = null,
    val code: Int? = null
)