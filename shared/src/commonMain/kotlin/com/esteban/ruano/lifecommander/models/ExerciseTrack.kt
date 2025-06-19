package com.esteban.ruano.lifecommander.models

import kotlinx.serialization.Serializable

@Serializable
data class ExerciseTrack(
    val id: String,
    val exerciseId: String,
    val workoutDayId: String,
    val doneDateTime: String,
    val status: String = "ACTIVE"
)

@Serializable
data class CreateExerciseTrack(
    val exerciseId: String,
    val workoutDayId: String,
    val doneDateTime: String
) 