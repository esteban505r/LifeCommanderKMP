package utils

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

class BackgroundServiceManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val jobs = ConcurrentHashMap<String, Job>()
    private val _activeServices = MutableStateFlow<Set<String>>(emptySet())
    val activeServices: StateFlow<Set<String>> = _activeServices.asStateFlow()

    fun startPeriodicTask(
        taskId: String,
        interval: Duration,
        initialDelay: Duration = Duration.ZERO,
        task: suspend () -> Unit
    ) {
        if (jobs.containsKey(taskId)) {
            return
        }

        val job = scope.launch {
            delay(initialDelay.toMillis())
            while (isActive) {
                try {
                    task()
                } catch (e: Exception) {
                    println("Error in periodic task $taskId: ${e.message}")
                }
                delay(interval.toMillis())
            }
        }

        jobs[taskId] = job
        _activeServices.value = _activeServices.value + taskId
    }

    fun stopTask(taskId: String) {
        jobs[taskId]?.cancel()
        jobs.remove(taskId)
        _activeServices.value = _activeServices.value - taskId
    }

    fun stopAllTasks() {
        jobs.values.forEach { it.cancel() }
        jobs.clear()
        _activeServices.value = emptySet()
    }

    fun isTaskRunning(taskId: String): Boolean {
        return jobs.containsKey(taskId)
    }

    fun restartTask(
        taskId: String,
        interval: Duration,
        initialDelay: Duration = Duration.ZERO,
        task: suspend () -> Unit
    ) {
        stopTask(taskId)
        startPeriodicTask(taskId, interval, initialDelay, task)
    }
} 