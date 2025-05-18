package com.esteban.ruano.database.entities

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import java.util.UUID

object UserSettings : UUIDTable() {
    val userId = reference("user_id", Users, onDelete = ReferenceOption.CASCADE).uniqueIndex()
    val defaultTimerListId = reference("default_timer_list_id", TimerLists, onDelete = ReferenceOption.SET_NULL).nullable()
    val dailyPomodoroGoal = integer("daily_pomodoro_goal").default(8)
    val notificationsEnabled = bool("notifications_enabled").default(true)
}

class UserSetting(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<UserSetting>(UserSettings)

    var userId by User referencedOn UserSettings.userId
    var defaultTimerListId by TimerList optionalReferencedOn UserSettings.defaultTimerListId
    var dailyPomodoroGoal by UserSettings.dailyPomodoroGoal
    var notificationsEnabled by UserSettings.notificationsEnabled
}
