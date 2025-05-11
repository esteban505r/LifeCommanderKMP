package com.esteban.ruano.database.converters

import com.esteban.ruano.utils.formatDateTime
import kotlinx.datetime.*
import com.esteban.ruano.database.entities.Task
import com.esteban.ruano.models.tasks.CreateTaskDTO
import com.esteban.ruano.models.tasks.TaskDTO
import com.esteban.ruano.models.tasks.UpdateTaskDTO


fun Task.toDTO(): TaskDTO {
    return TaskDTO(
        id = this.id.toString(),
        name = this.name,
        dueDateTime = dueDate?.let { formatDateTime(it) },
        note = this.note,
        done = this.doneDate!=null,
        reminders = emptyList(),
        priority = this.priority.value,
        scheduledDateTime = scheduledDate?.let { formatDateTime(it) },
    )
}

fun TaskDTO.toCreateTaskDTO(): CreateTaskDTO {
    val currentDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    return CreateTaskDTO(
        name = this.name,
        dueDateTime = this.dueDateTime,
        note = this.note,
        doneDateTime = if(this.done!=null && this.done) formatDateTime(
            currentDateTime
        ) else null
    )
}

fun TaskDTO.toUpdateTaskDTO(): UpdateTaskDTO {
    return UpdateTaskDTO(
        name = this.name,
        dueDateTime = this.dueDateTime,
        note = this.note,
        doneDateTime = if(this.done!=null && this.done) formatDateTime(
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        ) else null
    )
}


