package com.esteban.ruano.lifecommander.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esteban.ruano.lifecommander.models.Recipe
import com.esteban.ruano.lifecommander.models.RecipeTrack
import com.esteban.ruano.lifecommander.services.meals.RecipesService
import com.esteban.ruano.lifecommander.ui.state.RecipesState
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
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
                val recipes = service.getRecipes()
                _state.value = _state.value.copy(
                    recipes = recipes,
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
                getRecipes()
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
                getRecipes()
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
                val recipes = if (day == null) service.getRecipes() else service.getRecipesByDay(day)
                _state.value = _state.value.copy(
                    recipes = recipes,
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
            _state.value = _state.value.copy(isLoading = true)
            try {
                val recipes = service.getRecipesByDay(day)
                _state.value = _state.value.copy(
                    recipes = recipes,
                    isLoading = false,
                    isError = false,
                    errorMessage = "",
                    daySelected = day
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

    // Recipe Tracking Methods
    fun consumeRecipe(recipeId: String) {
        viewModelScope.launch {
            try {
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                val consumedDateTime = now.formatDefault()
                
                val success = service.trackRecipeConsumption(recipeId, consumedDateTime)
                if (success) {
                    // Refresh the recipes to show updated state
                    getRecipes()
                } else {
                    _state.value = _state.value.copy(
                        isError = true,
                        errorMessage = "Failed to consume recipe"
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
                    errorMessage = e.message ?: "Unknown error"
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
} 