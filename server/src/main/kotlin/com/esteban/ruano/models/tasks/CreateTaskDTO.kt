package com.esteban.ruano.models.tasks

import kotlinx.serialization.Serializable
import com.esteban.ruano.models.reminders.CreateReminderDTO

@Serializable
data class CreateTaskDTO(
    val name: String,
    val doneDateTime: String? = null,
    val scheduledDateTime: String? = null,
    val priority: Int = 0,
    val note: String,
    val reminders: List<CreateReminderDTO>? = null,
    val dueDateTime: String? = null,
    val createdAt: String? = null,
)