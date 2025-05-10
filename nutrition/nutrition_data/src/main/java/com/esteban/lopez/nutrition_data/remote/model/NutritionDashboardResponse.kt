package com.esteban.ruano.nutrition_data.remote.model

data class NutritionDashboardResponse(
    val totalRecipes: Int,
    val recipesForToday: List<RecipeResponse>,
)