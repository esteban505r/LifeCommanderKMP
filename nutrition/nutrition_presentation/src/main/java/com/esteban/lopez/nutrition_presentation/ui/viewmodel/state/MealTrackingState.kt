package com.esteban.ruano.nutrition_presentation.ui.viewmodel.state

import com.esteban.ruano.core_ui.view_model.ViewState
import com.esteban.ruano.lifecommander.models.Recipe

data class MealTrackingState(
    val todayRecipes: List<Recipe> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false
) : ViewState