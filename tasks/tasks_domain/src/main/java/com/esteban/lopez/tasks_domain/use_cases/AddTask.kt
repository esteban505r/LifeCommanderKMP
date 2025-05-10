package com.esteban.ruano.tasks_domain.use_cases

import com.esteban.ruano.core.domain.model.DataException
import com.esteban.ruano.tasks_domain.model.Task
import com.esteban.ruano.tasks_domain.repository.TasksRepository

class AddTask(
    val repository: TasksRepository
){

    suspend operator fun invoke(task: Task): Result<Unit> {
        return repository.addTask(task)
    }
}