package com.esteban.ruano.tasks_presentation.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.esteban.ruano.core_ui.view_model.BaseViewModel
import com.esteban.ruano.tasks_domain.model.Task
import com.esteban.ruano.tasks_domain.model.TaskReminder
import com.esteban.ruano.tasks_domain.use_cases.TaskUseCases
import com.esteban.ruano.tasks_presentation.intent.TaskEffect
import com.esteban.ruano.tasks_presentation.intent.TaskIntent
import com.esteban.ruano.tasks_presentation.ui.viewmodel.state.TaskDetailState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
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

    private fun addTask(name:String, note:String?, dueDate:String?,scheduledDate:String?,reminders:List<TaskReminder>, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            emitState {
                currentState.copy(isLoading = true)
            }
            val added = taskUseCases.addTask(
                Task(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    note = note,
                    scheduledDateTime = scheduledDate,
                    dueDateTime = dueDate,
                    reminders = reminders
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
                else -> {}
            }
        }
    }


}