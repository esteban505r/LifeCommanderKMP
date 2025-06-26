package com.esteban.ruano.models.workout

import kotlinx.serialization.Serializable

@Serializable
data class CreateWorkoutTrackDTO(
    val dayId: Int,
    val doneDateTime: String
) 