package com.esteban.ruano.workout_domain.use_cases

data class WorkoutUseCases(
    val getWorkoutDays: GetWorkoutDays,
    val getWorkoutDaysWithExercises: GetWorkoutDaysWithExercises,
    val getWorkoutDashboard: GetWorkoutDashboard,
    val getWorkoutDayById: GetWorkoutDayById,
    val getWorkoutDayStatus:GetWorkoutDayStatus,
    val getWorkoutDayByNumber: GetWorkoutDayByNumber,
    val getExercisesByWorkoutDay: GetExercisesByWorkoutDay,
    val getExercises: GetExercises,
    val saveExercise: SaveExercise,
    val updateExercise: UpdateExercise,
    val deleteExercise: DeleteExercise,
    val addSet: AddSet,
    val removeSet: RemoveSet,
    val completeExercise: CompleteExercise,
    val undoExercise: UndoExercise,
    val saveWorkoutDay: SaveWorkoutDay,
    val updateWorkoutDay: UpdateWorkoutDay,
    val linkExerciseToWorkoutDay: LinkExerciseWithWorkoutDay,
    val getExerciseById: GetExerciseById,
    val unlinkExerciseFromDay:UnLinkExerciseWithWorkoutDay
)