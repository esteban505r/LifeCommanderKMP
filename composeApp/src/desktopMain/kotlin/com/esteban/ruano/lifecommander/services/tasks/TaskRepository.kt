package services.tasks

import services.tasks.models.TaskRequest
import services.tasks.models.TaskResponse

interface TaskRepository {
    suspend fun getByDate(token: String, page: Int, limit: Int, date: String): List<TaskResponse>
    suspend fun getByDateRange(token: String, page: Int, limit: Int, startDate: String, endDate: String): List<TaskResponse>
    suspend fun completeTask(token: String, id: String, dateTime: String)
    suspend fun unCompleteTask(token: String, id: String, dateTime: String)
    suspend fun getAll(token: String, page: Int, limit: Int): List<TaskResponse>
    suspend fun addTask(token: String, name: String, dueDate: String?, scheduledDate: String?, note: String?, priority: Int)
    suspend fun getNoDueDateTasks(token: String, page: Int, limit: Int): List<TaskResponse>
    suspend fun updateTask(token: String, id: String, task: TaskResponse)
    suspend fun deleteTask(token: String, id: String)
} 