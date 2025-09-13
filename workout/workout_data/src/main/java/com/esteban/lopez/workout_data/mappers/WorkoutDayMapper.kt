package com.esteban.ruano.workout_data.mappers

import com.esteban.ruano.workout_data.local.model.WorkoutDayWithExercises
import com.esteban.ruano.workout_data.remote.dto.WorkoutDashboardResponse
import com.esteban.ruano.workout_data.remote.dto.WorkoutDayResponse
import com.esteban.ruano.workout_domain.model.WorkoutDashboard
import com.esteban.ruano.workout_domain.model.Workout
import com.esteban.ruano.workout_data.local.model.WorkoutDay as LocalWorkoutDay


fun WorkoutDayResponse.toDomainModel():Workout{
    return Workout(
        id = id?.toString()?:"",
        day = day,
        time = time,
        name = name,
        exercises = exercises.map { it.toExercise() }
    )
}

fun Workout.toResponseModel():WorkoutDayResponse{
    return WorkoutDayResponse(
        id = id?:"",
        day = day,
        time = time,
        name = name,
        exercises = exercises.map { it.toExerciseResponse() }
    )
}

fun LocalWorkoutDay.toDomainModel():Workout{
    return Workout(
        id = id?.toString()?:"",
        day = day,
        time = time,
        name = name,
        exercises = emptyList()
    )
}

fun Workout.toLocalWorkoutDay():LocalWorkoutDay{
    return LocalWorkoutDay(
        id = 0,
        day = day,
        time = time,
        name = name
    )
}

fun WorkoutDayWithExercises.toDomainModel():Workout{
    return Workout(
        id = workoutDay.id?.toString()?:"",
        day = workoutDay.day,
        time = workoutDay.time,
        name = workoutDay.name,
        exercises = exercises.map { it.toExercise() }
    )
}

fun WorkoutDashboardResponse.toDomainModel():WorkoutDashboard{
    return WorkoutDashboard(
        workouts = workoutDays.map { it.toDomainModel() },
        totalExercises = totalExercises
    )
}

fun WorkoutDashboard.toResponseModel():WorkoutDashboardResponse{
    return WorkoutDashboardResponse(
        workoutDays = workouts.map { it.toResponseModel() },
        totalExercises = totalExercises
    )
}