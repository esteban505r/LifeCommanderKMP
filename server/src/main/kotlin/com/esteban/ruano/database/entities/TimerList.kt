package com.esteban.ruano.database.entities

import com.esteban.ruano.database.models.Status
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import java.util.UUID

object TimerLists : UUIDTable() {
    val name = varchar("name", 255)
    val userId = reference("user_id", Users, onDelete = ReferenceOption.CASCADE)
    val loopTimers = bool("loop_timers").default(false)
    val pomodoroGrouped = bool("pomodoro_grouped").default(false)
    val status = enumerationByName("status", 10, Status::class).default(Status.ACTIVE)
}

class TimerList(id: EntityID<UUID>) : UUIDEntity(id) {

    companion object : UUIDEntityClass<TimerList>(TimerLists)

    var name by TimerLists.name
    var userId by User referencedOn TimerLists.userId
    var loopTimers by TimerLists.loopTimers
    var pomodoroGrouped by TimerLists.pomodoroGrouped
    var status by TimerLists.status
}
