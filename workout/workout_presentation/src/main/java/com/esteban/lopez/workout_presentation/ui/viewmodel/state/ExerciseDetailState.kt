package com.esteban.ruano.workout_presentation.ui.viewmodel.state

import com.esteban.ruano.core_ui.view_model.ViewState
import com.esteban.ruano.lifecommander.models.Exercise

data class ExerciseDetailState (
    val exercise: Exercise? = null,
    val loading : Boolean = true,
    val errorMessage: String? = null,
    val showNewExerciseDialog: Boolean = false,
): ViewState