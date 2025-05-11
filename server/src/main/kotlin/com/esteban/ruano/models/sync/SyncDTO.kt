package com.esteban.ruano.models.sync

import com.esteban.ruano.models.habits.HabitDTO
import com.esteban.ruano.models.tasks.TaskDTO
import com.esteban.ruano.models.workout.day.WorkoutDayDTO

data class SyncDTO(
    val tasks: List<SyncItemDTO<TaskDTO>>,
    val habits: List<SyncItemDTO<HabitDTO>>,
    val tasksSynced: List<SyncItemDTO<TaskDTO>>,
    val habitsSynced: List<SyncItemDTO<HabitDTO>>,
    val workoutDays: List<SyncItemDTO<WorkoutDayDTO>>,
    val lastTimeStamp: Long
)