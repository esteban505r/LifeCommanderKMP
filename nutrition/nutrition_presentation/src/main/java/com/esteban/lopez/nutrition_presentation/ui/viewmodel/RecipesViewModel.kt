package com.esteban.ruano.nutrition_presentation.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.esteban.ruano.core.domain.preferences.Preferences
import com.esteban.lopez.core.utils.SnackbarType
import com.esteban.ruano.core_ui.view_model.BaseViewModel
import com.esteban.ruano.lifecommander.models.nutrition.RecipeFilters
import com.esteban.ruano.lifecommander.models.nutrition.RecipesResponse
import com.esteban.ruano.nutrition_domain.use_cases.RecipeUseCases
import com.esteban.ruano.nutrition_presentation.intent.RecipesEffect
import com.esteban.ruano.nutrition_presentation.intent.RecipesIntent
import com.esteban.ruano.nutrition_presentation.ui.viewmodel.state.RecipesState
import com.esteban.ruano.nutrition_presentation.ui.viewmodel.state.ViewMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.delay

@HiltViewModel
class RecipesViewModel @Inject constructor(
    private val recipeUseCases: RecipeUseCases,
    private val preferences: Preferences,
) : BaseViewModel<RecipesIntent, RecipesState, RecipesEffect>() {

    override fun handleIntent(intent: RecipesIntent) {
        intent.let {
            when (it) {
                is RecipesIntent.GetRecipes -> {
                    fetchRecipes()
                }

                is RecipesIntent.GetRecipesByDay -> {
                    fetchRecipesByDay(it.day)
                }

                is RecipesIntent.GetAllRecipes -> {
                    fetchAllRecipes()
                }

                is RecipesIntent.GetHistoryForDate -> {
                    fetchHistoryForDate(it.date)
                }

                is RecipesIntent.SearchRecipes -> {
                    searchRecipes(it.query)
                }

                is RecipesIntent.ClearSearch -> {
                    clearSearch()
                }

                is RecipesIntent.ApplyFilters -> {
                    applyFilters(it.filters)
                }

                is RecipesIntent.ClearAllFilters -> {
                    clearAllFilters()
                }

                is RecipesIntent.SetViewMode -> {
                    setViewMode(it.viewMode)
                }
            }
        }
    }

    private fun fetchRecipesByDay(day: Int) {
        viewModelScope.launch {
            emitState {
                currentState.copy(
                    isLoading = true,
                    viewMode = ViewMode.PLAN,
                    daySelected = day
                )
            }
            
            // Build query parameters from filters
            val queryParams = buildQueryParameters()
            
            val result = recipeUseCases.getByDay(
                day = day,
                filter = queryParams.takeIf { it.isNotEmpty() },
                page = null,
                limit = 30
            )
            result.fold(
                onFailure = {
                    emitState {
                        currentState.copy(
                            isLoading = false,
                            daySelected = 0
                        )
                    }
                    sendEffect {
                        RecipesEffect.ShowSnackBar("Error fetching recipes for day", SnackbarType.ERROR)
                    }
                    fetchRecipes()
                },
                onSuccess = { recipesResponse ->
                    emitState {
                        currentState.copy(
                            recipes = recipesResponse,
                            isLoading = false,
                            daySelected = day,
                            totalRecipes = recipesResponse.totalCount
                        )
                    }
                }
            )
        }
    }

    private fun searchRecipes(query: String) {
        viewModelScope.launch {
            emitState {
                currentState.copy(
                    searchQuery = query,
                    recipeFilters = currentState.recipeFilters.copy(searchPattern = query.takeIf { it.isNotBlank() })
                )
            }
            // Debounced search to prevent excessive API calls
            delay(300) // Wait 300ms after user stops typing
            // Only search if the query hasn't changed during the delay
            if (currentState.searchQuery == query) {
                refreshCurrentView()
            }
        }
    }

    private fun clearSearch() {
        viewModelScope.launch {
            emitState {
                currentState.copy(
                    searchQuery = "",
                    recipeFilters = currentState.recipeFilters.copy(searchPattern = null)
                )
            }
            refreshCurrentView()
        }
    }

    private fun applyFilters(filters: RecipeFilters) {
        viewModelScope.launch {
            emitState {
                currentState.copy(
                    recipeFilters = filters
                )
            }
            refreshCurrentView()
        }
    }

    private fun clearAllFilters() {
        viewModelScope.launch {
            emitState {
                currentState.copy(
                    recipeFilters = RecipeFilters()
                )
            }
            refreshCurrentView()
        }
    }

    private fun setViewMode(viewMode: ViewMode) {
        viewModelScope.launch {
            emitState {
                currentState.copy(
                    viewMode = viewMode
                )
            }
        }
    }

    private fun fetchHistoryForDate(date: String) {
        viewModelScope.launch {
            emitState {
                currentState.copy(
                    isLoading = true,
                    viewMode = ViewMode.HISTORY,
                    historicalDate = date
                )
            }
            
            // TODO: Implement history fetching
            // For now, just show empty state
            emitState {
                currentState.copy(
                    isLoading = false,
                    recipes = RecipesResponse(emptyList(), 0)
                )
            }
        }
    }

    private fun refreshCurrentView() {
        when (currentState.viewMode) {
            ViewMode.PLAN -> {
                if (currentState.daySelected > 0) {
                    fetchRecipesByDay(currentState.daySelected)
                } else {
                    fetchRecipes()
                }
            }
            ViewMode.DATABASE -> {
                fetchAllRecipes()
            }
            ViewMode.HISTORY -> {
                currentState.historicalDate?.let { fetchHistoryForDate(it) }
            }
        }
    }

    private fun fetchRecipes(
        page: Int? = null,
        limit: Int = 30,
    ) {
        viewModelScope.launch {
            emitState {
                currentState.copy(
                    isLoading = true,
                )
            }
            
            // Build query parameters from filters
            val queryParams = buildQueryParameters()
            
            val result = recipeUseCases.getAll(
                filter = queryParams,
                page = page,
                limit = limit,
            )
            result.fold(
                onFailure = {
                    emitState {
                        currentState.copy(
                            isLoading = false,
                        )
                    }
                    sendEffect {
                        RecipesEffect.ShowSnackBar("Error fetching recipes", SnackbarType.ERROR)
                    }
                },
                onSuccess = { recipesResponse ->
                    emitState {
                        currentState.copy(
                            recipes = recipesResponse,
                            isLoading = false,
                            daySelected = 0,
                            totalRecipes = recipesResponse.totalCount
                        )
                    }
                }
            )
        }
    }

    private fun fetchAllRecipes(
        page: Int? = null,
        limit: Int = 30,
    ) {
        viewModelScope.launch {
            emitState {
                currentState.copy(
                    isLoading = true,
                    viewMode = ViewMode.DATABASE
                )
            }
            
            // Build query parameters from filters
            val queryParams = buildQueryParameters()
            
            val result = recipeUseCases.getAll(
                filter = queryParams,
                page = page,
                limit = limit,
            )
            result.fold(
                onFailure = {
                    emitState {
                        currentState.copy(
                            isLoading = false,
                        )
                    }
                    sendEffect {
                        RecipesEffect.ShowSnackBar("Error fetching all recipes", SnackbarType.ERROR)
                    }
                },
                onSuccess = { recipesResponse ->
                    emitState {
                        currentState.copy(
                            recipes = recipesResponse,
                            isLoading = false,
                            daySelected = -1, // -1 indicates database view
                            totalRecipes = recipesResponse.totalCount
                        )
                    }
                }
            )
        }
    }

    private fun buildQueryParameters(filters: RecipeFilters = currentState.recipeFilters): String {
        val params = mutableListOf<String>()
        
        filters.searchPattern?.let { params.add("search=$it") }
        filters.mealTypes?.forEach { params.add("mealType=$it") }
        filters.days?.forEach { params.add("day=$it") }
        filters.minCalories?.let { params.add("minCalories=$it") }
        filters.maxCalories?.let { params.add("maxCalories=$it") }
        filters.minProtein?.let { params.add("minProtein=$it") }
        filters.maxProtein?.let { params.add("maxProtein=$it") }
        filters.minCarbs?.let { params.add("minCarbs=$it") }
        filters.maxCarbs?.let { params.add("maxCarbs=$it") }
        filters.minFat?.let { params.add("minFat=$it") }
        filters.maxFat?.let { params.add("maxFat=$it") }
        filters.minFiber?.let { params.add("minFiber=$it") }
        filters.maxFiber?.let { params.add("maxFiber=$it") }
        filters.minSugar?.let { params.add("minSugar=$it") }
        filters.maxSugar?.let { params.add("maxSugar=$it") }
        filters.minSodium?.let { params.add("minSodium=$it") }
        filters.maxSodium?.let { params.add("maxSodium=$it") }
        filters.sortField.takeIf { it != com.esteban.ruano.lifecommander.models.nutrition.RecipeSortField.NAME }?.let { params.add("sortField=$it") }
        filters.sortOrder.takeIf { it != com.esteban.ruano.lifecommander.models.nutrition.RecipeSortOrder.NONE }?.let { params.add("sortOrder=$it") }
        
        return if (params.isNotEmpty()) params.joinToString("&") else ""
    }

    fun changeIsRefreshing(isRefreshing: Boolean) {
        emitState {
            currentState.copy(
                isRefreshing = isRefreshing
            )
        }
    }

    override fun createInitialState(): RecipesState {
        return RecipesState()
    }
}