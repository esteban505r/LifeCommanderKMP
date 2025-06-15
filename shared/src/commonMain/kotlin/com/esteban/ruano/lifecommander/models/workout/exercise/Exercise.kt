package com.esteban.ruano.lifecommander.models.workout.exercise

import kotlinx.serialization.Serializable

@Serializable
data class Exercise(
    val id: Int,
    val name: String,
    val description: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
) 