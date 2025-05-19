package com.esteban.ruano.database.entities

import com.esteban.ruano.lifecommander.models.timers.TimerState
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import java.util.UUID

object Timers : UUIDTable() {
    val name = varchar("name", 255)
    val duration = long("duration") // in seconds
    val enabled = bool("enabled").default(true)
    val countsAsPomodoro = bool("counts_as_pomodoro").default(false)
    val state = enumerationByName("state", 50, TimerState::class).default(TimerState.STOPPED)
    val listId = reference("list_id", TimerLists, onDelete = ReferenceOption.CASCADE)
    val order = integer("order")
    val startTime = datetime("start_time").nullable()
    val pauseTime = datetime("pause_time").nullable()
}

class Timer(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Timer>(Timers)

    var name by Timers.name
    var duration by Timers.duration
    var state by Timers.state
    var enabled by Timers.enabled
    var countsAsPomodoro by Timers.countsAsPomodoro
    var list by TimerList referencedOn Timers.listId
    var order by Timers.order
    var startTime by Timers.startTime
    var pauseTime by Timers.pauseTime
}