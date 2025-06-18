package com.lifecommander.models.dashboard

import com.lifecommander.models.Habit
import com.lifecommander.models.Task
import kotlinx.serialization.Serializable

@Serializable
data class DashboardResponse(
    val nextTask: Task?,
    val nextHabit: Habit?,
    val taskStats: TaskStats,
    val habitStats: HabitStats,
    val overdueTasks: Int = 0,
    val overdueHabits: Int = 0,
    val overdueTasksList: List<Task> = emptyList(),
    val overdueHabitsList: List<Habit> = emptyList(),
    // Finance
    val recentTransactions: List<TransactionDTO>? = emptyList(),
    val accountBalance: Double = 0.0,
    // Meals
    val todayCalories: Int = 0,
    val mealsLogged: Int = 0,
    val nextMeal: MealDTO? = null,
    // Workout
    val todayWorkout: WorkoutDTO? = null,
    val caloriesBurned: Int = 0,
    val workoutStreak: Int = 0,
    // Journal
    val journalCompleted: Boolean = false,
    val journalStreak: Int = 0,
    val recentJournalEntries: List<JournalEntryDTO>? = emptyList(),
    // Weekly/Monthly Progress
    val weeklyTaskCompletion: Float = 0f,
    val weeklyHabitCompletion: Float = 0f,
    val weeklyWorkoutCompletion: Float = 0f,
    val weeklyMealLogging: Float = 0f,
    // Daily completion data for charts
    val tasksCompletedPerDayThisWeek: List<Int>? = emptyList(),
    val habitsCompletedPerDayThisWeek: List<Int>? = emptyList(),
    val workoutsCompletedPerDayThisWeek: List<Int>? = emptyList(),
    val mealsLoggedPerDayThisWeek: List<Int>? = emptyList()
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

@Serializable
data class TransactionDTO(
    val id: String,
    val title: String,
    val amount: Double,
    val date: String,
    val isIncome: Boolean
)

@Serializable
data class MealDTO(
    val id: String,
    val name: String,
    val calories: Int,
    val time: String
)

@Serializable
data class WorkoutDTO(
    val id: String,
    val name: String,
    val duration: Int, // in minutes
    val caloriesBurned: Int
)

@Serializable
data class JournalEntryDTO(
    val id: String,
    val date: String,
    val summary: String
) 