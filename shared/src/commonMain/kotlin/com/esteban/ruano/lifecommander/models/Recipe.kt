package com.esteban.ruano.lifecommander.models

import kotlinx.serialization.Serializable

@Serializable
data class Recipe(
    val id: String,
    val name: String,
    val note: String? = null,
    val protein: Double? = null,
    val image: String? = null,
    val day: Int? = null,
    val mealTag: String? = null,
)