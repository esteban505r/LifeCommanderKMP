package com.esteban.ruano.lifecommander.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esteban.ruano.lifecommander.models.Recipe
import com.esteban.ruano.lifecommander.services.meals.RecipesService
import com.esteban.ruano.lifecommander.ui.state.RecipesState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

    fun deleteRecipe(id: String) {
        viewModelScope.launch {
            try {
                service.deleteRecipe(id)
                getRecipes()
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
} 