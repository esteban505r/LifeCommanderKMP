package com.esteban.ruano.service

import com.esteban.ruano.lifecommander.models.timers.CompletedTimerInfo
import com.esteban.ruano.lifecommander.timer.TimerWebSocketServerMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.UUID
import com.esteban.ruano.database.entities.Reminder
import com.esteban.ruano.database.entities.Habit
import com.esteban.ruano.database.entities.Task
import kotlinx.datetime.Instant
import com.esteban.ruano.database.entities.Habits
import com.esteban.ruano.database.entities.Tasks
import com.esteban.ruano.database.entities.HabitTracks
import com.esteban.ruano.database.entities.HabitTrack
import com.esteban.ruano.database.entities.User
import com.esteban.ruano.database.entities.UserSetting
import com.esteban.ruano.database.entities.UserSettings
import com.esteban.ruano.database.models.Status
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import io.sentry.Sentry
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.transactions.transaction
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDate
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import kotlinx.datetime.TimeZone.Companion.of
import kotlinx.datetime.toInstant
import kotlin.time.Duration.Companion.milliseconds


class TimerCheckerService(
    private val timerService: TimerService,
    private val notificationService: NotificationService,
    private val reminderService: ReminderService,
    private val taskService: TaskService,
    private val habitService: HabitService,
    private val settingsService: SettingsService
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    // Track last notification times for each user to respect frequency settings
    private val lastTaskNotificationTimes = mutableMapOf<Int, Long>()
    private val lastHabitNotificationTimes = mutableMapOf<Int, Long>()

    fun start() {
        scope.launch {
            while (true) {
                try {
                    // Log notification timing for user ID 1
                    logNotificationTimingForUser1()
                    
//                    checkTimers()
                    checkReminders()
                    checkDueTasks()
                    checkDueHabits()
                    checkHabitStarts()
                } catch (e: Exception) {
                    Sentry.captureException(e)
                    e.printStackTrace()
                }
                delay(30_000L) // 30 seconds
            }
        }
    }

    private suspend fun logNotificationTimingForUser1() {
        try {
            val userId = 1
            val user = transaction { User.findById(userId) }
            if (user == null) {
                println("[User1] User ID 1 not found")
                return
            }

            val userSettings = settingsService.getUserSettings(userId)
            val userTimeZone = try {
                TimeZone.of(user.timeZone ?: "UTC")
            } catch (e: Exception) {
                Sentry.captureException(e)
                TimeZone.UTC
            }
            
            val now = Clock.System.now().toLocalDateTime(userTimeZone)
            val currentTimeMillis = Clock.System.now().toEpochMilliseconds()
            
            // Calculate next notification times
            val lastTaskNotificationTime = lastTaskNotificationTimes[userId] ?: 0L
            val lastHabitNotificationTime = lastHabitNotificationTimes[userId] ?: 0L
            
            val nextTaskNotificationTime = if (lastTaskNotificationTime > 0) {
                lastTaskNotificationTime + (userSettings.dueTasksNotificationFrequency * 60 * 1000L)
            } else {
                currentTimeMillis // Can send immediately
            }
            
            val nextHabitNotificationTime = if (lastHabitNotificationTime > 0) {
                lastHabitNotificationTime + (userSettings.dueHabitsNotificationFrequency * 60 * 1000L)
            } else {
                currentTimeMillis // Can send immediately
            }
            
            val timeUntilNextTaskNotification = nextTaskNotificationTime - currentTimeMillis
            val timeUntilNextHabitNotification = nextHabitNotificationTime - currentTimeMillis
            
            println("=".repeat(80))
            println("[User1] Notification Timing Check - ${now.formatDefault()}")
            println("[User1] User timezone: ${user.timeZone ?: "UTC"}")
            println("[User1] Notifications enabled: ${userSettings.notificationsEnabled}")
            println("[User1] Task notification frequency: ${userSettings.dueTasksNotificationFrequency} minutes")
            println("[User1] Habit notification frequency: ${userSettings.dueHabitsNotificationFrequency} minutes")
            println("[User1] Last task notification: ${if (lastTaskNotificationTime > 0) Instant.fromEpochMilliseconds(lastTaskNotificationTime).toLocalDateTime(userTimeZone).formatDefault() else "Never"}")
            println("[User1] Last habit notification: ${if (lastHabitNotificationTime > 0) Instant.fromEpochMilliseconds(lastHabitNotificationTime).toLocalDateTime(userTimeZone).formatDefault() else "Never"}")
            println("[User1] Next task notification: ${Instant.fromEpochMilliseconds(nextTaskNotificationTime).toLocalDateTime(userTimeZone).formatDefault()} (in ${timeUntilNextTaskNotification / 1000 / 60} minutes, ${(timeUntilNextTaskNotification / 1000) % 60} seconds, ${timeUntilNextTaskNotification % 1000} ms)")
            println("[User1] Next habit notification: ${Instant.fromEpochMilliseconds(nextHabitNotificationTime).toLocalDateTime(userTimeZone).formatDefault()} (in ${timeUntilNextHabitNotification / 1000 / 60} minutes, ${(timeUntilNextHabitNotification / 1000) % 60} seconds, ${timeUntilNextHabitNotification % 1000} ms)")
            
            // Check if notifications can be sent now
            val canSendTaskNotification = timeUntilNextTaskNotification <= 0
            val canSendHabitNotification = timeUntilNextHabitNotification <= 0
            
            println("[User1] Can send task notification: $canSendTaskNotification")
            println("[User1] Can send habit notification: $canSendHabitNotification")
            println("=".repeat(80))
            
        } catch (e: Exception) {
            println("[User1] Error logging notification timing: ${e.message}")
            Sentry.captureException(e)
            e.printStackTrace()
        }
    }

    private suspend fun checkTimers() {
        val now = Clock.System.now()
        
        // Get all users and their timezones
        val users = transaction {
            User.all().associate { it.id.value to it.timeZone }
        }

        // Process timers for each user with their respective timezone
        for ((userId, userTimezone) in users) {
            try {
                val userTimeZone = try {
                    TimeZone.of(userTimezone ?: "UTC")
                } catch (e: Exception) {
                    Sentry.captureException(e)
                    TimeZone.UTC
                }
                
                val currentTimeInUserTz = now.toLocalDateTime(userTimeZone)
                
                // Fetch completed timers for this specific user with their timezone
                val completedTimers: List<CompletedTimerInfo> = timerService.checkCompletedTimers(
                    userTimeZone,
                    currentTimeInUserTz,
                    userId
                )

                if (completedTimers.isNotEmpty()) {
                    println("[Timers] Processing ${completedTimers.size} completed timers for user $userId in timezone: $userTimezone, local time: $currentTimeInUserTz")
                    
                    for (info in completedTimers) {
                        try {
                            CoroutineScope(Dispatchers.Default).launch {
                                // Fetch tokens for the user
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
                            }

                            val nextTimer = timerService.getNextTimerToStart(UUID.fromString(info.listId), info.domainTimer)
                            val updatedTimer = nextTimer?.let {
                                timerService.startTimer(
                                    userId = info.userId,
                                    listId = UUID.fromString(info.listId),
                                    timerId = it.id.value
                                ).firstOrNull()
                            }

                            TimerNotifier.broadcastUpdate(
                                TimerWebSocketServerMessage.TimerUpdate(
                                    timer = updatedTimer ?: info.domainTimer,
                                    listId = info.listId,
                                    remainingTime = updatedTimer?.duration ?: info.domainTimer.duration,
                                ),
                                info.userId
                            )

                            println("[Timers] Timer update broadcasted for list ${info.listId}")

                        } catch (e: Exception) {
                            Sentry.captureException(e)
                            println("[Timers] Error processing timer ${info.domainTimer.id}: ${e.message}")
                            e.printStackTrace()
                        }
                    }
                }
                
            } catch (e: Exception) {
                Sentry.captureException(e)
                println("[Timers] Error processing timers for user $userId: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private suspend fun checkReminders() {
        val now = Clock.System.now().toEpochMilliseconds()
        val dueReminders = reminderService.getDueReminders(now)
        
        for (reminder in dueReminders) {
            try {
                val (userId, userTimezone) = when {
                    reminder.taskId != null -> {
                        transaction {
                            val task = Task.findById(reminder.taskId!!)
                            val user = task?.user
                            Pair(user?.id?.value, user?.timeZone)
                        }
                    }
                    reminder.habitId != null -> {
                        transaction {
                            val habit = Habit.findById(reminder.habitId!!)
                            val user = habit?.user
                            Pair(user?.id?.value, user?.timeZone)
                        }
                    }
                    else -> Pair(null, null)
                }
                
                // Check if reminder time matches current time in user's timezone
                val userTimeZone = try {
                    TimeZone.of(userTimezone ?: "UTC")
                } catch (e: Exception) {
                    Sentry.captureException(e)
                    TimeZone.UTC
                }
                
                val currentTimeInUserTz = Clock.System.now().toLocalDateTime(userTimeZone)
                val reminderTimeInUserTz = Instant.fromEpochMilliseconds(reminder.time).toLocalDateTime(userTimeZone)
                
                // Only send notification if reminder time is within 30 seconds of current time
                val timeDifference = kotlin.math.abs((currentTimeInUserTz.toInstant(userTimeZone) - reminderTimeInUserTz.toInstant(userTimeZone)).inWholeSeconds)
                if (timeDifference > 30) continue
                
                val tokens = timerService.getDeviceTokensForUser(userId!!)
                val (title, body, data) = when {
                    reminder.taskId != null -> {
                        transaction {
                            val task = Task.findById(reminder.taskId!!)
                            Triple(
                                "Task Reminder",
                                "You have a task: ${task?.name ?: "Unnamed Task"}",
                                mapOf("type" to "TASK_REMINDER", "taskId" to (task?.id?.value?.toString() ?: ""))
                            )
                        }
                    }
                    reminder.habitId != null -> {
                        transaction {
                            val habit = Habit.findById(reminder.habitId!!)
                            Triple(
                                "Habit Reminder",
                                "Don't forget your habit: ${habit?.name ?: "Unnamed Habit"}",
                                mapOf("type" to "HABIT_REMINDER", "habitId" to (habit?.id?.value?.toString() ?: ""))
                            )
                        }
                    }
                    else -> Triple("Reminder", "You have a reminder.", emptyMap())
                }
                notificationService.sendNotificationToTokens(tokens, title, body, data)
                println("[Reminders] Sent notification for reminder in timezone: $userTimezone")
                
            } catch (e: Exception) {
                Sentry.captureException(e)
                println("[Reminders] Error processing reminder: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private suspend fun checkDueTasks() {
        // Get all users and their timezones
        val users = transaction {
            User.all().associate { it.id.value to it.timeZone }
        }
        
        for ((userId, userTimezone) in users) {
            try {
                // Get user settings to check notification frequency
                val userSettings = settingsService.getUserSettings(userId)
                if (!userSettings.notificationsEnabled) {
                    if (userId == 1) {
                        println("[User1] Skipping task notifications - notifications disabled")
                    }
                    continue // Skip if notifications are disabled
                }
                
                val userTimeZone = try {
                    TimeZone.of(userTimezone ?: "UTC")
                } catch (e: Exception) {
                    Sentry.captureException(e)
                    TimeZone.UTC
                }
                
                val now = Clock.System.now().toLocalDateTime(userTimeZone)
                val today = now.date
                val currentTimeMillis = Clock.System.now().toEpochMilliseconds()
                
                // Check if enough time has passed since last notification
                val lastNotificationTime = lastTaskNotificationTimes[userId] ?: 0L
                if (!taskService.shouldSendTaskNotification(userId, lastNotificationTime, userSettings.dueTasksNotificationFrequency)) {
                    if (userId == 1) {
                        val remainingTime = (userSettings.dueTasksNotificationFrequency * 60 * 1000L) - (currentTimeMillis - lastNotificationTime)
                        val remainingMinutes = remainingTime / 1000 / 60
                        val remainingSeconds = (remainingTime / 1000) % 60
                        val remainingMs = remainingTime % 1000
                        println("[User1] Skipping task notification - ${remainingMinutes} minutes, ${remainingSeconds} seconds, ${remainingMs} ms remaining until next notification")
                    }
                    continue
                }
                
                // Get task notification info from service
                val taskInfo = taskService.getDueTasksForNotification(userId, today)
                val tokens = timerService.getDeviceTokensForUser(userId)
                
                if (userId == 1) {
                    println("[User1] Task notification info:")
                    println("[User1] Overdue tasks: ${taskInfo.overdueTasks.size}")
                    println("[User1] Due today tasks: ${taskInfo.dueTodayTasks.size}")
                    println("[User1] Scheduled today tasks: ${taskInfo.scheduledTodayTasks.size}")
                    println("[User1] Tokens count: ${tokens.size}")
                }
                
                // Send separate notifications for different task types
                var notificationsSent = 0
                
                // 1. Overdue tasks notification
                if (taskInfo.overdueTasks.isNotEmpty()) {
                    val title = "Overdue Tasks"
                    val body = "You have ${taskInfo.overdueTasks.size} overdue task(s): ${taskInfo.overdueTasks.joinToString(", ") { it.name }}"
                    val data = mapOf(
                        "type" to "TASKS_OVERDUE",
                        "taskCount" to taskInfo.overdueTasks.size.toString(),
                        "taskNames" to taskInfo.overdueTasks.joinToString(", ") { it.name }
                    )
                    
                    notificationService.sendNotificationToTokens(tokens, title, body, data)
                    notificationsSent++
                    
                    if (userId == 1) {
                        println("[User1] *** OVERDUE TASK NOTIFICATION SENT ***")
                        println("[User1] Overdue tasks: ${taskInfo.overdueTasks.map { it.name }}")
                    }
                }
                
                // 2. Due today tasks notification
                if (taskInfo.dueTodayTasks.isNotEmpty()) {
                    val title = "Tasks Due Today"
                    val body = "You have ${taskInfo.dueTodayTasks.size} task(s) due today: ${taskInfo.dueTodayTasks.joinToString(", ") { it.name }}"
                    val data = mapOf(
                        "type" to "TASKS_DUE_TODAY",
                        "taskCount" to taskInfo.dueTodayTasks.size.toString(),
                        "taskNames" to taskInfo.dueTodayTasks.joinToString(", ") { it.name }
                    )
                    
                    notificationService.sendNotificationToTokens(tokens, title, body, data)
                    notificationsSent++
                    
                    if (userId == 1) {
                        println("[User1] *** DUE TODAY TASK NOTIFICATION SENT ***")
                        println("[User1] Due today tasks: ${taskInfo.dueTodayTasks.map { it.name }}")
                    }
                }
                
                // 3. Scheduled today tasks notification (only if no overdue or due today tasks)
                if (taskInfo.scheduledTodayTasks.isNotEmpty() && taskInfo.overdueTasks.isEmpty() && taskInfo.dueTodayTasks.isEmpty()) {
                    val title = "Scheduled Tasks"
                    val body = "You have ${taskInfo.scheduledTodayTasks.size} scheduled task(s) for today: ${taskInfo.scheduledTodayTasks.joinToString(", ") { it.name }}"
                    val data = mapOf(
                        "type" to "TASKS_SCHEDULED_TODAY",
                        "taskCount" to taskInfo.scheduledTodayTasks.size.toString(),
                        "taskNames" to taskInfo.scheduledTodayTasks.joinToString(", ") { it.name }
                    )
                    
                    notificationService.sendNotificationToTokens(tokens, title, body, data)
                    notificationsSent++
                    
                    if (userId == 1) {
                        println("[User1] *** SCHEDULED TASK NOTIFICATION SENT ***")
                        println("[User1] Scheduled tasks: ${taskInfo.scheduledTodayTasks.map { it.name }}")
                    }
                }
                
                if (notificationsSent > 0) {
                    lastTaskNotificationTimes[userId] = currentTimeMillis
                    println("[Tasks] Sent $notificationsSent notification(s) for tasks in timezone: $userTimezone")
                    
                    if (userId == 1) {
                        println("[User1] Total task notifications sent: $notificationsSent")
                        println("[User1] Last notification time updated to: ${currentTimeMillis}")
                    }
                } else {
                    if (userId == 1) {
                        println("[User1] No task notifications to send")
                    }
                }
                
            } catch (e: Exception) {
                Sentry.captureException(e)
                println("[Tasks] Error processing task notifications for user $userId: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private suspend fun checkDueHabits() {
        // Get all users and their timezones
        val users = transaction {
            User.all().associate { it.id.value to it.timeZone }
        }
        
        for ((userId, userTimezone) in users) {
            try {
                // Get user settings to check notification frequency
                val userSettings = settingsService.getUserSettings(userId)
                if (!userSettings.notificationsEnabled) {
                    if (userId == 1) {
                        println("[User1] Skipping habit notifications - notifications disabled")
                    }
                    continue // Skip if notifications are disabled
                }
                
                val userTimeZone = try {
                    TimeZone.of(userTimezone ?: "UTC")
                } catch (e: Exception) {
                    Sentry.captureException(e)
                    TimeZone.UTC
                }
                
                val now = Clock.System.now().toLocalDateTime(userTimeZone)
                val today = now.date
                val currentTimeMillis = Clock.System.now().toEpochMilliseconds()
                
                // Check if enough time has passed since last notification
                val lastNotificationTime = lastHabitNotificationTimes[userId] ?: 0L
                if (!habitService.shouldSendHabitNotification(userId, lastNotificationTime, userSettings.dueHabitsNotificationFrequency)) {
                    if (userId == 1) {
                        val remainingTime = (userSettings.dueHabitsNotificationFrequency * 60 * 1000L) - (currentTimeMillis - lastNotificationTime)
                        val remainingMinutes = remainingTime / 1000 / 60
                        val remainingSeconds = (remainingTime / 1000) % 60
                        val remainingMs = remainingTime % 1000
                        println("[User1] Skipping habit notification - ${remainingMinutes} minutes, ${remainingSeconds} seconds, ${remainingMs} ms remaining until next notification")
                    }
                    continue
                }
                
                // Get habit notification info from service
                val habitInfo = habitService.getDueHabitsForNotification(userId, today)
                val tokens = timerService.getDeviceTokensForUser(userId)
                
                if (userId == 1) {
                    println("[User1] Habit notification info:")
                    println("[User1] Incomplete habits: ${habitInfo.incompleteHabits.size}")
                    println("[User1] Total habits found: ${habitInfo.totalHabits}")
                    println("[User1] Tokens count: ${tokens.size}")
                }
                
                if (habitInfo.incompleteHabits.isNotEmpty()) {
                    val title = "Habits Due"
                    val body = "You have ${habitInfo.incompleteHabits.size} habit(s) to complete today: ${habitInfo.incompleteHabits.joinToString(", ") { it.name }}"
                    val data = mapOf(
                        "type" to "HABITS_DUE",
                        "habitCount" to habitInfo.incompleteHabits.size.toString(),
                        "habitNames" to habitInfo.incompleteHabits.joinToString(", ") { it.name }
                    )
                    
                    notificationService.sendNotificationToTokens(tokens, title, body, data)
                    lastHabitNotificationTimes[userId] = currentTimeMillis
                    println("[Habits] Sent notification for ${habitInfo.incompleteHabits.size} habits in timezone: $userTimezone")
                    
                    if (userId == 1) {
                        println("[User1] *** HABIT NOTIFICATION SENT ***")
                        println("[User1] Habits due: ${habitInfo.incompleteHabits.map { it.name }}")
                        println("[User1] Notification sent at: ${now.formatDefault()}")
                        println("[User1] Last notification time updated to: ${currentTimeMillis}")
                    }
                } else {
                    if (userId == 1) {
                        println("[User1] No incomplete habits found for today")
                        println("[User1] Total habits found: ${habitInfo.totalHabits}")
                    }
                }
                
            } catch (e: Exception) {
                Sentry.captureException(e)
                println("[Habits] Error processing habit notifications for user $userId: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private suspend fun checkHabitStarts() {
        // Get all users and their timezones
        val users = transaction {
            User.all().associate { it.id.value to it.timeZone }
        }
        
        for ((userId, userTimezone) in users) {
            try {
                // Get user settings to check notification frequency
                val userSettings = settingsService.getUserSettings(userId)
                if (!userSettings.notificationsEnabled) {
                    if (userId == 1) {
                        println("[User1] Skipping habit start notifications - notifications disabled")
                    }
                    continue // Skip if notifications are disabled
                }
                
                val userTimeZone = try {
                    TimeZone.of(userTimezone ?: "UTC")
                } catch (e: Exception) {
                    Sentry.captureException(e)
                    TimeZone.UTC
                }
                
                val now = Clock.System.now().toLocalDateTime(userTimeZone)
                val currentTime = now.time
                val currentTimeMillis = Clock.System.now().toEpochMilliseconds()
                
                // Check if enough time has passed since last notification (use same frequency as due habits)
                val lastNotificationTime = lastHabitNotificationTimes[userId] ?: 0L
                val timeSinceLastNotification = currentTimeMillis - lastNotificationTime
                val notificationIntervalMs = userSettings.dueHabitsNotificationFrequency * 60 * 1000L // Convert minutes to milliseconds
                
                if (timeSinceLastNotification < notificationIntervalMs) {
                    if (userId == 1) {
                        val remainingTime = notificationIntervalMs - timeSinceLastNotification
                        val remainingMinutes = remainingTime / 1000 / 60
                        val remainingSeconds = (remainingTime / 1000) % 60
                        val remainingMs = remainingTime % 1000
                        println("[User1] Skipping habit start notification - ${remainingMinutes} minutes, ${remainingSeconds} seconds, ${remainingMs} ms remaining until next notification")
                        println("[User1] Time since last notification: ${timeSinceLastNotification / 1000 / 60} minutes, ${(timeSinceLastNotification / 1000) % 60} seconds, ${timeSinceLastNotification % 1000} ms")
                        println("[User1] Notification interval: ${notificationIntervalMs / 1000 / 60} minutes, ${(notificationIntervalMs / 1000) % 60} seconds, ${notificationIntervalMs % 1000} ms")
                    }
                    continue // Not enough time has passed
                }
                
                // Get all active habits for this user
                val activeHabits = transaction {
                    Habit.find {
                        (Habits.status eq Status.ACTIVE) and
                        (Habits.user eq userId)
                    }.toList()
                }
                
                for (habit in activeHabits) {
                    try {
                        val habitDateTime = habit.baseDateTime
                        val habitTime = habitDateTime.time
                        
                        // Check if habit is starting within the next 5 minutes
                        val timeDifference = habitTime.toMillisecondOfDay().minus(currentTime.toMillisecondOfDay()).milliseconds
                        val timeInMinutes = timeDifference.inWholeMinutes
                        
                        // Only send notification if habit is starting within 5 minutes and hasn't been done today
                        if (timeInMinutes in 0..5) {
                            // Check if habit is already done for today
                            val today = now.date
                            val isDoneToday = transaction {
                                HabitTrack.find {
                                    (HabitTracks.habitId eq habit.id) and
                                    (HabitTracks.status eq Status.ACTIVE) and
                                    (HabitTracks.doneDateTime.date() eq today)
                                }.count() > 0
                            }
                            
                            if (!isDoneToday) {
                                // Check if this habit is actually due today based on its frequency
                                val isDueToday = when (habit.frequency.uppercase()) {
                                    "DAILY" -> true
                                    "WEEKLY" -> habitDateTime.date.dayOfWeek == today.dayOfWeek
                                    "MONTHLY" -> habitDateTime.date.month == today.month
                                    "YEARLY" -> habitDateTime.date.dayOfYear == today.dayOfYear
                                    else -> true // For other frequencies, assume it's due
                                }
                                
                                if (isDueToday) {
                                    val tokens = timerService.getDeviceTokensForUser(userId)
                                    
                                    if (userId == 1) {
                                        println("[User1] *** ABOUT TO SEND HABIT START NOTIFICATION ***")
                                        println("[User1] Habit: ${habit.name}")
                                        println("[User1] Tokens count: ${tokens.size}")
                                        println("[User1] Current time: ${now.formatDefault()}")
                                        println("[User1] Habit time: ${habitTime.formatDefault()}")
                                        println("[User1] Time difference: ${timeInMinutes} minutes")
                                        println("[User1] Time since last notification: ${timeSinceLastNotification / 1000 / 60} minutes, ${(timeSinceLastNotification / 1000) % 60} seconds")
                                    }
                                    
                                    val title = "Habit Starting"
                                    val body = "It's time to start your habit: '${habit.name}'"
                                    val data = mapOf(
                                        "type" to "HABIT_STARTING",
                                        "habitId" to habit.id.value.toString(),
                                        "habitName" to habit.name,
                                        "habitTime" to habitTime.formatDefault()
                                    )
                                    
                                    notificationService.sendNotificationToTokens(tokens, title, body, data)
                                    lastHabitNotificationTimes[userId] = currentTimeMillis
                                    println("[Habit Starts] Sent notification for habit starting: ${habit.name} in timezone: $userTimezone")
                                    
                                    if (userId == 1) {
                                        println("[User1] *** HABIT START NOTIFICATION SENT ***")
                                        println("[User1] Habit starting: ${habit.name}")
                                        println("[User1] Notification sent at: ${now.formatDefault()}")
                                        println("[User1] Last notification time updated to: ${currentTimeMillis}")
                                    }
                                }
                            }
                        }
                        
                    } catch (e: Exception) {
                        Sentry.captureException(e)
                        println("[Habit Starts] Error processing habit ${habit.name}: ${e.message}")
                        e.printStackTrace()
                    }
                }
                
            } catch (e: Exception) {
                Sentry.captureException(e)

                println("[Habit Starts] Error processing habit start notifications for user $userId: ${e.message}")
                e.printStackTrace()
            }
        }
    }
} 