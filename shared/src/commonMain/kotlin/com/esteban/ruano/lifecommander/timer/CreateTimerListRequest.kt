package com.esteban.ruano.lifecommander.timer

import kotlinx.serialization.Serializable

@Serializable
data class CreateTimerListRequest(
    val name: String,
    val loopTimers: Boolean,
    val pomodoroGrouped: Boolean
)

@Serializable
data class CreateTimerRequest(
    val listId: String,
    val name: String,
    val duration: Long,
    val enabled: Boolean,
    val countsAsPomodoro: Boolean,
    val order: Int
)

@Serializable
data class UpdateTimerRequest(
    val name: String? = null,
    val duration: Long? = null,
    val enabled: Boolean? = null,
    val countsAsPomodoro: Boolean? = null,
    val order: Int? = null
)

@Serializable
data class UpdateUserSettingsRequest(
    val defaultTimerListId: String?,
    val dailyPomodoroGoal: Int,
    val notificationsEnabled: Boolean
) 