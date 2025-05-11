package com.esteban.ruano.models

data class Habit(
    val id: String,
    val name: String,
    val note: String?,
    val dateTime: String?,
    val done: Boolean?,
    val frequency: HabitFrequency,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

enum class HabitFrequency {
    DAILY,
    WEEKLY,
    MONTHLY,
    CUSTOM
} 