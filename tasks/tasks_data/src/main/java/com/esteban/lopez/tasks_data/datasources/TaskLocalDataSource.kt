package com.esteban.ruano.tasks_data.datasources

import com.esteban.ruano.core_data.local.HistoryTrackDao
import com.esteban.ruano.core_data.local.addDeleteOperation
import com.esteban.ruano.core_data.local.addInsertOperation
import com.esteban.ruano.core_data.local.addUpdateOperation
import com.esteban.ruano.tasks_data.local.TaskDao
import com.esteban.ruano.tasks_data.mappers.toDatabaseEntity
import com.esteban.ruano.tasks_data.mappers.toDomainModel
import com.esteban.ruano.tasks_domain.model.Task
import com.esteban.ruano.tasks_data.local.model.Task as LocalTask

class TaskLocalDataSource(
    private val taskDao: TaskDao,
    private val historyTrackDao: HistoryTrackDao
) : TaskDataSource {
    override suspend fun getTasks(filter: String, page: Int, limit: Int): List<Task> {
        return taskDao.getTasks(filter, page, limit).map { it.toDomainModel() }
    }

    override suspend fun getTasksByDateRange(
        filter: String,
        page: Int,
        limit: Int,
        startDate: String,
        endDate: String
    ): List<Task> = taskDao.getTasksByDateRange(filter, page, limit, startDate, endDate)
        .map { it.toDomainModel() }

    override suspend fun getTasksNoDueDate(filter: String, page: Int, limit: Int): List<Task> =
        taskDao.getTasksNoDueDate(filter, page, limit).map { it.toDomainModel() }

    override suspend fun getTask(taskId: String): Task = taskDao.getTask(taskId).toDomainModel()

    override suspend fun addTask(task: Task) {
        val entityId = taskDao.addTask(
            task.toDatabaseEntity()
        )
        historyTrackDao.addInsertOperation(LocalTask.TABLE_NAME, task.id)
    }

    override suspend fun deleteTask(taskId: String) {
        taskDao.deleteTask(taskId)
        historyTrackDao.addDeleteOperation(LocalTask.TABLE_NAME, taskId)
    }

    override suspend fun completeTask(taskId: String, time: String) {
        taskDao.completeTask(taskId, time)
        historyTrackDao.addUpdateOperation(LocalTask.TABLE_NAME, taskId)
    }

    override suspend fun unCompleteTask(taskId: String) {
        taskDao.unCompleteTask(taskId)
        historyTrackDao.addUpdateOperation(LocalTask.TABLE_NAME, taskId)
    }

    override suspend fun updateTask(taskId: String, task: Task) {
        taskDao.updateTask(
            id = taskId,
            title = task.name ?: "",
            description = task.note ?: "",
            dueDate = task.dueDateTime ?: "",
            completed = task.done ?: false,
            updatedAt = System.currentTimeMillis().toString()
        )
        historyTrackDao.addUpdateOperation(LocalTask.TABLE_NAME, taskId)
    }
}