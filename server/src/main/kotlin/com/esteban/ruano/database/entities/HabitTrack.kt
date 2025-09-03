package com.esteban.ruano.database.entities

import com.esteban.ruano.database.models.Status
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import org.jetbrains.exposed.v1.datetime.datetime
import java.util.*

object HabitTracks : UUIDTable() {

    val habitId = reference("habit_id", Habits.id, onDelete = ReferenceOption.CASCADE)
    val doneDateTime = datetime("done_date_time")
    val status = enumerationByName("status", 10, Status::class).default(Status.ACTIVE)
}

class HabitTrack(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<HabitTrack>(HabitTracks)

    var date by HabitTracks.doneDateTime
    var habit by Habit referencedOn HabitTracks.habitId
    var doneDateTime by HabitTracks.doneDateTime
    var status by HabitTracks.status
}