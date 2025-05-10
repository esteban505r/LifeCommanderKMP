package utils

import services.habits.models.HabitResponse
import services.tasks.models.TaskResponse
import utils.DateUIUtils.toLocalDateTime
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime

data class TimeBasedItemInfo<T>(
    val currentItem: T?,
    val nextItem: T?,
    val timeRemaining: Duration?
)

data class StatusBarInfo(
    val title: String,
    val subtitle: String? = null,
    val isUrgent: Boolean = false,
    val isOverdue: Boolean = false
)

object TimeBasedItemUtils {
    private fun getDayLetter(dateTime: LocalDateTime): String {
        return when (dateTime.dayOfWeek) {
            java.time.DayOfWeek.MONDAY -> "M"
            java.time.DayOfWeek.TUESDAY -> "T"
            java.time.DayOfWeek.WEDNESDAY -> "W"
            java.time.DayOfWeek.THURSDAY -> "Th"
            java.time.DayOfWeek.FRIDAY -> "F"
            java.time.DayOfWeek.SATURDAY -> "Sa"
            java.time.DayOfWeek.SUNDAY -> "Su"
        }
    }

    fun <T> calculateItemTimes(
        items: List<T>,
        currentTime: LocalTime,
        getTime: (T) -> LocalTime?
    ): TimeBasedItemInfo<T> {
        val itemsWithTimes = items.mapNotNull { item ->
            getTime(item)?.let { time ->
                item to time
            }
        }

        // Find current item (closest in time, but before current hour)
        val currentItem = itemsWithTimes
            .filter { (_, time) -> 
                time <= currentTime
            }
            .maxByOrNull { (_, time) -> time }?.first

        // Find next item (closest in future)
        val nextItem = itemsWithTimes
            .filter { (_, time) -> time > currentTime }
            .minByOrNull { (_, time) -> 
                Duration.between(currentTime, time).toMinutes()
            }?.first

        // Calculate time remaining for current item
        val timeRemaining = nextItem?.let { item ->
            getTime(item)?.let { habitTime ->
                Duration.between(currentTime, habitTime)
            }
        }

        return TimeBasedItemInfo(currentItem, nextItem, timeRemaining)
    }

    // Convenience function for habits
    fun calculateHabitTimes(habits: List<HabitResponse>, currentTime: LocalTime): TimeBasedItemInfo<HabitResponse> {
        return calculateItemTimes(habits, currentTime) { habit ->
            habit.dateTime?.toLocalDateTime()?.toLocalTime()
        }
    }

    // Convenience function for tasks
    fun calculateTaskTimes(tasks: List<TaskResponse>, currentTime: LocalTime): TimeBasedItemInfo<TaskResponse> {
        return calculateItemTimes(tasks, currentTime) { task ->
            // Prioritize scheduled time over due time
            task.scheduledDateTime?.toLocalDateTime()?.toLocalTime() 
                ?: task.dueDateTime?.toLocalDateTime()?.toLocalTime()
        }
    }

    fun getTaskStatusBarText(tasks: List<TaskResponse>, currentTime: LocalDateTime): String {
        val taskTimeInfo = calculateTaskTimes(tasks, currentTime.toLocalTime())
        val currentTask = taskTimeInfo.currentItem
        val nextTask = taskTimeInfo.nextItem

        // Check for overdue tasks
        val overdueTasks = tasks.filter { task ->
            task.done != true && task.dueDateTime?.toLocalDateTime()?.isBefore(currentTime) == true
        }

        if (overdueTasks.isNotEmpty()) {
            val overdueTask = overdueTasks[0]
            val overdueTime = overdueTask.dueDateTime?.toLocalDateTime()
            val timeOverdue = overdueTime?.let { Duration.between(it, currentTime) }
            val overdueText = when {
                timeOverdue?.toHours() ?: 0 > 0 -> "${timeOverdue?.toHours()}h ${timeOverdue?.toMinutesPart()}m"
                else -> "${timeOverdue?.toMinutes()}m"
            }
            
            return when (overdueTasks.size) {
                1 -> "⚠️ Overdue: ${overdueTask.name} (${getDayLetter(overdueTime!!)} ${overdueTime.toLocalTime()}, +$overdueText)"
                2 -> "⚠️ 2 overdue: ${overdueTasks[0].name} (${getDayLetter(overdueTasks[0].dueDateTime?.toLocalDateTime()!!)} ${overdueTasks[0].dueDateTime?.toLocalDateTime()?.toLocalTime()}, +${Duration.between(overdueTasks[0].dueDateTime?.toLocalDateTime(), currentTime).toHours()}h), ${overdueTasks[1].name}"
                else -> "⚠️ ${overdueTasks.size} overdue tasks"
            }
        }

        // Check for high priority tasks
        val highPriorityTasks = tasks.filter { task ->
            task.done != true && (task.priority ?: 0) >= 3
        }

        if (highPriorityTasks.isNotEmpty()) {
            val priorityTask = highPriorityTasks[0]
            val taskDateTime = priorityTask.scheduledDateTime?.toLocalDateTime() 
                ?: priorityTask.dueDateTime?.toLocalDateTime()
            val taskTime = taskDateTime?.toLocalTime()
            
            return when (highPriorityTasks.size) {
                1 -> "🔴 Priority: ${priorityTask.name} (${getDayLetter(taskDateTime!!)} ${taskTime})"
                2 -> "🔴 2 priority: ${highPriorityTasks[0].name} (${getDayLetter(highPriorityTasks[0].scheduledDateTime?.toLocalDateTime()!!)} ${highPriorityTasks[0].scheduledDateTime?.toLocalDateTime()?.toLocalTime()}), ${highPriorityTasks[1].name}"
                else -> "🔴 ${highPriorityTasks.size} priority tasks"
            }
        }

        // Show current task if available
        if (currentTask != null && currentTask.done != true) {
            val taskDateTime = currentTask.scheduledDateTime?.toLocalDateTime() 
                ?: currentTask.dueDateTime?.toLocalDateTime()
            val taskTime = taskDateTime?.toLocalTime()
            return "📝 ${currentTask.name} (${getDayLetter(taskDateTime!!)} ${taskTime})"
        }

        // Show next task if available
        if (nextTask != null) {
            val nextTaskDateTime = nextTask.scheduledDateTime?.toLocalDateTime() 
                ?: nextTask.dueDateTime?.toLocalDateTime()
            
            if (nextTaskDateTime != null) {
                val timeUntilNext = Duration.between(currentTime, nextTaskDateTime)
                val minutesUntilNext = timeUntilNext.toMinutes()
                val timeText = when {
                    timeUntilNext.toHours() > 0 -> "${timeUntilNext.toHours()}h ${timeUntilNext.toMinutesPart()}m"
                    else -> "${timeUntilNext.toMinutes()}m"
                }
                
                return when {
                    minutesUntilNext < 5 -> "⏰ Starting soon: ${nextTask.name} (${getDayLetter(nextTaskDateTime)} ${nextTaskDateTime.toLocalTime()}, in $timeText)"
                    minutesUntilNext < 30 -> "⏳ Next task in ${minutesUntilNext}m: ${nextTask.name} (${getDayLetter(nextTaskDateTime)} ${nextTaskDateTime.toLocalTime()})"
                    else -> "📅 Next task in ${timeUntilNext.toHours()}h: ${nextTask.name} (${getDayLetter(nextTaskDateTime)} ${nextTaskDateTime.toLocalTime()})"
                }
            }
        }

        return "✅ No pending tasks"
    }

    fun getHabitStatusBarText(habits: List<HabitResponse>, currentTime: LocalDateTime): String {
        val habitTimeInfo = calculateHabitTimes(habits, currentTime.toLocalTime())
        val currentHabit = habitTimeInfo.currentItem
        val nextHabit = habitTimeInfo.nextItem

        // Check for overdue habits today
        val todayOverdueHabits = habits.filter { habit ->
            val habitDateTime = habit.dateTime?.toLocalDateTime()
            habitDateTime != null &&
            habitDateTime.toLocalTime().isBefore(currentTime.toLocalTime()) &&
            habit.done != true
        }

        // Show current habit if available
        if (currentHabit != null) {
            val habitDateTime = currentHabit.dateTime?.toLocalDateTime()
            val habitTime = habitDateTime?.toLocalTime()
            val overdueText = if (todayOverdueHabits.isNotEmpty()) {
                " (${todayOverdueHabits.size} overdue today)"
            } else ""
            return "🔄 ${currentHabit.name} (${getDayLetter(habitDateTime!!)} ${habitTime})$overdueText"
        }

        // Show next habit if available
        if (nextHabit != null) {
            val nextHabitDateTime = nextHabit.dateTime?.toLocalDateTime()
            
            if (nextHabitDateTime != null) {
                val timeUntilNext = Duration.between(currentTime, nextHabitDateTime)
                val minutesUntilNext = timeUntilNext.toMinutes()
                val timeText = when {
                    timeUntilNext.toHours() > 0 -> "${timeUntilNext.toHours()}h ${timeUntilNext.toMinutesPart()}m"
                    else -> "${timeUntilNext.toMinutes()}m"
                }
                
                val overdueText = if (todayOverdueHabits.isNotEmpty()) {
                    " (${todayOverdueHabits.size} overdue today)"
                } else ""
                
                return when {
                    minutesUntilNext < 5 -> "⏰ Next habit starting: ${nextHabit.name} (${getDayLetter(nextHabitDateTime)} ${nextHabitDateTime.toLocalTime()}, in $timeText)$overdueText"
                    minutesUntilNext < 30 -> "⏳ Next habit in ${minutesUntilNext}m: ${nextHabit.name} (${getDayLetter(nextHabitDateTime)} ${nextHabitDateTime.toLocalTime()})$overdueText"
                    else -> "📅 Next habit in ${timeUntilNext.toHours()}h: ${nextHabit.name} (${getDayLetter(nextHabitDateTime)} ${nextHabitDateTime.toLocalTime()})$overdueText"
                }
            }
        }

        // If no current or next habit, but there are overdue habits
        if (todayOverdueHabits.isNotEmpty()) {
            return "⚠️ ${todayOverdueHabits.size} overdue habits today"
        }

        return "✨ No habits scheduled"
    }
} 