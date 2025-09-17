package com.esteban.ruano.lifecommander.services.timers

import com.esteban.ruano.lifecommander.models.TimerList
import com.esteban.ruano.lifecommander.models.UserSettings

interface TimerRepository {
    suspend fun getTimerLists(token: String): List<TimerList>

    suspend fun getTimerList(token: String, listId: String): TimerList
    
    suspend fun createTimerList(
        token: String,
        name: String,
        loopTimers: Boolean,
        pomodoroGrouped: Boolean
    ): TimerList
    
    suspend fun updateTimerList(
        token: String,
        listId: String,
        name: String,
        loopTimers: Boolean,
        pomodoroGrouped: Boolean
    ): TimerList
    
    suspend fun deleteTimerList(token: String, listId: String)
    
    suspend fun createTimer(
        token: String,
        listId: String,
        name: String,
        duration: Long,
        enabled: Boolean,
        countsAsPomodoro: Boolean,
        order: Int
    ): TimerList
    
    suspend fun updateTimer(
        token: String? = null,
        timerId: String?= null,
        name: String? = null,
        timerListId: String? = null,
        duration: Long? = null,
        enabled: Boolean? = null,
        countsAsPomodoro: Boolean? = null,
        order: Int? = null
    ): TimerList
    
    suspend fun deleteTimer(token: String, timerId: String)
    
    suspend fun getUserSettings(token: String): UserSettings
    
    suspend fun updateUserSettings(
        token: String,
        defaultTimerListId: String?,
        dailyPomodoroGoal: Int,
        notificationsEnabled: Boolean
    ): UserSettings
} 