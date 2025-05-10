package com.esteban.ruano.nutrition_presentation.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.esteban.ruano.core.domain.preferences.Preferences
import com.esteban.ruano.core_ui.utils.SnackbarType
import com.esteban.ruano.core_ui.view_model.BaseViewModel
import com.esteban.ruano.nutrition_domain.use_cases.RecipeUseCases
import com.esteban.ruano.nutrition_presentation.intent.RecipesEffect
import com.esteban.ruano.nutrition_presentation.intent.RecipesIntent
import com.esteban.ruano.nutrition_presentation.ui.viewmodel.state.RecipesState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.first

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
            }
        }
    }

    private fun fetchRecipesByDay(day: Int) {
        viewModelScope.launch {
            emitState {
                currentState.copy(
                    isLoading = true,
                )
            }
            recipeUseCases.getByDay(day).fold(
                onFailure = {
                    emitState {
                        currentState.copy(
                            isLoading = false,
                            daySelected = 0
                        )
                    }
                    sendEffect {
                        RecipesEffect.ShowSnackBar("Error", SnackbarType.ERROR)
                    }
                    fetchRecipes()
                },
                onSuccess = {
                    emitState {
                        currentState.copy(
                            recipes = it,
                            isLoading = false,
                            daySelected = day
                        )
                    }
                }
            )
        }
    }

    private fun changeFilter(filter: String) {
        emitState {
            currentState.copy(
                filter = filter
            )
        }
        fetchRecipes()
    }


    fun changeIsRefreshing(isRefreshing: Boolean){
        emitState {
            currentState.copy(
                isRefreshing = isRefreshing
            )
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
            val result = recipeUseCases.getAll(
                filter = currentState.filter,
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
                        RecipesEffect.ShowSnackBar("Error", SnackbarType.ERROR)
                    }
                },
                onSuccess = {
                    emitState {
                        currentState.copy(
                            recipes = it,
                            isLoading = false,
                            daySelected = 0
                        )
                    }
                }
            )

        }
    }

    override fun createInitialState(): RecipesState {
        return RecipesState()
    }




}