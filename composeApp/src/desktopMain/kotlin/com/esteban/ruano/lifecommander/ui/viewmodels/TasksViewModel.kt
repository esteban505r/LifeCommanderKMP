package ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esteban.ruano.models.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import services.auth.TokenStorage
import services.tasks.TaskService
import services.tasks.sortedByDefault
import ui.models.TaskFilters
import com.esteban.ruano.utils.DateUIUtils.toLocalDateTime
import utils.DateUtils.parseDateTime
import utils.StatusBarService
import com.esteban.ruano.lifecommander.utils.TimeBasedItemUtils
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class TasksViewModel(
    private val tokenStorage: TokenStorage,
    private val taskService: TaskService,
    private val statusBarService: StatusBarService,
) : ViewModel() {

    private val _tasks = MutableStateFlow<List<Task>>(
        emptyList()
    )

    val tasks = _tasks

    private val _currentTask = MutableStateFlow<Task?>(null)

    private val _selectedFilter = MutableStateFlow(TaskFilters.TODAY)
    val selectedFilter = _selectedFilter.asStateFlow()

    val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private fun getTasks() {
        viewModelScope.launch {
            _loading.value = true
            try{
                val response = taskService.getAll(
                    token = tokenStorage.getToken() ?: "",
                    page = 0,
                    limit = 30,
                )
                _tasks.value = response.sortedByDefault()
            }catch (e: Exception){
                e.printStackTrace()
            }
            finally {
                _loading.value = false
            }
        }
    }

    fun getNoDueDateTasks() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = taskService.getNoDueDateTasks(
                    token = tokenStorage.getToken() ?: "",
                    page = 0,
                    limit = 30
                )
                _tasks.value = response.sortedByDefault()
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
            finally {
                _loading.value = false
            }
        }
    }

    fun changeCheckHabit(id:String, checked: Boolean) {
        viewModelScope.launch {
            _loading.value = true
            try {
                if (checked) {
                    taskService.completeTask(
                        token = tokenStorage.getToken() ?: "",
                        id = id,
                        dateTime = LocalDateTime.now().parseDateTime()
                    )
                } else {
                    taskService.unCompleteTask(
                        token = tokenStorage.getToken() ?: "",
                        id = id,
                        dateTime = LocalDateTime.now().parseDateTime()
                    )
                }
                _tasks.value = _tasks.value.map {
                    if (it.id == id) {
                        it.copy(done = checked)
                    } else {
                        it
                    }
                }
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
            finally {
                _loading.value = false
            }
        }
    }

    fun getTasksByFilter() {
        viewModelScope.launch {
            _loading.value = true
            try {
                if (_selectedFilter.value == TaskFilters.ALL) {
                    getTasks()
                    return@launch
                }
                if (_selectedFilter.value == TaskFilters.NO_DUE_DATE) {
                    getNoDueDateTasks()
                    return@launch
                }
                val dates = _selectedFilter.value.getDateRangeByFilter()
                val response = taskService.getByDateRange(
                    token = tokenStorage.getToken() ?: "",
                    page = 0,
                    limit = 30,
                    startDate = dates.first,
                    endDate = dates.second
                )
                _tasks.value = response.sortedByDefault()
                if(_selectedFilter.value == TaskFilters.TODAY) {
                    statusBarService.updateTaskStatus(
                        TimeBasedItemUtils.getTaskStatusBarText(
                            tasks = response,
                            currentTime = LocalDateTime.now()
                        )
                    )
                }
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
            finally {
                _loading.value = false
            }
        }
    }

    fun addTask(name: String, dueDate: String?, scheduledDate: String?, note: String?, priority: Int) {
        viewModelScope.launch {
            _loading.value = true
            try {
                taskService.addTask(
                    token = tokenStorage.getToken() ?: "",
                    name = name,
                    dueDate = dueDate,
                    scheduledDate = scheduledDate,
                    note = note,
                    priority = priority
                )
                getTasksByFilter()
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
            finally {
                _loading.value = false
            }
        }
    }

    fun updateTask(id: String, task: Task) {
        viewModelScope.launch {
            _loading.value = true
            try {
                taskService.updateTask(
                    token = tokenStorage.getToken() ?: "",
                    id = id,
                    task = task
                )
                getTasksByFilter()
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
            finally {
                _loading.value = false
            }
        }
    }

    fun deleteTask(id: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                taskService.deleteTask(
                    token = tokenStorage.getToken() ?: "",
                    id = id
                )
                getTasksByFilter()
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
            finally {
                _loading.value = false
            }
        }
    }

    fun changeFilter(filter: TaskFilters) {
        _selectedFilter.value = filter
        getTasksByFilter()
    }

    fun rescheduleTask(task: Task) {
        viewModelScope.launch {
            _loading.value = true
            try {
                // Get the current time
                val now = LocalDateTime.now()
                
                // Get the original time from either scheduled or due date
                val originalDateTime = task.scheduledDateTime?.toLocalDateTime() 
                    ?: task.dueDateTime?.toLocalDateTime()
                
                if (originalDateTime != null) {
                    // Create tomorrow's date with the original time
                    val tomorrow = now.plus(1, ChronoUnit.DAYS)
                        .withHour(originalDateTime.hour)
                        .withMinute(originalDateTime.minute)
                    
                    // Create updated task with new date
                    val updatedTask = task.copy(
                        scheduledDateTime = tomorrow.parseDateTime(),
                        dueDateTime = if (task.dueDateTime != null) tomorrow.parseDateTime() else null
                    )
                    
                    // Update the task
                    taskService.updateTask(
                        token = tokenStorage.getToken() ?: "",
                        id = task.id,
                        task = updatedTask
                    )
                    getTasksByFilter()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _loading.value = false
            }
        }
    }
}