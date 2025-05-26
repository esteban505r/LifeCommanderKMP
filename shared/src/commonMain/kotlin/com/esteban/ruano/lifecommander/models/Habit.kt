package com.lifecommander.models

import com.esteban.ruano.lifecommander.models.HabitReminder

data class Habit(
    val id: String,
    val name: String,
    val note: String?,
    val dateTime: String?,
    val done: Boolean?,
    val frequency: String,
    val reminders: List<HabitReminder>?=null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
