package com.esteban.ruano.models.habits

import kotlinx.serialization.Serializable
import com.esteban.ruano.models.reminders.CreateReminderDTO

@Serializable
data class CreateHabitDTO(
    val name: String,
    val frequency: String,
    val dateTime: String?,
    val note: String,
    val reminders: List<CreateReminderDTO>
)