package com.esteban.ruano.lifecommander.models.timers

import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserSettingsRequest(
    val defaultTimerListId: String?,
    val dailyPomodoroGoal: Int,
    val notificationsEnabled: Boolean
) 