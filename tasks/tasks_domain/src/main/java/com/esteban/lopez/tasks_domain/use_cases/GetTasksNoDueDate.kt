package com.esteban.ruano.tasks_domain.use_cases

import com.esteban.ruano.tasks_domain.model.Task
import com.esteban.ruano.tasks_domain.repository.TasksRepository
import com.esteban.ruano.core.domain.model.DataException

class GetTasksNoDueDate(
    val repository: TasksRepository
) {
    suspend operator fun invoke(
        filter: String? = null,
        page: Int? = null,
        limit: Int? = null
    ): Result<List<Task>> {
        return repository.getTasksNoDueDate(
            filter = filter,
            page = page,
            limit = limit
        )
    }
}