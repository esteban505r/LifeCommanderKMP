package com.esteban.ruano.lifecommander.models.workout.day

import com.esteban.ruano.lifecommander.models.Exercise
import kotlinx.serialization.Serializable

@Serializable
data class WorkoutDay(
    val id: String,
    val name: String,
    val description: String? = null,
    val day: Int? = null,
    val exercises: List<Exercise>? = emptyList(),
    val isCompleted: Boolean = false,
)