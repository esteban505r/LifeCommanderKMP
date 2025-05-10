package com.esteban.ruano.tasks_data.mappers

import com.esteban.ruano.tasks_data.remote.model.TaskReminderResponse
import com.esteban.ruano.tasks_data.remote.model.TaskResponse
import com.esteban.ruano.tasks_domain.model.Task
import com.esteban.ruano.tasks_domain.model.TaskReminder
import com.esteban.ruano.tasks_data.local.model.Task as TaskEntity

fun TaskResponse.toDomainModel(): Task {
    return Task(
        id = id,
        name = name,
        done = done,
        note = note,
        priority = priority,
        scheduledDateTime = scheduledDateTime,
        dueDateTime = dueDateTime,
        reminders = reminders?.map { it.toDomainModel() }
    )
}

fun TaskReminderResponse.toDomainModel(): TaskReminder {
    return TaskReminder(
        id = id,
        time = time)
}

fun TaskReminder.toResponseModel(): TaskReminderResponse {
    return TaskReminderResponse(
        id = id,
        time = time
    )
}

fun Task.toResponseModel(): TaskResponse {
    return TaskResponse(
        id = id,
        name = name,
        done = done,
        note = note,
        priority = priority,
        scheduledDateTime = scheduledDateTime,
        dueDateTime = dueDateTime,
        reminders = reminders?.map { it.toResponseModel() }
    )
}

fun Task.toDatabaseEntity(): TaskEntity {
    val currentTime = System.currentTimeMillis().toString()
    return TaskEntity(
        id = id ?: throw IllegalArgumentException("Task id cannot be null"),
        title = name ?: "",
        description = note ?: "",
        dueDate = dueDateTime,
        completed = done ?: false,
        createdAt = this.createdAt,
        scheduledDate = scheduledDateTime,
        updatedAt = updatedAt,
        completedAt = currentTime
    )
}

fun TaskEntity.toDomainModel(): Task {
    return Task(
        id = id,
        name = title,
        scheduledDateTime = scheduledDate,
        done = completed,
        note = description,
        priority = priority,
        dueDateTime = dueDate,
    )
}