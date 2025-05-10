package com.esteban.ruano.workout_presentation.models

import com.esteban.ruano.workout_domain.model.Exercise

data class ExerciseInProgress(
    val exercise: Exercise,
    val setsDone : Int = 0,
    val repsDone : Int = 0,
    val done : Boolean = false
)