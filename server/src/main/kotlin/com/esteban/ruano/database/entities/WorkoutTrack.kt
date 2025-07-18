package com.esteban.ruano.database.entities

import com.esteban.ruano.database.models.Status
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import java.util.UUID

object WorkoutTracks : UUIDTable() {
    val workoutDayId = reference("workout_day_id", WorkoutDays.id, onDelete = ReferenceOption.CASCADE)
    val doneDateTime = datetime("done_date_time")
    val status = enumerationByName("status", 10, Status::class).default(Status.ACTIVE)
}

class WorkoutTrack(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<WorkoutTrack>(WorkoutTracks)

    var workoutDay by WorkoutDay referencedOn WorkoutTracks.workoutDayId
    var doneDateTime by WorkoutTracks.doneDateTime
    var status by WorkoutTracks.status
}