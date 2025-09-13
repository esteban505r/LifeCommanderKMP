package com.esteban.ruano.workout_presentation.intent

import com.esteban.ruano.core_ui.view_model.Effect
import com.esteban.ruano.core_ui.view_model.UserIntent
import com.esteban.ruano.lifecommander.models.Exercise

sealed class ExercisesIntent : UserIntent{
    data object FetchExercises : ExercisesIntent()
    data class FetchExercisesByWorkoutDay(
        val workoutDayId: Int
    ) : ExercisesIntent()
    data class SaveExercise(
        val exercise: Exercise
    ) : ExercisesIntent()
    data object NavigateUp : ExercisesIntent()
}

sealed class ExercisesEffect: Effect {
    data object NavigateUp: ExercisesEffect()
    data class ShowSnackbarErrorMessage(
        val message: String? = null
    ): ExercisesEffect()
}