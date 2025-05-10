package com.esteban.ruano.tasks_data.repository

import com.esteban.ruano.tasks_domain.model.Task
import com.esteban.ruano.tasks_domain.repository.TasksRepository

class FakeTaskRepository : TasksRepository {


    override suspend fun getTasks(
        filter: String?,
        page: Int?,
        limit: Int?
    ): Result<List<Task>> {
        return Result.success(
            listOf(
                Task(
                    id = "1",
                    name = "Tarea 1",
                    done = false,
                    dueDateTime = "10/10/2024",
                    note = ""
                ),
                Task(
                    id = "2",
                    name = "Tarea 2",
                    done = false,
                    dueDateTime = "11/10/2024",
                    note = ""
                ),
                Task(
                    id = "3",
                    name = "Tarea 3",
                    done = false,
                    dueDateTime = "12/10/2024",
                    note = ""
                ),
                Task(
                    id = "4",
                    name = "Tarea 4",
                    done = false,
                    dueDateTime = "13/10/2024",
                    note = ""
                ),
            )
        )
    }

    override suspend fun getTasksByDateRange(
        filter: String?,
        page: Int?,
        limit: Int?,
        startDate: String,
        endDate: String
    ): Result<List<Task>> {
        TODO("Not yet implemented")
    }

    override suspend fun getTasksNoDueDate(
        filter: String?,
        page: Int?,
        limit: Int?
    ): Result<List<Task>> {
        TODO("Not yet implemented")
    }

    override suspend fun getTask(taskId: String): Result<Task> {
        TODO("Not yet implemented")
    }

    override suspend fun addTask(task: Task): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteTask(taskId: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun completeTask(taskId: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun unCompleteTask(taskId: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun updateTask(id: String, task: Task): Result<Unit> {
        TODO("Not yet implemented")
    }


}