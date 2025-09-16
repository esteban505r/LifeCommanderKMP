package com.esteban.ruano.lifecommander.models

import kotlinx.serialization.Serializable


@Serializable
data class BindExercise(
    val exerciseId: String,
    val workoutDayId: String,
)

@Serializable
data class UnBindExercise(
    val exerciseId: String,
    val workoutDayId: String,
)
