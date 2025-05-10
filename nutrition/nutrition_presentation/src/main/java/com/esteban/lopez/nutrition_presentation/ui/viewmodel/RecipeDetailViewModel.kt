package com.esteban.ruano.nutrition_presentation.ui.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.esteban.ruano.core_ui.utils.SnackbarType
import com.esteban.ruano.core_ui.view_model.BaseViewModel
import com.esteban.ruano.nutrition_domain.model.Recipe
import com.esteban.ruano.nutrition_domain.use_cases.RecipeUseCases
import com.esteban.ruano.nutrition_presentation.intent.RecipeDetailEffect
import com.esteban.ruano.nutrition_presentation.intent.RecipeDetailIntent
import com.esteban.ruano.nutrition_presentation.ui.viewmodel.state.RecipeDetailState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    private val recipeUseCases: RecipeUseCases,
) : BaseViewModel<RecipeDetailIntent, RecipeDetailState, RecipeDetailEffect>() {

    override fun handleIntent(intent: RecipeDetailIntent) {
        when (intent) {
            is RecipeDetailIntent.GetRecipe -> fetchRecipe(intent.id)
            is RecipeDetailIntent.DeleteRecipe -> deleteRecipe()
            is RecipeDetailIntent.EditRecipe -> editRecipe()
        }
    }

    private fun editRecipe() {
        currentState.recipe?.let {
            sendEffect {
                RecipeDetailEffect.EditRecipe(it.id)
            }
        } ?: {
            sendEffect {
                RecipeDetailEffect.ShowSnackBar("Error editing recipe", SnackbarType.ERROR)
            }
            sendEffect {
                RecipeDetailEffect.CloseScreen()
            }
        }
    }

    private fun deleteRecipe() {
        viewModelScope.launch {
            try {
                currentState.recipe?.let {
                    recipeUseCases.deleteRecipe(it.id).fold(
                        onSuccess = {
                            sendEffect {
                                RecipeDetailEffect.ShowSnackBar("Recipe deleted", SnackbarType.SUCCESS)
                            }
                            sendEffect {
                                RecipeDetailEffect.CloseScreen()
                            }
                        },
                        onFailure = {
                            sendEffect {
                                RecipeDetailEffect.ShowSnackBar("Error deleting recipe", SnackbarType.ERROR)
                            }
                            sendEffect {
                                RecipeDetailEffect.CloseScreen()
                            }
                        }
                    )
                }
            } catch (e: Exception) {
                emitState {
                    currentState.copy(
                        isLoading = false,
                        isError = true,
                        errorMessage = e.message ?: "Error"
                    )
                }
            }
        }
    }

    private fun fetchRecipe(id: String) {
        viewModelScope.launch {
            emitState {
                currentState.copy(
                    isLoading = true,
                )
            }
            try {
                val recipe = recipeUseCases.getRecipe(id)
                recipe.fold(
                    onSuccess = {
                        emitState {
                            currentState.copy(
                                recipe = it,
                                isLoading = false,
                                isError = false,
                            )
                        }
                    },
                    onFailure = {
                        sendEffect {
                            RecipeDetailEffect.CloseScreen()
                        }
                    }
                )
            } catch (e: Exception) {
                emitState {
                    currentState.copy(
                        isLoading = false,
                        isError = true,
                        errorMessage = e.message ?: "Error"
                    )
                }
            }
        }
    }



    override fun createInitialState(): RecipeDetailState {
        return RecipeDetailState()
    }



}