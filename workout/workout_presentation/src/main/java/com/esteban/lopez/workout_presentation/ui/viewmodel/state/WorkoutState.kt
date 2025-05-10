package com.esteban.ruano.workout_presentation.ui.viewmodel.state

import com.esteban.ruano.core_ui.view_model.ViewState
import com.esteban.ruano.workout_domain.model.WorkoutDay

data class WorkoutState(
    val workoutDays: List<WorkoutDay> = emptyList(),
    val totalExercises: Long = 0,
    val selectedWorkoutDay: WorkoutDay? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null
) : ViewState