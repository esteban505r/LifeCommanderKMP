package com.esteban.ruano.database.entities

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import java.util.UUID

object Timers : UUIDTable() {
    val name = varchar("name", 255)
    val duration = integer("duration") // in seconds
    val enabled = bool("enabled").default(true)
    val countsAsPomodoro = bool("counts_as_pomodoro").default(false)
    val listId = reference("list_id", TimerLists, onDelete = ReferenceOption.CASCADE)
    val order = integer("order")
}

class Timer(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Timer>(Timers)

    var name by Timers.name
    var duration by Timers.duration
    var enabled by Timers.enabled
    var countsAsPomodoro by Timers.countsAsPomodoro
    var listId by TimerList referencedOn Timers.listId
    var order by Timers.order
}