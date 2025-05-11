package com.esteban.ruano.utils

import com.esteban.ruano.models.*
import com.esteban.ruano.models.tasks.TaskDTO
import com.esteban.ruano.models.habits.HabitDTO
import com.esteban.ruano.models.reminders.ReminderDTO
import kotlinx.datetime.*

object ModelConverters {
    fun String.toLocalDateTime(): LocalDateTime? {
        return try {
            LocalDateTime.parse(this)
        } catch (e: Exception) {
            null
        }
    }

    fun LocalDateTime?.toIsoString(): String? {
        return this?.toString()
    }

    // Task conversions
    fun TaskDTO.toTask(): Task {
        return Task(
            id = id,
            name = name,
            done = done,
            note = note,
            dueDateTime = dueDateTime,
            scheduledDateTime = scheduledDateTime,
            reminders = reminders?.map { it.toReminder() },
            priority = priority,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    fun Task.toTaskDTO(): TaskDTO {
        return TaskDTO(
            id = id,
            name = name,
            done = done,
            note = note,
            dueDateTime = dueDateTime,
            scheduledDateTime = scheduledDateTime,
            reminders = reminders?.map { it.toReminderDTO() },
            priority = priority,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    fun ReminderDTO.toReminder(): Reminder {
        return Reminder(
            id = id ?: "",
            time = time,
            type = ReminderType.NOTIFICATION
        )
    }

    fun Reminder.toReminderDTO(): ReminderDTO {
        return ReminderDTO(
            id = id,
            time = time,
        )
    }

    // Habit conversions
    fun HabitDTO.toHabit(): Habit {
        return Habit(
            id = id,
            name = name,
            dateTime = dateTime,
            note = note,
            done = done,
            frequency = HabitFrequency.valueOf(frequency),
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    fun Habit.toHabitDTO(): HabitDTO {
        return HabitDTO(
            id = id,
            name = name,
            note = note ?:"",
            dateTime = dateTime,
            done = done ?: false,
            frequency = frequency.name,
            createdAt = createdAt,
            updatedAt = updatedAt,
            reminders = emptyList()
        )
    }
} 