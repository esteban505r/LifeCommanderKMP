package com.esteban.ruano.workout_presentation.ui.viewmodel.state

import com.esteban.ruano.core_ui.view_model.ViewState
import com.esteban.ruano.workout_domain.model.Exercise
import com.esteban.ruano.workout_domain.model.WorkoutDay

data class ExerciseDetailState (
    val exercise: Exercise? = null,
    val loading : Boolean = false,
    val errorMessage: String? = null,
    val showNewExerciseDialog: Boolean = false,
): ViewState