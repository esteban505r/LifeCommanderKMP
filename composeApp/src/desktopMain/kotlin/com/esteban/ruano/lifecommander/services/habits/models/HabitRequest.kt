package services.habits.models

import kotlinx.serialization.Serializable
import services.tasks.models.Reminder

@Serializable
data class HabitRequest(
    val name: String? = null,
    val frequency: String? = null,
    val note: String? = null,
    val done: Boolean? = null,
    val dateTime: String?=null,
    val reminders: List<Reminder>? = null
)