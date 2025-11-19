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
    val tags: List<Tag>? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val clearFields: List<String>? = null
)

@Serializable
data class Reminder(
    val id: String? = null,
    val time: Long,
    val type: ReminderType = ReminderType.NOTIFICATION,
)

enum class ReminderTimes{
    FIFTEEN_MINUTES,
    ONE_HOUR,
    EIGHT_HOURS;

    fun toTime():Long{
        when(this){
            FIFTEEN_MINUTES -> return 900000
            ONE_HOUR -> return 3600000
            EIGHT_HOURS -> return 28800000
        }
    }
}

enum class ReminderType {
    NOTIFICATION,
    EMAIL,
    SMS
} 