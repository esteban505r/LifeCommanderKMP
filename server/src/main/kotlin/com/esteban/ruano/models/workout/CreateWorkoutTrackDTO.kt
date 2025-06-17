package com.esteban.ruano.models.workout

import kotlinx.serialization.Serializable

@Serializable
data class CreateWorkoutTrackDTO(
    val workoutDayId: String,
    val doneDateTime: String
) 