package ui.habits

import com.lifecommander.models.Habit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import services.auth.TokenStorage
import services.habits.HabitService
import com.esteban.ruano.utils.DateUIUtils.toLocalDateTime
import com.esteban.ruano.utils.DateUIUtils.toLocalTime
import kotlinx.datetime.toJavaLocalTime
import utils.DateUtils.parseDate
import utils.DateUtils.toLocalDateTime
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

class HabitReminderManager(
    private val habitsService: HabitService,
    private val coroutineScope: CoroutineScope,
    private val onReminder: (String, String) -> Unit,
    private val tokenStorage: TokenStorage,
) {
    private var isRunning = false
    private val checkIntervalMillis = (1000 * 60 * 30).toLong() // 30 minutes

    fun start() {
        if (isRunning) return
        isRunning = true

        coroutineScope.launch {
            while (isRunning) {
                try {
                    delay(2000)
                    checkHabits()
                    delay(checkIntervalMillis)
                } catch (e: Exception) {
                    delay(checkIntervalMillis)
                    // Log error but continue running
                    println("Error checking habits: ${e.message}")
                }
            }
        }
    }

    fun stop() {
        isRunning = false
    }

    private suspend fun checkHabits() {
        val habits = habitsService.getByDate(
            token = tokenStorage.getToken() ?: "",
            page = 0,
            limit = 30,
            date = LocalDate.now().parseDate(),
        )

        for (habit in habits) {
            val sended = scheduleReminder(habit)
            if(sended){
               break
            }
        }
    }

    private fun scheduleReminder(habit: Habit): Boolean {
        val now = LocalTime.now()
        val habitDateTime = habit.dateTime?.toLocalDateTime()?.toLocalTime() ?: return false
        val delayMillis = ChronoUnit.MILLIS.between(now, habitDateTime.toJavaLocalTime())

        when {
            // Overdue habits
            delayMillis < 0 -> {
                onReminder(
                    "Overdue Habit",
                    //"You missed your habit '${habit.name}'! Try to complete it now!"
                    "You have overdue habits! Try to complete them now!"
                )
                return true
            }
            // Habits due within 30 minutes
            delayMillis > 0 && delayMillis <= checkIntervalMillis -> {
                onReminder(
                    "Habit Reminder",
                    "It's time to ${habit.name}!"
                )
                return true
            }
        }
        return false
    }
} 