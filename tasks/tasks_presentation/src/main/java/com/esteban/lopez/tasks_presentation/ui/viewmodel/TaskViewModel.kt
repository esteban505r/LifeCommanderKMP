package com.esteban.ruano.tasks_presentation.ui.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.esteban.ruano.core.domain.preferences.Preferences
import com.esteban.ruano.core.interfaces.OperationListener
import com.esteban.lopez.core.utils.AppConstants.SYNC_INTERVAL
import com.esteban.ruano.core_ui.WorkManagerUtils
import com.esteban.ruano.tasks_domain.use_cases.TaskUseCases
import com.esteban.ruano.core_ui.view_model.BaseViewModel
import com.esteban.ruano.tasks_presentation.intent.TaskIntent
import com.esteban.ruano.tasks_presentation.intent.TaskEffect
import com.esteban.ruano.tasks_presentation.ui.viewmodel.state.TaskState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.first

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskUseCases: TaskUseCases,
    private val operationListener: OperationListener,
    private val preferences: Preferences,
    private val workManagerUtils: WorkManagerUtils,
    private val workManager: WorkManager
) : BaseViewModel<TaskIntent, TaskState, TaskEffect>() {


    override fun handleIntent(intent: TaskIntent) {
        intent.let {
            when (it) {
                is TaskIntent.FetchTasks -> fetchTasks(
                    it.page,
                    it.limit,
                )
                is TaskIntent.FetchIsOfflineModeEnabled -> {
                    viewModelScope.launch {
                        val offlineModeEnabled = preferences.loadOfflineMode().first()
                        emitState {
                            currentState.copy(
                                offlineModeEnabled = offlineModeEnabled
                            )
                        }
                        if (offlineModeEnabled) {
                            it.onOfflineMode()
                        } else {
                            it.onOnlineMode()
                        }
                    }
                }

                is TaskIntent.CompleteTask -> completeTask(it.id, it.onComplete)
                is TaskIntent.UnCompleteTask -> unCompleteTask(it.id, it.onComplete)
                is TaskIntent.ToggleCalendarView -> {
                    emitState {
                        currentState.copy(
                            calendarViewEnabled = currentState.calendarViewEnabled.not()
                        )
                    }
                }
                is TaskIntent.ToggleOfflineMode -> {
                    viewModelScope.launch {
                        preferences.saveOfflineMode(it.offlineModeEnabled)
                        emitState {
                            currentState.copy(
                                offlineModeEnabled = it.offlineModeEnabled
                            )
                        }
                    }
                }
                is TaskIntent.TrySync -> {
                    viewModelScope.launch {
                        preferences.loadLastFetchTime().let { lastFetchTime ->
                            if ((System.currentTimeMillis() - lastFetchTime.first()) > SYNC_INTERVAL) {
                                Log.d("TaskViewModel", "Syncing")
                                it.sync()
                            }
                        }
                    }
                }
                is TaskIntent.Refresh -> {
                    fetchTasks(
                        limit = 20,
                    )
                }
                is TaskIntent.SetFilter -> changeFilter(it.filter)
                is TaskIntent.SetDateRangeSelectedIndex -> changeDateRangeSelectedIndex(it.index)
                is TaskIntent.SetDateRange -> changeDateRange(it.startDate to it.endDate)
                is TaskIntent.ClearDateRange -> changeDateRange(null)
                else -> {}
            }
        }
    }

    private fun changeFilter(filter: String) {
        emitState {
            currentState.copy(
                filter = filter
            )
        }
        fetchTasks()
    }

    private fun changeDateRange(dateRange: Pair<String?, String?>?) {
        emitState {
            copy(
                dateRange = dateRange
            )
        }
        fetchTasks()
    }

    private fun changeDateRangeSelectedIndex(index: Int) {
        emitState {
            currentState.copy(
                dateRangeSelectedIndex = index
            )
        }
    }

    fun changeIsRefreshing(isRefreshing: Boolean){
        emitState {
            currentState.copy(
                isRefreshing = isRefreshing
            )
        }
    }

    private fun fetchTasks(
        page: Int? = null,
        limit: Int = 30,
    ) {
        viewModelScope.launch {
            val dateRange = currentState.dateRange
            emitState {
                currentState.copy(
                    isLoading = true,
                )
            }
            val result =
                if(currentState.dateRange?.first == null && currentState.dateRange?.second == null)
                taskUseCases.getTaskNoDueDate(
                    filter = currentState.filter,
                    page = page,
                    limit = limit
                )
            else
                taskUseCases.getTasks(
                filter = currentState.filter,
                page = page,
                limit = limit,
                startDate = dateRange?.first ?: currentState.dateRange?.first,
                endDate = dateRange?.second ?: currentState.dateRange?.second
            )
            result.fold(
                onFailure = {
                    sendErrorEffect()
                },
                onSuccess = {
                    if(it != currentState.tasks){
                        workManagerUtils.runTasksTasks(
                            workManager = workManager,
                            restart = true
                        )
                    }
                    emitState {
                        currentState.copy(
                            tasks = it,
                            isLoading = false,
                        )
                    }
                }
            )

        }
    }


    private fun completeTask(id: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val completed = taskUseCases.completeTask(id)
            completed.fold(
                onSuccess = {
                    currentState.let { state ->
                        emitState {
                            state.copy(tasks = state.tasks.map { task ->
                                if (task.id == id) task.copy(done = true) else task
                            })
                        }
                    }
                    onComplete(true)
                   // sendEffect(UiEvent.ShowSnackbar(UiText.StringResource(R.string.task_completed)))
                },
                onFailure = {
                    sendErrorEffect()
                    return@launch
                }
            )

        }
    }

    private fun unCompleteTask(id: String, onComplete: (Boolean) -> Unit) {

        viewModelScope.launch {
            val unCompleted = taskUseCases.unCompleteTask(id)
            unCompleted.fold(
                onSuccess = {
                    currentState.let { state ->
                        emitState {
                            state.copy(tasks = state.tasks.map { task ->
                                if (task.id == id) task.copy(done = false) else task
                            })
                        }
                    }
                    onComplete(false)
                    //sendEffect(UiEvent.ShowSnackbar(UiText.StringResource(R.string.task_uncompleted)))
                },
                onFailure = {
                    sendErrorEffect()
                },
            )
        }
    }

    override fun createInitialState(): TaskState {
        return TaskState()
    }



}