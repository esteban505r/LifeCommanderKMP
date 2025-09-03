package com.esteban.ruano.database.entities

import com.esteban.ruano.database.models.Status
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import java.util.*

object Reminders : UUIDTable() {
    val time = long("time")
    val enabled = bool("enabled")
    val habitId = reference("habit_id",Habits.id).nullable()
    val taskId = reference("task_id",Tasks.id).nullable()
    val status = enumerationByName("status", 10, Status::class).default(Status.ACTIVE)
}

class Reminder(id: EntityID<UUID>) :  UUIDEntity(id) {
    companion object : UUIDEntityClass<Reminder>(Reminders)
    var time by Reminders.time
    var enabled by Reminders.enabled
    var habitId by Reminders.habitId
    var taskId by Reminders.taskId
    var status by Reminders.status
}