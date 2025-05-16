package services.tasks.models

import com.lifecommander.models.Reminder
import kotlinx.serialization.Serializable

@Serializable
data class TaskResponse(
    val id: String,
    val name: String? = null,
    val done: Boolean? = false,
    val note: String? = null,
    val priority: Int? = 0,
    val dueDateTime: String? = null,
    val scheduledDateTime: String? = null,
    val reminders: List<Reminder>?=null,
)