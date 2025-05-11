package com.esteban.ruano.database.entities

import com.esteban.ruano.database.models.Status
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import java.util.UUID

object Pomodoros : UUIDTable() {
    val startDateTime = datetime("start_date_time")
    val endDateTime = datetime("end_date_time")
    val user = reference("user_id", Users, ReferenceOption.CASCADE)
    val status = enumerationByName("status", 10, Status::class).default(Status.ACTIVE)
}

class Pomodoro(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Pomodoro>(Pomodoros)
    var startDateTime by Pomodoros.startDateTime
    var endDateTime by Pomodoros.endDateTime
    var user by User referencedOn Pomodoros.user
    var status by Pomodoros.status
} 