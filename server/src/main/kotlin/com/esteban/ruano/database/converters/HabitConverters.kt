package com.esteban.ruano.database.converters


import com.esteban.ruano.database.entities.Habit
import com.esteban.ruano.models.habits.CreateHabitDTO
import com.esteban.ruano.models.habits.HabitDTO
import com.esteban.ruano.models.habits.UpdateHabitDTO
import com.esteban.ruano.utils.formatDateTime

fun Habit.toHabitDTO(): HabitDTO {
    return HabitDTO(
        id = this.id.toString(),
        name = this.name,
        frequency = this.frequency,
        note = this.note,
        dateTime = formatDateTime(this.baseDateTime),
        done = false,
        reminders = emptyList(),
    )
}

fun HabitDTO.toCreateHabitDTO(): CreateHabitDTO {
    return CreateHabitDTO(
        name = this.name,
        frequency = this.frequency,
        note = this.note,
        dateTime = this.dateTime,
        reminders = this.reminders.map { it.toCreateReminderDTO() },
    )
}

fun HabitDTO.toUpdateHabitDTO(): UpdateHabitDTO {
    return UpdateHabitDTO(
        name = this.name,
        frequency = this.frequency,
        note = this.note,
        dateTime = this.dateTime,
    )
}