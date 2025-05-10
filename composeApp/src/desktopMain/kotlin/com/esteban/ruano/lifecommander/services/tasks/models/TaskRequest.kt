package services.tasks.models

import kotlinx.serialization.Serializable

@Serializable
data class TaskRequest(
    val name: String? = null,
    val done: Boolean? = false,
    val note: String? = null,
    val dueDateTime: String? = null,
    val scheduledDateTime: String? = null,
    val reminders: List<Reminder>?=null,
    val priority: Int? = 0,
)