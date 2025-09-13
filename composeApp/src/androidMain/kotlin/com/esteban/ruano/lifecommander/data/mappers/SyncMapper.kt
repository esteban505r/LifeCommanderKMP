package com.esteban.ruano.lifecommander.data.mappers

import com.esteban.ruano.core_data.local.model.SyncItemDTO
import com.esteban.ruano.habits_data.mapper.toDomainModel
import com.esteban.ruano.habits_data.mapper.toResponseModel
import com.esteban.ruano.habits_data.remote.dto.HabitResponse
import com.lifecommander.models.Habit
import com.esteban.ruano.lifecommander.data.remote.model.SyncItemResponse
import com.esteban.ruano.lifecommander.data.remote.model.SyncResponse
import com.esteban.ruano.lifecommander.database.SyncDTO
import com.esteban.ruano.lifecommander.models.Workout
import com.esteban.ruano.tasks_data.mappers.toDomainModel
import com.esteban.ruano.tasks_data.mappers.toResponseModel
import com.esteban.ruano.tasks_data.remote.model.TaskResponse
import com.lifecommander.models.Task
import com.esteban.ruano.workout_data.mappers.toDomainModel
import com.esteban.ruano.workout_data.mappers.toResponseModel
import com.esteban.ruano.workout_data.remote.dto.WorkoutDayResponse

fun SyncResponse.toDomainModel() = SyncDTO(
    tasks = tasks.map { it.toDomainModel(
        TaskResponse::toDomainModel
    ) },
    habits = habits.map { it.toDomainModel(
        HabitResponse::toDomainModel
    ) },
    workoutDays = workoutDays.map { it.toDomainModel(
        WorkoutDayResponse::toDomainModel
    ) },
    lastTimeStamp = lastTimeStamp,
    tasksSynced = tasksSynced.map { it.toDomainModel(
        TaskResponse::toDomainModel
    ) },
    habitsSynced = habitsSynced.map { it.toDomainModel(
        HabitResponse::toDomainModel
    ) }
)

fun <Y,T> SyncItemResponse<T>.toDomainModel(
    itemMapper: T.() -> Y
) = SyncItemDTO(
    item = item.itemMapper(),
    action = action
)

fun <Y,T> SyncItemDTO<T>.toResponseModel(
    itemMapper: T.() -> Y
) = SyncItemResponse(
    item = item.itemMapper(),
    action = action
)

fun SyncDTO.toResponseModel() = SyncResponse(
    tasks = tasks.map { it.toResponseModel(
        Task::toResponseModel
    ) },
    habits = habits.map { it.toResponseModel(
        Habit::toResponseModel
    ) },
    workoutDays = workoutDays.map { it.toResponseModel(
        com.esteban.ruano.workout_domain.model.Workout::toResponseModel
    ) },
    lastTimeStamp = lastTimeStamp,
    tasksSynced = tasksSynced.map { it.toResponseModel(
        Task::toResponseModel
    ) },
    habitsSynced = habitsSynced.map { it.toResponseModel(
        Habit::toResponseModel
    ) }
)
