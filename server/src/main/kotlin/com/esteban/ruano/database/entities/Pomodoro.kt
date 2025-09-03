package com.esteban.ruano.database.entities

import com.esteban.ruano.database.models.Status
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import org.jetbrains.exposed.v1.datetime.datetime
import java.util.*

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