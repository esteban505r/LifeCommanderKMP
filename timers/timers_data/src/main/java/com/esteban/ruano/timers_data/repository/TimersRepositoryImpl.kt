package com.esteban.ruano.timers_data.repository

import com.esteban.ruano.core.domain.preferences.Preferences
import com.esteban.ruano.lifecommander.models.TimerList
import com.esteban.ruano.lifecommander.models.UserSettings
import com.esteban.ruano.lifecommander.services.timers.TimerService
import com.esteban.ruano.timers_domain.repository.TimersRepository
import kotlinx.coroutines.flow.first

class TimersRepositoryImpl(
    private val timerService: TimerService,
    private val preferences: Preferences
) : TimersRepository {

    private suspend fun getToken(): String {
        return preferences.loadAuthToken().first()
    }

    override suspend fun getTimerLists(): Result<List<TimerList>> {
        return try {
            val token = getToken()
            val lists = timerService.getTimerLists(token)
            Result.success(lists)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTimerList(listId: String): Result<TimerList> {
        return try {
            val token = getToken()
            val list = timerService.getTimerList(token, listId)
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createTimerList(
        name: String,
        loopTimers: Boolean,
        pomodoroGrouped: Boolean
    ): Result<TimerList> {
        return try {
            val token = getToken()
            val list = timerService.createTimerList(token, name, loopTimers, pomodoroGrouped)
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTimerList(
        listId: String,
        name: String,
        loopTimers: Boolean,
        pomodoroGrouped: Boolean
    ): Result<TimerList> {
        return try {
            val token = getToken()
            val list = timerService.updateTimerList(token, listId, name, loopTimers, pomodoroGrouped)
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteTimerList(listId: String): Result<Unit> {
        return try {
            val token = getToken()
            timerService.deleteTimerList(token, listId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createTimer(
        listId: String,
        name: String,
        duration: Long,
        enabled: Boolean,
        countsAsPomodoro: Boolean,
        sendNotificationOnComplete: Boolean,
        order: Int
    ): Result<TimerList> {
        return try {
            val token = getToken()
            val list = timerService.createTimer(
                token, listId, name, duration, enabled,
                countsAsPomodoro, sendNotificationOnComplete, order
            )
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTimer(
        timerId: String,
        name: String?,
        timerListId: String?,
        duration: Long?,
        enabled: Boolean?,
        countsAsPomodoro: Boolean?,
        sendNotificationOnComplete: Boolean?,
        order: Int?
    ): Result<TimerList> {
        return try {
            val token = getToken()
            val list = timerService.updateTimer(
                token, timerId, timerListId, name, duration, enabled,
                countsAsPomodoro, sendNotificationOnComplete, order
            )
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteTimer(timerId: String): Result<Unit> {
        return try {
            val token = getToken()
            timerService.deleteTimer(token, timerId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserSettings(): Result<UserSettings> {
        return try {
            val token = getToken()
            val settings = timerService.getUserSettings(token)
            Result.success(settings)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserSettings(
        defaultTimerListId: String?,
        dailyPomodoroGoal: Int,
        notificationsEnabled: Boolean
    ): Result<UserSettings> {
        return try {
            val token = getToken()
            val settings = timerService.updateUserSettings(
                token, defaultTimerListId, dailyPomodoroGoal, notificationsEnabled
            )
            Result.success(settings)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun startTimer(listId: String, timerId: String?): Result<Unit> {
        return try {
            val token = getToken()
            timerService.startTimer(token, listId, timerId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun pauseTimer(listId: String, timerId: String?): Result<Unit> {
        return try {
            val token = getToken()
            timerService.pauseTimer(token, listId, timerId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resumeTimer(listId: String): Result<Unit> {
        return try {
            val token = getToken()
            timerService.resumeTimer(token, listId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun stopTimer(listId: String, timerId: String?): Result<Unit> {
        return try {
            val token = getToken()
            timerService.stopTimer(token, listId, timerId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun restartTimer(listId: String, timerId: String?): Result<Unit> {
        return try {
            val token = getToken()
            timerService.restartTimer(token, listId, timerId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

