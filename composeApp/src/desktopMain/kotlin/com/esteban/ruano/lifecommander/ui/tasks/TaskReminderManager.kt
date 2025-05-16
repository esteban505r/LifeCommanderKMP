package ui.tasks

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import services.auth.TokenStorage
import services.tasks.TaskService
import ui.models.TaskFilters
import com.esteban.ruano.utils.DateUIUtils.toLocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import utils.DateUtils.toLocalDateTime
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class TaskReminderManager(
    private val taskService: TaskService,
    private val coroutineScope: CoroutineScope,
    private val tokenStorage: TokenStorage,
    private val onReminder: (String, String) -> Unit
) {
    private var isRunning = false
    private val checkIntervalMillis = (1000 * 60 * 30).toLong() // 30 minutes

    fun start() {
        if (isRunning) return
        isRunning = true

        coroutineScope.launch {
            println("TaskReminderManager started isRunning: $isRunning")
            while (isRunning) {
                try {
                    checkTasks()
                    delay(checkIntervalMillis)
                } catch (e: Exception) {
                    delay(checkIntervalMillis)
                    // Log error but continue running
                    println("Error checking tasks: ${e.message}")
                }
            }
        }
    }

    fun stop() {
        isRunning = false
    }

    private suspend fun checkTasks() {
        val tasks = taskService.getByDate(
            token = tokenStorage.getToken()?:"",
            page = 0,
            limit = 30,
            date = TaskFilters.TODAY.getDateRangeByFilter().first,
        )

        println("Checking tasks: ${tasks.size} tasks found")

        tasks.forEach { task ->
            if (task.dueDateTime != null) {
                val now = LocalDateTime.now()
                val taskDateTime = task?.dueDateTime?.toLocalDateTime()
                val delayMillis = ChronoUnit.MILLIS.between(now, taskDateTime?.toJavaLocalDateTime())

                when {
                    // Overdue tasks
                    delayMillis < 0 -> {
                        val priorityMessage = when (task.priority) {
                            4 -> "URGENT! Task '${task.name}' is overdue!"
                            3 -> "IMPORTANT! Task '${task.name}' is overdue!"
                            else -> "Task '${task.name}' is overdue!"
                        }
                        onReminder("Overdue Task", priorityMessage)
                        return@forEach
                    }
                    // Tasks due within 30 minutes
                    delayMillis > 0 && delayMillis <= checkIntervalMillis -> {
                        val priorityMessage = when (task.priority) {
                            4 -> "URGENT! Task '${task.name}' is due soon!"
                            3 -> "IMPORTANT! Task '${task.name}' is due soon!"
                            else -> "Task '${task.name}' is due soon!"
                        }
                        onReminder("Task Reminder", priorityMessage)
                        return@forEach
                    }
                }
            }
        }
    }
} 