package com.esteban.ruano.nutrition_presentation.ui.viewmodel.state

import com.esteban.lopez.core.utils.AppConstants.EMPTY_STRING
import com.esteban.ruano.core_ui.view_model.ViewState
import com.esteban.ruano.nutrition_domain.model.Recipe

data class NutritionState (
    val totalRecipes: Int = 0,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isError: Boolean = false,
    val todayRecipes: List<Recipe> = emptyList(),
    val errorMessage: String = EMPTY_STRING,
    val emptyImageRes: Int? = null
) : ViewState