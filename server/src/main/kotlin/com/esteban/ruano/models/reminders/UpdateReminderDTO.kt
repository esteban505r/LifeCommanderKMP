package com.esteban.ruano.models.reminders

import kotlinx.serialization.Serializable

@Serializable
data class UpdateReminderDTO(
    val id: String,
    val taskId: String? = null,
    val habitId: String? = null,
    val time: Long,
)