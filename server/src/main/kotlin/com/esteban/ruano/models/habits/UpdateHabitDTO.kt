package com.esteban.ruano.models.habits

import kotlinx.serialization.Serializable
import com.esteban.ruano.models.reminders.CreateReminderDTO

@Serializable
data class UpdateHabitDTO(
    val name: String? = null,
    val frequency: String? = null,
    val dateTime: String? = null,
    val note: String? = null,
    val reminders: List<CreateReminderDTO>? = emptyList()
)