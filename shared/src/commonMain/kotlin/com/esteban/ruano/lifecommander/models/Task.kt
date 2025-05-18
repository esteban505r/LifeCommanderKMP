package com.lifecommander.models

import kotlinx.serialization.Serializable

data class Task(
    val id: String,
    val name: String,
    val done: Boolean?,
    val note: String,
    val dueDateTime: String?,
    val scheduledDateTime: String?,
    val reminders: List<Reminder>?,
    val priority: Int,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class Reminder(
    val id: String,
    val time: Long,
    val type: ReminderType
)

enum class ReminderType {
    NOTIFICATION,
    EMAIL,
    SMS
} 