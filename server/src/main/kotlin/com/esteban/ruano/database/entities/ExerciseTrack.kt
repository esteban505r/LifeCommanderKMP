package com.esteban.ruano.database.entities

import com.esteban.ruano.database.models.Status
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import org.jetbrains.exposed.v1.datetime.datetime
import java.util.*

object ExerciseTracks : UUIDTable() {
    val exerciseId = reference("exercise_id", Exercises.id, onDelete = ReferenceOption.CASCADE)
    val workoutDayId = reference("workout_day_id", WorkoutDays.id, onDelete = ReferenceOption.CASCADE)
    val doneDateTime = datetime("done_date_time").nullable()
    val dateTime = datetime("date_time").nullable()
    val status = enumerationByName("status", 10, Status::class).default(Status.ACTIVE)
}

class ExerciseTrack(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ExerciseTrack>(ExerciseTracks)

    var exercise by Exercise referencedOn ExerciseTracks.exerciseId
    var workoutDay by WorkoutDay referencedOn ExerciseTracks.workoutDayId
    var doneDateTime by ExerciseTracks.doneDateTime
    var dateTime by ExerciseTracks.dateTime
    var status by ExerciseTracks.status
} 