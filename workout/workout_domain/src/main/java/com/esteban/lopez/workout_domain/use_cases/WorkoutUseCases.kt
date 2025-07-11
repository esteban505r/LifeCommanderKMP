package com.esteban.ruano.workout_domain.use_cases

data class WorkoutUseCases(
    val getWorkoutDays: GetWorkoutDays,
    val getWorkoutDaysWithExercises: GetWorkoutDaysWithExercises,
    val getWorkoutDashboard: GetWorkoutDashboard,
    val getWorkoutDayById: GetWorkoutDayById,
    val getWorkoutDayByNumber: GetWorkoutDayByNumber,
    val getExercisesByWorkoutDay: GetExercisesByWorkoutDay,
    val getExercises: GetExercises,
    val saveExercise: SaveExercise,
    val saveWorkoutDay: SaveWorkoutDay,
    val updateWorkoutDay: UpdateWorkoutDay,
    val linkExerciseToWorkoutDay: LinkExerciseWithWorkoutDay,
    val getExerciseById: GetExerciseById,
)