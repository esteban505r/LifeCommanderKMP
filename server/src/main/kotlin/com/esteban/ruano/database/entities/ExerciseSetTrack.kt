package com.esteban.ruano.database.entities

import com.esteban.ruano.database.entities.ExerciseTracks
import com.esteban.ruano.database.models.Status
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import org.jetbrains.exposed.v1.datetime.datetime
import java.util.*

object ExerciseSetTracks : UUIDTable() {
    val exerciseTrackId = reference("exercise_track_id", ExerciseTracks.id, onDelete = ReferenceOption.CASCADE)
    val reps = integer("reps")
    val doneDateTime = datetime("done_date_time")
    val status = enumerationByName("status", 10, Status::class).default(Status.ACTIVE)
}

class ExerciseSetTrack(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ExerciseSetTrack>(ExerciseSetTracks)

    var exerciseTrack by ExerciseTrack referencedOn ExerciseSetTracks.exerciseTrackId
    var reps by ExerciseSetTracks.reps
    var doneDateTime by ExerciseSetTracks.doneDateTime
    var status by ExerciseTracks.status
}