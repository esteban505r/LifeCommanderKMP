package com.esteban.ruano.lifecommander.services.timers

import com.esteban.ruano.lifecommander.models.*
import com.esteban.ruano.lifecommander.models.timers.CreateTimerListRequest
import com.esteban.ruano.lifecommander.models.timers.CreateTimerRequest
import com.esteban.ruano.lifecommander.models.timers.UpdateTimerListRequest
import com.esteban.ruano.lifecommander.models.timers.UpdateTimerRequest
import com.esteban.ruano.lifecommander.models.timers.UpdateUserSettingsRequest
import com.esteban.ruano.lifecommander.utils.TIMER_ENDPOINT
import com.esteban.ruano.lifecommander.utils.appHeaders
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class TimerService(
    private val client: HttpClient
) : TimerRepository {

    override suspend fun getTimerLists(token: String): List<TimerList> {
        return withContext(Dispatchers.IO) {
            try {
                client.get("$TIMER_ENDPOINT/lists") {
                    appHeaders(token)
                }.body<List<TimerList>>()
            } catch (e: Exception) {
                throw TimerServiceException("Failed to fetch timer lists: ${e.message}", e)
            }
        }
    }

    override suspend fun getTimerList(
        token: String,
        listId: String
    ): TimerList {
        return withContext(Dispatchers.IO) {
            try {
                client.get("$TIMER_ENDPOINT/lists/$listId") {
                    appHeaders(token)
                }.body()
            } catch (e: Exception) {
                throw TimerServiceException("Failed to fetch timer list: ${e.message}", e)
            }
        }
    }

    override suspend fun createTimerList(
        token: String,
        name: String,
        loopTimers: Boolean,
        pomodoroGrouped: Boolean
    ): TimerList {
        return withContext(Dispatchers.IO) {
            try {
                val response = client.post("$TIMER_ENDPOINT/lists") {
                    appHeaders(token)
                    contentType(ContentType.Application.Json)
                    setBody(
                        CreateTimerListRequest(
                            name = name,
                            loopTimers = loopTimers,
                            pomodoroGrouped = pomodoroGrouped
                        )
                    )
                }
                if (response.status != HttpStatusCode.Created) {
                    throw TimerServiceException("Failed to create timer list: ${response.status}")
                }
                response.body()
            } catch (e: Exception) {
                throw TimerServiceException("Failed to create timer list: ${e.message}", e)
            }
        }
    }

    override suspend fun updateTimerList(
        token: String,
        listId: String,
        name: String,
        loopTimers: Boolean,
        pomodoroGrouped: Boolean
    ): TimerList {
        return withContext(Dispatchers.IO) {
            try {
                val response = client.patch("$TIMER_ENDPOINT/lists/$listId") {
                    appHeaders(token)
                    contentType(ContentType.Application.Json)
                    setBody(
                        UpdateTimerListRequest(
                            name = name,
                            loopTimers = loopTimers,
                            pomodoroGrouped = pomodoroGrouped
                        )
                    )
                }
                if (response.status != HttpStatusCode.OK) {
                    throw TimerServiceException("Failed to update timer list: ${response.status}")
                }
                response.body()
            } catch (e: Exception) {
                throw TimerServiceException("Failed to update timer list: ${e.message}", e)
            }
        }
    }

    override suspend fun deleteTimerList(token: String, listId: String) {
        withContext(Dispatchers.IO) {
            try {
                val response = client.delete("$TIMER_ENDPOINT/lists/$listId") {
                    appHeaders(token)
                }
                if (response.status != HttpStatusCode.OK) {
                    throw TimerServiceException("Failed to delete timer list: ${response.status}")
                }
            } catch (e: Exception) {
                throw TimerServiceException("Failed to delete timer list: ${e.message}", e)
            }
        }
    }

    override suspend fun createTimer(
        token: String,
        listId: String,
        name: String,
        duration: Long,
        enabled: Boolean,
        countsAsPomodoro: Boolean,
        sendNotificationOnComplete: Boolean,
        order: Int
    ): TimerList {
        return withContext(Dispatchers.IO) {
            try {
                val response = client.post(TIMER_ENDPOINT) {
                    appHeaders(token)
                    contentType(ContentType.Application.Json)
                    setBody(
                        CreateTimerRequest(
                            timerListId = listId,
                            name = name,
                            duration = duration,
                            enabled = enabled,
                            countsAsPomodoro = countsAsPomodoro,
                            sendNotificationOnComplete = sendNotificationOnComplete,
                            order = order
                        )
                    )
                }
                if (response.status != HttpStatusCode.Created) {
                    throw TimerServiceException("Failed to create timer: ${response.status}")
                }
                response.body()
            } catch (e: Exception) {
                throw TimerServiceException("Failed to create timer: ${e.message}", e)
            }
        }
    }

    override suspend fun updateTimer(
        token: String?,
        timerId: String?,
        timerlistId: String?,
        name: String?,
        duration: Long?,
        enabled: Boolean?,
        countsAsPomodoro: Boolean?,
        sendNotificationOnComplete: Boolean?,
        order: Int?
    ): TimerList {
        return withContext(Dispatchers.IO) {
            try {
                val response = client.patch("$TIMER_ENDPOINT/$timerId") {
                    appHeaders(token)
                    contentType(ContentType.Application.Json)
                    setBody(
                        UpdateTimerRequest(
                            name = name,
                            duration = duration,
                            enabled = enabled,
                            countsAsPomodoro = countsAsPomodoro,
                            sendNotificationOnComplete = sendNotificationOnComplete,
                            order = order,
                            timerListId = timerId
                        )
                    )
                }
                if (response.status != HttpStatusCode.OK) {
                    throw TimerServiceException("Failed to update timer: ${response.status}")
                }
                response.body()
            } catch (e: Exception) {
                throw TimerServiceException("Failed to update timer: ${e.message}", e)
            }
        }
    }

    override suspend fun deleteTimer(token: String, timerId: String) {
        withContext(Dispatchers.IO) {
            try {
                val response = client.delete("$TIMER_ENDPOINT/$timerId") {
                    appHeaders(token)
                }
                if (response.status != HttpStatusCode.OK) {
                    throw TimerServiceException("Failed to delete timer: ${response.status}")
                }
            } catch (e: Exception) {
                throw TimerServiceException("Failed to delete timer: ${e.message}", e)
            }
        }
    }

    override suspend fun getUserSettings(token: String): UserSettings {
        return withContext(Dispatchers.IO) {
            try {
                client.get("$TIMER_ENDPOINT/settings") {
                    appHeaders(token)
                }.body()
            } catch (e: Exception) {
                throw TimerServiceException("Failed to fetch user settings: ${e.message}", e)
            }
        }
    }

    override suspend fun updateUserSettings(
        token: String,
        defaultTimerListId: String?,
        dailyPomodoroGoal: Int,
        notificationsEnabled: Boolean
    ): UserSettings {
        return withContext(Dispatchers.IO) {
            try {
                val response = client.patch("$TIMER_ENDPOINT/settings") {
                    appHeaders(token)
                    contentType(ContentType.Application.Json)
                    setBody(
                        UpdateUserSettingsRequest(
                            defaultTimerListId = defaultTimerListId,
                            dailyPomodoroGoal = dailyPomodoroGoal,
                            notificationsEnabled = notificationsEnabled
                        )
                    )
                }
                if (response.status != HttpStatusCode.OK) {
                    throw TimerServiceException("Failed to update user settings: ${response.status}")
                }
                response.body()
            } catch (e: Exception) {
                throw TimerServiceException("Failed to update user settings: ${e.message}", e)
            }
        }
    }
    
    // Timer control actions
    suspend fun startTimer(token: String, listId: String, timerId: String? = null): List<Timer> {
        return withContext(Dispatchers.IO) {
            try {
                val url = if (timerId != null) {
                    "$TIMER_ENDPOINT/control/$listId/start?timerId=$timerId"
                } else {
                    "$TIMER_ENDPOINT/control/$listId/start"
                }
                val response = client.post(url) {
                    appHeaders(token)
                }
                if (response.status != HttpStatusCode.OK) {
                    throw TimerServiceException("Failed to start timer: ${response.status}")
                }
                response.body()
            } catch (e: Exception) {
                throw TimerServiceException("Failed to start timer: ${e.message}", e)
            }
        }
    }
    
    suspend fun pauseTimer(token: String, listId: String, timerId: String? = null): List<Timer> {
        return withContext(Dispatchers.IO) {
            try {
                val url = if (timerId != null) {
                    "$TIMER_ENDPOINT/control/$listId/pause?timerId=$timerId"
                } else {
                    "$TIMER_ENDPOINT/control/$listId/pause"
                }
                val response = client.post(url) {
                    appHeaders(token)
                }
                if (response.status != HttpStatusCode.OK) {
                    throw TimerServiceException("Failed to pause timer: ${response.status}")
                }
                response.body()
            } catch (e: Exception) {
                throw TimerServiceException("Failed to pause timer: ${e.message}", e)
            }
        }
    }
    
    suspend fun resumeTimer(token: String, listId: String): List<Timer> {
        return withContext(Dispatchers.IO) {
            try {
                val response = client.post("$TIMER_ENDPOINT/control/$listId/resume") {
                    appHeaders(token)
                }
                if (response.status != HttpStatusCode.OK) {
                    throw TimerServiceException("Failed to resume timer: ${response.status}")
                }
                response.body()
            } catch (e: Exception) {
                throw TimerServiceException("Failed to resume timer: ${e.message}", e)
            }
        }
    }
    
    suspend fun stopTimer(token: String, listId: String, timerId: String? = null): List<Timer> {
        return withContext(Dispatchers.IO) {
            try {
                val url = if (timerId != null) {
                    "$TIMER_ENDPOINT/control/$listId/stop?timerId=$timerId"
                } else {
                    "$TIMER_ENDPOINT/control/$listId/stop"
                }
                val response = client.post(url) {
                    appHeaders(token)
                }
                if (response.status != HttpStatusCode.OK) {
                    throw TimerServiceException("Failed to stop timer: ${response.status}")
                }
                response.body()
            } catch (e: Exception) {
                throw TimerServiceException("Failed to stop timer: ${e.message}", e)
            }
        }
    }
    
    suspend fun restartTimer(token: String, listId: String, timerId: String? = null): List<Timer> {
        return withContext(Dispatchers.IO) {
            try {
                val url = if (timerId != null) {
                    "$TIMER_ENDPOINT/control/$listId/restart?timerId=$timerId"
                } else {
                    "$TIMER_ENDPOINT/control/$listId/restart"
                }
                val response = client.post(url) {
                    appHeaders(token)
                }
                if (response.status != HttpStatusCode.OK) {
                    throw TimerServiceException("Failed to restart timer: ${response.status}")
                }
                response.body()
            } catch (e: Exception) {
                throw TimerServiceException("Failed to restart timer: ${e.message}", e)
            }
        }
    }
}

class TimerServiceException(message: String, cause: Throwable? = null) : Exception(message, cause) 