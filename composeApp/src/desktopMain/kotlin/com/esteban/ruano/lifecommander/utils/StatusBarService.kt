package utils

import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import utils.DateUtils.parseTime

class StatusBarService {
    private val configDir = File(System.getProperty("user.home"), ".config/LifeCommanderDesktop")
    
    init {
        // Ensure the config directory exists
        if (!configDir.exists()) {
            configDir.mkdirs()
        }
    }

    suspend fun writeToFile(filename: String, content: String) {
        withContext(Dispatchers.IO) {
            val file = File(configDir, filename)
            file.writeText(content)
        }
    }

    suspend fun updateTimerStatus(timerName: String, timeRemaining: Long) {
        writeToFile("timer", "echo \"$timerName - ${timeRemaining.parseTime()}\"")
    }

    suspend fun updateNightBlockStatus(isActive: Boolean, timeRemaining: String? = null) {
        val status = if (isActive) {
            "Night Block Active"
        } else {
            timeRemaining?.let { "Night Block in $it" } ?: "Night Block Inactive"
        }
        writeToFile("nightblock", "echo \"$status\"")
    }

    suspend fun updatePomodoroCount(count: Int) {
        writeToFile("pomodoro", "echo \"Pomodoros: $count\"")
    }

    suspend fun updateHabitStatus(text: String) {
        writeToFile("habits", "echo \"$text\"")
    }

    suspend fun updateTaskStatus(text: String) {
        writeToFile("tasks", "echo \"$text\"")
    }

    suspend fun clearStatus(filename: String) {
        writeToFile(filename, "")
    }
} 