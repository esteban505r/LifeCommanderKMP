package com.esteban.ruano.database.entities

import com.esteban.ruano.database.models.Priority
import com.esteban.ruano.database.models.Status
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import org.jetbrains.exposed.v1.datetime.datetime
import java.util.*

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