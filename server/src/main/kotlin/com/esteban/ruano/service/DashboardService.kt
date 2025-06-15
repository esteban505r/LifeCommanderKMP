package com.esteban.ruano.service

import DashboardResponseDTO
import HabitStatsDTO
import TaskStatsDTO
import com.esteban.ruano.models.habits.HabitDTO
import com.esteban.ruano.models.tasks.TaskDTO
import com.esteban.ruano.utils.DateUIUtils.toLocalDateTime
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime as toLocalDateTimeKt

class DashboardService(
    private val taskService: TaskService,
    private val habitService: HabitService,
    private val transactionService: TransactionService,
    private val accountService: AccountService,
    private val nutritionService: NutritionService,
    private val workoutService: WorkoutService,
    private val journalService: DailyJournalService
) {
    suspend fun getDashboardData(userId: Int, dateTime: String): DashboardResponseDTO {
        val currentDateTime = dateTime.toLocalDateTime()
        val today = currentDateTime.date
        val dayOfWeek = today.dayOfWeek.value

        val tasks = taskService.fetchAllByDateRange(
            userId,
            "",
            today,
            today,
            100,
            0
        )
        val habits = habitService.fetchAllByDateRange(
            userId,
            "",
            today,
            today,
            100,
            0
        )

        val nextTask = getNextTask(tasks, currentDateTime)
        val nextHabit = getNextHabit(habits, currentDateTime)
        val taskStats = calculateTaskStats(tasks)
        val habitStats = calculateHabitStats(habits)

        // --- Finance ---
        val recentTransactions = transactionService.getTransactionsByUser(userId, limit = 3).transactions.map {
            // Map TransactionResponseDTO to TransactionDTO (for dashboard)
            com.lifecommander.models.dashboard.TransactionDTO(
                id = it.id,
                title = it.description,
                amount = it.amount,
                date = it.date,
                isIncome = it.amount > 0 // crude check, refine as needed
            )
        }
        val accountBalance = accountService.getTotalBalance(userId)

        // --- Meals ---
        val todayMeals = nutritionService.getRecipesByDay(userId, dayOfWeek)
        val todayCalories = todayMeals.sumOf {  0 }
        val mealsLogged = todayMeals.size
        val nextMeal = todayMeals.firstOrNull { /* TODO: add time field to RecipeDTO if needed */ true }
        val weeklyMealLogging = 0.0f // TODO: Implement weekly meal logging rate

        // --- Workout ---
        val todayWorkout = workoutService.getWorkoutDayById(userId, dayOfWeek)
        val caloriesBurned = todayWorkout.exercises.sumOf {  0 }
        val workoutStreak = 0 // TODO: Implement workout streak
        val weeklyWorkoutCompletion = 0.0f // TODO: Implement weekly workout completion

        // --- Journal ---
        val recentJournalEntries = journalService.getByUserId(userId, limit = 3, offset = 0).map {
            com.lifecommander.models.dashboard.JournalEntryDTO(
                id = it.id,
                date = it.date,
                summary = it.summary ?: ""
            )
        }
        val journalCompleted = recentJournalEntries.any { it.date == today.toString() } // crude check
        val journalStreak = 0 // TODO: Implement journal streak

        // --- Weekly/Monthly Progress ---
        val weeklyTaskCompletion = 0.0f // TODO: Implement weekly task completion
        val weeklyHabitCompletion = 0.0f // TODO: Implement weekly habit completion

        // --- Chart Data ---
        val tasksCompletedPerDayThisWeek = taskService.getTasksCompletedPerDayThisWeek(userId)

        return DashboardResponseDTO(
            nextTask = nextTask,
            nextHabit = nextHabit,
            taskStats = taskStats,
            habitStats = habitStats,
            recentTransactions = recentTransactions,
            accountBalance = accountBalance,
            todayCalories = todayCalories,
            mealsLogged = mealsLogged,
            nextMeal = nextMeal,
            todayWorkout = todayWorkout,
            caloriesBurned = caloriesBurned,
            workoutStreak = workoutStreak,
            journalCompleted = journalCompleted,
            journalStreak = journalStreak,
            recentJournalEntries = recentJournalEntries,
            weeklyTaskCompletion = weeklyTaskCompletion,
            weeklyHabitCompletion = weeklyHabitCompletion,
            weeklyWorkoutCompletion = weeklyWorkoutCompletion,
            weeklyMealLogging = weeklyMealLogging,
            tasksCompletedPerDayThisWeek = tasksCompletedPerDayThisWeek
        )
    }

    private fun getNextTask(tasks: List<TaskDTO>, currentDateTime: LocalDateTime): TaskDTO? {
        return tasks
            .filter { it.done == true }
            .minByOrNull { task ->
                val taskDateTime = (task.dueDateTime ?: task.scheduledDateTime)?.toLocalDateTime()
                    ?: currentDateTime
                if (taskDateTime < currentDateTime) {
                    Clock.System.now().toLocalDateTimeKt(TimeZone.Companion.UTC)
                } else {
                    taskDateTime
                }
            }
    }

    private fun getNextHabit(habits: List<HabitDTO>, currentDateTime: LocalDateTime): HabitDTO? {
        return habits
            .filter { !it.done }
            .minByOrNull { habit ->
                val habitDateTime = habit.dateTime?.toLocalDateTime()
                    ?: currentDateTime
                if (habitDateTime < currentDateTime) {
                    Clock.System.now().toLocalDateTimeKt(TimeZone.Companion.UTC)
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