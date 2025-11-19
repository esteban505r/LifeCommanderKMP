package com.esteban.ruano.models.tasks

import kotlinx.serialization.Serializable
import com.esteban.ruano.models.reminders.ReminderDTO
import com.esteban.ruano.models.tags.TagDTO

@Serializable
data class TaskDTO(
    val id: String,
    val name: String,
    val done: Boolean?,
    val note: String,
    val dueDateTime: String?,
    val scheduledDateTime: String?,
    val reminders: List<ReminderDTO>?,
    val priority: Int,
    val tags: List<TagDTO>? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)