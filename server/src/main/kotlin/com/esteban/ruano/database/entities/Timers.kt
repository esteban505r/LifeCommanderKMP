package com.esteban.ruano.database.entities

import com.esteban.ruano.lifecommander.models.timers.TimerState
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import org.jetbrains.exposed.v1.datetime.datetime
import java.util.*

object Timers : UUIDTable() {
    val name = varchar("name", 255)
    val duration = long("duration") // in milliseconds (total planned duration) - changed from seconds for better accuracy
    val enabled = bool("enabled").default(true)
    val countsAsPomodoro = bool("counts_as_pomodoro").default(false)
    val sendNotificationOnComplete = bool("send_notification_on_complete").default(true) // Send push notification when timer completes
    val state = enumerationByName("state", 50, TimerState::class).default(TimerState.STOPPED)
    val listId = reference("list_id", TimerLists, onDelete = ReferenceOption.CASCADE)
    val order = integer("order")
    val startTime = datetime("start_time").nullable() // Server time when last started
    val pauseTime = datetime("pause_time").nullable() // Server time when last paused (if any)
    val accumulatedPausedMs = long("accumulated_paused_ms").default(0) // Total time paused so far (ms)
    val updatedAt = datetime("updated_at") // For optimistic concurrency / conflict detection
}

class Timer(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Timer>(Timers)

    var name by Timers.name
    var duration by Timers.duration
    var state by Timers.state
    var enabled by Timers.enabled
    var countsAsPomodoro by Timers.countsAsPomodoro
    var sendNotificationOnComplete by Timers.sendNotificationOnComplete
    var list by TimerList referencedOn Timers.listId
    var order by Timers.order
    var startTime by Timers.startTime
    var pauseTime by Timers.pauseTime
    var accumulatedPausedMs by Timers.accumulatedPausedMs
    var updatedAt by Timers.updatedAt
}