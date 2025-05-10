package com.esteban.ruano.habits_data.mapper

import com.esteban.ruano.habits_data.remote.dto.HabitReminderResponseResponse
import com.esteban.ruano.habits_data.remote.dto.HabitResponse
import com.esteban.ruano.habits_domain.model.Frequency
import com.esteban.ruano.habits_domain.model.Habit
import com.esteban.ruano.habits_domain.model.HabitReminder
import com.esteban.ruano.habits_data.local.model.Habit as HabitEntity


fun HabitResponse.toDomainModel(): Habit {
    return Habit(
        id = id,
        name = name,
        frequency = frequency,
        dateTime = dateTime,
        note = note,
        done = done,
        reminders = reminders?.map { it.toDomainModel() }
    )
}

fun Habit.toResponseModel(): HabitResponse {
    return HabitResponse(
        id = id,
        name = name,
        frequency = frequency,
        dateTime = dateTime,
        note = note,
        done = done,
        reminders = reminders?.map { it.toResponseModel() }
    )
}

fun HabitReminderResponseResponse.toDomainModel(): HabitReminder {
    return HabitReminder(
        id = id,
        time = time,
    )
}

fun HabitReminder.toResponseModel(): HabitReminderResponseResponse {
    return HabitReminderResponseResponse(
        id = id,
        time = time,
    )
}

fun HabitResponse.toDatabaseEntity(): HabitEntity {
    return HabitEntity(
        id = this.id ?: throw IllegalArgumentException("Id cannot be null"),
        name = this.name?:"",
        frequency = this.frequency?:Frequency.DAILY.value,
        note = this.note?:"",
        done = this.done?:false,
        dateTime = this.dateTime?:"00:00"
    )
}

fun Habit.toDatabaseEntity(): HabitEntity {
    return HabitEntity(
        id = this.id ?: throw IllegalArgumentException("Id cannot be null"),
        name = this.name?:"",
        frequency = this.frequency?:Frequency.DAILY.value,
        note = this.note?:"",
        done = this.done?:false,
        dateTime = this.dateTime?:"00:00"
    )
}

fun HabitEntity.toDomainModel(): Habit {
    return Habit(
        id = this.id,
        name = this.name,
        frequency = this.frequency,
        note = this.note,
        done = this.done,
        dateTime = this.dateTime
    )
}

