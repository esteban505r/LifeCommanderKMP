package com.esteban.ruano.service

import DashboardResponseDTO
import HabitStatsDTO
import TaskStatsDTO
import com.esteban.ruano.models.habits.HabitDTO
import com.esteban.ruano.models.tasks.TaskDTO
import com.esteban.ruano.utils.DateUIUtils.toLocalDateTime
import kotlinx.datetime.*
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

        // Get data for the current week
        val weekStart = today.minus(DatePeriod(days = today.dayOfWeek.value - 1))
        val weekEnd = weekStart.plus(DatePeriod(days = 6))

        val tasks = taskService.fetchAllByDateRange(
            userId,
            "",
            weekStart,
            weekEnd,
            100,
            0
        )

        val habits = habitService.fetchAllByDateRange(
            userId,
            "",
            weekStart,
            weekEnd,
            100,
            0
        )

        val nextTask = getNextTask(tasks, currentDateTime)
        val nextHabit = getNextHabit(habits, currentDateTime)
        val taskStats = calculateTaskStats(tasks)
        val habitStats = calculateHabitStats(habits)

        // --- Finance ---
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

        // --- Meals ---
        val todayMeals = nutritionService.getRecipesByDay(userId, dayOfWeek)
        val todayCalories = todayMeals.sumOf { 0 }
        val mealsLogged = todayMeals.size
        val nextMeal = todayMeals.firstOrNull { true }
        val weeklyMealLogging = calculateWeeklyMealLogging(userId, weekStart, weekEnd)

        // --- Workout ---
        val todayWorkout = workoutService.getWorkoutDayById(userId, dayOfWeek)
        val caloriesBurned = todayWorkout.exercises.sumOf { 0 }
        val workoutStreak = calculateWorkoutStreak(userId, today)
        val weeklyWorkoutCompletion = calculateWeeklyWorkoutCompletion(userId, weekStart, weekEnd)

        // --- Journal ---
        val recentJournalEntries = journalService.getByUserId(userId, limit = 3, offset = 0).map {
            com.lifecommander.models.dashboard.JournalEntryDTO(
                id = it.id,
                date = it.date,
                summary = it.summary ?: ""
            )
        }
        val journalCompleted = recentJournalEntries.any { it.date == today.toString() }
        val journalStreak = calculateJournalStreak(userId, today)

        // --- Weekly Progress ---
        val weeklyTaskCompletion = calculateWeeklyTaskCompletion(tasks)
        val weeklyHabitCompletion = calculateWeeklyHabitCompletion(habits)

        // --- Chart Data ---
        val tasksCompletedPerDayThisWeek = getTasksCompletedPerDayThisWeek(tasks, weekStart)
        val habitsCompletedPerDayThisWeek = getHabitsCompletedPerDayThisWeek(habits, weekStart)
        val workoutsCompletedPerDayThisWeek = getWorkoutsCompletedPerDayThisWeek(userId, weekStart)
        val mealsLoggedPerDayThisWeek = getMealsLoggedPerDayThisWeek(userId, weekStart)

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
            workoutsCompletedPerDayThisWeek = workoutsCompletedPerDayThisWeek,
            mealsLoggedPerDayThisWeek = mealsLoggedPerDayThisWeek
        )
    }

    private fun getTasksCompletedPerDayThisWeek(tasks: List<TaskDTO>, weekStart: LocalDate): List<Int> {
        val dailyCompletions = MutableList(7) { 0 }
        tasks.forEach { task ->
            if (task.done == true) {
                val taskDate = task.dueDateTime?.toLocalDateTime()?.date ?: task.scheduledDateTime?.toLocalDateTime()?.date
                if (taskDate != null) {
                    val dayIndex = (taskDate - weekStart).days
                    if (dayIndex in 0..6) {
                        dailyCompletions[dayIndex]++
                    }
                }
            }
        }
        return dailyCompletions
    }

    private fun getHabitsCompletedPerDayThisWeek(habits: List<HabitDTO>, weekStart: LocalDate): List<Int> {
        val dailyCompletions = MutableList(7) { 0 }
        habits.forEach { habit ->
            if (habit.done) {
                val habitDate = habit.dateTime?.toLocalDateTime()?.date
                if (habitDate != null) {
                    val dayIndex = (habitDate - weekStart).days
                    if (dayIndex in 0..6) {
                        dailyCompletions[dayIndex]++
                    }
                }
            }
        }
        return dailyCompletions
    }

    private suspend fun getWorkoutsCompletedPerDayThisWeek(userId: Int, weekStart: LocalDate): List<Int> {
        val dailyCompletions = MutableList(7) { 0 }
        for (i in 0..6) {
            val currentDate = weekStart.plus(DatePeriod(days = i))
            val workout = workoutService.getWorkoutDayById(userId, currentDate.dayOfWeek.value)
            if (workout.completed) {
                dailyCompletions[i] = 1
            }
        }
        return dailyCompletions
    }

    private suspend fun getMealsLoggedPerDayThisWeek(userId: Int, weekStart: LocalDate): List<Int> {
        val dailyMeals = MutableList(7) { 0 }
        for (i in 0..6) {
            val currentDate = weekStart.plus(DatePeriod(days = i))
            val meals = nutritionService.getRecipesByDay(userId, currentDate.dayOfWeek.value)
            dailyMeals[i] = meals.size
        }
        return dailyMeals
    }

    private fun calculateWeeklyTaskCompletion(tasks: List<TaskDTO>): Float {
        val completedTasks = tasks.count { it.done == true }
        return if (tasks.isEmpty()) 0f else completedTasks.toFloat() / tasks.size
    }

    private fun calculateWeeklyHabitCompletion(habits: List<HabitDTO>): Float {
        val completedHabits = habits.count { it.done }
        return if (habits.isEmpty()) 0f else completedHabits.toFloat() / habits.size
    }

    private suspend fun calculateWeeklyWorkoutCompletion(userId: Int, weekStart: LocalDate, weekEnd: LocalDate): Float {
        var completedWorkouts = 0
        var totalWorkouts = 0
        for (i in 0..6) {
            val currentDate = weekStart.plus(DatePeriod(days = i))
            val workout = workoutService.getWorkoutDayById(userId, currentDate.dayOfWeek.value)
            if (workout.exercises.isNotEmpty()) {
                totalWorkouts++
                if (workout.completed) {
                    completedWorkouts++
                }
            }
        }
        return if (totalWorkouts == 0) 0f else completedWorkouts.toFloat() / totalWorkouts
    }

    private suspend fun calculateWeeklyMealLogging(userId: Int, weekStart: LocalDate, weekEnd: LocalDate): Float {
        var totalMeals = 0
        var loggedMeals = 0
        for (i in 0..6) {
            val currentDate = weekStart.plus(DatePeriod(days = i))
            val meals = nutritionService.getRecipesByDay(userId, currentDate.dayOfWeek.value)
            if (meals.isNotEmpty()) {
                totalMeals += meals.size
                loggedMeals += meals.count { it.logged }
            }
        }
        return if (totalMeals == 0) 0f else loggedMeals.toFloat() / totalMeals
    }

    private suspend fun calculateWorkoutStreak(userId: Int, currentDate: LocalDate): Int {
        var streak = 0
        var checkDate = currentDate
        while (true) {
            val workout = workoutService.getWorkoutDayById(userId, checkDate.dayOfWeek.value)
            if (!workout.completed) break
            streak++
            checkDate = checkDate.minus(DatePeriod(days = 1))
        }
        return streak
    }

    private suspend fun calculateJournalStreak(userId: Int, currentDate: LocalDate): Int {
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