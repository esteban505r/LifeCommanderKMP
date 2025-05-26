package com.esteban.ruano.lifecommander.database

import com.esteban.ruano.core_data.local.model.SyncItemDTO
import com.lifecommander.models.Task
import com.esteban.ruano.workout_domain.model.WorkoutDay
import com.lifecommander.models.Habit

data class SyncDTO(
    val tasks: List<SyncItemDTO<Task>>,
    val habits: List<SyncItemDTO<Habit>>,
    val tasksSynced: List<SyncItemDTO<Task>>,
    val habitsSynced: List<SyncItemDTO<Habit>>,
    val workoutDays: List<SyncItemDTO<WorkoutDay>>,
    val lastTimeStamp: Long
)