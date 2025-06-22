package com.esteban.ruano.nutrition_presentation.ui.viewmodel.state

import com.esteban.lopez.core.utils.AppConstants.EMPTY_STRING
import com.esteban.ruano.core_ui.view_model.ViewState
import com.esteban.ruano.lifecommander.models.Recipe
import com.esteban.ruano.lifecommander.models.nutrition.RecipeFilters
import com.esteban.ruano.lifecommander.models.nutrition.RecipesResponse

enum class ViewMode {
    PLAN,
    DATABASE,
    HISTORY
}

data class RecipesState (
    val recipes: RecipesResponse? = null, // Contains recipes and pagination info
    val totalRecipes: Long = 0,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isError: Boolean = false,
    val daySelected: Int = 0, // Used for PLAN mode
    val viewMode: ViewMode = ViewMode.PLAN,
    val historicalDate: String? = null, // Used for HISTORY mode
    val searchQuery: String = "", // Search functionality
    val limit: Int = 50, // Pagination limit
    val offset: Long = 0, // Pagination offset
    val recipeFilters: RecipeFilters = RecipeFilters(), // New RecipeFilters system
    val errorMessage: String = EMPTY_STRING
) : ViewState