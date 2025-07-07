package com.esteban.ruano.tasks_domain.use_cases

import com.esteban.ruano.core.domain.model.DataException
import com.esteban.ruano.core.utils.DateUtils.parseDateTime
import com.lifecommander.models.Task
import com.esteban.ruano.tasks_domain.repository.TasksRepository
import com.esteban.ruano.utils.DateUIUtils.toLocalDateTime
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class RescheduleTask(
    val repository: TasksRepository
) {
    suspend operator fun invoke(id: String, task: Task): Result<Unit> {
        return try {
            // Get the current time
            val now = LocalDateTime.now()

            // Get the original time from either scheduled or due date
            val originalDateTime = task.scheduledDateTime?.toLocalDateTime()
                ?: task.dueDateTime?.toLocalDateTime()

            if (originalDateTime != null) {
                // Create tomorrow's date with the original time
                val tomorrow = now.plus(1, ChronoUnit.DAYS)
                    .withHour(originalDateTime.hour)
                    .withMinute(originalDateTime.minute)

                // Create updated task with new date
                val updatedTask = task.copy(
                    scheduledDateTime = tomorrow.parseDateTime(),
                    dueDateTime = if (task.dueDateTime != null) tomorrow.parseDateTime() else null
                )

                repository.updateTask(id, updatedTask)
            } else {
                Result.failure(Exception("There should be a originalDateTime"))
            }
        } catch (e: Exception) {
           throw e
        }
    }
} 