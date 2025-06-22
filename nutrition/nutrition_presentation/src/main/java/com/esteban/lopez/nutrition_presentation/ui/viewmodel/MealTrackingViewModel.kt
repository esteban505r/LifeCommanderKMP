package com.esteban.ruano.nutrition_presentation.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.esteban.ruano.core.domain.preferences.Preferences
import com.esteban.ruano.core_ui.utils.SnackbarType
import com.esteban.ruano.core_ui.view_model.BaseViewModel
import com.esteban.ruano.nutrition_domain.use_cases.RecipeUseCases
import com.esteban.ruano.nutrition_presentation.intent.MealTrackingEffect
import com.esteban.ruano.nutrition_presentation.intent.MealTrackingIntent
import com.esteban.ruano.nutrition_presentation.ui.viewmodel.state.MealTrackingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

@HiltViewModel
class MealTrackingViewModel @Inject constructor(
    private val recipeUseCases: RecipeUseCases,
    private val preferences: Preferences,
) : BaseViewModel<MealTrackingIntent, MealTrackingState, MealTrackingEffect>() {

    override fun handleIntent(intent: MealTrackingIntent) {
        intent.let {
            when (it) {
                is MealTrackingIntent.RefreshMeals -> {
                    refreshTodayMeals()
                }
                is MealTrackingIntent.TrackMealConsumed -> {
                    trackMealConsumed(it.recipeId)
                }
                is MealTrackingIntent.TrackMealSkipped -> {
                    trackMealSkipped(it.recipeId)
                }
                is MealTrackingIntent.TrackMealSkippedWithAlternative -> {
                    trackMealSkippedWithAlternative(
                        recipeId = it.recipeId,
                        alternativeRecipeId = it.alternativeRecipeId,
                        alternativeMealName = it.alternativeMealName
                    )
                }
            }
        }
    }

    private fun refreshTodayMeals() {
        viewModelScope.launch {
            emitState {
                currentState.copy(
                    isRefreshing = true
                )
            }
            
            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            val dayOfWeek = today.dayOfWeek.value
            
            val result = recipeUseCases.getByDay(dayOfWeek)
            result.fold(
                onFailure = {
                    emitState {
                        currentState.copy(
                            isRefreshing = false
                        )
                    }
                    sendEffect {
                        MealTrackingEffect.ShowSnackBar("Error loading meals", SnackbarType.ERROR)
                    }
                },
                onSuccess = {
                    emitState {
                        currentState.copy(
                            todayRecipes = it.recipes,
                            isRefreshing = false
                        )
                    }
                }
            )
        }
    }

    private fun trackMealConsumed(recipeId: String) {
        viewModelScope.launch {
            // Here you would call the API to track meal consumption
            // For now, we'll just show a success message
            sendEffect {
                MealTrackingEffect.ShowSnackBar("Meal marked as consumed!", SnackbarType.SUCCESS)
            }
            // Refresh the meals list
            refreshTodayMeals()
        }
    }

    private fun trackMealSkipped(recipeId: String) {
        viewModelScope.launch {
            // Here you would call the API to track meal as skipped
            // For now, we'll just show a success message
            sendEffect {
                MealTrackingEffect.ShowSnackBar("Meal marked as skipped!", SnackbarType.SUCCESS)
            }
            // Refresh the meals list
            refreshTodayMeals()
        }
    }

    private fun trackMealSkippedWithAlternative(
        recipeId: String,
        alternativeRecipeId: String?,
        alternativeMealName: String?
    ) {
        viewModelScope.launch {
            // Here you would call the API to track meal as skipped with alternative
            // For now, we'll just show a success message
            val message = if (alternativeMealName != null) {
                "Meal skipped, alternative: $alternativeMealName"
            } else {
                "Meal marked as skipped!"
            }
            sendEffect {
                MealTrackingEffect.ShowSnackBar(message, SnackbarType.SUCCESS)
            }
            // Refresh the meals list
            refreshTodayMeals()
        }
    }

    override fun createInitialState(): MealTrackingState {
        return MealTrackingState()
    }
} 