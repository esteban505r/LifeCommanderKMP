package com.esteban.ruano.models.workout.exercise

import kotlinx.serialization.Serializable

@Serializable
data class UpdateExerciseDTO(
    val name: String? = null,
    val note: String? = null,
    val doneDateTime: String? = null,
    val dueDate: String? = null,
)