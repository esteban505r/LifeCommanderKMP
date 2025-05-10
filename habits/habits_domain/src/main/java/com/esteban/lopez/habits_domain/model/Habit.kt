package com.esteban.ruano.habits_domain.model

data class Habit(
    val id: String,
    val name: String? = null,
    val type: String? = null,
    val done: Boolean? = null,
    val dateTime: String? = null,
    val reminders: List<HabitReminder>?=null,
    val frequency: String? = null,
    val note: String? = null,
)