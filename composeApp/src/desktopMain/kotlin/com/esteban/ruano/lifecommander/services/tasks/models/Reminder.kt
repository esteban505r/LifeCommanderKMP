package services.tasks.models

import kotlinx.serialization.Serializable

@Serializable
data class Reminder(
    val id: String? = null,
    val time: Long,
)