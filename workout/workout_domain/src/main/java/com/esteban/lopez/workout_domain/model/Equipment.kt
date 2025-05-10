package com.esteban.ruano.workout_domain.model

data class Equipment(
    val id: Int,
    val name: String,
    val description: String,
    val resource: Resource
)
