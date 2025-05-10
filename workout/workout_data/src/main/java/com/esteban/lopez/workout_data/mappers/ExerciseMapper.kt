package com.esteban.ruano.workout_data.mappers

import com.esteban.ruano.workout_domain.model.MuscleGroup
import com.esteban.ruano.workout_data.remote.dto.ExerciseResponse
import com.esteban.ruano.workout_domain.model.Exercise
import com.esteban.ruano.workout_domain.model.toMuscleGroupString
import com.esteban.ruano.workout_data.local.model.Exercise as LocalExercise

fun LocalExercise.toExercise(): Exercise {
    return Exercise(
        id = id?.toString()?:"-1",
        name = name,
        description = description,
        equipment = emptyList(),
        baseReps = baseReps,
        baseSets = baseSets,
        muscleGroup = MuscleGroup.fromValue(muscleGroup),
        restSecs = restSecs,
    )
}

fun ExerciseResponse.toExercise(): Exercise {
    return Exercise(
        id = id,
        name = name,
        description = description,
        equipment = equipment.map { it.toEquipment() },
        baseReps = baseReps,
        baseSets = baseSets,
        muscleGroup = MuscleGroup.fromValue(muscleGroup),
        resource = resource?.toResource(),
        restSecs = restSecs
    )
}

fun Exercise.toExerciseResponse(): ExerciseResponse {
    return ExerciseResponse(
        id = id,
        name = name,
        description = description,
        equipment = equipment.map { it.toEquipmentResponse() },
        baseReps = baseReps,
        baseSets = baseSets,
        muscleGroup = muscleGroup.toMuscleGroupString(),
        resource = resource?.toResourceResponse(),
        restSecs = restSecs
    )
}

fun Exercise.toLocalExercise(): LocalExercise {
    return LocalExercise(
        id = id?.toIntOrNull(),
        name = name,
        description = description,
        baseReps = baseReps,
        baseSets = baseSets,
        muscleGroup = muscleGroup.toMuscleGroupString(),
        restSecs = restSecs
    )
}



