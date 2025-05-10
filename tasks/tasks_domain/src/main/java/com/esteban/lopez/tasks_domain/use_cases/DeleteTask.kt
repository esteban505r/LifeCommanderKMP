package com.esteban.ruano.tasks_domain.use_cases

import com.esteban.ruano.core.domain.model.DataException
import com.esteban.ruano.tasks_domain.repository.TasksRepository

class DeleteTask(
    val repository: TasksRepository
){

    suspend operator fun invoke(id: String): Result<Unit> {
        return repository.deleteTask(id)
    }
}