package com.esteban.ruano.habits_data.remote.dto

import com.esteban.ruano.core.models.ReminderResponseInterface
import kotlinx.serialization.Serializable

@Serializable
data class HabitReminderResponseResponse(
    override val id: String? = null,
    override val time: Long,
): ReminderResponseInterface