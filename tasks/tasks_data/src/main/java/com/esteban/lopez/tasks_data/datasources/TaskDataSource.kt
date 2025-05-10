package com.esteban.ruano.tasks_data.datasources

import com.esteban.ruano.tasks_domain.model.Task

interface TaskDataSource {
    suspend fun getTasks(
        filter:String,
        page:Int,
        limit:Int,
    ): List<Task>
    suspend fun getTasksByDateRange(
        filter:String,
        page:Int,
        limit:Int,
        startDate:String,
        endDate:String
    ): List<Task>
    suspend fun getTasksNoDueDate(
        filter:String,
        page:Int,
        limit:Int,
    ): List<Task>
    suspend fun getTask(taskId: String): Task
    suspend fun addTask(task: Task)
    suspend fun deleteTask(taskId: String)
    suspend fun completeTask(taskId: String,time:String)
    suspend fun unCompleteTask(taskId: String)
    suspend fun updateTask(taskId: String,task:Task)
}
