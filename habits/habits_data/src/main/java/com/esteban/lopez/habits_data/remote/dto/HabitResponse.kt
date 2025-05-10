package com.esteban.ruano.habits_data.remote.dto

import com.esteban.ruano.core.models.habits.HabitResponseInterface
import kotlinx.serialization.Serializable

@Serializable
data class HabitResponse(
    override val id: String,
    override val name: String? = null,
    override val frequency: String? = null,
    override val note: String? = null,
    override val done: Boolean? = null,
    override val dateTime: String?=null,
    override val reminders: List<HabitReminderResponseResponse>?=null,
): HabitResponseInterface