package services.habits.models

import com.lifecommander.models.Reminder
import kotlinx.serialization.Serializable

@Serializable
data class HabitResponse(
    val id: String,
    val name: String? = null,
    val frequency: String? = null,
    val note: String? = null,
    val done: Boolean? = null,
    val dateTime: String?=null,
    val reminders: List<Reminder>? = null
)