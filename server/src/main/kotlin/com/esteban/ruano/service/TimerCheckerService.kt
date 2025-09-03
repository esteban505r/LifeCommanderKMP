package com.esteban.ruano.service

import com.esteban.ruano.lifecommander.models.timers.CompletedTimerInfo
import com.esteban.ruano.lifecommander.timer.TimerWebSocketServerMessage
import com.esteban.ruano.database.entities.*
import com.esteban.ruano.database.models.Status
import com.esteban.ruano.lifecommander.models.UserSettings
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import io.sentry.Sentry
import kotlinx.coroutines.*
import kotlinx.datetime.*
import org.jetbrains.exposed.v1.jdbc.and
import org.jetbrains.exposed.v1.jdbc.kotlin.datetime.date
import org.jetbrains.exposed.v1.jdbc.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transactionManager
import java.util.UUID
import kotlin.math.abs
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class TimerCheckerService(
    private val timerService: TimerService,
    private val notificationService: NotificationService,
    private val reminderService: ReminderService,
    private val taskService: TaskService,
    private val habitService: HabitService,
    private val settingsService: SettingsService,
    private val logger: (String) -> Unit = { println(it) }, // plug your logger here
    private val loopInterval: Duration = 30_000.milliseconds, // configurable
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Throttle maps per user (epoch ms). You can migrate these to a persistence later.
    private val lastTaskNotificationTimes = mutableMapOf<Int, Long>()
    private val lastHabitNotificationTimes = mutableMapOf<Int, Long>()

    /** Non-blocking transaction helper */
    private suspend fun <T> db(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    /** Safe timezone parsing with fallback (and sentry breadcrumb) */
    private fun tzOf(tz: String?): TimeZone =
        try { TimeZone.of(tz ?: "UTC") } catch (e: Exception) {
            Sentry.captureException(e); TimeZone.UTC
        }

    /** Frequency gate by minutes (epochMillis-based, computed once per tick) */
    private fun canSend(lastMs: Long?, freqMinutes: Int, nowMs: Long): Boolean {
        if (freqMinutes <= 0) return true
        val last = lastMs ?: return true
        val intervalMs = freqMinutes * 60 * 1000L
        return (nowMs - last) >= intervalMs
    }

    /** Start loop. Returns cancellable Job. */
    fun start(): Job = scope.launch {
        while (isActive) {
            val tickStartInstant = Clock.System.now()
            val tickStartMs = tickStartInstant.toEpochMilliseconds()

            try {
                // Prefetch lightweight user context
                val users: List<UserContext> = db {
                    User.all().map { u ->
                        val settings = settingsService.getUserSettings(u.id.value)
                        UserContext(
                            id = u.id.value,
                            timeZone = tzOf(u.timeZone),
                            settings = settings
                        )
                    }
                }

                // Optional: special debug for user 1
                logNotificationTimingForUser1(users, tickStartInstant)

                // Run checks per user concurrently but within parent scope for cancellation
                coroutineScope {
                    // Timers can be checked per user (if enabled for your product)
                    users.forEach { user -> launch { checkTimersForUser(user, tickStartInstant) } }

                    // These don’t require per-user loops first (reminders are entity-bound)
                    launch { checkReminders(tickStartInstant) }

                    // Due tasks/habits per user
                    users.forEach { user ->
                        launch { checkDueTasksForUser(user, tickStartInstant, tickStartMs) }
                        launch { checkDueHabitsForUser(user, tickStartInstant, tickStartMs) }
                        launch { checkHabitStartsForUser(user, tickStartInstant, tickStartMs) }
                    }
                }
            } catch (e: Exception) {
                Sentry.captureException(e)
                logger("[Loop] Unhandled error: ${e.message}")
            }

            // Respect loop interval
            delay(loopInterval)
        }
    }

    fun stop() { scope.cancel() }

    // ------------------------------------------------------------
    // Logging helpers / user 1 debug (kept, but made non-blocking)
    // ------------------------------------------------------------

    private suspend fun logNotificationTimingForUser1(
        users: List<UserContext>,
        nowInstant: Instant
    ) {
        val user = users.firstOrNull { it.id == 1 } ?: return
        try {
            val nowLocal = nowInstant.toLocalDateTime(user.timeZone)
            val nowMs = nowInstant.toEpochMilliseconds()

            val taskLast = lastTaskNotificationTimes[user.id]
            val habitLast = lastHabitNotificationTimes[user.id]

            val nextTask = (taskLast ?: nowMs) + (user.settings.dueTasksNotificationFrequency * 60 * 1000L)
            val nextHabit = (habitLast ?: nowMs) + (user.settings.dueHabitsNotificationFrequency * 60 * 1000L)

            fun Long.asLdt() = Instant.fromEpochMilliseconds(this).toLocalDateTime(user.timeZone)

            val untilTask = nextTask - nowMs
            val untilHabit = nextHabit - nowMs

            val lines = buildString {
                appendLine("=".repeat(72))
                appendLine("[User1] Notification Timing Check - ${nowLocal.formatDefault()}")
                appendLine("[User1] TZ: ${user.timeZone.id}")
                appendLine("[User1] Enabled: ${user.settings.notificationsEnabled}")
                appendLine("[User1] Task freq: ${user.settings.dueTasksNotificationFrequency} min")
                appendLine("[User1] Habit freq: ${user.settings.dueHabitsNotificationFrequency} min")
                appendLine("[User1] Last task: ${taskLast?.asLdt()?.formatDefault() ?: "Never"}")
                appendLine("[User1] Last habit: ${habitLast?.asLdt()?.formatDefault() ?: "Never"}")
                appendLine("[User1] Next task: ${nextTask.asLdt().formatDefault()} (in ${untilTask / 1000 / 60}m ${(untilTask / 1000) % 60}s)")
                appendLine("[User1] Next habit: ${nextHabit.asLdt().formatDefault()} (in ${untilHabit / 1000 / 60}m ${(untilHabit / 1000) % 60}s)")
                appendLine("[User1] Can send task? ${canSend(taskLast, user.settings.dueTasksNotificationFrequency, nowMs)}")
                appendLine("[User1] Can send habit? ${canSend(habitLast, user.settings.dueHabitsNotificationFrequency, nowMs)}")
                appendLine("=".repeat(72))
            }
            logger(lines)
        } catch (e: Exception) {
            Sentry.captureException(e)
            logger("[User1] Error logging timing: ${e.message}")
        }
    }

    // ------------------------------------------------------------
    // Timers
    // ------------------------------------------------------------

    private suspend fun checkTimersForUser(user: UserContext, nowInstant: Instant) {
        try {
            val nowLocal = nowInstant.toLocalDateTime(user.timeZone)
            val completed: List<CompletedTimerInfo> =
                timerService.checkCompletedTimers(user.timeZone, nowLocal, user.id)

            if (completed.isEmpty()) return

            logger("[Timers] ${completed.size} completed for user ${user.id} @ ${nowLocal.formatDefault()} (${user.timeZone.id})")

            for (info in completed) {
                try {
                    val tokens = timerService.getDeviceTokensForUser(info.userId)
                    notificationService.sendNotificationToTokens(
                        tokens,
                        title = "Timer Completed",
                        body = "Your timer '${info.name}' has completed.",
                        data = mapOf(
                            "type" to "TIMER_COMPLETED",
                            "timerId" to info.domainTimer.id,
                            "listId" to info.listId.toString()
                        )
                    )

                    val next = timerService.getNextTimerToStart(UUID.fromString(info.listId), info.domainTimer)
                    val updated = next?.let {
                        timerService.startTimer(
                            userId = info.userId,
                            listId = UUID.fromString(info.listId),
                            timerId = it.id.value
                        ).firstOrNull()
                    }

                    TimerNotifier.broadcastUpdate(
                        TimerWebSocketServerMessage.TimerUpdate(
                            timer = updated ?: info.domainTimer,
                            listId = info.listId,
                            remainingTime = updated?.duration ?: info.domainTimer.duration
                        ),
                        info.userId
                    )
                } catch (e: Exception) {
                    Sentry.captureException(e)
                    logger("[Timers] Error per-timer ${info.domainTimer.id}: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Sentry.captureException(e)
            logger("[Timers] Error for user ${user.id}: ${e.message}")
        }
    }

    // ------------------------------------------------------------
    // Reminders
    // ------------------------------------------------------------

    private suspend fun checkReminders(nowInstant: Instant) {
        try {
            val nowMs = nowInstant.toEpochMilliseconds()
            val due = reminderService.getDueReminders(nowMs)
            if (due.isEmpty()) return

            for (r in due) {
                try {
                    // Resolve user + tz once
                    val (userId, tz) = db {
                        when {
                            r.taskId != null -> {
                                val task = Task.findById(r.taskId!!)
                                val user = task?.user
                                Pair(user?.id?.value, tzOf(user?.timeZone))
                            }
                            r.habitId != null -> {
                                val habit = Habit.findById(r.habitId!!)
                                val user = habit?.user
                                Pair(user?.id?.value, tzOf(user?.timeZone))
                            }
                            else -> Pair(null, TimeZone.UTC)
                        }
                    }
                    userId ?: continue

                    val nowLocal = nowInstant.toLocalDateTime(tz)
                    val reminderLocal = Instant.fromEpochMilliseconds(r.time).toLocalDateTime(tz)

                    // only within 30s window
                    val deltaSec = abs(
                        (nowLocal.toInstant(tz) - reminderLocal.toInstant(tz)).inWholeSeconds
                    )
                    if (deltaSec > 30) continue

                    val tokens = timerService.getDeviceTokensForUser(userId)
                    val (title, body, data) = db {
                        when {
                            r.taskId != null -> {
                                val t = Task.findById(r.taskId!!)
                                Triple(
                                    "Task Reminder",
                                    "You have a task: ${t?.name ?: "Unnamed Task"}",
                                    mapOf("type" to "TASK_REMINDER", "taskId" to (t?.id?.value?.toString() ?: ""))
                                )
                            }
                            r.habitId != null -> {
                                val h = Habit.findById(r.habitId!!)
                                Triple(
                                    "Habit Reminder",
                                    "Don't forget your habit: ${h?.name ?: "Unnamed Habit"}",
                                    mapOf("type" to "HABIT_REMINDER", "habitId" to (h?.id?.value?.toString() ?: ""))
                                )
                            }
                            else -> Triple("Reminder", "You have a reminder.", emptyMap())
                        }
                    }

                    notificationService.sendNotificationToTokens(tokens, title, body, data)
                    logger("[Reminders] Sent for user $userId @ ${tz.id}")
                } catch (e: Exception) {
                    Sentry.captureException(e)
                    logger("[Reminders] Error: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Sentry.captureException(e)
            logger("[Reminders] Loop error: ${e.message}")
        }
    }

    // ------------------------------------------------------------
    // Tasks
    // ------------------------------------------------------------

    private suspend fun checkDueTasksForUser(user: UserContext, nowInstant: Instant, nowMs: Long) {
        try {
            if (!user.settings.notificationsEnabled) {
                if (user.id == 1) logger("[User1] Skip task notif — disabled")
                return
            }

            val last = lastTaskNotificationTimes[user.id]
            if (!canSend(last, user.settings.dueTasksNotificationFrequency, nowMs)) {
                if (user.id == 1) {
                    val remainingMs = (user.settings.dueTasksNotificationFrequency * 60 * 1000L) - (nowMs - (last ?: 0L))
                    logger("[User1] Skip task notif — ${remainingMs / 1000 / 60}m ${(remainingMs / 1000) % 60}s remaining")
                }
                return
            }

            val today = nowInstant.toLocalDateTime(user.timeZone).date
            val info = taskService.getDueTasksForNotification(user.id, today)
            val tokens = timerService.getDeviceTokensForUser(user.id)

            var sent = 0

            if (info.overdueTasks.isNotEmpty()) {
                notificationService.sendNotificationToTokens(
                    tokens,
                    "Overdue Tasks",
                    "You have ${info.overdueTasks.size} overdue task(s): ${
                        info.overdueTasks.joinToString(", ") { it.name }
                    }",
                    data = mapOf(
                        "type" to "TASKS_OVERDUE",
                        "taskCount" to info.overdueTasks.size.toString(),
                        "taskNames" to info.overdueTasks.joinToString(", ") { it.name }
                    )
                )
                sent++
                if (user.id == 1) logger("[User1] *** OVERDUE TASK NOTIF SENT ***")
            }

            if (info.dueTodayTasks.isNotEmpty()) {
                notificationService.sendNotificationToTokens(
                    tokens,
                    "Tasks Due Today",
                    "You have ${info.dueTodayTasks.size} task(s) due today: ${
                        info.dueTodayTasks.joinToString(", ") { it.name }
                    }",
                    data = mapOf(
                        "type" to "TASKS_DUE_TODAY",
                        "taskCount" to info.dueTodayTasks.size.toString(),
                        "taskNames" to info.dueTodayTasks.joinToString(", ") { it.name }
                    )
                )
                sent++
                if (user.id == 1) logger("[User1] *** DUE TODAY TASK NOTIF SENT ***")
            }

            if (info.scheduledTodayTasks.isNotEmpty()
                && info.overdueTasks.isEmpty()
                && info.dueTodayTasks.isEmpty()
            ) {
                notificationService.sendNotificationToTokens(
                    tokens,
                    "Scheduled Tasks",
                    "You have ${info.scheduledTodayTasks.size} scheduled task(s) for today: ${
                        info.scheduledTodayTasks.joinToString(", ") { it.name }
                    }",
                    data = mapOf(
                        "type" to "TASKS_SCHEDULED_TODAY",
                        "taskCount" to info.scheduledTodayTasks.size.toString(),
                        "taskNames" to info.scheduledTodayTasks.joinToString(", ") { it.name }
                    )
                )
                sent++
                if (user.id == 1) logger("[User1] *** SCHEDULED TASK NOTIF SENT ***")
            }

            if (sent > 0) lastTaskNotificationTimes[user.id] = nowMs
            if (user.id == 1) logger("[User1] Task notifications sent: $sent")
        } catch (e: Exception) {
            Sentry.captureException(e)
            logger("[Tasks] Error user ${user.id}: ${e.message}")
        }
    }

    // ------------------------------------------------------------
    // Habits (due today)
    // ------------------------------------------------------------

    private suspend fun checkDueHabitsForUser(user: UserContext, nowInstant: Instant, nowMs: Long) {
        try {
            if (!user.settings.notificationsEnabled) {
                if (user.id == 1) logger("[User1] Skip habit notif — disabled")
                return
            }

            val last = lastHabitNotificationTimes[user.id]
            if (!canSend(last, user.settings.dueHabitsNotificationFrequency, nowMs)) {
                if (user.id == 1) {
                    val remainingMs = (user.settings.dueHabitsNotificationFrequency * 60 * 1000L) - (nowMs - (last ?: 0L))
                    logger("[User1] Skip habit notif — ${remainingMs / 1000 / 60}m ${(remainingMs / 1000) % 60}s remaining")
                }
                return
            }

            val today = nowInstant.toLocalDateTime(user.timeZone).date
            val info = habitService.getDueHabitsForNotification(user.id, today)
            if (info.incompleteHabits.isEmpty()) {
                if (user.id == 1) logger("[User1] No incomplete habits today")
                return
            }

            val tokens = timerService.getDeviceTokensForUser(user.id)
            notificationService.sendNotificationToTokens(
                tokens,
                "Habits Due",
                "You have ${info.incompleteHabits.size} habit(s) to complete today: ${
                    info.incompleteHabits.joinToString(", ") { it.name }
                }",
                data = mapOf(
                    "type" to "HABITS_DUE",
                    "habitCount" to info.incompleteHabits.size.toString(),
                    "habitNames" to info.incompleteHabits.joinToString(", ") { it.name }
                )
            )

            lastHabitNotificationTimes[user.id] = nowMs
            if (user.id == 1) logger("[User1] *** HABIT NOTIF SENT ***")
        } catch (e: Exception) {
            Sentry.captureException(e)
            logger("[Habits] Error user ${user.id}: ${e.message}")
        }
    }

    // ------------------------------------------------------------
    // Habit starts window (next 5 min)
    // ------------------------------------------------------------

    private suspend fun checkHabitStartsForUser(user: UserContext, nowInstant: Instant, nowMs: Long) {
        try {
            if (!user.settings.notificationsEnabled) {
                if (user.id == 1) logger("[User1] Skip habit-start notif — disabled")
                return
            }

            // reuse habit freq throttle
            val last = lastHabitNotificationTimes[user.id]
            if (!canSend(last, user.settings.dueHabitsNotificationFrequency, nowMs)) {
                if (user.id == 1) {
                    val remainingMs = (user.settings.dueHabitsNotificationFrequency * 60 * 1000L) - (nowMs - (last ?: 0L))
                    logger("[User1] Skip habit-start — ${remainingMs / 1000 / 60}m ${(remainingMs / 1000) % 60}s remaining")
                }
                return
            }

            val nowLocal = nowInstant.toLocalDateTime(user.timeZone)
            val currentTime = nowLocal.time

            val activeHabits = db {
                Habit.find {
                    (Habits.status eq Status.ACTIVE) and (Habits.user eq user.id)
                }.toList()
            }

            for (habit in activeHabits) {
                try {
                    val habitTime = habit.baseDateTime.time
                    val diffMin = (habitTime.toMillisecondOfDay() - currentTime.toMillisecondOfDay()).milliseconds.inWholeMinutes

                    if (diffMin !in 0..5) continue // only if starting within 5 min

                    val today = nowLocal.date
                    val doneToday = db {
                        HabitTrack.find {
                            (HabitTracks.habitId eq habit.id) and
                                    (HabitTracks.status eq Status.ACTIVE) and
                                    (HabitTracks.doneDateTime.date() eq today)
                        }.empty().not()
                    }
                    if (doneToday) continue

                    val isDueToday = when (habit.frequency.uppercase()) {
                        "DAILY" -> true
                        "WEEKLY" -> habit.baseDateTime.date.dayOfWeek == today.dayOfWeek
                        "MONTHLY" -> habit.baseDateTime.date.dayOfMonth == today.dayOfMonth
                        "YEARLY" -> habit.baseDateTime.date.dayOfYear == today.dayOfYear
                        else -> true
                    }
                    if (!isDueToday) continue

                    val tokens = timerService.getDeviceTokensForUser(user.id)
                    notificationService.sendNotificationToTokens(
                        tokens,
                        title = "Habit Starting",
                        body = "It's time to start your habit: '${habit.name}'",
                        data = mapOf(
                            "type" to "HABIT_STARTING",
                            "habitId" to habit.id.value.toString(),
                            "habitName" to habit.name,
                            "habitTime" to habitTime.formatDefault()
                        )
                    )
                    lastHabitNotificationTimes[user.id] = nowMs

                    if (user.id == 1) logger("[User1] *** HABIT START NOTIF SENT *** (${habit.name})")
                } catch (e: Exception) {
                    Sentry.captureException(e)
                    logger("[HabitStarts] Error habit ${habit.name}: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Sentry.captureException(e)
            logger("[HabitStarts] Error user ${user.id}: ${e.message}")
        }
    }

    // ------------------------------------------------------------
    // Model
    // ------------------------------------------------------------

    private data class UserContext(
        val id: Int,
        val timeZone: TimeZone,
        val settings: UserSettings
    )
}
