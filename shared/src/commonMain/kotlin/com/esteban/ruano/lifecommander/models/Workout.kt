package com.esteban.ruano.lifecommander.models

import kotlinx.serialization.Serializable

@Serializable
data class Workout(
    val id: String,
    val name: String,
    val description: String? = null,
    val day: Int? = null, // 0 = Sunday, 6 = Saturday
    val time: String? = null,
    val exercises: List<Exercise> = emptyList(),
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class Exercise(
    val id: String,
    val name: String,
    val description: String? = null,
    val restSecs: Int? = null,
    val baseSets: Int? = null,
    val baseReps: Int? = null,
    val muscleGroup: String? = null,
    val equipment: List<String>? = emptyList(),
    val resource: String? = null
) 