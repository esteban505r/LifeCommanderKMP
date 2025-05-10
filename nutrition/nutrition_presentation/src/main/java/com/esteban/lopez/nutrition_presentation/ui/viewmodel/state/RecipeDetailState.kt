package com.esteban.ruano.nutrition_presentation.ui.viewmodel.state

import com.esteban.ruano.core.utils.Constants.EMPTY_STRING
import com.esteban.ruano.core_ui.view_model.ViewState
import com.esteban.ruano.nutrition_domain.model.Recipe

data class RecipeDetailState (
    val recipe:Recipe? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isError: Boolean = false,
    val filter: String = EMPTY_STRING,
    val errorMessage: String = EMPTY_STRING
) : ViewState