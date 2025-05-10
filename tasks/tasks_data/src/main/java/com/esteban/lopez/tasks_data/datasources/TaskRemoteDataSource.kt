package com.esteban.ruano.tasks_data.datasources

import com.esteban.ruano.tasks_data.mappers.toDomainModel
import com.esteban.ruano.tasks_data.mappers.toResponseModel
import com.esteban.ruano.tasks_data.remote.TasksApi
import com.esteban.ruano.tasks_domain.model.Task

class TaskRemoteDataSource(
    private val api:TasksApi
): TaskDataSource {
    override suspend fun getTasks(filter: String, page: Int, limit: Int): List<Task> = api.getTasks(filter, page, limit).map { it.toDomainModel() }

    override suspend fun getTasksByDateRange(
        filter: String,
        page: Int,
        limit: Int,
        startDate: String,
        endDate: String
    ): List<Task> {
        return api.getTasksByDateRange(filter, page, limit, startDate, endDate).map { it.toDomainModel() }
    }

    override suspend fun getTasksNoDueDate(filter: String, page: Int, limit: Int): List<Task> {
        return api.getTasksNoDueDate(filter, page, limit).map { it.toDomainModel() }
    }

    override suspend fun getTask(taskId: String): Task {
        return api.getTask(taskId).toDomainModel()
    }

    override suspend fun addTask(task: Task) {
        api.addTask(task.toResponseModel())
    }

    override suspend fun deleteTask(taskId: String) {
        return api.deleteTask(taskId)
    }

    override suspend fun completeTask(taskId: String, time: String) {
        return api.completeTask(taskId, time)
    }

    override suspend fun unCompleteTask(taskId: String) {
        return api.unCompleteTask(taskId)
    }

    override suspend fun updateTask(taskId: String, task: Task) {
        return api.updateTask(taskId, task.toResponseModel())
    }
}