package com.esteban.ruano.database.entities

import com.esteban.ruano.database.models.Status

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.time
import java.util.*

object WorkoutDays : UUIDTable() {

    val name = varchar("name", 255).default("Workout Day")
    val day = integer("day")
    val time = time("time")
    val user = reference("user_id", Users, ReferenceOption.CASCADE)
    val status = enumerationByName("status", 10, Status::class).default(Status.ACTIVE)
}

class WorkoutDay(id: EntityID<UUID>) :  UUIDEntity(id) {
    companion object : UUIDEntityClass<WorkoutDay>(WorkoutDays)

    var day by WorkoutDays.day
    var time by WorkoutDays.time
    var name by WorkoutDays.name
    var status by WorkoutDays.status
    var user by User referencedOn WorkoutDays.user
}