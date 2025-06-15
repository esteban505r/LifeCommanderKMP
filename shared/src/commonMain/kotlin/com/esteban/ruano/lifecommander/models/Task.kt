package com.lifecommander.models

import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val id: String,
    val name: String,
    val done: Boolean = false,
    val note: String? = null,
    val dueDateTime: String? = null,
    val scheduledDateTime: String? = null,
    val reminders: List<Reminder>? = null,
    val priority: Int = 0,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class Reminder(
    val id: String? = null,
    val time: Long,
    val type: ReminderType = ReminderType.NOTIFICATION,
)

enum class ReminderType {
    NOTIFICATION,
    EMAIL,
    SMS
} 