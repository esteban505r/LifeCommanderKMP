package com.esteban.ruano.lifecommander.models.workout.day

import kotlinx.serialization.Serializable

@Serializable
data class WorkoutDay(
    val id: String,
    val name: String,
    val description: String? = null,
    val day: Int? = null // 0 = Sunday, 6 = Saturday
) 