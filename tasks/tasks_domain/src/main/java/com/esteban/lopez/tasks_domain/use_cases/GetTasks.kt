package com.esteban.ruano.tasks_domain.use_cases

import com.esteban.ruano.core.domain.model.DataException
import com.lifecommander.models.Task
import com.esteban.ruano.tasks_domain.repository.TasksRepository

class GetTasks(
    val repository: TasksRepository
) {
    suspend operator fun invoke(
        filter: String? = null,
        page: Int? = null,
        limit: Int? = null,
        startDate: String? = null,
        endDate: String? = null,
    ): Result<List<Task>> {
        if(startDate != null && endDate != null) {
            return repository.getTasksByDateRange(
                filter = filter,
                page = page,
                limit = limit,
                startDate = startDate,
                endDate = endDate
            )
        }
        return repository.getTasks(
            filter = filter,
            page = page,
            limit = limit,
        )
    }
}