package com.esteban.ruano.database.entities

import com.esteban.ruano.database.models.Status
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import org.jetbrains.exposed.v1.datetime.datetime
import java.util.*

object TaskTracks : UUIDTable() {
    val taskId = reference("task_id", Tasks.id, onDelete = ReferenceOption.CASCADE)
    val doneDateTime = datetime("done_date_time")
    val status = enumerationByName("status", 10, Status::class).default(Status.ACTIVE)
}

class TaskTrack(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object :  UUIDEntityClass<TaskTrack>(TaskTracks)

    var task by Task referencedOn TaskTracks.taskId
    var doneDateTime by TaskTracks.doneDateTime
    var status by TaskTracks.status
} 