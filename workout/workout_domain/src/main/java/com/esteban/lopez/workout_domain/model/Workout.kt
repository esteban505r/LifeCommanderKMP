package com.esteban.ruano.workout_domain.model

import com.esteban.ruano.lifecommander.models.Exercise

data class Workout(
    val id: String? = null,
    val day: Int,
    val name:String,
    val time: String,
    val exercises: List<Exercise>,
    val remoteId: Int? = null
)