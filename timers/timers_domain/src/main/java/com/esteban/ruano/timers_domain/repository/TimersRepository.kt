package com.esteban.ruano.timers_domain.repository

import com.esteban.ruano.lifecommander.models.TimerList
import com.esteban.ruano.lifecommander.models.UserSettings

interface TimersRepository {
    suspend fun getTimerLists(): Result<List<TimerList>>

    suspend fun getTimerList(listId: String): Result<TimerList>
    
    suspend fun createTimerList(
        name: String,
        loopTimers: Boolean,
        pomodoroGrouped: Boolean
    ): Result<TimerList>
    
    suspend fun updateTimerList(
        listId: String,
        name: String,
        loopTimers: Boolean,
        pomodoroGrouped: Boolean
    ): Result<TimerList>
    
    suspend fun deleteTimerList(listId: String): Result<Unit>
    
    suspend fun createTimer(
        listId: String,
        name: String,
        duration: Long,
        enabled: Boolean,
        countsAsPomodoro: Boolean,
        sendNotificationOnComplete: Boolean,
        order: Int
    ): Result<TimerList>
    
    suspend fun updateTimer(
        timerId: String,
        name: String? = null,
        timerListId: String? = null,
        duration: Long? = null,
        enabled: Boolean? = null,
        countsAsPomodoro: Boolean? = null,
        sendNotificationOnComplete: Boolean? = null,
        order: Int? = null
    ): Result<TimerList>
    
    suspend fun deleteTimer(timerId: String): Result<Unit>
    
    suspend fun getUserSettings(): Result<UserSettings>
    
    suspend fun updateUserSettings(
        defaultTimerListId: String?,
        dailyPomodoroGoal: Int,
        notificationsEnabled: Boolean
    ): Result<UserSettings>
    
    suspend fun startTimer(listId: String, timerId: String?): Result<Unit>
    
    suspend fun pauseTimer(listId: String, timerId: String?): Result<Unit>
    
    suspend fun resumeTimer(listId: String): Result<Unit>
    
    suspend fun stopTimer(listId: String, timerId: String?): Result<Unit>
    
    suspend fun restartTimer(listId: String, timerId: String?): Result<Unit>
}

