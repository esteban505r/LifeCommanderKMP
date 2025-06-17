package com.esteban.ruano.models.workout

import kotlinx.serialization.Serializable

@Serializable
data class WorkoutTrackDTO(
    val id: String,
    val workoutDayId: String,
    val doneDateTime: String,
    val status: String = "ACTIVE"
) 