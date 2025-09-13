package com.esteban.ruano.lifecommander.data.datasource

import com.esteban.ruano.core_data.local.HistoryTrackDao
import com.esteban.ruano.core_data.local.model.SyncItemDTO
import com.esteban.ruano.habits_data.local.HabitsDao
import com.esteban.ruano.habits_data.local.model.Habit
import com.esteban.ruano.habits_data.mapper.toDomainModel
import com.lifecommander.models.Habit as HabitDomain
import com.esteban.ruano.lifecommander.database.SyncDTO
import com.esteban.ruano.tasks_data.local.TaskDao
import com.esteban.ruano.tasks_data.local.model.Task
import com.esteban.ruano.tasks_data.mappers.toDomainModel
import com.lifecommander.models.Task as TaskDomain
import com.esteban.ruano.workout_data.local.WorkoutDao
import com.esteban.ruano.workout_data.local.model.WorkoutDay
import com.esteban.ruano.workout_domain.model.Workout as WorkoutDayDomain
import com.esteban.ruano.workout_data.mappers.toDomainModel

class SyncLocalDataSource(
    val historyTrackDao: HistoryTrackDao,
    val taskDao: TaskDao, 
    val habitDao: HabitsDao,
    val workoutDayDao: WorkoutDao
) : SyncDataSource {
    override suspend fun getSyncData(
        lastSyncTimestamp: Long
    ): SyncDTO {
        val historyTracks = historyTrackDao.getHistoryTracks(lastSyncTimestamp)
        val tasks = mutableListOf<SyncItemDTO<TaskDomain>>()
        val habits = mutableListOf<SyncItemDTO<HabitDomain>>()
        val workoutDays = mutableListOf<SyncItemDTO<WorkoutDayDomain>>()
        historyTracks.forEach {
            if(!it.isSynced){
                when (it.entityName) {
                    Task.TABLE_NAME -> {
                        try {
                            val task = taskDao.getTask(it.entityId)
                            tasks.add(SyncItemDTO(task.toDomainModel(), it.actionType))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    Habit.TABLE_NAME -> {
                        try {
                            val habit = habitDao.getHabit(it.entityId)
                            habits.add(SyncItemDTO(habit.toDomainModel(), it.actionType))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    WorkoutDay.TABLE_NAME -> {
                        try {
                            val workoutDay = workoutDayDao.getWorkoutDayById(it.entityId)
                            workoutDays.add(SyncItemDTO(workoutDay.toDomainModel(), it.actionType))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
        return SyncDTO(
            tasks = tasks,
            habits = habits,
            workoutDays = workoutDays,
            lastTimeStamp = historyTracks.lastOrNull()?.timestamp ?: lastSyncTimestamp,
            tasksSynced = emptyList(),
            habitsSynced = emptyList()
        )
    }

    override suspend fun sync(localSync: SyncDTO): SyncDTO? {
        return null
    }

}
