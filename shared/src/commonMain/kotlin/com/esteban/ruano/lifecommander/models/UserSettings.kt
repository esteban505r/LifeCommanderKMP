package com.esteban.ruano.lifecommander.models

import com.esteban.ruano.lifecommander.models.settings.UnbudgetedPeriodType
import kotlinx.serialization.Serializable

@Serializable
data class UserSettings(
    val id: String? = null,
    val defaultTimerListId: String? = null,
    val dailyPomodoroGoal: Int = 0,
    val notificationsEnabled: Boolean = false,
    val unbudgetedPeriodType: UnbudgetedPeriodType = UnbudgetedPeriodType.MONTHLY,
    val unbudgetedPeriodStartDay: Int = 1,
    val unbudgetedPeriodEndDay: Int = 31
)