package com.esteban.ruano.habits_presentation.ui.screens.viewmodel

import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.esteban.lopez.core.utils.SnackbarType
import com.esteban.ruano.core.utils.DateUtils.parseDate
import com.esteban.ruano.core_ui.WorkManagerUtils
import com.esteban.ruano.core_ui.view_model.BaseViewModel
import com.lifecommander.models.Habit
import com.esteban.ruano.habits_domain.use_case.HabitUseCases
import com.esteban.ruano.habits_presentation.ui.intent.HabitIntent
import com.esteban.ruano.habits_presentation.ui.intent.HabitEffect
import com.esteban.ruano.habits_presentation.ui.screens.viewmodel.state.HabitState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HabitViewModel @Inject constructor(
    private val habitUseCases: HabitUseCases,
    private val workManager: WorkManager,
    private val workManagerUtils: WorkManagerUtils
) : BaseViewModel<HabitIntent, HabitState, HabitEffect>() {

    private fun changeFilter(filter: String) {
        emitState {
            currentState.copy(
                filter = filter
            )
        }
        fetchHabits()
    }

    private fun changeDate(date: String) {
        emitState {
            currentState.copy(
                date = date
            )
        }
        fetchHabits()
    }

    private fun changeDateRangeSelectedIndex(index: Int) {
        emitState {
            currentState.copy(
                dateRangeSelectedIndex = index
            )
        }
        fetchHabits()
    }

    fun changeIsRefreshing(isRefreshing: Boolean){
        emitState {
            currentState.copy(
                isRefreshing = isRefreshing
            )
        }
    }


    private fun fetchHabitsByDateRange(
        filter: String? = null,
        page: Int? = null,
        limit: Int,
        startDate: String? = null,
        endDate: String? = null
    ) {
        viewModelScope.launch {
            emitState {
                currentState.copy(
                    isLoading = true
                )
            }
            val result = habitUseCases.getHabitsByRangeDate(
                filter = filter,
                page = page,
                limit = limit,
                startDate = startDate,
                endDate = endDate,
            )

            result.fold(
                onFailure = {
                    sendEffect {
                        HabitEffect.ShowSnackBar(
                            message = "Error, try again",
                            type = SnackbarType.ERROR
                        )
                    }
                },
                onSuccess = {
                    emitState {
                        currentState.copy(
                            habits = it
                        )
                    }
                }
            )

            emitState {
                currentState.copy(
                    isLoading = false
                )
            }
        }
    }

    private fun fetchHabits(
        page: Int? = null,
        limit: Int = 30,
    ) {
        viewModelScope.launch {
            emitState {
                currentState.copy(
                    isLoading = true
                )
            }
            val result = habitUseCases.getHabits(
                filter = currentState.filter,
                page = page,
                limit = limit,
                date = currentState.date
            )
            result.fold(
                onFailure = {
                    sendEffect {
                        HabitEffect.ShowSnackBar(
                            message = "Error, try again",
                            type = SnackbarType.ERROR
                        )
                    }
                },
                onSuccess = {
                    if(it != currentState.habits){
                        workManagerUtils.runHabitsTasks(
                            workManager = workManager,
                            restart = true
                        )
                    }
                    emitState {
                        currentState.copy(
                            habits = it
                        )
                    }
                }
            )
            emitState {
                currentState.copy(
                    isLoading = false
                )
            }
        }
    }

    private fun completeHabit(id: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val completed = habitUseCases.completeHabit(id)
            completed.fold(
                onSuccess = {
                   // sendEffect(UiEvent.ShowSnackbar(UiText.StringResource(R.string.habit_completed)))
                    onComplete(true)
                    emitState {
                        currentState.copy(
                            habits = habits.map {
                                if(it.id == id){
                                    it.copy(done = true)
                                }
                                else {
                                    it
                                }
                            }
                        )
                    }
                },
                onFailure = {
                    //sendEffect(UiEvent.ShowSnackbar(UiText.DynamicString(it.message?:"Error")))
                }
            )
        }
    }

    private fun unCompleteHabit(id: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            emitState {
                currentState.copy(
                    isLoading = true
                )
            }
            val unCompleted = habitUseCases.unCompleteHabit(id)
            unCompleted.fold(
                onSuccess = {
                    //sendEffect(UiEvent.ShowSnackbar(UiText.StringResource(R.string.habit_uncompleted)))
                    onComplete(false)
                    emitState {
                        currentState.copy(
                            habits = habits.map {
                                if(it.id == id){
                                    it.copy(done = false)
                                }
                                else {
                                    it
                                }
                            },
                            isLoading = false
                        )
                    }
                },
                onFailure = {
                    emitState {
                        currentState.copy(
                            isLoading = false
                        )
                    }
                    //sendEffect(UiEvent.ShowSnackbar(UiText.DynamicString(it.message?:"Error")))
                }
            )

        }
    }

    override fun createInitialState(): HabitState {
        return HabitState(
            date = LocalDate.now().parseDate()
        )
    }

    override fun handleIntent(intent: HabitIntent) {
       intent.let {
           when (it) {
               is HabitIntent.FetchHabitsByDateRange -> fetchHabitsByDateRange(
                   it.filter,
                   it.page,
                   it.limit,
                   it.startDate,
                   it.endDate
               )

               is HabitIntent.FetchHabits -> fetchHabits()

               is HabitIntent.CompleteHabit -> completeHabit(it.id, it.onComplete)
               is HabitIntent.UnCompleteHabit -> unCompleteHabit(it.id, it.onComplete)
               else -> {}
           }
       }
    }


}