package com.esteban.ruano.workout_data.remote.dto

import com.esteban.ruano.workout_domain.model.Resource
import kotlinx.serialization.Serializable

@Serializable
data class ExerciseResponse(
    val id: String? = null,
    val name: String,
    val description: String,
    val restSecs: Int,
    val baseSets: Int,
    val baseReps: Int,
    val muscleGroup: String,
    val equipment: List<EquipmentResponse>,
    val resource: ResourceResponse? = null
)