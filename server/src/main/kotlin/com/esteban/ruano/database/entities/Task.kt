package com.esteban.ruano.database.entities

import com.esteban.ruano.database.models.Priority
import com.esteban.ruano.database.models.Status
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import java.util.UUID

object Tasks : UUIDTable() {
    val name = varchar("name", 50)
    val note = varchar("note", 255)
    val doneDateTime = datetime("done_date_time").nullable()
    val scheduledDateTime = datetime("scheduled_date_time").nullable()
    val dueDateTime = datetime("due_date_time").nullable()
    val priority = enumerationByName("priority", 10, Priority::class).default(Priority.NONE)
    val user = reference("user_id", Users, ReferenceOption.CASCADE)
    val status = enumerationByName("status", 10, Status::class).default(Status.ACTIVE)
}

class Task(id: EntityID<UUID>) :  UUIDEntity(id) {
    companion object : UUIDEntityClass<Task>(Tasks)
    var name by Tasks.name
    var doneDate by Tasks.doneDateTime
    var dueDate by Tasks.dueDateTime
    var scheduledDate by Tasks.scheduledDateTime
    var priority by Tasks.priority
    var note by Tasks.note
    var user by User referencedOn Tasks.user
    var status by Tasks.status
}