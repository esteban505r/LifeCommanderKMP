package com.esteban.ruano.models.nutrition

import kotlinx.serialization.Serializable

@Serializable
data class CreateRecipeDTO(
    val name: String,
    val note: String? = null,
    val protein: Double? = null,
    val image: String? = null,
    val day: Int? = null,
    val createdAt: String? = null,
    val mealTag: String? = null,
)