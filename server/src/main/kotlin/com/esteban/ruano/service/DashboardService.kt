package com.esteban.ruano.service

import DashboardResponseDTO
import HabitStatsDTO
import TaskStatsDTO
import com.esteban.ruano.database.entities.*
import com.esteban.ruano.database.models.Status
import com.esteban.ruano.models.habits.HabitDTO
import com.esteban.ruano.models.tasks.TaskDTO
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.esteban.ruano.utils.DateUIUtils.toLocalDateTime
import kotlinx.datetime.*
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class DashboardService(
    private val taskService: TaskService,
    private val habitService: HabitService,
    private val transactionService: TransactionService,
    private val accountService: AccountService,
    private val nutritionService: NutritionService,
    private val workoutService: WorkoutService,
    private val journalService: DailyJournalService
) {

    // Data class for habit occurrence tracking
    private data class HabitOccurrence(
        val habit: HabitDTO,
        val nextDate: LocalDate,
        val effectiveTime: LocalTime,
        val baseTime: LocalTime?,
        val nextOccurrenceDateTime: LocalDateTime
    )

    fun getDashboardData(userId: Int, dateTime: String): DashboardResponseDTO {
        val currentDateTime = dateTime.toLocalDateTime()
        val today = currentDateTime.date
        val dayOfWeek = today.dayOfWeek.ordinal + 1

        val weekStart = today.minus(DatePeriod(days = dayOfWeek - 1))
        val weekEnd = weekStart.plus(DatePeriod(days = 6))

        val tasks = taskService.fetchAllByDateRange(userId, "", weekStart, weekEnd, 100, 0)
        
        // Get all habits (not filtered by date range) to properly calculate overdue
        val allHabits = habitService.fetchAll(userId, "", 100, 0)
        val habits = habitService.fetchAllByDateRange(userId, "", weekStart, weekEnd, 100, 0)
        
        println("=== DASHBOARD DATA DEBUG ===")
        println("Current time: $currentDateTime")
        println("Week range: $weekStart to $weekEnd")
        println("All habits: ${allHabits.size}")
        println("Filtered habits: ${habits.size}")
        allHabits.forEach { habit ->
            println("  All Habit: ${habit.name} (${habit.frequency}) - Done: ${habit.done} - DateTime: ${habit.dateTime}")
        }
        println("=== END DASHBOARD DATA DEBUG ===")

        val nextTask = getNextTask(tasks, currentDateTime)
        val nextHabit = getNextHabit(habits, currentDateTime)
        val taskStats = calculateTaskStats(tasks)
        val habitStats = calculateHabitStats(habits)
        val overdueTasks = calculateOverdueTasks(tasks, currentDateTime)
        val overdueHabits = calculateOverdueHabits(allHabits, currentDateTime)
        val overdueTasksList = getOverdueTasksList(tasks, currentDateTime)
        val overdueHabitsList = getOverdueHabitsList(habits, currentDateTime)

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
        val unexpectedMeals = nutritionService.getRecipesNotAssignedToDay(userId, "", 100, 0)
        val allTodayMeals = todayMeals + unexpectedMeals
        val todayCalories = allTodayMeals.sumOf { 0 }
        val mealsLogged = allTodayMeals.size
        val nextMeal = allTodayMeals.firstOrNull()
        val weeklyMealLogging = allTodayMeals.size / 7f

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
        val weeklyRecipeCompletion = calculateRecipeCompletionFromTracks(weekStart, RecipeTracks.consumedDateTime, RecipeTrack.Companion::find)

        val tasksCompletedPerDayThisWeek = getTrackCountsPerDay(weekStart, TaskTracks.doneDateTime, TaskTrack.Companion::find)
        val habitsCompletedPerDayThisWeek = getTrackCountsPerDay(weekStart, HabitTracks.doneDateTime, HabitTrack.Companion::find)
        val recipesConsumedPerDayThisWeek = getTrackCountsPerDay(weekStart, RecipeTracks.consumedDateTime, RecipeTrack.Companion::find)
        val workoutsCompletedPerDayThisWeek = getWorkoutTrackCountsPerDay(userId, weekStart)
        
        // Calculate planned vs unexpected meals per day
        val plannedMealsPerDayThisWeek = getPlannedMealsPerDay(userId, weekStart)
        val unexpectedMealsPerDayThisWeek = getUnexpectedMealsByDateRange(userId, weekStart,weekEnd)

        return DashboardResponseDTO(
            nextTask = nextTask,
            nextHabit = nextHabit,
            taskStats = taskStats,
            habitStats = habitStats,
            overdueTasks = overdueTasks,
            overdueHabits = overdueHabits,
            overdueTasksList = overdueTasksList,
            overdueHabitsList = overdueHabitsList,
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
            weeklyMealLogging = weeklyRecipeCompletion,
            tasksCompletedPerDayThisWeek = tasksCompletedPerDayThisWeek,
            habitsCompletedPerDayThisWeek = habitsCompletedPerDayThisWeek,
            workoutsCompletedPerDayThisWeek = workoutsCompletedPerDayThisWeek,
            mealsLoggedPerDayThisWeek = recipesConsumedPerDayThisWeek,
            plannedMealsPerDayThisWeek = plannedMealsPerDayThisWeek,
            unexpectedMealsPerDayThisWeek = unexpectedMealsPerDayThisWeek
        )
    }

    private fun getTrackCountsPerDay(
        weekStart: LocalDate,
        column: Column<LocalDateTime>,
        finder: (Op<Boolean>) -> Iterable<*>
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
        column: Column<LocalDateTime>,
        finder: (Op<Boolean>) -> Iterable<*>
    ): Float = transaction {
        val start = weekStart.atTime(0, 0)
        val end = weekStart.plus(DatePeriod(days = 6)).atTime(23, 59)
        val total = finder(column greaterEq start and (column lessEq end)).count()
        total / 7f
    }

    private fun calculateHabitCompletionFromTracks(
        weekStart: LocalDate,
        column: Column<LocalDateTime>,
        finder: (Op<Boolean>) -> Iterable<*>
    ): Float = transaction {
        val start = weekStart.atTime(0, 0)
        val end = weekStart.plus(DatePeriod(days = 6)).atTime(23, 59)
        val total = finder(column greaterEq start and (column lessEq end)).count()
        total / 7f
    }

    private fun calculateRecipeCompletionFromTracks(
        weekStart: LocalDate,
        column: Column<LocalDateTime>,
        finder: (Op<Boolean>) -> Iterable<*>
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
            val workout = workoutService.getWorkoutDayById(userId, checkDate.dayOfWeek.ordinal)
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

    private fun getNextTask(tasks: List<TaskDTO>, now: LocalDateTime): TaskDTO? {
        val pendingTasks = tasks.filter { it.done != true }
        
        if (pendingTasks.isEmpty()) return null
        
        // Prioritize tasks by:
        // 1. Overdue tasks first (by priority)
        // 2. Today's tasks (by priority, then by time)
        // 3. Future tasks (by priority, then by date/time)
        
        val today = now.date
        val currentTime = now.time
        
        // Separate tasks into categories
        val overdueTasks = pendingTasks.filter { task ->
            val dueDate = (task.dueDateTime ?: task.scheduledDateTime)?.toLocalDateTime()?.date
            dueDate != null && dueDate < today
        }
        
        val todaysTasks = pendingTasks.filter { task ->
            val dueDate = (task.dueDateTime ?: task.scheduledDateTime)?.toLocalDateTime()?.date
            dueDate == today
        }
        
        val futureTasks = pendingTasks.filter { task ->
            val dueDate = (task.dueDateTime ?: task.scheduledDateTime)?.toLocalDateTime()?.date
            dueDate != null && dueDate > today
        }
        
        // Return overdue task with highest priority
        if (overdueTasks.isNotEmpty()) {
            return overdueTasks.maxByOrNull { it.priority }
        }
        
        // Return today's task with highest priority, then earliest time
        if (todaysTasks.isNotEmpty()) {
            val highPriorityTasks = todaysTasks.filter { it.priority >= 4 } // High priority threshold
            if (highPriorityTasks.isNotEmpty()) {
                return highPriorityTasks.minByOrNull { 
                    (it.dueDateTime ?: it.scheduledDateTime)?.toLocalDateTime()?.time ?: currentTime 
                }
            }
            
            // Return by priority first, then by time
            return todaysTasks.maxByOrNull { it.priority }?.let { highPriorityTask ->
                val samePriorityTasks = todaysTasks.filter { it.priority == highPriorityTask.priority }
                samePriorityTasks.minByOrNull { 
                    (it.dueDateTime ?: it.scheduledDateTime)?.toLocalDateTime()?.time ?: currentTime 
                }
            }
        }
        
        // Return future task with highest priority, then earliest date
        if (futureTasks.isNotEmpty()) {
            return futureTasks.maxByOrNull { it.priority }?.let { highPriorityTask ->
                val samePriorityTasks = futureTasks.filter { it.priority == highPriorityTask.priority }
                samePriorityTasks.minByOrNull { 
                    (it.dueDateTime ?: it.scheduledDateTime)?.toLocalDateTime() ?: now 
                }
            }
        }
        
        return null
    }

    fun getNextHabit(habits: List<HabitDTO>, now: LocalDateTime): HabitDTO? {
        println("=== NEXT HABIT ===")
        println("Total habits: ${habits.size}")
        
        val pendingHabits = habits.filter { !it.done }
        println("Pending habits: ${pendingHabits.size}")
        
        if (pendingHabits.isEmpty()) return null
        
        val today = now.date
        val currentTime = now.time
        
        // Calculate next occurrences for ALL pending habits (not just non-overdue ones)
        val habitsWithNextOccurrence = pendingHabits.map { habit ->
            val baseDateTime = habit.dateTime?.toLocalDateTime()
            val baseDate = baseDateTime?.date ?: today
            val baseTime = baseDateTime?.time ?: LocalTime(9, 0)
            
            val nextOccurrence = when (habit.frequency.uppercase()) {
                "DAILY" -> if(baseTime > currentTime) {
                    today
                } else {
                    today.plus(DatePeriod(days = 1))
                }
                "WEEKLY" -> {
                    val targetDayOfWeek = baseDate.dayOfWeek
                    if (today.dayOfWeek == targetDayOfWeek) today else {
                        var nextDate = today
                        while (nextDate.dayOfWeek != targetDayOfWeek) {
                            nextDate = nextDate.plus(DatePeriod(days = 1))
                        }
                        nextDate
                    }
                }
                "MONTHLY" -> {
                    val targetDayOfMonth = baseDate.dayOfMonth
                    if (today.dayOfMonth == targetDayOfMonth) today else {
                        var nextDate = today
                        while (nextDate.dayOfMonth != targetDayOfMonth) {
                            nextDate = nextDate.plus(DatePeriod(days = 1))
                        }
                        nextDate
                    }
                }
                "YEARLY" -> {
                    val targetDayOfYear = baseDate.dayOfYear
                    if (today.dayOfYear == targetDayOfYear) today else {
                        var nextDate = today
                        while (nextDate.dayOfYear != targetDayOfYear) {
                            nextDate = nextDate.plus(DatePeriod(days = 1))
                        }
                        nextDate
                    }
                }
                else -> baseDate
            }
            
            val nextOccurrenceDateTime = LocalDateTime(nextOccurrence, baseTime)
            HabitOccurrence(habit, nextOccurrence, baseTime, baseTime, nextOccurrenceDateTime)
        }
        
        if (habitsWithNextOccurrence.isEmpty()) {
            println("No next occurrences calculated")
            return pendingHabits.firstOrNull()
        }
        
        // Filter out habits that are overdue for their calculated next occurrence
        val nonOverdueHabitsWithOccurrence = habitsWithNextOccurrence.filter { occurrence ->
            val isOverdue = now > occurrence.nextOccurrenceDateTime
            if (isOverdue) {
                println("Filtering out overdue habit: ${occurrence.habit.name} (next occurrence: ${occurrence.nextOccurrenceDateTime})")
            }
            !isOverdue
        }
        
        println("Non-overdue habits with occurrence: ${nonOverdueHabitsWithOccurrence.size}")
        
        if (nonOverdueHabitsWithOccurrence.isEmpty()) {
            println("No non-overdue habits found")
            return null
        }
        
        // Categorize habits
        val todaysHabits = nonOverdueHabitsWithOccurrence.filter { it.nextDate == today }
        val futureHabits = nonOverdueHabitsWithOccurrence.filter { it.nextDate > today }
        
        val futureTimesToday = todaysHabits.filter { it.effectiveTime >= currentTime }.sortedBy { it.effectiveTime }
        val pastTimesToday = todaysHabits.filter { it.effectiveTime < currentTime }.sortedBy { it.effectiveTime }
        
        println("Today's habits: ${todaysHabits.size} (future: ${futureTimesToday.size}, past: ${pastTimesToday.size})")
        println("Future habits: ${futureHabits.size}")
        
        val result = when {
            futureTimesToday.isNotEmpty() -> futureTimesToday.first().habit
            pastTimesToday.isNotEmpty() -> pastTimesToday.first().habit
            futureHabits.isNotEmpty() -> futureHabits.minByOrNull { it.nextOccurrenceDateTime }?.habit
            else -> nonOverdueHabitsWithOccurrence.firstOrNull()?.habit
        }
        
        println("Selected next habit: ${result?.name}")
        println("=== END NEXT HABIT ===")
        return result
    }

    private fun calculateOverdueTasks(tasks: List<TaskDTO>, now: LocalDateTime): Int {
        val today = now.date
        return tasks.count { task ->
            task.done != true && // Not completed
            (task.dueDateTime?.toLocalDateTime()?.date ?: task.scheduledDateTime?.toLocalDateTime()?.date)?.let { dueDate ->
                dueDate < today
            } ?: false
        }
    }

    private fun calculateOverdueHabits(habits: List<HabitDTO>, now: LocalDateTime): Int {
        return getOverdueHabitsList(habits, now).size
    }

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

    private fun getWorkoutTrackCountsPerDay(userId: Int, weekStart: LocalDate): List<Int> {
        return transaction {
            (0..6).map { i ->
                val day = weekStart.plus(DatePeriod(days = i))
                val workout = workoutService.getWorkoutDaysByDay(userId, day.dayOfWeek.ordinal+1,day.atTime(0,0).formatDefault())
                workout.firstOrNull()?.isCompleted.let { if(it == true) 1 else 0 }
            }
        }
    }

    private fun getPlannedMealsPerDay(userId: Int, weekStart: LocalDate): List<Int> {
        return transaction {
            (0..6).map { i ->
                val day = weekStart.plus(DatePeriod(days = i))
                val dayOfWeek = day.dayOfWeek.ordinal + 1
                
                // Get recipe IDs that are assigned to this day of the week
                val recipeIds = RecipeDay.find {
                    (RecipeDays.user eq userId) and
                    (RecipeDays.day eq dayOfWeek) and
                    (RecipeDays.status eq Status.ACTIVE)
                }.map { it.recipe.id }
                
                // Get the recipes and count those that have been consumed on this specific day
                val recipes = Recipe.find {
                    (Recipes.id inList recipeIds) and
                    (Recipes.status eq Status.ACTIVE)
                }.toList()
                
                recipes.count { recipe ->
                    // Check if there is a RecipeTrack for this recipe on this day (not skipped)
                    RecipeTrack.find {
                        (RecipeTracks.recipeId eq recipe.id.value) and
                        (RecipeTracks.consumedDateTime greaterEq day.atTime(0, 0)) and
                        (RecipeTracks.consumedDateTime lessEq day.atTime(23, 59)) and
                        (RecipeTracks.status eq Status.ACTIVE) and
                        (RecipeTracks.skipped eq false)
                    }.any()
                }
            }
        }
    }

    private fun getUnexpectedMealsByDateRange(userId: Int, startDate: LocalDate, endDate: LocalDate): List<Int> {
        return transaction {
            val unExpectedRecipeIds = RecipeDay.find {
                (RecipeDays.user eq userId) and
                (RecipeDays.status eq Status.ACTIVE) and
                (RecipeDays.day greaterEq startDate.dayOfWeek.ordinal) and
                (RecipeDays.day lessEq endDate.dayOfWeek.ordinal)
            }.map { it.recipe.id }

            val unexpectedMeals = RecipeTrack.find {
                (RecipeTracks.recipeId notInList unExpectedRecipeIds) and
                (RecipeTracks.consumedDateTime greaterEq startDate.atTime(0, 0)) and
                (RecipeTracks.consumedDateTime lessEq endDate.atTime(23, 59)) and
                (RecipeTracks.status eq Status.ACTIVE) and
                (RecipeTracks.skipped eq true)
            }

            val range = List(7) { i ->
                startDate.plus(DatePeriod(days = i))
            }

            val result = (range).map { date ->
                unexpectedMeals.count { track ->
                    track.consumedDateTime.date == date
                }
            }

            result
        }
    }

    private fun getOverdueTasksList(tasks: List<TaskDTO>, now: LocalDateTime): List<TaskDTO> {
        val today = now.date
        return tasks.filter { task ->
            task.done != true && // Not completed
            (task.dueDateTime?.toLocalDateTime()?.date ?: task.scheduledDateTime?.toLocalDateTime()?.date)?.let { dueDate ->
                dueDate < today
            } ?: false
        }
    }

    fun getOverdueHabitsList(habits: List<HabitDTO>, now: LocalDateTime): List<HabitDTO> {
        println("=== OVERDUE HABITS DEBUG ===")
        println("Time: $now, Total habits: ${habits.size}")
        
        val today = now.date
        val currentTime = now.time
        println("Today: $today, Current time: $currentTime")
        
        val overdueHabits = habits.filter { habit ->
            // Skip habits that are already done
            if (habit.done) {
                println("Skipping ${habit.name} - already done")
                return@filter false
            }
            
            val baseDateTime = habit.dateTime?.toLocalDateTime()
            if (baseDateTime == null) {
                println("Skipping ${habit.name} - no base datetime")
                return@filter false
            }
            
            val baseDate = baseDateTime.date
            val baseTime = baseDateTime.time
            
            println("Checking: ${habit.name} (${habit.frequency}) - Base: $baseDate $baseTime")
            
            // Check if this habit should be overdue based on its frequency and base date/time
            val shouldBeOverdue = when (habit.frequency.uppercase()) {
                "DAILY" -> {
                    // For daily habits, calculate the next occurrence and check if it's overdue
                    val nextOccurrenceDate = if (baseDate <= today) today else baseDate
                    val nextOccurrenceDateTime = LocalDateTime(nextOccurrenceDate, baseTime)
                    
                    // Daily habit is overdue if the next occurrence has passed
                    val result = now > nextOccurrenceDateTime
                    println("  DAILY: next occurrence: $nextOccurrenceDateTime, has passed: $result")
                    result
                }
                "WEEKLY" -> {
                    val targetDayOfWeek = baseDate.dayOfWeek
                    
                    // If the base date is in the future, the habit is not overdue
                    if (baseDate > today) {
                        println("  WEEKLY: base date is in the future")
                        false
                    } else if (today.dayOfWeek == targetDayOfWeek) {
                        // If today is the target day, check if time has passed
                        val timeOverdue = currentTime > baseTime
                        println("  WEEKLY: today is target day, time overdue: $timeOverdue")
                        timeOverdue
                    } else {
                        // Check if we've passed the target day this week
                        val todayDayOfWeek = today.dayOfWeek.ordinal + 1
                        val targetDayOfWeekValue = targetDayOfWeek.ordinal + 1
                        
                        println("  WEEKLY: today day of week: $todayDayOfWeek, target day of week: $targetDayOfWeekValue")
                        
                        if (todayDayOfWeek > targetDayOfWeekValue) {
                            // We've passed the target day this week, calculate the last occurrence
                            val daysSinceTarget = todayDayOfWeek - targetDayOfWeekValue
                            val lastOccurrenceDate = today.minus(DatePeriod(days = daysSinceTarget))
                            val lastOccurrenceDateTime = LocalDateTime(lastOccurrenceDate, baseTime)
                            
                            // Check if the last occurrence has passed
                            val result = now > lastOccurrenceDateTime
                            println("  WEEKLY: last occurrence was $lastOccurrenceDateTime, has passed: $result")
                            result
                        } else {
                            // Target day is in the future this week
                            println("  WEEKLY: target day is in the future this week")
                            false
                        }
                    }
                }
                "MONTHLY" -> {
                    val targetDayOfMonth = baseDate.dayOfMonth
                    
                    // If the base date is in the future, the habit is not overdue
                    if (baseDate > today) {
                        println("  MONTHLY: base date is in the future")
                        false
                    } else if (today.dayOfMonth == targetDayOfMonth) {
                        // If today is the target day, check if time has passed
                        val timeOverdue = currentTime > baseTime
                        println("  MONTHLY: today is target day, time overdue: $timeOverdue")
                        timeOverdue
                    } else if (today.dayOfMonth > targetDayOfMonth) {
                        // We've passed the target day this month, calculate the last occurrence
                        val lastOccurrenceDate = LocalDate(today.year, today.month, targetDayOfMonth)
                        val lastOccurrenceDateTime = LocalDateTime(lastOccurrenceDate, baseTime)
                        
                        // Check if the last occurrence has passed
                        val result = now > lastOccurrenceDateTime
                        println("  MONTHLY: last occurrence was $lastOccurrenceDateTime, has passed: $result")
                        result
                    } else {
                        // Target day is in the future this month
                        println("  MONTHLY: target day is in the future this month")
                        false
                    }
                }
                "YEARLY" -> {
                    val targetDayOfYear = baseDate.dayOfYear
                    
                    // If the base date is in the future, the habit is not overdue
                    if (baseDate > today) {
                        println("  YEARLY: base date is in the future")
                        false
                    } else if (today.dayOfYear == targetDayOfYear) {
                        // If today is the target day, check if time has passed
                        val timeOverdue = currentTime > baseTime
                        println("  YEARLY: today is target day, time overdue: $timeOverdue")
                        timeOverdue
                    } else if (today.dayOfYear > targetDayOfYear) {
                        // We've passed the target day this year, calculate the last occurrence
                        val lastOccurrenceDate = LocalDate(today.year, 1, 1).plus(DatePeriod(days = targetDayOfYear - 1))
                        val lastOccurrenceDateTime = LocalDateTime(lastOccurrenceDate, baseTime)
                        
                        // Check if the last occurrence has passed
                        val result = now > lastOccurrenceDateTime
                        println("  YEARLY: last occurrence was $lastOccurrenceDateTime, has passed: $result")
                        result
                    } else {
                        // Target day is in the future this year
                        println("  YEARLY: target day is in the future this year")
                        false
                    }
                }
                else -> {
                    println("  UNKNOWN frequency: ${habit.frequency}")
                    false
                }
            }
            
            println("  Final result for ${habit.name}: $shouldBeOverdue")
            shouldBeOverdue
        }
        
        println("Overdue habits found: ${overdueHabits.size}")
        overdueHabits.forEach { habit ->
            println("  - ${habit.name} (${habit.frequency})")
        }
        println("=== END OVERDUE HABITS ===")
        
        return overdueHabits
    }
}
