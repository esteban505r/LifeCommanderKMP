package com.esteban.ruano.nutrition_presentation.ui.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.esteban.ruano.core_ui.utils.SnackbarType
import com.esteban.ruano.core_ui.view_model.BaseViewModel
import com.esteban.ruano.nutrition_domain.model.Recipe
import com.esteban.ruano.nutrition_domain.use_cases.RecipeUseCases
import com.esteban.ruano.nutrition_presentation.intent.NewEditRecipeEffect
import com.esteban.ruano.nutrition_presentation.intent.NewEditRecipeIntent
import com.esteban.ruano.nutrition_presentation.ui.viewmodel.state.NewEditRecipeState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class NewEditRecipeViewModel @Inject constructor(
    private val recipeUseCases: RecipeUseCases,
) : BaseViewModel<NewEditRecipeIntent, NewEditRecipeState, NewEditRecipeEffect>() {

    override fun handleIntent(intent: NewEditRecipeIntent) {
        when (intent) {
            is NewEditRecipeIntent.CreateRecipe -> addRecipe(intent.name,intent.note
                ,intent.protein,intent.day)

            is NewEditRecipeIntent.GetRecipe -> fetchRecipe(intent.id)
            is NewEditRecipeIntent.UpdateRecipe -> updateRecipe(intent.id, intent.recipe)
        }
    }

    private fun fetchRecipe(id: String) {
        viewModelScope.launch {
            emitState {
                currentState.copy(isLoading = true)
            }
            val recipe = recipeUseCases.getRecipe(id)
            recipe.fold(
                onSuccess = {
                    emitState {
                        currentState.copy(isLoading = false, recipe = it)
                    }
                },
                onFailure = {
                    Log.e("NewEditRecipeViewModel", "fetchRecipe: $it")
                    emitState {
                        currentState.copy(isLoading = false)
                    }
                    sendEffect {
                        NewEditRecipeEffect.ShowSnackBar(
                            message = "Error",
                            type = SnackbarType.ERROR
                        )
                    }
                    sendEffect {
                        NewEditRecipeEffect.CloseScreen(wasUpdated = false)
                    }
                }
            )
        }
    }

    private fun addRecipe(name:String, note:String?,
        protein:Double?, day:Int?, ) {
        viewModelScope.launch {
            emitState {
                currentState.copy(isLoading = true)
            }
            val added = recipeUseCases.addRecipe(
                Recipe(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    note = note,
                    protein = protein,
                    day = day
                )
            )
            added.fold(
                onSuccess = {
                    emitState {
                        currentState.copy(isLoading = false)
                    }
                    sendEffect {
                        NewEditRecipeEffect.CloseScreen(wasUpdated = true)
                    }
                },
                onFailure = {
                    Log.e("NewEditRecipeViewModel", "addRecipe: $it")
                    emitState {
                        currentState.copy(isLoading = false)
                    }
                    sendEffect {
                        NewEditRecipeEffect.ShowSnackBar(
                            message = "Error",
                            type = SnackbarType.ERROR
                        )
                    }
                }
            )
        }
    }



    private fun updateRecipe(id: String, recipe: Recipe) {
        viewModelScope.launch {
            val result = recipeUseCases.updateRecipe(id, recipe)
            result.fold(
                onSuccess = {
                    sendEffect {
                        NewEditRecipeEffect.CloseScreen(wasUpdated = true)
                    }
                },
                onFailure = {
                    Log.e("NewEditRecipeViewModel", "updateRecipe: $it")
                    sendEffect {
                        NewEditRecipeEffect.ShowSnackBar(
                            message = "Error",
                            type = SnackbarType.ERROR
                        )

                    }
                }
            )
        }
    }

    override fun createInitialState(): NewEditRecipeState {
        return NewEditRecipeState()
    }



}