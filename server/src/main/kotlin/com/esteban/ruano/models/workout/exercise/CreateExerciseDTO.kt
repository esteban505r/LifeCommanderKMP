package com.esteban.ruano.models.workout.exercise

import kotlinx.serialization.Serializable

@Serializable
data class CreateExerciseDTO(
    val name: String,
    val doneDateTime: String? = null,
    val note: String,
    val dueDate: String? = null,
)