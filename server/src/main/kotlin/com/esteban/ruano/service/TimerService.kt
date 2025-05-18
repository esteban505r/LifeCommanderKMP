package com.esteban.ruano.service

import com.esteban.ruano.database.converters.toDomainModel
import com.esteban.ruano.database.converters.toResponseDTO
import com.esteban.ruano.database.entities.Timer
import com.esteban.ruano.database.entities.TimerList
import com.esteban.ruano.database.entities.UserSetting
import com.esteban.ruano.database.entities.User
import com.esteban.ruano.database.entities.TimerLists
import com.esteban.ruano.database.entities.UserSettings
import com.esteban.ruano.lifecommander.models.Timer as DomainTimer
import com.esteban.ruano.lifecommander.models.TimerList as DomainTimerList
import com.esteban.ruano.lifecommander.models.UserSettings as DomainUserSettings
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class TimerService : BaseService() {
    fun createTimerList(userId: Int, name: String, loopTimers: Boolean, pomodoroGrouped: Boolean): DomainTimerList? {
        return transaction {
            try {
                TimerList.new {
                    this.name = name
                    this.userId = User[userId]
                    this.loopTimers = loopTimers
                    this.pomodoroGrouped = pomodoroGrouped
                }.toDomainModel()
            } catch (e: Exception) {
                null
            }
        }
    }

    fun getTimerLists(userId: Int): List<DomainTimerList> {
        return transaction {
            TimerList.find { TimerLists.userId eq userId }
                .map { it.toDomainModel() }
                .toList()
        }
    }

    fun getTimerList(listId: UUID): DomainTimerList? {
        return transaction {
            TimerList.findById(listId)?.toDomainModel()
        }
    }

    fun updateTimerList(listId: UUID, name: String, loopTimers: Boolean, pomodoroGrouped: Boolean): DomainTimerList? {
        return transaction {
            TimerList.findById(listId)?.apply {
                this.name = name
                this.loopTimers = loopTimers
                this.pomodoroGrouped = pomodoroGrouped
            }?.toDomainModel()
        }
    }

    fun deleteTimerList(listId: UUID): Boolean {
        return transaction {
            try {
                TimerList.findById(listId)?.delete()
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    fun createTimer(
        listId: UUID,
        name: String,
        duration: Int,
        enabled: Boolean,
        countsAsPomodoro: Boolean,
        order: Int
    ): DomainTimer? {
        return transaction {
            try {
                Timer.new {
                    this.name = name
                    this.duration = duration
                    this.enabled = enabled
                    this.countsAsPomodoro = countsAsPomodoro
                    this.listId = TimerList[listId]
                    this.order = order
                }.toDomainModel()
            } catch (e: Exception) {
                null
            }
        }
    }

    fun updateTimer(
        timerId: UUID,
        name: String,
        duration: Int,
        enabled: Boolean,
        countsAsPomodoro: Boolean,
        order: Int
    ): DomainTimer? {
        return transaction {
            Timer.findById(timerId)?.apply {
                this.name = name
                this.duration = duration
                this.enabled = enabled
                this.countsAsPomodoro = countsAsPomodoro
                this.order = order
            }?.toDomainModel()
        }
    }

    fun deleteTimer(timerId: UUID): Boolean {
        return transaction {
            try {
                Timer.findById(timerId)?.delete()
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    fun getUserSettings(userId: Int): DomainUserSettings? {
        return transaction {
            UserSetting.find { UserSettings.userId eq userId }.firstOrNull()
                ?.toDomainModel()
                ?: UserSetting.new {
                    this.userId = User[userId]
                }.toDomainModel()
        }
    }

    fun updateUserSettings(
        userId: Int,
        defaultTimerListId: UUID?,
        dailyPomodoroGoal: Int,
        notificationsEnabled: Boolean
    ): DomainUserSettings? {
        return transaction {
            try {
                val settings = UserSetting.find { UserSettings.userId eq userId }.firstOrNull()
                    ?: UserSetting.new {
                        this.userId = User[userId]
                    }
                
                settings.apply {
                    this.defaultTimerListId = defaultTimerListId?.let { TimerList[it] }
                    this.dailyPomodoroGoal = dailyPomodoroGoal
                    this.notificationsEnabled = notificationsEnabled
                }.toDomainModel()
            } catch (e: Exception) {
                null
            }
        }
    }
} 