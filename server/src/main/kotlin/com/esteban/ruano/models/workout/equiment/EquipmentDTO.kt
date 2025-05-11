package com.esteban.ruano.models.workout.equiment

import kotlinx.serialization.Serializable
import com.esteban.ruano.models.resource.ResourceDTO

@Serializable
data class EquipmentDTO(
    val id: Int,
    val name: String,
    val description: String,
    val resource: ResourceDTO
)