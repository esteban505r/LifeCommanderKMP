package com.esteban.ruano.lifecommander.utilities

import android.util.Log
import com.esteban.ruano.core.domain.preferences.Preferences
import com.esteban.ruano.core.helpers.NetworkHelper
import com.esteban.ruano.core_data.local.model.DBActions
import com.esteban.ruano.core_data.local.model.SyncItemDTO
import com.esteban.lopez.habits_domain.repository.HabitsRepository
import com.esteban.ruano.lifecommander.database.SyncDTO
import com.esteban.ruano.lifecommander.domain.repository.SyncRepository
import com.esteban.ruano.tasks_domain.repository.TasksRepository
import kotlinx.coroutines.flow.first

object SyncUtils{

    fun <T> List<SyncItemDTO<T>>.sortedByAction(): List<SyncItemDTO<T>> {
        val actionOrder = listOf(DBActions.INSERT, DBActions.UPDATE, DBActions.DELETE)
        return this.sortedWith(compareBy { actionOrder.indexOf(DBActions.valueOf(it.action)) })
    }

    suspend fun sync(
        repository: SyncRepository,
        tasksRepository: TasksRepository,
        habitsRepository: HabitsRepository,
        preferences: Preferences,
        networkHelper: NetworkHelper
    ): Result<SyncDTO>{
        return if (networkHelper.isNetworkAvailable()) {
            val localData = repository.getLocalSyncData(
                lastSyncTimestamp = preferences.loadLastSyncTime().first()
            )
            val localSync = if(localData.isSuccess){
                localData.getOrThrow()
            }
            else {
                Log.e("SyncUtils", "Error getting local data")
                return Result.failure(
                    Exception("Error getting local data")
                )
            }

            val data = repository.sync(localSync)

            if (data.isSuccess) {
                return data.fold(
                    onSuccess = {
                        SyncHelper.syncData(
                            data = it.tasks.sortedByAction(),
                            onCreate = { item ->
                                tasksRepository.addTask(item)
                            },
                            onUpdate = { item ->
                                item.id.let { id -> tasksRepository.updateTask(id.toString(), item) }
                            },
                            onDelete = { item ->
                                item.id.let { id -> tasksRepository.deleteTask(id.toString()) }
                            }
                        )
                        SyncHelper.syncData(
                            data = it.habits.sortedByAction(),
                            onCreate = { item ->
                                habitsRepository.addHabit(item)
                            },
                            onUpdate = { item ->
                                item.id.let { id -> habitsRepository.updateHabit(id, item) }
                            },
                            onDelete = { item ->
                                item.id.let { id -> habitsRepository.deleteHabit(id) }
                            }
                        )

                        it.tasksSynced.forEach { task ->
                            task.remoteId?.let { remoteId ->
                                tasksRepository.updateTask(task.item.id, task.item.copy(id = remoteId))
                            }
                        }

                        it.habitsSynced.forEach { habit ->
                            habit.remoteId?.let { remoteId ->
                                habitsRepository.updateHabit(habit.item.id, habit.item.copy(id = remoteId))
                            }
                        }

                        preferences.saveLastSyncTime(it.lastTimeStamp)
                        Result.success(it)
                    },
                    onFailure = {
                        Result.failure(
                            it
                        )
                    }
                )
            } else {
                Result.failure(
                    Exception("Error syncing data")
                )
            }
        } else {
            Result.failure(
                Exception("No internet connection")
            )
        }
    }
}