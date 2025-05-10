package com.esteban.ruano.core.models.tasks

import com.esteban.ruano.core.models.ReminderResponseInterface

interface TaskResponseInterface {
    val id: String
    val name: String?
    val note: String?
    val done: Boolean?
    val dueDateTime: String?
    val scheduledDateTime: String?
    val priority: Int?
    val reminders: List<ReminderResponseInterface>?
}