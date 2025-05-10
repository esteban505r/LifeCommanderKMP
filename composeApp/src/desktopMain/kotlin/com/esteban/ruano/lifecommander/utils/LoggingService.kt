package utils

import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import utils.DateUtils.parseTime

class LoggingService {
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

    suspend fun writeTimerInfo(timerName: String, timeRemaining: Long) {
        writeToFile("timer", "echo \"$timerName - ${timeRemaining.parseTime()}\"")
    }

    suspend fun writeNightBlockStatus(isActive: Boolean, timeRemaining: String? = null) {
        val status = if (isActive) {
            "Night Block Active"
        } else {
            timeRemaining?.let { "Night Block in $it" } ?: "Night Block Inactive"
        }
        writeToFile("nightblock", "echo \"$status\"")
    }

    suspend fun writePomodoroCount(count: Int) {
        writeToFile("pomodoro", "echo \"Pomodoros: $count\"")
    }

    suspend fun writeHabitStatus(habitName: String, isCompleted: Boolean) {
        writeToFile("habits", "echo \"$habitName: ${if (isCompleted) "✓" else "✗"}\"")
    }

    suspend fun clearFile(filename: String) {
        writeToFile(filename, "")
    }
} 