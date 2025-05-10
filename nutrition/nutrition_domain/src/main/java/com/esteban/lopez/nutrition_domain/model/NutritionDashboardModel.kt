package com.esteban.ruano.nutrition_domain.model

data class NutritionDashboardModel(
    val totalRecipes: Int,
    val recipesForToday: List<Recipe>,
)