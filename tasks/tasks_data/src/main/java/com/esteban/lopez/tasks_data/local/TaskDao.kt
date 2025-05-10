package com.esteban.ruano.tasks_data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.esteban.ruano.tasks_data.local.model.Task

@Dao
interface TaskDao{

    @Query("SELECT * FROM tasks WHERE title LIKE '%' || :filter || '%' ORDER BY title ASC LIMIT :limit OFFSET :page")
    suspend fun getTasks(filter: String, page: Int?, limit: Int?): List<Task>

    @Query("SELECT * FROM tasks WHERE title LIKE '%' || :filter || '%' AND dueDate BETWEEN :startDate AND :endDate ORDER BY title ASC LIMIT :limit OFFSET :page")
    suspend fun getTasksByDateRange(filter: String, page: Int?, limit: Int?, startDate: String, endDate: String): List<Task>

    @Query("SELECT * FROM tasks WHERE title LIKE '%' || :filter || '%' AND dueDate IS NULL ORDER BY title ASC LIMIT :limit OFFSET :page")
    suspend fun getTasksNoDueDate(filter: String, page: Int?, limit: Int?): List<Task>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTask(taskId: String): Task

    @Insert
    suspend fun addTask(task:Task)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTask(taskId: String):Int

    @Query("UPDATE tasks SET completed = 1, completedAt = :time WHERE id = :taskId")
    suspend fun completeTask(taskId: String, time: String) : Int

    @Query("UPDATE tasks SET completed = 0, completedAt = NULL WHERE id = :taskId")
    suspend fun unCompleteTask(taskId: String): Int

    @Query("UPDATE tasks SET title = :title, description = :description, dueDate = :dueDate, completed = :completed, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateTask(id: String, title: String, description: String, dueDate: String, completed: Boolean, updatedAt: String): Int
}