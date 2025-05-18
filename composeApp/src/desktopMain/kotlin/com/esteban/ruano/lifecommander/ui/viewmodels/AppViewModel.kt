package ui.viewmodels

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kdroid.composenotification.builder.Notification
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import services.NightBlockService
import services.auth.TokenStorageImpl
import services.habits.HabitService
import services.tasks.TaskService
import ui.habits.HabitReminderManager
import ui.state.AppState
import ui.tasks.TaskReminderManager

class AppViewModel(
    private val nightBlockService: NightBlockService,
    private val dataStore: DataStore<Preferences>,
    private val habitsService: HabitService,
    private val taskService: TaskService,
    private val tokenStorageImpl: TokenStorageImpl
) : ViewModel() {
    
    private val _appState = MutableStateFlow(AppState())
    val appState: StateFlow<AppState> = _appState.asStateFlow()

    private val habitReminderManager = HabitReminderManager(
        habitsService = habitsService,
        coroutineScope = viewModelScope,
        onReminder = { title, message ->
            showNotification(title, message)
        },
        tokenStorageImpl = tokenStorageImpl
    )

    private val taskReminderManager = TaskReminderManager(
        taskService = taskService,
        coroutineScope = viewModelScope,
        onReminder = { title, message ->
            showNotification(title, message)
        },
        tokenStorageImpl = tokenStorageImpl
    )

    init {
        startReminders()
    }

    private fun startReminders() {
        viewModelScope.launch {
            habitReminderManager.start()
            taskReminderManager.start()
        }
    }

    private fun showNotification(title: String, message: String) {
        Notification(
            title = title,
            message = message,
            onActivated = { /* Handle notification click */ },
            onDismissed = { /* Handle notification dismissal */ },
            onFailed = { /* Handle notification failure */ }
        )
    }

    fun minimize() {
        _appState.value = _appState.value.copy(isMinimized = true)
    }

    fun restore() {
        _appState.value = _appState.value.copy(isMinimized = false)
    }

    override fun onCleared() {
        super.onCleared()
        habitReminderManager.stop()
        taskReminderManager.stop()
    }

    fun hideDialog() {
        _appState.value = _appState.value.copy(isDialogOpen = false)
    }

    fun checkNightBlock() {
        viewModelScope.launch {
            nightBlockService.checkAndActivateNightBlock()
        }
    }
} 