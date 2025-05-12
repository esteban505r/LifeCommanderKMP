package services.habits.models

import com.esteban.ruano.models.Reminder
import kotlinx.serialization.Serializable

@Serializable
data class HabitRequest(
    val name: String? = null,
    val frequency: String? = null,
    val note: String? = null,
    val done: Boolean? = null,
    val dateTime: String?=null,
    val reminders: List<Reminder>? = null
)