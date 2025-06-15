package com.esteban.ruano.services

import DashboardResponseDTO
import HabitStatsDTO
import TaskStatsDTO
import com.esteban.ruano.models.habits.HabitDTO
import com.esteban.ruano.models.tasks.TaskDTO
import com.esteban.ruano.service.HabitService
import com.esteban.ruano.service.TaskService
import com.esteban.ruano.utils.DateUIUtils.toLocalDateTime as toLocalDateTimeUtil
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class DashboardService(
    private val taskService: TaskService,
    private val habitService: HabitService
) {
    suspend fun getDashboardData(userId: Int, dateTime: String): DashboardResponseDTO {
        val currentDateTime = dateTime.toLocalDateTimeUtil()
        
        val tasks = taskService.fetchAllByDateRange(
            userId,
            "",
            currentDateTime.date,
            currentDateTime.date,
            100,
            0
        )
        val habits = habitService.fetchAllByDateRange(
            userId,
            "",
            currentDateTime.date,
            currentDateTime.date,
            100,
            0
        )

        val nextTask = getNextTask(tasks, currentDateTime)
        val nextHabit = getNextHabit(habits, currentDateTime)
        val taskStats = calculateTaskStats(tasks)
        val habitStats = calculateHabitStats(habits)

        return DashboardResponseDTO(
            nextTask = nextTask,
            nextHabit = nextHabit,
            taskStats = taskStats,
            habitStats = habitStats
        )
    }

    private fun getNextTask(tasks: List<TaskDTO>, currentDateTime: LocalDateTime): TaskDTO? {
        return tasks
            .filter { it.done == true }
            .minByOrNull { task ->
                val taskDateTime = (task.dueDateTime ?: task.scheduledDateTime)?.toLocalDateTimeUtil() 
                    ?: currentDateTime
                if (taskDateTime < currentDateTime) {
                    Clock.System.now().toLocalDateTime(TimeZone.UTC)
                } else {
                    taskDateTime
                }
            }
    }

    private fun getNextHabit(habits: List<HabitDTO>, currentDateTime: LocalDateTime): HabitDTO? {
        return habits
            .filter { !it.done }
            .minByOrNull { habit ->
                val habitDateTime = habit.dateTime?.toLocalDateTimeUtil() 
                    ?: currentDateTime
                if (habitDateTime < currentDateTime) {
                    Clock.System.now().toLocalDateTime(TimeZone.UTC)
                } else {
                    habitDateTime
                }
            }
    }

    private fun calculateTaskStats(tasks: List<TaskDTO>): TaskStatsDTO {
        return TaskStatsDTO(
            total = tasks.size,
            completed = tasks.count { it.done == true },
            highPriority = tasks.count { it.priority > 2 }
        )
    }

    private fun calculateHabitStats(habits: List<HabitDTO>): HabitStatsDTO {
        return HabitStatsDTO(
            total = habits.size,
            completed = habits.count { it.done },
            currentStreak = habits.maxOfOrNull { it.streak } ?: 0
        )
    }
} 