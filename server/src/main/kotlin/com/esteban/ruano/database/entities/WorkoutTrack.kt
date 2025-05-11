package com.esteban.ruano.database.entities

import com.esteban.ruano.database.models.Status

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.kotlin.datetime.time

object WorkoutTracks : IntIdTable() {

    val doneDateTime = datetime("done_date_time")
    val status = enumerationByName("status", 10, Status::class).default(Status.ACTIVE)
}

class WorkoutTrack(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<WorkoutTrack>(WorkoutTracks)

    var doneDateTime by WorkoutTracks.doneDateTime
    var status by WorkoutTracks.status
}