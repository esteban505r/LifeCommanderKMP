package com.esteban.ruano.workout_data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class EquipmentResponse(
    val id: Int,
    val name: String,
    val description : String,
    val resourceResponse: ResourceResponse
)