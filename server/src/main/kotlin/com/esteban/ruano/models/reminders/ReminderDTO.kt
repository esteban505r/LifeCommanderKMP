package com.esteban.ruano.models.reminders

import kotlinx.serialization.Serializable

@Serializable
data class ReminderDTO(
    val id: String?,
    val time: Long,
)