package com.esteban.ruano.tasks_domain.use_cases

import com.esteban.ruano.core.domain.model.DataException
import com.lifecommander.models.Task
import com.esteban.ruano.tasks_domain.repository.TasksRepository

class GetTask(
    val repository: TasksRepository
){
    suspend operator fun invoke(id:String): Result<Task>{
        return repository.getTask(id)
    }
}