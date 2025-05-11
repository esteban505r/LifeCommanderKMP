package com.esteban.ruano.database.converters

import com.esteban.ruano.database.entities.Reminder
import com.esteban.ruano.models.reminders.CreateReminderDTO
import com.esteban.ruano.models.reminders.ReminderDTO

fun CreateReminderDTO.toReminderDTO() = ReminderDTO(
    id = this.id,
    time = this.time
)

fun ReminderDTO.toCreateReminderDTO(
    userId: Int
) = CreateReminderDTO(
    id = this.id,
    time = this.time,
    userId = userId
)

fun Reminder.toDTO(): ReminderDTO {
    return ReminderDTO(
        id = this.id.toString(),
        time = this.time
    )
}