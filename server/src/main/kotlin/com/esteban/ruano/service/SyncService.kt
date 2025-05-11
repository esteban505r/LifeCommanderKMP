package com.esteban.ruano.service

import formatDateTime
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import com.esteban.ruano.database.converters.toCreateHabitDTO
import com.esteban.ruano.database.converters.toCreateTaskDTO
import com.esteban.ruano.database.converters.toUpdateHabitDTO
import com.esteban.ruano.database.converters.toUpdateTaskDTO
import com.esteban.ruano.database.entities.Habit
import com.esteban.ruano.database.entities.HistoryTrack
import com.esteban.ruano.database.entities.HistoryTracks
import com.esteban.ruano.database.entities.Task
import com.esteban.ruano.database.models.DBActions
import com.esteban.ruano.models.habits.HabitDTO
import com.esteban.ruano.models.sync.SyncDTO
import com.esteban.ruano.models.sync.SyncItemDTO
import com.esteban.ruano.models.tasks.TaskDTO
import com.esteban.ruano.utils.SyncUtils.sortedByAction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class SyncService(
    private val taskService: TaskService,
    private val habitService: HabitService,
    private val workoutDayService: WorkoutService
) : BaseService() {

    fun sync(userId: Int, localSyncDTO: SyncDTO) : SyncDTO{
        println("Syncing user $userId")
        val currentDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

        val historyTracks = getHistoryTracks(userId, localSyncDTO.lastTimeStamp)
        val tasksToSync = mutableListOf<SyncItemDTO<TaskDTO>>()
        val habitsToSync = mutableListOf<SyncItemDTO<HabitDTO>>()
        val tasksSynced = mutableListOf<SyncItemDTO<TaskDTO>>()
        val habitsSynced = mutableListOf<SyncItemDTO<HabitDTO>>()

        //val workoutDaysToSync = mutableListOf<TaskDTO>()

        historyTracks.forEach{
            try {
                when(it.entityName){
                    Task.table.tableName -> {
                        val task = taskService.getByIdAndUserId(it.entityId,userId)
                        if(task != null) {
                            tasksToSync.add(
                                SyncItemDTO(
                                    action = it.actionType,
                                    item = task
                                )
                            )
                        }
                    }
                    Habit.table.tableName -> {
                        val habit = habitService.getByIdAndUserId(it.entityId,userId,formatDateTime(currentDateTime))!!
                        habitsToSync.add(
                            SyncItemDTO(
                                action = it.actionType,
                                item = habit
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                println("Error: ${e.message}")
            }
        }

        var tasks = localSyncDTO.tasks.sortedByAction()
        var habits = localSyncDTO.habits.sortedByAction()
        //val workoutDays = localSyncDTO.workoutDays.sortedByAction()

        tasks.forEach { item ->
            val id = UUID.fromString(item.item.id)
            when (DBActions.valueOf(item.action)) {
                DBActions.INSERT -> {
                    val remoteId = taskService.create(userId, item.item.toCreateTaskDTO())
                    if(remoteId != null) {
                        tasksSynced.add(
                            SyncItemDTO(
                                action = item.action,
                                item = item.item,
                                remoteId = remoteId.toString()
                            )
                        )
                        tasks.map {
                            if(it.item.id == id.toString()) {
                                it.copy(
                                    item = it.item.copy(
                                        id = remoteId.toString()
                                    )
                                )
                            }
                        }
                    }
                }
                DBActions.UPDATE -> {
                    val synced = taskService.update(userId, id, item.item.toUpdateTaskDTO())
                    if(synced) {
                        tasksSynced.add(
                            SyncItemDTO(
                                action = item.action,
                                item = item.item
                            )
                        )
                    }
                }
                DBActions.DELETE -> {
                    val synced = taskService.delete(userId, id)
                    if(synced) {
                        tasksSynced.add(
                            SyncItemDTO(
                                action = item.action,
                                item = item.item
                            )
                        )
                    }
                }
            }
        }

        habits.forEach { item ->
            val id = UUID.fromString(item.item.id)
            when (DBActions.valueOf(item.action)) {
                DBActions.INSERT -> {
                    val remoteId = habitService.create(userId, item.item.toCreateHabitDTO(
                        userId
                    ))
                    if(remoteId != null) {
                        habitsSynced.add(
                            SyncItemDTO(
                                action = item.action,
                                item = item.item,
                                remoteId = remoteId.toString()
                            )
                        )
                        habits.map {
                            if (it.item.id == id.toString()) {
                                it.copy(
                                    item = it.item.copy(
                                        id = remoteId.toString()
                                    )
                                )
                            }
                        }
                    }
                }
                DBActions.UPDATE -> {
                    val synced = habitService.update(userId, id, item.item.toUpdateHabitDTO())
                    if(synced) {
                        habitsSynced.add(
                            SyncItemDTO(
                                action = item.action,
                                item = item.item
                            )
                        )
                    }
                }
                DBActions.DELETE -> {
                    val synced = habitService.delete(userId, id)
                    if(synced) {
                        habitsSynced.add(
                            SyncItemDTO(
                                action = item.action,
                                item = item.item
                            )
                        )
                    }
                }
            }
        }

        val syncResult = SyncDTO(
            tasks = tasksToSync,
            habits = habitsToSync,
            lastTimeStamp = historyTracks.lastOrNull()?.timestamp ?: 0,
            workoutDays = emptyList(),
            tasksSynced = tasksSynced,
            habitsSynced = habitsSynced
        )

        return syncResult

    }

    private fun getHistoryTracks(userId: Int, lastSyncTimeStamp: Long): List<HistoryTrack> {
        return transaction {
            HistoryTrack.find {
                (HistoryTracks.user_id eq userId) and (HistoryTracks.timestamp greater lastSyncTimeStamp)
            }.toList()
        }
    }
}