package com.esteban.ruano.models.reminders

import kotlinx.serialization.Serializable

@Serializable
data class CreateReminderDTO(
    val id: String? = null,
    val taskId: String? = null,
    val habitId: String? = null,
    val time: Long,
)