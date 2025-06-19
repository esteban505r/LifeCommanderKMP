package com.esteban.ruano.models.workout

import kotlinx.serialization.Serializable

@Serializable
data class ExerciseTrackDTO(
    val id: String,
    val exerciseId: String,
    val workoutDayId: String,
    val doneDateTime: String,
    val status: String = "ACTIVE"
)

@Serializable
data class CreateExerciseTrackDTO(
    val exerciseId: String,
    val workoutDayId: String,
    val doneDateTime: String
) 