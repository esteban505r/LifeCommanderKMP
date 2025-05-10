package com.esteban.ruano.workout_presentation.intent

import com.esteban.ruano.core_ui.view_model.Effect
import com.esteban.ruano.core_ui.view_model.UserIntent
import com.esteban.ruano.workout_domain.model.Exercise

sealed class ExerciseDetailIntent : UserIntent{
    data class FetchExercise(
        val exerciseId: String
    ): ExerciseDetailIntent()
    data object NavigateUp : ExerciseDetailIntent()
}

sealed class ExerciseDetailEffect : Effect{
    data object NavigateUp: ExerciseDetailEffect()
    data class ShowSnackbarErrorMessage(
        val message: String? = null
    ): ExerciseDetailEffect()
}