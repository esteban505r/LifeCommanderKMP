package com.esteban.ruano.tasks_data.remote.model

import com.esteban.ruano.core.models.tasks.TaskResponseInterface
import kotlinx.serialization.Serializable

@Serializable
data class TaskResponse(
    override val id: String,
    override val name: String? = null,
    override val done: Boolean? = false,
    override val note: String? = null,
    override val scheduledDateTime: String?=null,
    override val priority: Int?=null,
    override val dueDateTime: String?=null,
    override val reminders: List<TaskReminderResponse>?=null,
): TaskResponseInterface