package com.esteban.ruano.models.nutrition

import kotlinx.serialization.Serializable

@Serializable
data class RecipeTrackDTO(
    val id: String,
    val recipeId: String,
    val consumedDateTime: String,
    val status: String = "ACTIVE"
) 