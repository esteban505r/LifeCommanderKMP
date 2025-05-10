package com.esteban.ruano.nutrition_presentation.ui.viewmodel.state

import com.esteban.ruano.core.utils.Constants.EMPTY_STRING
import com.esteban.ruano.core_ui.view_model.ViewState
import com.esteban.ruano.nutrition_domain.model.Recipe

data class RecipesState (
    val recipes: List<Recipe> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isError: Boolean = false,
    val filter: String = EMPTY_STRING,
    val daySelected : Int = 0,
    val errorMessage: String = EMPTY_STRING
) : ViewState