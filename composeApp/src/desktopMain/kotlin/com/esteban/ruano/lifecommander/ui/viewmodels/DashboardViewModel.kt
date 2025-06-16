package ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifecommander.models.Habit
import com.lifecommander.models.Task
import com.lifecommander.models.dashboard.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import services.dashboard.DashboardService
import kotlinx.coroutines.Job

class DashboardViewModel(
    private val dashboardService: DashboardService
) : ViewModel() {
    private val _nextTask = MutableStateFlow<Task?>(null)
    val nextTask: StateFlow<Task?> = _nextTask.asStateFlow()

    private val _nextHabit = MutableStateFlow<Habit?>(null)
    val nextHabit: StateFlow<Habit?> = _nextHabit.asStateFlow()

    private val _taskStats = MutableStateFlow<TaskStats?>(null)
    val taskStats: StateFlow<TaskStats?> = _taskStats.asStateFlow()

    private val _habitStats = MutableStateFlow<HabitStats?>(null)
    val habitStats: StateFlow<HabitStats?> = _habitStats.asStateFlow()

    private val _recentTransactions = MutableStateFlow<List<TransactionDTO>>(emptyList())
    val recentTransactions: StateFlow<List<TransactionDTO>> = _recentTransactions.asStateFlow()

    private val _accountBalance = MutableStateFlow(0.0)
    val accountBalance: StateFlow<Double> = _accountBalance.asStateFlow()

    private val _todayCalories = MutableStateFlow(0)
    val todayCalories: StateFlow<Int> = _todayCalories.asStateFlow()

    private val _mealsLogged = MutableStateFlow(0)
    val mealsLogged: StateFlow<Int> = _mealsLogged.asStateFlow()

    private val _nextMeal = MutableStateFlow<MealDTO?>(null)
    val nextMeal: StateFlow<MealDTO?> = _nextMeal.asStateFlow()

    private val _todayWorkout = MutableStateFlow<WorkoutDTO?>(null)
    val todayWorkout: StateFlow<WorkoutDTO?> = _todayWorkout.asStateFlow()

    private val _caloriesBurned = MutableStateFlow(0)
    val caloriesBurned: StateFlow<Int> = _caloriesBurned.asStateFlow()

    private val _workoutStreak = MutableStateFlow(0)
    val workoutStreak: StateFlow<Int> = _workoutStreak.asStateFlow()

    private val _journalCompleted = MutableStateFlow(false)
    val journalCompleted: StateFlow<Boolean> = _journalCompleted.asStateFlow()

    private val _journalStreak = MutableStateFlow(0)
    val journalStreak: StateFlow<Int> = _journalStreak.asStateFlow()

    private val _recentJournalEntries = MutableStateFlow<List<JournalEntryDTO>>(emptyList())
    val recentJournalEntries: StateFlow<List<JournalEntryDTO>> = _recentJournalEntries.asStateFlow()

    private val _weeklyTaskCompletion = MutableStateFlow(0f)
    val weeklyTaskCompletion: StateFlow<Float> = _weeklyTaskCompletion.asStateFlow()

    private val _weeklyHabitCompletion = MutableStateFlow(0f)
    val weeklyHabitCompletion: StateFlow<Float> = _weeklyHabitCompletion.asStateFlow()

    private val _weeklyWorkoutCompletion = MutableStateFlow(0f)
    val weeklyWorkoutCompletion: StateFlow<Float> = _weeklyWorkoutCompletion.asStateFlow()

    private val _weeklyMealLogging = MutableStateFlow(0f)
    val weeklyMealLogging: StateFlow<Float> = _weeklyMealLogging.asStateFlow()

    private val _currentTime = MutableStateFlow(System.currentTimeMillis())
    val currentTime: StateFlow<Long> = _currentTime.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _tasksCompletedPerDayThisWeek = MutableStateFlow<List<Int>>(emptyList())
    val tasksCompletedPerDayThisWeek: StateFlow<List<Int>> = _tasksCompletedPerDayThisWeek.asStateFlow()

    private val _habitsCompletedPerDayThisWeek = MutableStateFlow<List<Int>>(emptyList())
    val habitsCompletedPerDayThisWeek: StateFlow<List<Int>> = _habitsCompletedPerDayThisWeek.asStateFlow()

    private val _workoutsCompletedPerDayThisWeek = MutableStateFlow<List<Int>>(emptyList())
    val workoutsCompletedPerDayThisWeek: StateFlow<List<Int>> = _workoutsCompletedPerDayThisWeek.asStateFlow()

    private val _mealsLoggedPerDayThisWeek = MutableStateFlow<List<Int>>(emptyList())
    val mealsLoggedPerDayThisWeek: StateFlow<List<Int>> = _mealsLoggedPerDayThisWeek.asStateFlow()

    private var timerJob: Job? = null

    init {
        startTimer()
        refreshDashboard()
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (true) {
                _currentTime.value = System.currentTimeMillis()
                refreshDashboard()
                delay(60000) // Refresh every minute
            }
        }
    }

    fun refreshDashboard() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = dashboardService.getDashboardData()
                _nextTask.value = response.nextTask
                _nextHabit.value = response.nextHabit
                _taskStats.value = response.taskStats
                _habitStats.value = response.habitStats
                _recentTransactions.value = response.recentTransactions ?: emptyList()
                _accountBalance.value = response.accountBalance
                _todayCalories.value = response.todayCalories
                _mealsLogged.value = response.mealsLogged
                _nextMeal.value = response.nextMeal
                _todayWorkout.value = response.todayWorkout
                _caloriesBurned.value = response.caloriesBurned
                _workoutStreak.value = response.workoutStreak
                _journalCompleted.value = response.journalCompleted
                _journalStreak.value = response.journalStreak
                _recentJournalEntries.value = response.recentJournalEntries ?: emptyList()
                _weeklyTaskCompletion.value = response.weeklyTaskCompletion
                _weeklyHabitCompletion.value = response.weeklyHabitCompletion
                _weeklyWorkoutCompletion.value = response.weeklyWorkoutCompletion
                _weeklyMealLogging.value = response.weeklyMealLogging
                _tasksCompletedPerDayThisWeek.value = response.tasksCompletedPerDayThisWeek ?: emptyList()
                _habitsCompletedPerDayThisWeek.value = response.habitsCompletedPerDayThisWeek ?: emptyList()
                _workoutsCompletedPerDayThisWeek.value = response.workoutsCompletedPerDayThisWeek ?: emptyList()
                _mealsLoggedPerDayThisWeek.value = response.mealsLoggedPerDayThisWeek ?: emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _loading.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
} 