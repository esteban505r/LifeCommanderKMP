package ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifecommander.models.Habit
import com.lifecommander.models.Task
import com.lifecommander.models.dashboard.HabitStats
import com.lifecommander.models.dashboard.TaskStats
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

    private val _currentTime = MutableStateFlow(System.currentTimeMillis())
    val currentTime: StateFlow<Long> = _currentTime.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

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