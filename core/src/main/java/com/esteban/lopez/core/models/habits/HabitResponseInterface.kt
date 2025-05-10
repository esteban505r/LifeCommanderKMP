package com.esteban.ruano.core.models.habits

import com.esteban.ruano.core.models.ReminderResponseInterface

interface HabitResponseInterface {
    val id: String
    val name: String?
    val frequency: String?
    val note: String?
    val done: Boolean?
    val dateTime: String?
    val reminders: List<ReminderResponseInterface>?
}