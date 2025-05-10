package com.esteban.ruano.workout_data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ResourceResponse(
    val id: Int,
    val url: String,
    val type: String
)