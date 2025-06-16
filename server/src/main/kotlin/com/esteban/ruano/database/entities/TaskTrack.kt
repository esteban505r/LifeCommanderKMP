package com.esteban.ruano.database.entities

import com.esteban.ruano.database.models.Status
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object TaskTracks : IntIdTable() {
    val taskId = reference("task_id", Tasks.id, onDelete = ReferenceOption.CASCADE)
    val doneDateTime = datetime("done_date_time")
    val status = enumerationByName("status", 10, Status::class).default(Status.ACTIVE)
}

class TaskTrack(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<TaskTrack>(TaskTracks)

    var task by Task referencedOn TaskTracks.taskId
    var doneDateTime by TaskTracks.doneDateTime
    var status by TaskTracks.status
} 