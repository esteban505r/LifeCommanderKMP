package ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esteban.ruano.lifecommander.models.TaskFilters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import services.auth.TokenStorageImpl
import services.tasks.TaskService
import services.tasks.sortedByDefault
import com.esteban.ruano.utils.DateUIUtils.toLocalDateTime
import utils.DateUtils.parseDateTime
import utils.StatusBarService
import com.esteban.ruano.lifecommander.utils.TimeBasedItemUtils
import com.lifecommander.models.Task
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import kotlinx.datetime.TimeZone

class TasksViewModel(
    private val tokenStorageImpl: TokenStorageImpl,
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

    val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private fun getTasks() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try{
                val response = taskService.getAll(
                    token = tokenStorageImpl.getToken() ?: "",
                    page = 0,
                    limit = 30,
                )
                _tasks.value = response.sortedByDefault()
            }catch (e: Exception){
                _error.value = e.message ?: "Failed to load tasks"
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
            _error.value = null
            try {
                val response = taskService.getNoDueDateTasks(
                    token = tokenStorageImpl.getToken() ?: "",
                    page = 0,
                    limit = 30
                )
                _tasks.value = response.sortedByDefault()
            }
            catch (e: Exception) {
                _error.value = e.message ?: "Failed to load tasks"
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
            _error.value = null
            try {
                if (checked) {
                    taskService.completeTask(
                        token = tokenStorageImpl.getToken() ?: "",
                        id = id,
                        dateTime = LocalDateTime.now().parseDateTime()
                    )
                } else {
                    taskService.unCompleteTask(
                        token = tokenStorageImpl.getToken() ?: "",
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
                _error.value = e.message ?: "Failed to update task"
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
            _error.value = null
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
                    token = tokenStorageImpl.getToken() ?: "",
                    page = 0,
                    limit = 30,
                    startDate = dates.first!!,
                    endDate = dates.second!!
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
                _error.value = e.message ?: "Failed to load tasks"
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
            _error.value = null
            try {
                taskService.addTask(
                    token = tokenStorageImpl.getToken() ?: "",
                    name = name,
                    dueDate = dueDate,
                    scheduledDate = scheduledDate,
                    note = note,
                    priority = priority
                )
                getTasksByFilter()
            }
            catch (e: Exception) {
                _error.value = e.message ?: "Failed to add task"
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
            _error.value = null
            try {
                taskService.updateTask(
                    token = tokenStorageImpl.getToken() ?: "",
                    id = id,
                    task = task
                )
                getTasksByFilter()
            }
            catch (e: Exception) {
                _error.value = e.message ?: "Failed to update task"
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
            _error.value = null
            try {
                taskService.deleteTask(
                    token = tokenStorageImpl.getToken() ?: "",
                    id = id
                )
                getTasksByFilter()
            }
            catch (e: Exception) {
                _error.value = e.message ?: "Failed to delete task"
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
            _error.value = null
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
                        token = tokenStorageImpl.getToken() ?: "",
                        id = task.id,
                        task = updatedTask
                    )
                    getTasksByFilter()
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to reschedule task"
                e.printStackTrace()
            } finally {
                _loading.value = false
            }
        }
    }

    fun setSelectedFilter(indexOf: Int) {
        val filter = TaskFilters.entries.getOrNull(indexOf) ?: TaskFilters.TODAY
        _selectedFilter.value = filter
        getTasksByFilter()
    }

    fun markTaskDone(task: Task) {
        viewModelScope.launch {
            _loading.value = true
            try {
                taskService.completeTask(
                    token = tokenStorageImpl.getToken() ?: "",
                    id = task.id,
                    dateTime = task.dueDateTime ?: getCurrentDateTime(
                        TimeZone.currentSystemDefault()
                    ).formatDefault()
                )
                getTasksByFilter()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _loading.value = false
            }
        }
    }
}