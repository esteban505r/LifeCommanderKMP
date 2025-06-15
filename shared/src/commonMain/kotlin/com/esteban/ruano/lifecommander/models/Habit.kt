package com.lifecommander.models

import com.esteban.ruano.lifecommander.models.HabitReminder
import kotlinx.serialization.Serializable

@Serializable
data class Habit(
    val id: String,
    val name: String,
    val note: String?,
    val dateTime: String?,
    val done: Boolean?,
    val frequency: String,
    val reminders: List<HabitReminder>?=null,
    val streak:Int = 0,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
