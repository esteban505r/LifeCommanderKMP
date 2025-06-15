package com.lifecommander.models.dashboard

import com.lifecommander.models.Habit
import com.lifecommander.models.Task
import kotlinx.serialization.Serializable

@Serializable
data class DashboardResponse(
    val nextTask: Task?,
    val nextHabit: Habit?,
    val taskStats: TaskStats,
    val habitStats: HabitStats
)

@Serializable
data class TaskStats(
    val total: Int,
    val completed: Int,
    val highPriority: Int
)

@Serializable
data class HabitStats(
    val total: Int,
    val completed: Int,
    val currentStreak: Int
) 