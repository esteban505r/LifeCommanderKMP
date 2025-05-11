package com.esteban.ruano.database.entities

import com.esteban.ruano.database.entities.Users.default
import com.esteban.ruano.database.models.Status
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
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