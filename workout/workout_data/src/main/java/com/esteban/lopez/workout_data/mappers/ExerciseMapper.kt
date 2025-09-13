package com.esteban.ruano.workout_data.mappers

import com.esteban.ruano.lifecommander.models.Exercise
import com.esteban.ruano.workout_data.remote.dto.ExerciseResponse
import com.esteban.ruano.workout_domain.model.toMuscleGroupString
import com.esteban.ruano.workout_data.local.model.Exercise as LocalExercise

fun LocalExercise.toExercise(): Exercise {
    return Exercise(
        id = id?.toString()?:"-1",
        name = name,
        description = description,
        baseReps = baseReps,
        baseSets = baseSets,
        muscleGroup = muscleGroup,
        restSecs = restSecs,
    )
}

fun ExerciseResponse.toExercise(): Exercise {
    return Exercise(
        id = id ?: "",
        name = name,
        description = description,
        baseReps = baseReps,
        baseSets = baseSets,
        muscleGroup = muscleGroup,
        restSecs = restSecs
    )
}

fun Exercise.toExerciseResponse(): ExerciseResponse {
    return ExerciseResponse(
        id = id,
        name = name,
        description = description?:"",
        baseReps = baseReps?:0,
        baseSets = baseSets?:0,
        muscleGroup = muscleGroup.toString(),
        restSecs = restSecs?:0,
        equipment = listOf()
    )
}

fun Exercise.toLocalExercise(): LocalExercise {
    return LocalExercise(
        id = id?.toIntOrNull(),
        name = name,
        description = description?:"",
        baseReps = baseReps?:0,
        baseSets = baseSets?:0,
        muscleGroup = muscleGroup.toString(),
        restSecs = restSecs?:0
    )
}



