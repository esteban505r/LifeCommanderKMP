package com.esteban.ruano.service

import com.esteban.ruano.database.converters.toDomainModel
import com.esteban.ruano.database.entities.DeviceToken
import com.esteban.ruano.database.entities.DeviceTokens
import com.esteban.ruano.database.entities.Timer
import com.esteban.ruano.database.entities.TimerList
import com.esteban.ruano.database.entities.UserSetting
import com.esteban.ruano.database.entities.User
import com.esteban.ruano.database.entities.TimerLists
import com.esteban.ruano.database.entities.Timers
import com.esteban.ruano.database.entities.UserSettings
import org.jetbrains.exposed.dao.id.EntityID
import com.esteban.ruano.lifecommander.models.Timer as DomainTimer
import com.esteban.ruano.lifecommander.models.TimerList as DomainTimerList
import com.esteban.ruano.lifecommander.models.UserSettings as DomainUserSettings
import com.esteban.ruano.lifecommander.models.timers.TimerState
import com.esteban.ruano.lifecommander.timer.TimerWebSocketServerMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import org.slf4j.LoggerFactory

class TimerService : BaseService() {
    private val logger = LoggerFactory.getLogger(this::class.java)

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
            val listEntities = TimerList.find { TimerLists.userId eq userId }.toList()

            val timersByListId: Map<EntityID<UUID>, List<Timer>> =
                Timer.find { Timers.listId inList listEntities.map { it.id } }
                    .orderBy(Timers.order to SortOrder.ASC)
                    .groupBy { it.list.id }

            listEntities.map { listEntity ->
                val timersForList = timersByListId[listEntity.id].orEmpty()
                listEntity.toDomainModel().copy(
                    timers = timersForList.map { it.toDomainModel() }
                )
            }
        }
    }

    fun getTimerList(listId: UUID): DomainTimerList? {
        return transaction {
            val listEntity = TimerList.findById(listId)
            val timersForList = Timer.find { Timers.listId eq listEntity!!.id }
                .orderBy(Timers.order to SortOrder.ASC)
                .toList()
                .map { it.toDomainModel() }
            listEntity?.toDomainModel()?.copy(timers = timersForList)
                ?: throw IllegalArgumentException("Timer list not found")
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
        order: Int? = null
    ): DomainTimer? {
        return transaction {
            try {
                val defaultOrder = order ?: (Timer.find { Timers.listId eq listId }
                    .maxOfOrNull { it.order }?.plus(1) ?: 0)

                Timer.new {
                    this.name = name
                    this.duration = duration
                    this.enabled = enabled
                    this.countsAsPomodoro = countsAsPomodoro
                    this.list = TimerList[listId]
                    this.order = defaultOrder
                }.toDomainModel()
            } catch (e: Exception) {
                null
            }
        }
    }

    fun updateTimer(
        timerId: UUID,
        name: String?,
        duration: Int?,
        enabled: Boolean?,
        countsAsPomodoro: Boolean?,
        order: Int?
    ): DomainTimer? {
        return transaction {
            Timer.findById(timerId)?.apply {
                name?.let { this.name = it }
                duration?.let { this.duration = it }
                enabled?.let { this.enabled = it }
                countsAsPomodoro?.let { this.countsAsPomodoro = it }
                order?.let { this.order = it }
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

    fun reorderTimers(listId: UUID, timerOrders: List<Pair<UUID, Int>>): DomainTimerList? {
        return transaction {
            try {
                // Validate all timers belong to the list
                val timerIds = timerOrders.map { it.first }
                val timers = Timer.find { (Timers.id inList timerIds) and (Timers.listId eq listId) }
                if (timers.count() != timerIds.size.toLong()) {
                    throw IllegalArgumentException("Some timers do not belong to the specified list")
                }

                // Update orders
                timerOrders.forEach { (timerId, newOrder) ->
                    Timer.findById(timerId)?.apply {
                        this.order = newOrder
                    }
                }

                // Return updated list
                getTimerList(listId)
            } catch (e: Exception) {
                null
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

    suspend fun startTimer(listId: UUID, timerId: UUID? = null): List<DomainTimer> {
        return transaction {
            val now = Clock.System.now()
            val timerToStart: Timer? = when {
                timerId != null -> Timer.findById(timerId)
                else -> Timer.find {
                    (Timers.listId eq listId) and
                            (Timers.state neq TimerState.RUNNING) and
                            (Timers.state neq TimerState.COMPLETED) and
                            (Timers.enabled eq true)
                }
                    .orderBy(Timers.order to SortOrder.ASC)
                    .limit(1)
                    .firstOrNull()
            }

            if (timerToStart != null) {
                timerToStart.startTime = now.toLocalDateTime(TimeZone.UTC)
                timerToStart.state = TimerState.RUNNING
                listOf(timerToStart.toDomainModel())
            } else {
                emptyList()
            }
        }
    }


    suspend fun getNextTimerToStart(listId: UUID, completedTimer: Timer): Timer? {
        return transaction {
            Timer.find {
                (Timers.listId eq listId) and
                        (Timers.order greater completedTimer.order) and
                        (Timers.state neq TimerState.COMPLETED) and
                        (Timers.state neq TimerState.RUNNING) and
                        (Timers.enabled eq true)
            }
                .orderBy(Timers.order to SortOrder.ASC)
                .limit(1)
                .firstOrNull()
        }
    }


    suspend fun pauseTimer(listId: UUID, timerId: UUID? = null): List<DomainTimer> {
        return transaction {
            val timers = if (timerId != null) {
                listOfNotNull(Timer.findById(timerId))
            } else {
                Timer.find { Timers.listId eq listId }.toList()
            }

            timers.forEach { timer ->
                if (timer.state == TimerState.RUNNING) {
                    timer.state = TimerState.PAUSED
                    timer.pauseTime = Clock.System.now().toLocalDateTime(timeZone = TimeZone.UTC)
                }
            }

            timers.map { it.toDomainModel() }
        }
    }

    suspend fun stopTimer(listId: UUID, timerId: UUID? = null): List<DomainTimer> {
        return transaction {
            val timers = if (timerId != null) {
                listOfNotNull(Timer.findById(timerId))
            } else {
                Timer.find { Timers.listId eq listId }.toList()
            }

            timers.forEach { timer ->
                timer.state = TimerState.COMPLETED

                CoroutineScope(Dispatchers.Default).launch {
                    TimerNotifier.broadcastUpdate(TimerWebSocketServerMessage.TimerListCompleted(
                        listId = timer.list.id.toString(),
                    ), timer.list.userId.id.value)
                    sendPushNotificationToUser(
                        userId = timer.list.userId.id.value,
                        title = "Timer Completed",
                        body = "Timer '${timer.name}' has completed"
                    )
                }


            }

            timers.map { it.toDomainModel() }
        }
    }

    suspend fun restartTimer(listId: UUID, timerId: UUID? = null): List<DomainTimer> {
        return transaction {
            val timers = if (timerId != null) {
                listOfNotNull(Timer.findById(timerId))
            } else {
                Timer.find { Timers.listId eq listId }.toList()
            }

            val now = Clock.System.now()
            timers.forEach { timer ->
                timer.startTime = now.toLocalDateTime(timeZone = TimeZone.UTC)
                timer.state = TimerState.RUNNING
                timer.pauseTime = null
            }

            timers.map { it.toDomainModel() }
        }
    }

    suspend fun registerDeviceToken(userId: Int, token: String, platform: String): Boolean {
        return transaction {
            try {
                val existingToken = DeviceToken.find {
                    (DeviceTokens.token eq token) and (DeviceTokens.user eq userId)
                }.firstOrNull()

                if (existingToken != null) {
                    existingToken.apply {
                        this.platform = platform
                        this.updatedAt = Clock.System.now().toLocalDateTime(TimeZone.UTC)
                    }
                } else {
                    DeviceToken.new {
                        this.user = User[userId]
                        this.token = token
                        this.platform = platform
                        this.updatedAt = Clock.System.now().toLocalDateTime(TimeZone.UTC)
                    }
                }
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    fun sendPushNotificationToUser(
        userId: Int,
        title: String,
        body: String,
        data: Map<String, String> = emptyMap()
    ) {
        transaction {
            val tokens = DeviceToken.find { DeviceTokens.user eq userId }
                .map { it.token }
                .toList()

            if (tokens.isNotEmpty()) {
                println("Sending push notification to user $userId with tokens: $tokens")
            }
        }
    }
} 