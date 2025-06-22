package com.esteban.ruano.models.nutrition

import kotlinx.serialization.Serializable

@Serializable
data class IngredientDTO(
    val id: String? = null,
    val name: String,
    val quantity: Double,
    val unit: String
) 