package com.esteban.ruano.lifecommander.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.kizitonwose.calendar.core.atStartOfMonth
import com.kizitonwose.calendar.core.yearMonth
import com.lifecommander.models.Habit
import com.lifecommander.models.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import services.auth.TokenStorageImpl
import services.habits.HabitService
import services.tasks.TaskService

class CalendarViewModel(
    private val taskService: TaskService,
    private val habitService: HabitService,
    private val tokenStorageImpl: TokenStorageImpl
): ViewModel() {
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _habits = MutableStateFlow<List<Habit>>(emptyList())
    val habits: StateFlow<List<Habit>> = _habits.asStateFlow()

    var isLoading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            try {
                isLoading = true
                error = null
                
                val token = tokenStorageImpl.getToken() ?: throw Exception("No token available")
                val currentMonth = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.yearMonth
                
                // Get start and end dates for the month
                val startDate = currentMonth.atStartOfMonth()
                val endDate = startDate.plus(DatePeriod(months = 1)).minus(1, DateTimeUnit.DAY)
                
                println("Loading data for month: ${startDate.year}-${startDate.monthNumber.toString().padStart(2, '0')}")
                
                // Load tasks for the entire month
                val tasks = taskService.getByDateRange(
                    token = token,
                    page = 0,
                    limit = 100,
                    startDate = startDate.formatDefault(),
                    endDate = endDate.formatDefault()
                )
                println("Loaded ${tasks.size} tasks")
                _tasks.value = tasks

                // Load habits for the entire month
                val habits = habitService.getByDateRange(
                    token = token,
                    page = 0,
                    limit = 100,
                    excludeDaily = true,
                    startDate = startDate.formatDefault(),
                    endDate = endDate.formatDefault()
                )
                println("Loaded ${habits.size} habits")
                _habits.value = habits
            } catch (e: Exception) {
                error = "Failed to load data"
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    fun refresh() {
        loadData()
    }
} 