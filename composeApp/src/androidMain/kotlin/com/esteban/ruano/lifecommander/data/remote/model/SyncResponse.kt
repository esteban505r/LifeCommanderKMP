package com.esteban.ruano.lifecommander.data.remote.model

import com.esteban.ruano.habits_data.remote.dto.HabitResponse
import com.esteban.ruano.tasks_data.remote.model.TaskResponse
import com.esteban.ruano.workout_data.remote.dto.WorkoutDayResponse
import kotlinx.serialization.Serializable

@Serializable
data class SyncResponse(
    val tasks: List<SyncItemResponse<TaskResponse>>,
    val habits: List<SyncItemResponse<HabitResponse>>,
    val workoutDays: List<SyncItemResponse<WorkoutDayResponse>>,
    val lastTimeStamp: Long,
    val tasksSynced: List<SyncItemResponse<TaskResponse>>,
    val habitsSynced: List<SyncItemResponse<HabitResponse>>,
)