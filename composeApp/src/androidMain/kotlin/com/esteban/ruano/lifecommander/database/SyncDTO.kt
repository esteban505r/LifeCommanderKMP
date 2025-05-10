package com.esteban.ruano.lifecommander.database

import com.esteban.ruano.core_data.local.model.SyncItemDTO
import com.esteban.ruano.habits_domain.model.Habit
import com.esteban.ruano.tasks_domain.model.Task
import com.esteban.ruano.workout_domain.model.WorkoutDay

data class SyncDTO(
    val tasks: List<SyncItemDTO<Task>>,
    val habits: List<SyncItemDTO<Habit>>,
    val tasksSynced: List<SyncItemDTO<Task>>,
    val habitsSynced: List<SyncItemDTO<Habit>>,
    val workoutDays: List<SyncItemDTO<WorkoutDay>>,
    val lastTimeStamp: Long
)