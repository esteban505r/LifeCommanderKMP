package com.esteban.ruano.nutrition_presentation.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.esteban.ruano.core.domain.preferences.Preferences
import com.esteban.ruano.core_ui.R
import com.esteban.lopez.core.utils.SnackbarType
import com.esteban.ruano.core_ui.view_model.BaseViewModel
import com.esteban.ruano.lifecommander.models.AlternativeNutrients
import com.esteban.ruano.nutrition_domain.use_cases.NutritionUseCases
import com.esteban.ruano.nutrition_domain.use_cases.RecipeUseCases
import com.esteban.ruano.nutrition_presentation.intent.NutritionEffect
import com.esteban.ruano.nutrition_presentation.intent.NutritionIntent
import com.esteban.ruano.nutrition_presentation.ui.viewmodel.state.NutritionState
import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import javax.inject.Inject

@HiltViewModel
class NutritionViewModel @Inject constructor(
    private val nutritionUseCases: NutritionUseCases,
    private val recipeUseCases: RecipeUseCases,
    private val preferences: Preferences,
) : BaseViewModel<NutritionIntent, NutritionState, NutritionEffect>() {


    override fun handleIntent(intent: NutritionIntent) {
        intent.let {
            when (it) {
                is NutritionIntent.GetDashboard -> {
                    getDashboard(
                        day = it.day
                    )
                }

                is NutritionIntent.ConsumeRecipe -> {
                    consumeRecipe(
                        it.recipeId,
                        it.dateTime
                    )
                }

                is NutritionIntent.DeleteRecipe -> {

                }

                is NutritionIntent.SkipRecipe -> {
                    skipRecipe(
                        it.recipeId,
                        it.dateTime,
                        alternativeRecipeId = it.alternativeRecipeId,
                        alternativeMealName = it.alternativeMealName,
                        alternativeNutrients = it.alternativeNutrients
                    )
                }

                is NutritionIntent.SearchRecipe -> {
                    searchRecipe(it.name)
                }

                is NutritionIntent.UndoConsumedRecipe -> {
                    undoConsumedRecipe(it.id)
                }
            }
        }
    }

    private fun undoConsumedRecipe(id: String) {
        viewModelScope.launch {
            emitState {
                copy(isLoading = true)
            }
            recipeUseCases.undoConsumedRecipe(
                id
            ).fold(
                { result ->
                    emitState {
                        copy(
                            isLoading = false,
                            todayRecipes = currentState.todayRecipes.map {
                                if (it.consumedTrackId == id) {
                                    it.copy(
                                        consumed = false,
                                        consumedDateTime = null,
                                        consumedTrackId = null
                                    )
                                } else {
                                    it
                                }
                            }
                        )
                    }
                },
                {
                    emitState {
                        copy(
                            isLoading = false
                        )
                    }
                    sendEffect {
                        NutritionEffect.ShowSnackBar(
                            message = it.message ?: "",
                            type = SnackbarType.ERROR,
                        )
                    }
                }
            )
        }

    }

    private fun searchRecipe(name: String) {
        viewModelScope.launch {
            emitState {
                copy(isSearchingLoading = true)
            }
            recipeUseCases.getAll(
                filter = name
            ).fold(
                { result ->
                    emitState {
                        copy(
                            isSearchingLoading = false,
                            recipesSearched = result.recipes
                        )
                    }
                },
                {
                    emitState {
                        copy(
                            isSearchingLoading = false
                        )
                    }
                    sendEffect {
                        NutritionEffect.ShowSnackBar(
                            message = it.message ?: "",
                            type = SnackbarType.ERROR,
                        )
                    }
                }
            )
        }
    }

    fun skipRecipe(
        id: String,
        dateTime: String,
        alternativeRecipeId: String? = null,
        alternativeMealName: String? = null,
        alternativeNutrients: AlternativeNutrients? = null
    ) {
        viewModelScope.launch {
            emitState {
                copy(
                    isLoading = true
                )
            }
            recipeUseCases.skipRecipe(
                id,
                dateTime,
                alternativeRecipeId,
                alternativeMealName,
                alternativeNutrients
            ).fold(
                onSuccess = {
                    emitState {
                        copy(
                            todayRecipes = currentState.todayRecipes.map {
                                if (it.id == id) {
                                    it.copy(
                                        consumed = true
                                    )
                                } else {
                                    it
                                }
                            }
                        )
                    }
                },
                onFailure = {
                    emitState {
                        copy(
                            isLoading = false
                        )
                    }
                    sendEffect {
                        NutritionEffect.ShowSnackBar(
                            message = it.message ?: "",
                            type = SnackbarType.ERROR,
                        )
                    }
                },
            )
        }
    }

    fun consumeRecipe(
        id: String,
        dateTime: String,
    ) {
        viewModelScope.launch {
            emitState {
                copy(
                    isLoading = true
                )
            }
            recipeUseCases.consumeRecipe(id, dateTime).fold(
                onSuccess = {
                    emitState {
                        copy(
                            isLoading = false,
                        )
                    }
                    getDashboard(
                        getCurrentDateTime(
                            TimeZone.currentSystemDefault()
                        ).date.dayOfWeek.value
                    )
                },
                onFailure = {
                    emitState {
                        copy(
                            isLoading = false
                        )
                    }
                    sendEffect {
                        NutritionEffect.ShowSnackBar(
                            message = it.message ?: "",
                            type = SnackbarType.ERROR,
                        )
                    }
                },
            )
        }
    }

    fun changeIsRefreshing(isRefreshing: Boolean) {
        emitState {
            currentState.copy(
                isRefreshing = isRefreshing
            )
        }
    }

    private fun getDashboard(
        day: Int,
    ) {
        viewModelScope.launch {
            emitState {
                currentState.copy(
                    isLoading = true,
                )
            }
            val result = nutritionUseCases.getDashboard(
                day = day,
            )
            result.fold(
                onFailure = {
                    emitState {
                        currentState.copy(
                            isLoading = false,
                        )
                    }
                    sendEffect {
                        NutritionEffect.ShowSnackBar("Error", SnackbarType.ERROR)
                    }
                },
                onSuccess = {
                    emitState {
                        currentState.copy(
                            totalRecipes = it.totalRecipes,
                            todayRecipes = it.recipesForToday,
                            isLoading = false,
                        )
                    }
                }
            )

        }
    }

    override fun createInitialState(): NutritionState {
        return NutritionState(
            emptyImageRes = listOf(
                R.drawable.empty_recipes,
                R.drawable.empty_recipes_2,
                R.drawable.empty_recipes_3,
                R.drawable.empty_recipes_4,
                R.drawable.empty_recipes_5,
                R.drawable.empty_recipes_6,
                R.drawable.empty_recipes_7,
                R.drawable.empty_recipes_8,
                R.drawable.empty_recipes_9,
                R.drawable.empty_recipes_10,
                R.drawable.empty_recipes_11,
                R.drawable.empty_recipes_12,
                R.drawable.empty_recipes_13,
                R.drawable.empty_recipes_14,
                R.drawable.empty_recipes_15,
                R.drawable.empty_recipes_16,
                R.drawable.empty_recipes_17,
                R.drawable.empty_recipes_18,
            ).random()
        )
    }


}