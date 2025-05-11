package com.esteban.ruano.models.nutrition

import kotlinx.serialization.Serializable

@Serializable
data class NutritionDashboardDTO(
    val totalRecipes: Int,
    val recipesForToday: List<RecipeDTO>,
)