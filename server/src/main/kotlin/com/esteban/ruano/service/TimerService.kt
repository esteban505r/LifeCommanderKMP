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
import com.esteban.ruano.lifecommander.models.timers.CompletedTimerInfo
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import com.esteban.ruano.lifecommander.models.Timer as DomainTimer
import com.esteban.ruano.lifecommander.models.TimerList as DomainTimerList
import com.esteban.ruano.lifecommander.models.UserSettings as DomainUserSettings
import com.esteban.ruano.lifecommander.models.timers.TimerState
import com.esteban.ruano.lifecommander.timer.TimerWebSocketServerMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.UUID
import org.slf4j.LoggerFactory
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

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
        duration: Long,
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
        userId: Int,
        timerId: UUID,
        name: String?,
        duration: Long?,
        enabled: Boolean?,
        countsAsPomodoro: Boolean?,
        order: Int?
    ): DomainTimer? {
        return transaction {
            val lists = TimerList.find {
                (TimerLists.userId eq userId)
            }
            Timer.find {
                (Timers.id eq timerId) and
                        (Timers.listId inList lists.map { it.id })
            }.firstOrNull()?.let { timer ->
                timer.apply {
                    name?.let { this.name = it }
                    duration?.let { this.duration = it }
                    enabled?.let { this.enabled = it }
                    countsAsPomodoro?.let { this.countsAsPomodoro = it }
                    order?.let { this.order = it }
                }
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

    suspend fun getTimersUpdate(userId: Int): Pair<String?,List<DomainTimer>> = transaction {
        val userTimerLists = TimerList.find { TimerLists.userId eq userId }.map { it.id }

        if (userTimerLists.isEmpty()) return@transaction null to emptyList()

        val timers = Timer.find {
            (Timers.listId inList userTimerLists) and
                    (Timers.state inList listOf(TimerState.RUNNING, TimerState.PAUSED))
        }
            .orderBy(Timers.startTime to SortOrder.DESC)

        timers.first().list.id.value.toString() to
                timers.map { it.toDomainModel() }
    }



    @OptIn(ExperimentalTime::class)
    suspend fun startTimer(
        userId: Int,
        listId: UUID,
        timerId: UUID? = null
    ): List<DomainTimer> = transaction {
        val now = Clock.System.now()

        val list = TimerList.findById(listId) ?: return@transaction emptyList()
        if (list.userId.id.value != userId) return@transaction emptyList()

        // Prevent starting if a timer is already running in this list
        val isAlreadyRunning = Timer.find {
            (Timers.listId eq listId) and (Timers.state eq TimerState.RUNNING)
        }.any()
        if (isAlreadyRunning) return@transaction emptyList()

        // Determine which timer to start
        val timerToStart = if (timerId != null) {
            val timer = Timer.findById(timerId)
            if (timer == null || timer.list.id.value != listId) {
                println("[startTimer] Invalid timerId or does not belong to the list")
                null
            } else {
                timer
            }
        } else {
            Timer.find {
                (Timers.listId eq listId) and
                        (Timers.enabled eq true)
            }
                .orderBy(Timers.order to SortOrder.ASC)
                .limit(1)
                .firstOrNull()
        }

        if (timerToStart != null) {
            timerToStart.startTime = now.toLocalDateTime(TimeZone.UTC)
            timerToStart.state = TimerState.RUNNING
            println("[startTimer] Starting timer: ${timerToStart.name}")
            listOf(timerToStart.toDomainModel())
        } else {
            println("[startTimer] No timer found to start")
            emptyList()
        }
    }


    @OptIn(ExperimentalTime::class)
    suspend fun pauseTimer(userId: Int, listId: UUID, timerId: UUID? = null): List<DomainTimer> = transaction {
        val list = TimerList.findById(listId) ?: return@transaction emptyList()
        if (list.userId.id.value != userId) return@transaction emptyList()

        val timersToPause = if (timerId != null) {
            listOfNotNull(Timer.findById(timerId)).filter {
                it.list.id.value == listId && it.state == TimerState.RUNNING
            }
        } else {
            Timer.find {
                (Timers.listId eq listId) and (Timers.state eq TimerState.RUNNING)
            }.toList()
        }

        timersToPause.forEach { timer ->
            timer.state = TimerState.PAUSED
            timer.pauseTime = Clock.System.now().toLocalDateTime(TimeZone.UTC)
        }

        timersToPause.map { it.toDomainModel() }
    }

    suspend fun stopTimer(userId: Int, listId: UUID, timerId: UUID? = null): List<DomainTimer> {
        val affectedTimers = transaction {
            val list = TimerList.findById(listId) ?: return@transaction emptyList()
            if (list.userId.id.value != userId) return@transaction emptyList()

            val timers = if (timerId != null) {
                listOfNotNull(Timer.findById(timerId)).filter {
                    it.list.id.value == listId && it.state in listOf(TimerState.RUNNING, TimerState.PAUSED)
                }
            } else {
                Timer.find {
                    (Timers.listId eq listId) and
                            (Timers.state inList listOf(TimerState.RUNNING, TimerState.PAUSED))
                }.toList()
            }

            timers.forEach { it.state = TimerState.STOPPED }
            timers.map { it.toDomainModel() }
        }

        affectedTimers.forEach { timer ->
            CoroutineScope(Dispatchers.Default).launch {
                TimerNotifier.broadcastUpdate(
                    TimerWebSocketServerMessage.TimerUpdate(
                        timer = timer,
                        listId = listId.toString(),
                        remainingTime = 0
                    ),
                    userId
                )
                sendPushNotificationToUser(
                    userId = userId,
                    title = "Timer Completed",
                    body = "Timer '${timer.name}' has been marked as completed."
                )
            }
        }

        return affectedTimers
    }


    @OptIn(ExperimentalTime::class)
    suspend fun resumeTimer(userId: Int, listId: UUID): List<DomainTimer> = transaction {
        val list = TimerList.findById(listId) ?: return@transaction emptyList()
        if (list.userId.id.value != userId) return@transaction emptyList()

        val pausedTimer = Timer.find {
            (Timers.listId eq listId) and (Timers.state eq TimerState.PAUSED)
        }
            .orderBy(Timers.order to SortOrder.ASC)
            .limit(1)
            .firstOrNull()

        pausedTimer?.let {
            it.state = TimerState.RUNNING
            it.startTime = Clock.System.now().toLocalDateTime(TimeZone.UTC)
            listOf(it.toDomainModel())
        } ?: emptyList()
    }

    @OptIn(ExperimentalTime::class)
    suspend fun restartTimer(userId: Int, listId: UUID, timerId: UUID? = null): List<DomainTimer> = transaction {
        val list = TimerList.findById(listId) ?: return@transaction emptyList()
        if (list.userId.id.value != userId) return@transaction emptyList()

        val timers = if (timerId != null) {
            listOfNotNull(Timer.findById(timerId)).filter { it.list.id.value == listId }
        } else {
            Timer.find { Timers.listId eq listId }.toList()
        }

        val now = Clock.System.now()
        timers.forEach { timer ->
            timer.startTime = now.toLocalDateTime(TimeZone.UTC)
            timer.state = TimerState.RUNNING
            timer.pauseTime = null
        }

        timers.map { it.toDomainModel() }
    }


    suspend fun getNextTimerToStart(listId: UUID, completedTimer: DomainTimer): Timer? {
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

    @OptIn(ExperimentalTime::class)
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

    fun getDeviceTokensForUser(userId: Int): List<String> {
        return transaction {
            DeviceToken.find { DeviceTokens.user eq userId }
                .map { it.token }
                .toList()
        }
    }

    @OptIn(ExperimentalTime::class)
    fun checkCompletedTimers(
        timezone: TimeZone,
        currentTime: kotlinx.datetime.LocalDateTime,
        userId: Int? = null
    ): List<CompletedTimerInfo> {
        return transaction {
            val query = if (userId != null) {
                Timer.find {
                    (Timers.state eq TimerState.RUNNING) and
                            (Timers.startTime.isNotNull()) and
                            (Timers.listId inList TimerList.find { TimerLists.userId eq userId }.map { it.id })
                }
            } else {
                Timer.find {
                    (Timers.state eq TimerState.RUNNING) and
                            (Timers.startTime.isNotNull())
                }
            }
            
            query.limit(1000)
                .filter { timer ->
                    val endTime = timer.startTime!!
                        .toInstant(timezone)
                        .plus(timer.duration.seconds)
                        .toLocalDateTime(timezone)
                    endTime <= currentTime
                }
                .map { timer ->
                    timer.state = TimerState.COMPLETED
                    CompletedTimerInfo(
                        domainTimer = timer.toDomainModel(),
                        listId = timer.list.id.value.toString(),
                        userId = timer.list.userId.id.value,
                        name = timer.name
                    )
                }
        }
    }
} 