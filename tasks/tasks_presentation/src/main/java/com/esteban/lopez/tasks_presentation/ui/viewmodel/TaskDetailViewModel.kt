package com.esteban.ruano.tasks_presentation.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.esteban.ruano.core.utils.DateUtils.parseDateTime
import com.esteban.ruano.core_ui.view_model.BaseViewModel
import com.lifecommander.models.Task
import com.esteban.ruano.tasks_domain.use_cases.TaskUseCases
import com.esteban.ruano.tasks_presentation.intent.TaskEffect
import com.esteban.ruano.tasks_presentation.intent.TaskIntent
import com.esteban.ruano.tasks_presentation.ui.viewmodel.state.TaskDetailState
import com.esteban.ruano.utils.DateUIUtils.toLocalDateTime
import com.lifecommander.models.Reminder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val taskUseCases: TaskUseCases,
) : BaseViewModel<TaskIntent,TaskDetailState,TaskEffect>() {


    private fun fetchTask(id: String) {
        viewModelScope.launch {
            emitState {
                currentState.copy(isLoading = true)
            }
            val result = taskUseCases.getTask(id)
            result.fold(
                onSuccess = {
                    emitState {
                        currentState.copy(task = it, isLoading = false)
                    }
                },
                onFailure = {
                    sendErrorEffect()
                }
            )
        }
    }

    private fun addTask(name:String, note:String?, dueDate:String?, scheduledDate:String?, reminders:List<Reminder>, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            emitState {
                currentState.copy(isLoading = true)
            }
            val added = taskUseCases.addTask(
                Task(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    note = note?:"",
                    scheduledDateTime = scheduledDate,
                    dueDateTime = dueDate,
                    reminders = reminders,
                    done = false,
                    priority = 0,
                )
            )
            emitState {
                currentState.copy(isLoading = false)
            }
            added.fold(
                onSuccess = {
                    onComplete(true)
                },
                onFailure = {
                    sendErrorEffect()
                }
            )
        }
    }

    private fun completeTask(id: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = taskUseCases.completeTask(id)
            result.fold(
                onSuccess = {
                    onComplete(true)
                },
                onFailure = {
                    sendErrorEffect()
                }
            )
        }
    }

    private fun unCompleteTask(id: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = taskUseCases.unCompleteTask(id)
            result.fold(
                onSuccess = {
                    onComplete(false)
                },
                onFailure = {
                    sendErrorEffect()
                }
            )
        }
    }

    private fun deleteTask(id: String) {
        viewModelScope.launch {
            emitState {
                currentState.copy(isLoading = true)
            }
            val result = taskUseCases.deleteTask(id)
            result.fold(
                onFailure = {
                    sendErrorEffect()
                    return@launch
                },
                onSuccess = {
                })
        }
    }

    private fun updateTask(id: String, task: Task) {
        viewModelScope.launch {
            emitState {
                currentState.copy(isLoading = true)
            }
            val result = taskUseCases.updateTask(id, task)
            result.fold(
                onSuccess = {
                    fetchTask(id)
                },
                onFailure = {
                    sendErrorEffect()
                }
            )
        }
    }

    override fun createInitialState(): TaskDetailState {
        return TaskDetailState()
    }

    override fun handleIntent(intent: TaskIntent) {
        intent.let {
            when(it){
                is TaskIntent.FetchTask -> fetchTask(it.id)
                is TaskIntent.CompleteTask -> completeTask(it.id, it.onComplete)
                is TaskIntent.UnCompleteTask -> unCompleteTask(it.id, it.onComplete)
                is TaskIntent.DeleteTask -> deleteTask(it.id)
                is TaskIntent.UpdateTask -> updateTask(it.id, it.task)
                is TaskIntent.AddTask -> addTask(it.name,it.note,it.dueDate,it.scheduledDate,it.reminders, it.onComplete)
                is TaskIntent.ToggleCalendarView -> {

                }
                is TaskIntent.RescheduleTask -> {
                    rescheduleTask(it.id, it.task)
                }
                else -> {}
            }
        }
    }

    private fun rescheduleTask(id:String, task: Task) {
        viewModelScope.launch {
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

                    updateTask(
                        id = id,
                        task = updatedTask
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {

            }

        }
    }


}