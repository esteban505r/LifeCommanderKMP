package com.esteban.ruano.tasks_domain.repository

import com.esteban.ruano.core.domain.model.DataException
import com.esteban.ruano.tasks_domain.model.Task

interface TasksRepository {
    suspend fun getTasks(
        filter: String?,
        page: Int?,
        limit: Int?
    ): Result<List<Task>>

    suspend fun getTasksByDateRange(
        filter: String?,
        page: Int?,
        limit: Int?,
        startDate: String,
        endDate: String
    ): Result<List<Task>>

    suspend fun getTasksNoDueDate(
        filter: String?,
        page: Int?,
        limit: Int?
    ): Result<List<Task>>

    suspend fun getTask(taskId: String): Result<Task>
    suspend fun addTask(task: Task): Result<Unit>
    suspend fun deleteTask(taskId: String): Result<Unit>
    suspend fun completeTask(taskId: String): Result<Unit>
    suspend fun unCompleteTask(taskId: String): Result<Unit>
    suspend fun updateTask(id:String,task: Task): Result<Unit>
}