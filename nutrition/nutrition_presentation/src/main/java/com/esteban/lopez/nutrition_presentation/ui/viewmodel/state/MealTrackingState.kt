package com.esteban.ruano.nutrition_presentation.ui.viewmodel.state

import com.esteban.ruano.nutrition_domain.model.Recipe

data class MealTrackingState(
    val todayRecipes: List<Recipe> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false
) 