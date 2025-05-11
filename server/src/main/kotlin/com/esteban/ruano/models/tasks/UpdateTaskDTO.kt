package com.esteban.ruano.models.tasks

import kotlinx.serialization.Serializable
import com.esteban.ruano.models.reminders.CreateReminderDTO

@Serializable
data class UpdateTaskDTO(
    val name: String? = null,
    val note: String? = null,
    val doneDateTime: String? = null,
    val scheduledDateTime: String? = null,
    val priority: Int? = null,
    val dueDateTime: String? = null,
    val reminders: List<CreateReminderDTO>? = null,
    val updatedAt: String? = null,
)