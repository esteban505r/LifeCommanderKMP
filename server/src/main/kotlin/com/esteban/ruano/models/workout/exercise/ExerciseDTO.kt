package com.esteban.ruano.models.workout.exercise

import kotlinx.serialization.Serializable
import com.esteban.ruano.models.resource.ResourceDTO
import com.esteban.ruano.models.workout.equiment.EquipmentDTO


@Serializable
data class ExerciseDTO(
    val id: String,
    val name: String,
    val description: String,
    val restSecs: Int,
    val baseSets: Int,
    val baseReps: Int,
    val muscleGroup: String,
    val equipment: List<EquipmentDTO>,
    val resource: ResourceDTO?
)