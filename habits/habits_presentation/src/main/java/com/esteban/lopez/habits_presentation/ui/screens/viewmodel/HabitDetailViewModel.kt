package com.esteban.ruano.habits_presentation.ui.screens.viewmodel

import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.esteban.ruano.core.utils.UiText
import com.esteban.ruano.core_ui.R
import com.esteban.ruano.core_ui.WorkManagerUtils
import com.esteban.ruano.core_ui.utils.DateUIUtils.parseDate
import com.esteban.ruano.core_ui.view_model.BaseViewModel
import com.lifecommander.models.Habit
import com.esteban.ruano.lifecommander.models.HabitReminder
import com.esteban.ruano.habits_domain.use_case.HabitUseCases
import com.esteban.ruano.habits_presentation.ui.intent.HabitEffect
import com.esteban.ruano.habits_presentation.ui.intent.HabitIntent
import com.esteban.ruano.habits_presentation.ui.screens.viewmodel.state.HabitDetailState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class HabitDetailViewModel @Inject constructor(
    private val habitUseCases: HabitUseCases,
    private val workManager: WorkManager,
    private val workManagerUtils: WorkManagerUtils
) : BaseViewModel<HabitIntent, HabitDetailState, HabitEffect>() {


    override fun handleIntent(intent: HabitIntent) {
        intent.let {
            when (it) {
                is HabitIntent.FetchHabit -> fetchHabit(it.id, LocalDate.now().parseDate())
                is HabitIntent.AddHabit ->  addHabit(
                    it.name,
                    it.note,
                    it.dateTime,
                    it.frequency,
                    it.reminders
                )
                is HabitIntent.DeleteHabit -> deleteHabit(it.id)
                is HabitIntent.UpdateHabit -> updateHabit(it.id, it.habit)
                is HabitIntent.CompleteHabit -> completeHabit(it.id, it.onComplete)
                is HabitIntent.UnCompleteHabit -> unCompleteHabit(it.id, it.onComplete)
                else -> {}
            }
        }
    }

    private fun deleteHabit(id: String) {
        viewModelScope.launch {
            val deleted = habitUseCases.deleteHabit(id)
            deleted.onSuccess {
                triggerWorkManagerTasks()
            }
            deleted.onFailure {
                sendErrorEffect()
                return@launch
            }
            //_uiEvent.send(UiEvent.NavigateBack)
        }
    }

    private fun addHabit(
        name: String,
        note: String,
        dateTime: String,
        frequency: String,
        reminders: List<HabitReminder>
    ) {
        viewModelScope.launch {
            emitState {
                copy(
                    isLoading = true
                )
            }
            val added = habitUseCases.addHabit(
                Habit(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    note = note,
                    dateTime = dateTime,
                    frequency = frequency,
                    reminders = reminders,
                    done = false,
                )
            )
            added.fold(
                onSuccess = {
                    sendEffect {
                        HabitEffect.NavigateUp
                    }
                    triggerWorkManagerTasks()
                    sendSuccessEffect(UiText.StringResource(R.string.habit_saved))
                },
                onFailure = {
                    sendErrorEffect()
                    return@launch
                }
            )

        }
    }
    private fun updateHabit(id: String, habit: Habit) {
        viewModelScope.launch {
            val updated = habitUseCases.updateHabit(id,habit)
            updated.onSuccess {
                triggerWorkManagerTasks()
            }
            updated.onFailure {
                sendErrorEffect()
            }
        }
    }

    private fun triggerWorkManagerTasks() {
        viewModelScope.launch {
            workManagerUtils.runHabitsTasks(
                workManager = workManager,
                restart = true
            )
        }
    }

private fun fetchHabit(id: String, date: String) {
        viewModelScope.launch {
            emitState {
                copy(
                    isLoading = true
                )
            }
            val result = habitUseCases.getHabit(id, date )
            result.fold(
                onSuccess = {
                    emitState {
                        copy(
                            habit = it,
                            isLoading = false
                        )
                    }
                },
                onFailure = {
                    sendErrorEffect()
                    sendEffect {
                        HabitEffect.NavigateUp
                    }
                }
            )
        }
    }

    private fun completeHabit(id: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val completed = habitUseCases.completeHabit(id)
            completed.fold(
                onSuccess = {
                    onComplete(true)
                    triggerWorkManagerTasks()
                },
                onFailure = {
                    sendErrorEffect()
                }
            )
        }
    }

    private fun unCompleteHabit(id: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val unCompleted = habitUseCases.unCompleteHabit(id)
            unCompleted.fold(
                onSuccess = {
                    onComplete(false)
                    triggerWorkManagerTasks()
                },
                onFailure = {
                    sendErrorEffect()
                    return@launch
                }
            )

        }
    }

    override fun createInitialState(): HabitDetailState {
        return HabitDetailState()
    }



}