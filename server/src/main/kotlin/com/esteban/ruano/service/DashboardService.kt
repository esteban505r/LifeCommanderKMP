package com.esteban.ruano.service

import DashboardResponseDTO
import HabitStatsDTO
import TaskStatsDTO
import com.esteban.ruano.database.entities.*
import com.esteban.ruano.database.models.DBActions
import com.esteban.ruano.models.habits.HabitDTO
import com.esteban.ruano.models.tasks.TaskDTO
import com.esteban.ruano.utils.DateUIUtils.toLocalDateTime
import kotlinx.datetime.*
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq

class DashboardService(
    private val taskService: TaskService,
    private val habitService: HabitService,
    private val transactionService: TransactionService,
    private val accountService: AccountService,
    private val nutritionService: NutritionService,
    private val workoutService: WorkoutService,
    private val journalService: DailyJournalService
) {

    fun getDashboardData(userId: Int, dateTime: String): DashboardResponseDTO {
        val currentDateTime = dateTime.toLocalDateTime()
        val today = currentDateTime.date
        val dayOfWeek = today.dayOfWeek.value

        val weekStart = today.minus(DatePeriod(days = dayOfWeek - 1))
        val weekEnd = weekStart.plus(DatePeriod(days = 6))

        val tasks = taskService.fetchAllByDateRange(userId, "", weekStart, weekEnd, 100, 0)
        val habits = habitService.fetchAllByDateRange(userId, "", weekStart, weekEnd, 100, 0)

        val nextTask = getNextTask(tasks, currentDateTime)
        val nextHabit = getNextHabit(habits, currentDateTime)
        val taskStats = calculateTaskStats(tasks)
        val habitStats = calculateHabitStats(habits)

        val recentTransactions = transactionService.getTransactionsByUser(userId, limit = 3).transactions.map {
            com.lifecommander.models.dashboard.TransactionDTO(
                id = it.id,
                title = it.description,
                amount = it.amount,
                date = it.date,
                isIncome = it.amount > 0
            )
        }

        val accountBalance = accountService.getTotalBalance(userId)

        val todayMeals = nutritionService.getRecipesByDay(userId, dayOfWeek)
        val todayCalories = todayMeals.sumOf { 0 }
        val mealsLogged = todayMeals.size
        val nextMeal = todayMeals.firstOrNull()
        val weeklyMealLogging = todayMeals.size / 7f

        val todayWorkout = workoutService.getWorkoutDayById(userId, dayOfWeek)
        val caloriesBurned = todayWorkout.exercises.sumOf { 0 }

        val workoutStreak = calculateWorkoutStreak(userId, today)
        val weeklyWorkoutCompletion = todayWorkout.exercises.size / 7f

        val recentJournalEntries = journalService.getByUserId(userId, limit = 3, offset = 0).map {
            com.lifecommander.models.dashboard.JournalEntryDTO(
                id = it.id,
                date = it.date,
                summary = it.summary ?: ""
            )
        }

        val journalCompleted = recentJournalEntries.any { it.date == today.toString() }
        val journalStreak = calculateJournalStreak(userId, today)

        val weeklyTaskCompletion = calculateTaskCompletionFromTracks(weekStart, TaskTracks.doneDateTime, TaskTrack.Companion::find)
        val weeklyHabitCompletion = calculateHabitCompletionFromTracks(weekStart, HabitTracks.doneDateTime, HabitTrack.Companion::find)

        val tasksCompletedPerDayThisWeek = getTrackCountsPerDay(weekStart, TaskTracks.doneDateTime, TaskTrack.Companion::find)
        val habitsCompletedPerDayThisWeek = getTrackCountsPerDay(weekStart, HabitTracks.doneDateTime, HabitTrack.Companion::find)

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
            tasksCompletedPerDayThisWeek = tasksCompletedPerDayThisWeek,
            habitsCompletedPerDayThisWeek = habitsCompletedPerDayThisWeek,
            workoutsCompletedPerDayThisWeek = emptyList(),
            mealsLoggedPerDayThisWeek = emptyList()
        )
    }

    private fun getTrackCountsPerDay(
        weekStart: LocalDate,
        column: org.jetbrains.exposed.sql.Column<LocalDateTime>,
        finder: (org.jetbrains.exposed.sql.Op<Boolean>) -> Iterable<*>
    ): List<Int> {
        return transaction {
            (0..6).map { i ->
                val day = weekStart.plus(DatePeriod(days = i))
                val start = day.atTime(0, 0)
                val end = day.atTime(23, 59)

                finder(column greaterEq start and (column lessEq end)).count()
            }
        }
    }

    private fun calculateTaskCompletionFromTracks(
        weekStart: LocalDate,
        column: org.jetbrains.exposed.sql.Column<LocalDateTime>,
        finder: (org.jetbrains.exposed.sql.Op<Boolean>) -> Iterable<*>
    ): Float = transaction {
        val start = weekStart.atTime(0, 0)
        val end = weekStart.plus(DatePeriod(days = 6)).atTime(23, 59)
        val total = finder(column greaterEq start and (column lessEq end)).count()
        total / 7f
    }

    private fun calculateHabitCompletionFromTracks(
        weekStart: LocalDate,
        column: org.jetbrains.exposed.sql.Column<LocalDateTime>,
        finder: (org.jetbrains.exposed.sql.Op<Boolean>) -> Iterable<*>
    ): Float = transaction {
        val start = weekStart.atTime(0, 0)
        val end = weekStart.plus(DatePeriod(days = 6)).atTime(23, 59)
        val total = finder(column greaterEq start and (column lessEq end)).count()
        total / 7f
    }

    private fun calculateWorkoutStreak(userId: Int, currentDate: LocalDate): Int {
        var streak = 0
        var checkDate = currentDate
        while (true) {
            val workout = workoutService.getWorkoutDayById(userId, checkDate.dayOfWeek.value)
            if (workout.exercises.isEmpty()) break
            streak++
            checkDate = checkDate.minus(DatePeriod(days = 1))
        }
        return streak
    }

    private fun calculateJournalStreak(userId: Int, currentDate: LocalDate): Int {
        var streak = 0
        var checkDate = currentDate
        while (true) {
            val entries = journalService.getByUserId(userId, limit = 1, offset = 0)
            if (entries.none { it.date == checkDate.toString() }) break
            streak++
            checkDate = checkDate.minus(DatePeriod(days = 1))
        }
        return streak
    }

    private fun getNextTask(tasks: List<TaskDTO>, now: LocalDateTime): TaskDTO? =
        tasks.filter { it.done != true }
            .minByOrNull { (it.dueDateTime ?: it.scheduledDateTime)?.toLocalDateTime() ?: now }

    private fun getNextHabit(habits: List<HabitDTO>, now: LocalDateTime): HabitDTO? =
        habits.filter { !it.done }
            .minByOrNull { it.dateTime?.toLocalDateTime() ?: now }

    private fun calculateTaskStats(tasks: List<TaskDTO>): TaskStatsDTO = TaskStatsDTO(
        total = tasks.size,
        completed = tasks.count { it.done == true },
        highPriority = tasks.count { it.priority > 2 }
    )

    private fun calculateHabitStats(habits: List<HabitDTO>): HabitStatsDTO = HabitStatsDTO(
        total = habits.size,
        completed = habits.count { it.done },
        currentStreak = habits.maxOfOrNull { it.streak } ?: 0
    )
}
