package com.esteban.ruano.nutrition_presentation.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.esteban.ruano.core.domain.preferences.Preferences
import com.esteban.ruano.core_ui.R
import com.esteban.ruano.core_ui.utils.SnackbarType
import com.esteban.ruano.core_ui.view_model.BaseViewModel
import com.esteban.ruano.nutrition_domain.use_cases.NutritionUseCases
import com.esteban.ruano.nutrition_presentation.intent.NutritionEffect
import com.esteban.ruano.nutrition_presentation.intent.NutritionIntent
import com.esteban.ruano.nutrition_presentation.ui.viewmodel.state.NutritionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NutritionViewModel @Inject constructor(
    private val nutritionUseCases: NutritionUseCases,
    private val preferences: Preferences,
) : BaseViewModel<NutritionIntent, NutritionState, NutritionEffect>() {


    override fun handleIntent(intent: NutritionIntent) {
        intent.let {
            when (it) {
                is NutritionIntent.GetDashboard -> {
                    getDashboard(
                        date = it.date
                    )
                }
            }
        }
    }



    fun changeIsRefreshing(isRefreshing: Boolean){
        emitState {
            currentState.copy(
                isRefreshing = isRefreshing
            )
        }
    }

    private fun getDashboard(
        date: String,
    ) {
        viewModelScope.launch {
            emitState {
                currentState.copy(
                    isLoading = true,
                )
            }
            val result = nutritionUseCases.getDashboard(
                date = date,
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