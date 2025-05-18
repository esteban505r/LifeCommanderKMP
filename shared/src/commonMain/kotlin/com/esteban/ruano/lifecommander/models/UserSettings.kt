package com.esteban.ruano.lifecommander.models

data class UserSettings(
    val id: String? = null,
    val defaultTimerListId: String? = null,
    val dailyPomodoroGoal: Int = 0,
    val notificationsEnabled: Boolean = false,
)