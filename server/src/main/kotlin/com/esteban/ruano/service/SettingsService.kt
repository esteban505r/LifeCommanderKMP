package com.esteban.ruano.service

import com.esteban.ruano.database.entities.UserSetting
import com.esteban.ruano.database.entities.UserSettings
import com.esteban.ruano.lifecommander.models.settings.UnbudgetedPeriodType
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.*
import com.esteban.ruano.lifecommander.models.UserSettings as DomainUserSettings

class SettingsService {
    
    fun getUserSettings(userId: Int): DomainUserSettings {
        return transaction {
            UserSetting.find { UserSettings.userId eq userId }.firstOrNull()
                ?.let { userSetting ->
                    DomainUserSettings(
                        id = userSetting.id.value.toString(),
                        defaultTimerListId = userSetting.defaultTimerListId?.name,
                        dailyPomodoroGoal = userSetting.dailyPomodoroGoal,
                        notificationsEnabled = userSetting.notificationsEnabled,
                        unbudgetedPeriodType = userSetting.unbudgetedPeriodType,
                        unbudgetedPeriodStartDay = userSetting.unbudgetedPeriodStartDay,
                        unbudgetedPeriodEndDay = userSetting.unbudgetedPeriodEndDay,
                        dueTasksNotificationFrequency = userSetting.dueTasksNotificationFrequency,
                        dueHabitsNotificationFrequency = userSetting.dueHabitsNotificationFrequency
                    )
                } ?: createDefaultUserSettings(userId)
        }
    }
    
    fun updateUserSettings(userId: Int, settings: DomainUserSettings): DomainUserSettings {
        return transaction {
            val userSetting = UserSetting.find { UserSettings.userId eq userId }.firstOrNull()
                ?: UserSetting.new {
                    this.userId = com.esteban.ruano.database.entities.User[userId]
                }
            
            // Update timer settings
            settings.defaultTimerListId?.let { 
                userSetting.defaultTimerListId = com.esteban.ruano.database.entities.TimerList[UUID.fromString(it)]
            }
            userSetting.dailyPomodoroGoal = settings.dailyPomodoroGoal
            userSetting.notificationsEnabled = settings.notificationsEnabled
            
            // Update budget settings
            userSetting.unbudgetedPeriodType = settings.unbudgetedPeriodType
            userSetting.unbudgetedPeriodStartDay = settings.unbudgetedPeriodStartDay
            userSetting.unbudgetedPeriodEndDay = settings.unbudgetedPeriodEndDay
            
            // Update notification frequency settings
            userSetting.dueTasksNotificationFrequency = settings.dueTasksNotificationFrequency
            userSetting.dueHabitsNotificationFrequency = settings.dueHabitsNotificationFrequency
            
            DomainUserSettings(
                id = userSetting.id.value.toString(),
                defaultTimerListId = userSetting.defaultTimerListId?.name,
                dailyPomodoroGoal = userSetting.dailyPomodoroGoal,
                notificationsEnabled = userSetting.notificationsEnabled,
                unbudgetedPeriodType = userSetting.unbudgetedPeriodType,
                unbudgetedPeriodStartDay = userSetting.unbudgetedPeriodStartDay,
                unbudgetedPeriodEndDay = userSetting.unbudgetedPeriodEndDay,
                dueTasksNotificationFrequency = userSetting.dueTasksNotificationFrequency,
                dueHabitsNotificationFrequency = userSetting.dueHabitsNotificationFrequency
            )
        }
    }
    
    private fun createDefaultUserSettings(userId: Int): DomainUserSettings {
        return transaction {
            val userSetting = UserSetting.new {
                this.userId = com.esteban.ruano.database.entities.User[userId]
                this.dailyPomodoroGoal = 8
                this.notificationsEnabled = true
                this.unbudgetedPeriodType = UnbudgetedPeriodType.MONTHLY
                this.unbudgetedPeriodStartDay = 1
                this.unbudgetedPeriodEndDay = 31
                this.dueTasksNotificationFrequency = 30 // 30 minutes default
                this.dueHabitsNotificationFrequency = 60 // 60 minutes default
            }
            
            DomainUserSettings(
                id = userSetting.id.value.toString(),
                defaultTimerListId = null,
                dailyPomodoroGoal = userSetting.dailyPomodoroGoal,
                notificationsEnabled = userSetting.notificationsEnabled,
                unbudgetedPeriodType = userSetting.unbudgetedPeriodType,
                unbudgetedPeriodStartDay = userSetting.unbudgetedPeriodStartDay,
                unbudgetedPeriodEndDay = userSetting.unbudgetedPeriodEndDay,
                dueTasksNotificationFrequency = userSetting.dueTasksNotificationFrequency,
                dueHabitsNotificationFrequency = userSetting.dueHabitsNotificationFrequency
            )
        }
    }
} 