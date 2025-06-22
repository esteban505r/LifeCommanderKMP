package com.esteban.ruano.nutrition_domain.model

import com.esteban.ruano.lifecommander.models.Recipe

data class NutritionDashboardModel(
    val totalRecipes: Int,
    val recipesForToday: List<Recipe>,
)