package com.esteban.ruano.models.habits

import kotlinx.serialization.Serializable
import com.esteban.ruano.models.reminders.ReminderDTO

@Serializable
data class HabitDTO(
    val id: String,
    val name: String,
    val frequency: String,
    val dateTime: String?,
    val done: Boolean,
    val note: String,
    val reminders: List<ReminderDTO>,
    val createdAt: String? = null,
    val streak: Int = 0,
    val updatedAt: String? = null
)