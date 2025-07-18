package com.esteban.ruano.lifecommander.utils

import com.lifecommander.models.Habit
import com.esteban.ruano.utils.TimeBasedItemInfo
import com.esteban.ruano.utils.TimeBasedUtils
import com.esteban.ruano.utils.TimeFormatUtils
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.datetime.toKotlinLocalTime
import com.esteban.ruano.utils.DateUIUtils.toLocalDateTime
import com.esteban.ruano.utils.DateUIUtils.toLocalTime
import com.lifecommander.models.Task
import java.time.LocalDateTime
import java.time.LocalTime as JavaLocalTime

data class StatusBarInfo(
    val title: String,
    val subtitle: String? = null,
    val isUrgent: Boolean = false,
    val isOverdue: Boolean = false
)

object TimeBasedItemUtils {
    // Convenience function for habits
    fun calculateHabitTimes(habits: List<Habit>, currentTime: JavaLocalTime): TimeBasedItemInfo<Habit> {
        return TimeBasedUtils.calculateItemTimes(habits, currentTime.toKotlinLocalTime()) { habit ->
            habit.dateTime?.toLocalDateTime()?.toLocalTime()
        }
    }

    // Convenience function for tasks
    fun calculateTaskTimes(tasks: List<Task>, currentTime: JavaLocalTime): TimeBasedItemInfo<Task> {
        return TimeBasedUtils.calculateItemTimes(tasks, currentTime.toKotlinLocalTime()) { task ->
            // Prioritize scheduled time over due time
            task.scheduledDateTime?.toLocalDateTime()?.toLocalTime()
                ?: task.dueDateTime?.toLocalDateTime()?.toLocalTime()
        }
    }

    fun getTaskStatusBarText(tasks: List<Task>, currentTime: LocalDateTime): String {
        val taskTimeInfo = calculateTaskTimes(tasks, currentTime.toLocalTime())
        val currentTask = taskTimeInfo.currentItem
        val nextTask = taskTimeInfo.nextItem

        // Check for overdue tasks
        val overdueTasks = tasks.filter { task ->
            task.done != true && task.dueDateTime?.toLocalDateTime()?.let { it < currentTime.toKotlinLocalDateTime() } == true
        }

        if (overdueTasks.isNotEmpty()) {
            val overdueTask = overdueTasks[0]
            val overdueTime = overdueTask.dueDateTime?.toLocalDateTime()
            val timeOverdue = overdueTime?.let { 
                TimeFormatUtils.calculateTimeDifference(
                    currentTime.toLocalTime().toKotlinLocalTime(),
                    it.toJavaLocalDateTime().toLocalTime().toKotlinLocalTime()
                )
            }
            val overdueText = TimeFormatUtils.formatDuration(timeOverdue)
            
            return when (overdueTasks.size) {
                1 -> "⚠️ Overdue: ${overdueTask.name} (${TimeFormatUtils.getDayLetter(overdueTime!!)} ${overdueTime.toJavaLocalDateTime()}, +$overdueText)"
                2 -> "⚠️ 2 overdue: ${overdueTasks[0].name} (${TimeFormatUtils.getDayLetter(overdueTasks[0].dueDateTime!!.toLocalDateTime())} ${overdueTasks[0].dueDateTime?.toLocalDateTime()?.toLocalTime()}, +${TimeFormatUtils.calculateTimeDifference(currentTime.toLocalTime().toKotlinLocalTime(), 
                    overdueTasks[0].dueDateTime?.toLocalDateTime()?.toLocalTime()!!).inWholeHours}h), ${overdueTasks[1].name}"
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
                1 -> "🔴 Priority: ${priorityTask.name} (${TimeFormatUtils.getDayLetter(taskDateTime)} ${taskTime})"
                2 -> "🔴 2 priority: ${highPriorityTasks[0].name} (${TimeFormatUtils.getDayLetter(highPriorityTasks[0].scheduledDateTime?.toLocalDateTime())} ${highPriorityTasks[0].scheduledDateTime?.toLocalDateTime()?.toLocalTime()}), ${highPriorityTasks[1].name}"
                else -> "🔴 ${highPriorityTasks.size} priority tasks"
            }
        }

        // Show current task if available
        if (currentTask != null && currentTask.done != true) {
            val taskDateTime = currentTask.scheduledDateTime?.toLocalDateTime() 
                ?: currentTask.dueDateTime?.toLocalDateTime()
            val taskTime = taskDateTime?.toLocalTime()
            return "📝 ${currentTask.name} (${TimeFormatUtils.getDayLetter(taskDateTime)} ${taskTime})"
        }

        // Show next task if available
        if (nextTask != null) {
            val nextTaskDateTime = nextTask.scheduledDateTime?.toLocalDateTime() 
                ?: nextTask.dueDateTime?.toLocalDateTime()
            
            if (nextTaskDateTime != null) {
                val timeUntilNext = taskTimeInfo.timeRemaining
                val minutesUntilNext = timeUntilNext?.inWholeMinutes ?: 0
                val timeText = TimeFormatUtils.formatDuration(timeUntilNext)
                
                return when {
                    minutesUntilNext < 5 -> "⏰ Starting soon: ${nextTask.name} (${TimeFormatUtils.getDayLetter(nextTaskDateTime)} ${nextTaskDateTime.toLocalTime()}, in $timeText)"
                    minutesUntilNext < 30 -> "⏳ Next task in ${minutesUntilNext}m: ${nextTask.name} (${TimeFormatUtils.getDayLetter(nextTaskDateTime)} ${nextTaskDateTime.toLocalTime()})"
                    else -> "📅 Next task in ${timeUntilNext?.inWholeHours}h: ${nextTask.name} (${TimeFormatUtils.getDayLetter(nextTaskDateTime)} ${nextTaskDateTime.toLocalTime()})"
                }
            }
        }

        return "✅ No pending tasks"
    }

    fun getHabitStatusBarText(habits: List<Habit>, currentTime: LocalDateTime): String {
        val habitTimeInfo = calculateHabitTimes(habits, currentTime.toLocalTime())
        val currentHabit = habitTimeInfo.currentItem
        val nextHabit = habitTimeInfo.nextItem

        // Check for overdue habits today
        val todayOverdueHabits = habits.filter { habit ->
            val habitDateTime = habit.dateTime?.toLocalDateTime()
            habitDateTime != null &&
            TimeFormatUtils.isTimeBefore(habitDateTime.toLocalTime(), currentTime.toLocalTime().toKotlinLocalTime()) &&
            habit.done != true
        }

        // Show current habit if available
        if (currentHabit != null) {
            val habitDateTime = currentHabit.dateTime?.toLocalDateTime()
            val habitTime = habitDateTime?.toLocalTime()
            val overdueText = if (todayOverdueHabits.isNotEmpty()) {
                " (${todayOverdueHabits.size} overdue today)"
            } else ""
            return "🔄 ${currentHabit.name} (${TimeFormatUtils.getDayLetter(habitDateTime)} ${habitTime})$overdueText"
        }

        // Show next habit if available
        if (nextHabit != null) {
            val nextHabitDateTime = nextHabit.dateTime?.toLocalDateTime()
            
            if (nextHabitDateTime != null) {
                val timeUntilNext = habitTimeInfo.timeRemaining
                val minutesUntilNext = timeUntilNext?.inWholeMinutes ?: 0
                val timeText = TimeFormatUtils.formatDuration(timeUntilNext)
                
                val overdueText = if (todayOverdueHabits.isNotEmpty()) {
                    " (${todayOverdueHabits.size} overdue today)"
                } else ""
                
                return when {
                    minutesUntilNext < 5 -> "⏰ Next habit starting: ${nextHabit.name} (${TimeFormatUtils.getDayLetter(nextHabitDateTime)} ${nextHabitDateTime.toLocalTime()}, in $timeText)$overdueText"
                    minutesUntilNext < 30 -> "⏳ Next habit in ${minutesUntilNext}m: ${nextHabit.name} (${TimeFormatUtils.getDayLetter(nextHabitDateTime)} ${nextHabitDateTime.toLocalTime()})$overdueText"
                    else -> "📅 Next habit in ${timeUntilNext?.inWholeHours}h: ${nextHabit.name} (${TimeFormatUtils.getDayLetter(nextHabitDateTime)} ${nextHabitDateTime.toLocalTime()})$overdueText"
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