package com.esteban.ruano.database.converters

import com.esteban.ruano.database.entities.Timer
import com.esteban.ruano.database.entities.TimerList
import com.esteban.ruano.database.entities.UserSetting


fun UserSetting.toDomainModel(): com.esteban.ruano.lifecommander.models.UserSettings {
    return com.esteban.ruano.lifecommander.models.UserSettings(
        id = this.id.value.toString(),
        defaultTimerListId = this.defaultTimerListId?.id.toString(),
        dailyPomodoroGoal = this.dailyPomodoroGoal,
        notificationsEnabled = this.notificationsEnabled
    )
}