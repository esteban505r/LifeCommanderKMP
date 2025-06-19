package com.esteban.ruano.models.nutrition

import kotlinx.serialization.Serializable

@Serializable
data class RecipeTrackDTO(
    val id: String,
    val recipeId: String,
    val consumedDateTime: String,
    val skipped: Boolean = false,
    val alternativeRecipeId: String? = null,
    val alternativeMealName: String? = null
) 