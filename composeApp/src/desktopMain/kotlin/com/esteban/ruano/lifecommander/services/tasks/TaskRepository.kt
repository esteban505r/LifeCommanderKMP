package services.tasks

import com.esteban.ruano.models.Task

interface TaskRepository {
    suspend fun getByDate(token: String, page: Int, limit: Int, date: String): List<Task>
    suspend fun getByDateRange(token: String, page: Int, limit: Int, startDate: String, endDate: String): List<Task>
    suspend fun completeTask(token: String, id: String, dateTime: String)
    suspend fun unCompleteTask(token: String, id: String, dateTime: String)
    suspend fun getAll(token: String, page: Int, limit: Int): List<Task>
    suspend fun addTask(token: String, name: String, dueDate: String?, scheduledDate: String?, note: String?, priority: Int)
    suspend fun getNoDueDateTasks(token: String, page: Int, limit: Int): List<Task>
    suspend fun updateTask(token: String, id: String, task: Task)
    suspend fun deleteTask(token: String, id: String)
} 