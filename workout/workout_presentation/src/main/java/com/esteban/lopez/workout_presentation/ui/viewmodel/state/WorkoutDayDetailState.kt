package com.esteban.ruano.workout_presentation.ui.viewmodel.state

import com.esteban.ruano.core_ui.view_model.Effect
import com.esteban.ruano.core_ui.view_model.ViewState
import com.esteban.ruano.lifecommander.models.ExerciseDayStatus
import com.esteban.ruano.workout_domain.model.Workout
import com.esteban.ruano.workout_presentation.models.ExerciseInProgress

data class WorkoutDayDetailState(
    val workout: Workout? = null,
    val exercisesInProgress: List<ExerciseInProgress> = listOf(),
    val workoutStatus: List<ExerciseDayStatus> = listOf(),
    val time: String = "00:00",
    val errorMessage: String? = null,
    val isLoading: Boolean = true,
    val completedExercises: Set<String> = emptySet(),
    val isWorkoutCompleted: Boolean = false,
    val isLoadingSet: Boolean = false,
) : ViewState

sealed class WorkoutDayDetailEffect: Effect {
    data object NavigateUp: WorkoutDayDetailEffect()
    data object AnimateToNextExercise: WorkoutDayDetailEffect()
}