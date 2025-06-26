package com.esteban.ruano.lifecommander.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esteban.ruano.lifecommander.models.Recipe
import com.esteban.ruano.lifecommander.services.meals.RecipesService
import com.esteban.ruano.lifecommander.ui.state.RecipesState
import com.esteban.ruano.lifecommander.ui.state.ViewMode
import com.esteban.ruano.lifecommander.models.nutrition.RecipeFilters
import com.esteban.ruano.lifecommander.models.nutrition.RecipeSortField
import com.esteban.ruano.lifecommander.models.nutrition.RecipeSortOrder
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

class RecipesViewModel(
    private val service: RecipesService
) : ViewModel() {
    private val _state = MutableStateFlow(RecipesState())
    val state: StateFlow<RecipesState> = _state.asStateFlow()

    fun getRecipes() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val response = service.getRecipes(filters = _state.value.recipeFilters)
                _state.value = _state.value.copy(
                    recipes = response,
                    totalRecipes = response.totalCount,
                    isLoading = false,
                    isError = false,
                    errorMessage = ""
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    isError = true,
                    errorMessage = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun addRecipe(recipe: Recipe) {
        viewModelScope.launch {
            try {
                service.addRecipe(recipe)
                // Refresh current view
                when (_state.value.viewMode) {
                    ViewMode.PLAN -> getRecipesByDay(_state.value.daySelected)
                    ViewMode.DATABASE -> getRecipesWithFilters(filters = _state.value.recipeFilters)
                    ViewMode.HISTORY -> _state.value.historicalDate?.let { getHistoryForDate(it) }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isError = true,
                    errorMessage = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun updateRecipe(recipe: Recipe) {
        viewModelScope.launch {
            try {
                service.updateRecipe(recipe)
                // Refresh current view
                when (_state.value.viewMode) {
                    ViewMode.PLAN -> getRecipesByDay(_state.value.daySelected)
                    ViewMode.DATABASE -> getRecipesWithFilters(filters = _state.value.recipeFilters)
                    ViewMode.HISTORY -> _state.value.historicalDate?.let { getHistoryForDate(it) }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isError = true,
                    errorMessage = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun deleteRecipe(recipeId: String) {
        viewModelScope.launch {
            try {
                service.deleteRecipe(recipeId)
                getRecipesByDay(_state.value.daySelected)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isError = true,
                    errorMessage = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun filterByDay(day: Int?) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val response = if (day == null) {
                    service.getRecipes(filters = _state.value.recipeFilters)
                } else {
                    service.getRecipesByDay(day, filters = _state.value.recipeFilters)
                }
                _state.value = _state.value.copy(
                    recipes = response,
                    totalRecipes = response.totalCount,
                    isLoading = false,
                    isError = false,
                    errorMessage = "",
                    daySelected = day ?: 0
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    isError = true,
                    errorMessage = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun getRecipesByDay(day: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, daySelected = day)
            try {
                val response = service.getRecipesByDay(day, filters = _state.value.recipeFilters)
                _state.value = _state.value.copy(
                    recipes = response,
                    totalRecipes = response.totalCount,
                    isLoading = false,
                    isError = false,
                    errorMessage = "",
                    viewMode = ViewMode.PLAN
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    isError = true,
                    errorMessage = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun getAllRecipes() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val response = service.getAllRecipes(filters = _state.value.recipeFilters)
                _state.value = _state.value.copy(
                    recipes = response,
                    totalRecipes = response.totalCount,
                    isLoading = false,
                    isError = false,
                    errorMessage = "",
                    viewMode = ViewMode.DATABASE
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    isError = true,
                    errorMessage = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun getRecipesWithFilters(
        limit: Int = 50,
        offset: Int = 0,
        filters: RecipeFilters = RecipeFilters()
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val response = service.getRecipesWithFilters(limit, offset, filters)
                _state.value = _state.value.copy(
                    recipes = response,
                    totalRecipes = response.totalCount,
                    isLoading = false,
                    isError = false,
                    errorMessage = "",
                    viewMode = ViewMode.DATABASE,
                    recipeFilters = filters
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    isError = true,
                    errorMessage = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun getHistoryForDate(date: kotlinx.datetime.LocalDate) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val startDate = date.atTime(0, 0).date.formatDefault()
                val endDate = date.atTime(23, 59).date.formatDefault()
                val tracks = service.getRecipeTracksByDateRange(startDate, endDate)
                val recipes = tracks.map { it.recipe }
                
                _state.value = _state.value.copy(
                    recipes = com.esteban.ruano.lifecommander.models.nutrition.RecipesResponse(
                        recipes = recipes,
                        totalCount = recipes.size.toLong()
                    ),
                    totalRecipes = recipes.size.toLong(),
                    isLoading = false,
                    isError = false,
                    errorMessage = "",
                    viewMode = ViewMode.HISTORY,
                    historicalDate = date
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    isError = true,
                    errorMessage = e.message ?: "Failed to load history"
                )
            }
        }
    }

    // Recipe Tracking Methods
    fun consumeRecipe(recipeId: String) {
        viewModelScope.launch {
            try {
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                val success = service.trackRecipeConsumption(recipeId, now.formatDefault())
                if (success) {
                    // Refresh the current day's recipes to show updated consumption status
                    getRecipesByDay(_state.value.daySelected)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isError = true,
                    errorMessage = e.message ?: "Failed to consume recipe"
                )
            }
        }
    }

    fun skipRecipe(recipeId: String) {
        viewModelScope.launch {
            try {
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                val consumedDateTime = now.formatDefault()
                
                val success = service.trackRecipeSkipped(recipeId, consumedDateTime)
                if (success) {
                    // Refresh only the current day's recipes to show updated state
                    getRecipesByDay(_state.value.daySelected)
                } else {
                    _state.value = _state.value.copy(
                        isError = true,
                        errorMessage = "Failed to skip recipe"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isError = true,
                    errorMessage = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun skipRecipeWithAlternative(recipeId: String, alternativeRecipeId: String?, alternativeMealName: String?) {
        viewModelScope.launch {
            try {
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                val success = service.trackRecipeSkippedWithAlternative(
                    recipeId, 
                    now.formatDefault(),
                    alternativeRecipeId, 
                    alternativeMealName
                )
                if (success) {
                    getRecipesByDay(_state.value.daySelected)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isError = true,
                    errorMessage = e.message ?: "Failed to skip recipe"
                )
            }
        }
    }

    fun getRecipeTracksByDateRange(startDate: String, endDate: String) {
        viewModelScope.launch {
            try {
                val tracks = service.getRecipeTracksByDateRange(startDate, endDate)
                _state.value = _state.value.copy(
                    recipeTracks = tracks,
                    isError = false,
                    errorMessage = ""
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isError = true,
                    errorMessage = e.message ?: "Failed to load recipe tracks"
                )
            }
        }
    }

    fun getRecipeTracksByRecipe(recipeId: String) {
        viewModelScope.launch {
            try {
                val tracks = service.getRecipeTracksByRecipe(recipeId)
                _state.value = _state.value.copy(
                    recipeTracks = tracks,
                    isError = false,
                    errorMessage = ""
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isError = true,
                    errorMessage = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun deleteRecipeTrack(trackId: String) {
        viewModelScope.launch {
            try {
                val success = service.deleteRecipeTrack(trackId)
                if (success) {
                    // Refresh the current day's recipes to show updated state
                    getRecipesByDay(_state.value.daySelected)
                } else {
                    _state.value = _state.value.copy(
                        isError = true,
                        errorMessage = "Failed to delete recipe track"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isError = true,
                    errorMessage = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun getConsumedMealsForDay(day: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, daySelected = day)
            try {
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                val today = now.date
                val weekStart = today.minus(kotlinx.datetime.DatePeriod(days = today.dayOfWeek.value - 1))
                val selectedDate = weekStart.plus(kotlinx.datetime.DatePeriod(days = day - 1))
                val startDate = selectedDate.atTime(0, 0).date.formatDefault()
                val endDate = selectedDate.atTime(23, 59).date.formatDefault()
                
                // SINGLE network call to get the tracks, which now contain full recipe data.
                val tracks = service.getRecipeTracksByDateRange(startDate, endDate)
                
                // The recipe inside the track already has its `consumed` status set by the backend.
                // We just need to extract the list of recipes from the tracks.
                val recipes = tracks.map { it.recipe }

                _state.value = _state.value.copy(
                    recipes = com.esteban.ruano.lifecommander.models.nutrition.RecipesResponse(
                        recipes = recipes,
                        totalCount = recipes.size.toLong()
                    ),
                    totalRecipes = recipes.size.toLong(),
                    isLoading = false,
                    isError = false,
                    errorMessage = ""
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    isError = true,
                    errorMessage = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun searchRecipes(query: String) {
        _state.value = _state.value.copy(
            searchQuery = query,
            recipeFilters = _state.value.recipeFilters.copy(searchPattern = query.takeIf { it.isNotBlank() })
        )
        // Debounced search to prevent excessive API calls
        viewModelScope.launch {
            delay(300) // Wait 300ms after user stops typing
            // Only search if the query hasn't changed during the delay
            if (_state.value.searchQuery == query) {
                refreshCurrentView()
            }
        }
    }

    fun clearSearch() {
        _state.value = _state.value.copy(
            searchQuery = "",
            recipeFilters = _state.value.recipeFilters.copy(searchPattern = null)
        )
        refreshCurrentView()
    }

    fun setSortBy(sortBy: String) {
        val sortField = when (sortBy.lowercase()) {
            "name" -> RecipeSortField.NAME
            "calories" -> RecipeSortField.CALORIES
            "protein" -> RecipeSortField.PROTEIN
            "carbs" -> RecipeSortField.CARBS
            "fat" -> RecipeSortField.FAT
            "fiber" -> RecipeSortField.FIBER
            "sugar" -> RecipeSortField.SUGAR
            "sodium" -> RecipeSortField.SODIUM
            else -> RecipeSortField.NAME
        }
        _state.value = _state.value.copy(
            sortBy = sortBy,
            recipeFilters = _state.value.recipeFilters.copy(sortField = sortField)
        )
        refreshCurrentView()
    }

    fun setSortOrder(sortOrder: String) {
        val recipeSortOrder = when (sortOrder.lowercase()) {
            "asc" -> RecipeSortOrder.ASCENDING
            "desc" -> RecipeSortOrder.DESCENDING
            else -> RecipeSortOrder.NONE
        }
        _state.value = _state.value.copy(
            sortOrder = sortOrder,
            recipeFilters = _state.value.recipeFilters.copy(sortOrder = recipeSortOrder)
        )
        refreshCurrentView()
    }

    fun setMealTagFilter(mealTag: String?) {
        _state.value = _state.value.copy(
            mealTagFilter = mealTag,
            recipeFilters = _state.value.recipeFilters.copy(mealTypes = mealTag?.let { listOf(it) })
        )
        refreshCurrentView()
    }

    fun setNutritionFilter(nutritionType: String, minValue: Double?, maxValue: Double?) {
        // Update the new RecipeFilters system
        val updatedRecipeFilters = when (nutritionType.lowercase()) {
            "calories" -> _state.value.recipeFilters.copy(minCalories = minValue, maxCalories = maxValue)
            "protein" -> _state.value.recipeFilters.copy(minProtein = minValue, maxProtein = maxValue)
            "carbs" -> _state.value.recipeFilters.copy(minCarbs = minValue, maxCarbs = maxValue)
            "fat" -> _state.value.recipeFilters.copy(minFat = minValue, maxFat = maxValue)
            "fiber" -> _state.value.recipeFilters.copy(minFiber = minValue, maxFiber = maxValue)
            "sugar" -> _state.value.recipeFilters.copy(minSugar = minValue, maxSugar = maxValue)
            "sodium" -> _state.value.recipeFilters.copy(minSodium = minValue, maxSodium = maxValue)
            else -> _state.value.recipeFilters
        }
        _state.value = _state.value.copy(recipeFilters = updatedRecipeFilters)
        
        refreshCurrentView()
    }

    fun clearAllFilters() {
        _state.value = _state.value.copy(
            searchQuery = "",
            mealTagFilter = null,
            sortBy = "name",
            sortOrder = "asc",
            recipeFilters = RecipeFilters()
        )
        refreshCurrentView()
    }

    private fun refreshCurrentView() {
        when (_state.value.viewMode) {
            ViewMode.PLAN -> getRecipesByDay(_state.value.daySelected)
            ViewMode.DATABASE -> getRecipesWithFilters(filters = _state.value.recipeFilters)
            ViewMode.HISTORY -> _state.value.historicalDate?.let { getHistoryForDate(it) }
        }
    }
} 