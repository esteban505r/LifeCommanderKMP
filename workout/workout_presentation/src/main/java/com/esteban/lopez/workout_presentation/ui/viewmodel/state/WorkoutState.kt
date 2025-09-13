package com.esteban.ruano.workout_presentation.ui.viewmodel.state

import com.esteban.ruano.core_ui.view_model.ViewState
import com.esteban.ruano.workout_domain.model.Workout

data class WorkoutState(
    val workouts: List<Workout> = emptyList(),
    val totalExercises: Long = 0,
    val selectedWorkout: Workout? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val completedExercises: Set<String> = emptySet(),
    val weeklyWorkoutsCompleted: List<Int> = emptyList(),
    val daySelected: Int = 0
) : ViewState