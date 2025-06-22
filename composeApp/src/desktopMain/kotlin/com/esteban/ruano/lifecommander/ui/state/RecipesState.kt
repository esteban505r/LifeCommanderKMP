package com.esteban.ruano.lifecommander.ui.state

import com.esteban.ruano.lifecommander.models.Recipe
import com.esteban.ruano.lifecommander.models.RecipeTrack
import com.esteban.ruano.lifecommander.models.nutrition.RecipeFilters
import com.esteban.ruano.lifecommander.models.nutrition.RecipesResponse
import kotlinx.datetime.LocalDate

enum class ViewMode {
    PLAN,
    DATABASE,
    HISTORY
}

data class RecipesState(
    val recipes: RecipesResponse? = null, // Contains recipes and pagination info
    val totalRecipes: Long = 0,
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String = "",
    val daySelected: Int = 0, // Used for PLAN mode
    val recipeTracks: List<RecipeTrack> = emptyList(), // For tracking history
    val viewMode: ViewMode = ViewMode.PLAN,
    val historicalDate: LocalDate? = null, // Used for HISTORY mode
    val searchQuery: String = "", // Search functionality
    val limit: Int = 50, // Pagination limit
    val offset: Long = 0, // Pagination offset
    val sortBy: String = "name", // Sorting field (name, calories, protein, carbs, fat, fiber, sugar)
    val sortOrder: String = "asc", // Sorting order (asc, desc)
    val mealTagFilter: String? = null, // Filter by meal tag (BREAKFAST, LUNCH, DINNER, SNACK)
    val recipeFilters: RecipeFilters = RecipeFilters() // New RecipeFilters system
) 