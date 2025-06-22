package com.esteban.ruano.models.nutrition

import kotlinx.serialization.Serializable

@Serializable
data class RecipesResponseDTO(
    val recipes: List<RecipeDTO>,
    val totalCount: Long
) 