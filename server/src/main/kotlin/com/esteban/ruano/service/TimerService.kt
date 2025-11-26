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
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.isNotNull
import org.jetbrains.exposed.v1.core.neq
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.UUID
import org.slf4j.LoggerFactory
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import com.esteban.ruano.service.TimerTimeCalculator

class TimerService : BaseService() {
    private val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun createTimerList(userId: Int, name: String, loopTimers: Boolean, pomodoroGrouped: Boolean): DomainTimerList? {
        val timerList = transaction {
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
        
        // Broadcast WebSocket update
        timerList?.let {
            CoroutineScope(Dispatchers.Default).launch {
                TimerNotifier.broadcastUpdate(
                    TimerWebSocketServerMessage.TimerListUpdate(
                        timerList = it,
                        action = "created"
                    ),
                    userId
                )
            }
        }
        
        return timerList
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

    suspend fun updateTimerList(listId: UUID, name: String, loopTimers: Boolean, pomodoroGrouped: Boolean): DomainTimerList? {
        val timerList = transaction {
            TimerList.findById(listId)?.apply {
                this.name = name
                this.loopTimers = loopTimers
                this.pomodoroGrouped = pomodoroGrouped
            }?.toDomainModel()
        }
        
        // Broadcast WebSocket update
        timerList?.let {
            val userId = transaction {
                TimerList.findById(listId)?.userId?.id?.value
            }
            userId?.let { uid ->
                CoroutineScope(Dispatchers.Default).launch {
                    TimerNotifier.broadcastUpdate(
                        TimerWebSocketServerMessage.TimerListUpdate(
                            timerList = it,
                            action = "updated"
                        ),
                        uid
                    )
                }
            }
        }
        
        return timerList
    }

    suspend fun deleteTimerList(listId: UUID): Boolean {
        val userId = transaction {
            TimerList.findById(listId)?.userId?.id?.value
        }
        
        val deleted = transaction {
            try {
                TimerList.findById(listId)?.delete()
                true
            } catch (e: Exception) {
                false
            }
        }
        
        // Broadcast WebSocket update
        if (deleted && userId != null) {
            CoroutineScope(Dispatchers.Default).launch {
                TimerNotifier.broadcastUpdate(
                    TimerWebSocketServerMessage.TimerListRefresh(
                        listId = null // Refresh all lists when one is deleted
                    ),
                    userId
                )
            }
        }
        
        return deleted
    }

    @OptIn(ExperimentalTime::class)
    suspend fun createTimer(
        listId: UUID,
        name: String,
        duration: Long,
        enabled: Boolean,
        countsAsPomodoro: Boolean,
        sendNotificationOnComplete: Boolean = true,
        order: Int? = null
    ): DomainTimer? {
        val timer = transaction {
            try {
                val defaultOrder = order ?: (Timer.find { Timers.listId eq listId }
                    .maxOfOrNull { it.order }?.plus(1) ?: 0)
                val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)

                Timer.new {
                    this.name = name
                    this.duration = duration
                    this.enabled = enabled
                    this.countsAsPomodoro = countsAsPomodoro
                    this.sendNotificationOnComplete = sendNotificationOnComplete
                    this.list = TimerList[listId]
                    this.order = defaultOrder
                    this.accumulatedPausedMs = 0
                    this.updatedAt = now
                }.toDomainModel()
            } catch (e: Exception) {
                null
            }
        }
        
        // Broadcast WebSocket update
        timer?.let {
            val userId = transaction {
                TimerList.findById(listId)?.userId?.id?.value
            }
            userId?.let { uid ->
                CoroutineScope(Dispatchers.Default).launch {
                    TimerNotifier.broadcastUpdate(
                        TimerWebSocketServerMessage.TimerUpdate(
                            timer = it,
                            listId = listId.toString(),
                            remainingTime = it.duration / 1000 // Convert ms to seconds
                        ),
                        uid
                    )
                }
            }
        }
        
        return timer
    }



    @OptIn(ExperimentalTime::class)
    suspend fun updateTimer(
        userId: Int,
        timerId: UUID,
        name: String?,
        duration: Long?,
        enabled: Boolean?,
        countsAsPomodoro: Boolean?,
        sendNotificationOnComplete: Boolean?,
        order: Int?
    ): DomainTimer? {
        val now = Clock.System.now()
        val timer = transaction {
            val lists = TimerList.find {
                (TimerLists.userId eq userId)
            }
            Timer.find {
                (Timers.id eq timerId) and
                        (Timers.listId inList lists.map { it.id })
            }.firstOrNull()?.let { timerEntity ->
                timerEntity.apply {
                    name?.let { this.name = it }
                    duration?.let { this.duration = it }
                    enabled?.let { this.enabled = it }
                    countsAsPomodoro?.let { this.countsAsPomodoro = it }
                    sendNotificationOnComplete?.let { this.sendNotificationOnComplete = it }
                    order?.let { this.order = it }
                    this.updatedAt = now.toLocalDateTime(TimeZone.UTC)
                }
            }?.toDomainModel()
        }
        
        // Broadcast WebSocket update
        timer?.let {
            val listId = transaction {
                Timer.findById(timerId)?.list?.id?.value
            }
            listId?.let { lid ->
                CoroutineScope(Dispatchers.Default).launch {
                    val remainingSeconds = TimerTimeCalculator.calculateRemainingSeconds(
                        Timer.findById(timerId)!!,
                        now
                    )
                    TimerNotifier.broadcastUpdate(
                        TimerWebSocketServerMessage.TimerUpdate(
                            timer = it,
                            listId = lid.toString(),
                            remainingTime = remainingSeconds
                        ),
                        userId
                    )
                }
            }
        }
        
        return timer
    }

    suspend fun deleteTimer(timerId: UUID): Boolean {
        val (userId, listId) = transaction {
            val timer = Timer.findById(timerId)
            val uid = timer?.list?.userId?.id?.value
            val lid = timer?.list?.id?.value
            timer?.delete()
            uid to lid
        }
        
        val deleted = transaction {
            try {
                Timer.findById(timerId) == null // Check if deleted
            } catch (e: Exception) {
                false
            }
        }
        
        // Broadcast WebSocket update to refresh the list
        if (deleted && userId != null && listId != null) {
            CoroutineScope(Dispatchers.Default).launch {
                TimerNotifier.broadcastUpdate(
                    TimerWebSocketServerMessage.TimerListRefresh(
                        listId = listId.toString()
                    ),
                    userId
                )
            }
        }
        
        return deleted
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
    ): List<DomainTimer> {
        val now = Clock.System.now()
        val nowLocal = now.toLocalDateTime(TimeZone.UTC)

        val affectedTimers = transaction {
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
                    logger.warn("[startTimer] Invalid timerId or does not belong to the list")
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
                // Reset timer state for fresh start
                timerToStart.startTime = nowLocal
                timerToStart.pauseTime = null
                timerToStart.accumulatedPausedMs = 0
            timerToStart.state = TimerState.RUNNING
                timerToStart.updatedAt = nowLocal
                logger.info("[startTimer] Starting timer: ${timerToStart.name}")
            listOf(timerToStart.toDomainModel())
        } else {
                logger.warn("[startTimer] No timer found to start")
            emptyList()
        }
        }
        
        // Broadcast WebSocket update for each affected timer
        affectedTimers.forEach { timer ->
            CoroutineScope(Dispatchers.Default).launch {
                val remainingSeconds = TimerTimeCalculator.calculateRemainingSeconds(
                    Timer.findById(UUID.fromString(timer.id))!!,
                    now
                )
                TimerNotifier.broadcastUpdate(
                    TimerWebSocketServerMessage.TimerUpdate(
                        timer = timer,
                        listId = listId.toString(),
                        remainingTime = remainingSeconds
                    ),
                    userId
                )
            }
        }
        
        return affectedTimers
    }


    @OptIn(ExperimentalTime::class)
    suspend fun pauseTimer(userId: Int, listId: UUID, timerId: UUID? = null): List<DomainTimer> {
        val now = Clock.System.now()
        val nowLocal = now.toLocalDateTime(TimeZone.UTC)
        
        val affectedTimers = transaction {
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
                // When pausing, we just record the pause time
                // The accumulated pause time will be calculated when resuming
            timer.state = TimerState.PAUSED
                timer.pauseTime = nowLocal
                timer.updatedAt = nowLocal
        }

        timersToPause.map { it.toDomainModel() }
    }

        // Broadcast WebSocket update for each affected timer
        affectedTimers.forEach { timer ->
            CoroutineScope(Dispatchers.Default).launch {
                val remainingSeconds = TimerTimeCalculator.calculateRemainingSeconds(
                    Timer.findById(UUID.fromString(timer.id))!!,
                    now
                )
                TimerNotifier.broadcastUpdate(
                    TimerWebSocketServerMessage.TimerUpdate(
                        timer = timer,
                        listId = listId.toString(),
                        remainingTime = remainingSeconds
                    ),
                    userId
                )
            }
        }
        
        return affectedTimers
    }

    @OptIn(ExperimentalTime::class)
    suspend fun stopTimer(userId: Int, listId: UUID, timerId: UUID? = null): List<DomainTimer> {
        val now = Clock.System.now()
        val nowLocal = now.toLocalDateTime(TimeZone.UTC)
        
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

            timers.forEach { timer ->
                // When stopping, we don't need to update accumulated pause time
                // The timer state will be STOPPED and time calculations will use the last known state
                timer.state = TimerState.STOPPED
                timer.updatedAt = nowLocal
            }
            
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
                    title = "Timer Stopped",
                    body = "Timer '${timer.name}' has been stopped."
                )
            }
        }

        return affectedTimers
    }


    @OptIn(ExperimentalTime::class)
    suspend fun resumeTimer(userId: Int, listId: UUID): List<DomainTimer> {
        val now = Clock.System.now()
        val nowLocal = now.toLocalDateTime(TimeZone.UTC)
        
        val affectedTimers = transaction {
        val list = TimerList.findById(listId) ?: return@transaction emptyList()
        if (list.userId.id.value != userId) return@transaction emptyList()

        val pausedTimer = Timer.find {
            (Timers.listId eq listId) and (Timers.state eq TimerState.PAUSED)
        }
            .orderBy(Timers.order to SortOrder.ASC)
            .limit(1)
            .firstOrNull()

        pausedTimer?.let {
                // When resuming, calculate how long we were paused and add to accumulated
                // The pause duration is the time from pauseTime to now
                if (it.pauseTime != null) {
                    val pauseInstant = it.pauseTime!!.toInstant(TimeZone.UTC)
                    val pauseDuration = (now - pauseInstant).inWholeMilliseconds
                    // Add this pause duration to the accumulated paused time
                    it.accumulatedPausedMs += pauseDuration
                }
                
                // Resume: keep original startTime, just update state and clear pauseTime
            it.state = TimerState.RUNNING
                it.pauseTime = null // Clear pause time
                it.updatedAt = nowLocal
            listOf(it.toDomainModel())
        } ?: emptyList()
        }
        
        // Broadcast WebSocket update for each affected timer
        affectedTimers.forEach { timer ->
            CoroutineScope(Dispatchers.Default).launch {
                val remainingSeconds = TimerTimeCalculator.calculateRemainingSeconds(
                    Timer.findById(UUID.fromString(timer.id))!!,
                    now
                )
                TimerNotifier.broadcastUpdate(
                    TimerWebSocketServerMessage.TimerUpdate(
                        timer = timer,
                        listId = listId.toString(),
                        remainingTime = remainingSeconds
                    ),
                    userId
                )
            }
        }
        
        return affectedTimers
    }

    @OptIn(ExperimentalTime::class)
    suspend fun restartTimer(userId: Int, listId: UUID, timerId: UUID? = null): List<DomainTimer> {
        val now = Clock.System.now()
        val nowLocal = now.toLocalDateTime(TimeZone.UTC)
        
        val affectedTimers = transaction {
        val list = TimerList.findById(listId) ?: return@transaction emptyList()
        if (list.userId.id.value != userId) return@transaction emptyList()

        val timers = if (timerId != null) {
            listOfNotNull(Timer.findById(timerId)).filter { it.list.id.value == listId }
        } else {
            Timer.find { Timers.listId eq listId }.toList()
        }

        timers.forEach { timer ->
                // Reset all timer state for fresh restart
                timer.startTime = nowLocal
            timer.state = TimerState.RUNNING
            timer.pauseTime = null
                timer.accumulatedPausedMs = 0
                timer.updatedAt = nowLocal
        }

        timers.map { it.toDomainModel() }
        }
        
        // Broadcast WebSocket update for each affected timer
        affectedTimers.forEach { timer ->
            CoroutineScope(Dispatchers.Default).launch {
                val remainingSeconds = TimerTimeCalculator.calculateRemainingSeconds(
                    Timer.findById(UUID.fromString(timer.id))!!,
                    now
                )
                TimerNotifier.broadcastUpdate(
                    TimerWebSocketServerMessage.TimerUpdate(
                        timer = timer,
                        listId = listId.toString(),
                        remainingTime = remainingSeconds
                    ),
                    userId
                )
            }
        }
        
        return affectedTimers
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
            val nowServer = Clock.System.now()
            
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
                    // Use the new time calculation logic
                    TimerTimeCalculator.shouldComplete(timer, nowServer)
                }
                .map { timer ->
                    timer.state = TimerState.COMPLETED
                    timer.updatedAt = currentTime
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